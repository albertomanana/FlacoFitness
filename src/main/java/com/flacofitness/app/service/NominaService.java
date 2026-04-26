package com.flacofitness.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.Nomina;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.enums.EstadoNomina;
import com.flacofitness.app.repository.NominaRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;

@Service
@Transactional(readOnly = true)
public class NominaService {

    private final NominaRepository nominaRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final GastoService gastoService;
    private final OperationalClockService operationalClockService;

    public NominaService(NominaRepository nominaRepository,
                         StaffPerfilRepository staffPerfilRepository,
                         GastoService gastoService,
                         OperationalClockService operationalClockService) {
        this.nominaRepository = nominaRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.gastoService = gastoService;
        this.operationalClockService = operationalClockService;
    }

    public List<Nomina> listarTodas() {
        return nominaRepository.findByOrderByPeriodoDescIdDesc();
    }

    public List<Nomina> listarFiltradas(Long staffPerfilId) {
        if (staffPerfilId == null) {
            return listarTodas();
        }

        return nominaRepository.findByStaffPerfilIdOrderByPeriodoDescIdDesc(staffPerfilId);
    }

    public Nomina buscarPorId(Long id) {
        return nominaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nomina no encontrada con id: " + id));
    }

    public long contarPendientes() {
        return nominaRepository.countByEstado(EstadoNomina.EMITIDA);
    }

    @Transactional
    public Nomina guardarBorrador(Long staffPerfilId, String periodo, BigDecimal salarioBase, BigDecimal bonus, BigDecimal deducciones) {
        return crearNomina(staffPerfilId, periodo, salarioBase, bonus, deducciones, EstadoNomina.BORRADOR, false);
    }

    @Transactional
    public Nomina generar(Long staffPerfilId, String periodo, BigDecimal salarioBase, BigDecimal bonus, BigDecimal deducciones) {
        return crearNomina(staffPerfilId, periodo, salarioBase, bonus, deducciones, EstadoNomina.EMITIDA, true);
    }

    @Transactional
    public Nomina actualizarBorrador(Long id,
                                     Long staffPerfilId,
                                     String periodo,
                                     BigDecimal salarioBase,
                                     BigDecimal bonus,
                                     BigDecimal deducciones) {
        Nomina nomina = buscarPorId(id);
        if (nomina.getEstado() != EstadoNomina.BORRADOR) {
            throw new BusinessValidationException("Solo puedes editar nominas en estado borrador.");
        }

        StaffPerfil staffPerfil = staffPerfilRepository.findById(staffPerfilId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff no encontrado con id: " + staffPerfilId));

        String periodoNormalizado = normalizarPeriodo(periodo);
        if (nominaRepository.existsByStaffPerfilIdAndPeriodoAndIdNot(staffPerfilId, periodoNormalizado, id)) {
            throw new BusinessValidationException("Ya existe una nomina para este staff en el periodo "
                    + periodoNormalizado + ". Selecciona otro periodo o revisa el historico.");
        }

        BigDecimal base = normalizarImporte(salarioBase, staffPerfil.getSalarioBaseMensual());
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            if (salarioBase != null && salarioBase.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("El salario base indicado en el formulario debe ser mayor que 0.");
            }
            throw new BusinessValidationException("No se puede guardar el borrador porque el salario base no es valido.");
        }

        BigDecimal bonusNormalizado = normalizarImporte(bonus, staffPerfil.getBonusMensual());
        BigDecimal deduccionesNormalizadas = normalizarImporte(deducciones, staffPerfil.getDeduccionesMensuales());
        BigDecimal neto = base.add(bonusNormalizado).subtract(deduccionesNormalizadas);
        if (neto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("El salario neto debe ser mayor que 0. Revisa base, bonus y deducciones.");
        }

        nomina.setStaffPerfil(staffPerfil);
        nomina.setPeriodo(periodoNormalizado);
        nomina.setSalarioBase(base);
        nomina.setBonus(bonusNormalizado);
        nomina.setDeducciones(deduccionesNormalizadas);
        nomina.setSalarioNeto(neto);
        nomina.setReferencia(buildReferencia(staffPerfil, periodoNormalizado));
        nomina.setFechaEmision(operationalClockService.today());
        return nominaRepository.save(nomina);
    }

    @Transactional
    public Nomina emitir(Long id) {
        Nomina nomina = buscarPorId(id);
        if (nomina.getEstado() == EstadoNomina.PAGADA) {
            throw new BusinessValidationException("No puedes emitir una nomina que ya esta pagada.");
        }
        if (nomina.getEstado() == EstadoNomina.CANCELADA) {
            throw new BusinessValidationException("No puedes emitir una nomina cancelada.");
        }
        if (nomina.getGasto() == null) {
            Gasto gasto = gastoService.construirGastoNomina(
                    nomina.getStaffPerfil(),
                    "Nomina " + nombreStaff(nomina.getStaffPerfil()) + " " + nomina.getPeriodo(),
                    nomina.getSalarioNeto(),
                    operationalClockService.today(),
                    nomina.getReferencia());
            nomina.setGasto(gasto);
        }
        nomina.setEstado(EstadoNomina.EMITIDA);
        nomina.setFechaEmision(operationalClockService.today());
        return nominaRepository.save(nomina);
    }

    @Transactional
    public Nomina cancelar(Long id) {
        Nomina nomina = buscarPorId(id);
        if (nomina.getEstado() == EstadoNomina.PAGADA) {
            throw new BusinessValidationException("No puedes cancelar una nomina ya pagada.");
        }
        nomina.setEstado(EstadoNomina.CANCELADA);
        return nominaRepository.save(nomina);
    }

    private Nomina crearNomina(Long staffPerfilId,
                               String periodo,
                               BigDecimal salarioBase,
                               BigDecimal bonus,
                               BigDecimal deducciones,
                               EstadoNomina estadoInicial,
                               boolean generarGasto) {
        StaffPerfil staffPerfil = staffPerfilRepository.findById(staffPerfilId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff no encontrado con id: " + staffPerfilId));

        String periodoNormalizado = normalizarPeriodo(periodo);
        if (nominaRepository.existsByStaffPerfilIdAndPeriodo(staffPerfilId, periodoNormalizado)) {
            throw new BusinessValidationException("Ya existe una nomina para este staff en el periodo "
                    + periodoNormalizado + ". Selecciona otro periodo o revisa el historico.");
        }

        // En alta manual se priorizan los valores del formulario; los del staff son fallback.
        BigDecimal base = normalizarImporte(salarioBase, staffPerfil.getSalarioBaseMensual());
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            if (salarioBase != null && salarioBase.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("El salario base indicado en el formulario debe ser mayor que 0.");
            }
            throw new BusinessValidationException("No se puede generar la nomina porque el salario base no es valido. "
                    + "Indica un salario base en el formulario o configura un salario base mensual en el staff.");
        }

        BigDecimal bonusNormalizado = normalizarImporte(bonus, staffPerfil.getBonusMensual());
        BigDecimal deduccionesNormalizadas = normalizarImporte(deducciones, staffPerfil.getDeduccionesMensuales());
        BigDecimal neto = base.add(bonusNormalizado).subtract(deduccionesNormalizadas);
        if (neto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("El salario neto debe ser mayor que 0. "
                    + "Revisa base, bonus y deducciones del formulario.");
        }

        String referencia = buildReferencia(staffPerfil, periodoNormalizado);
        LocalDate fechaEmision = operationalClockService.today();
        Gasto gasto = generarGasto
                ? gastoService.construirGastoNomina(
                staffPerfil,
                "Nomina " + nombreStaff(staffPerfil) + " " + periodoNormalizado,
                neto,
                fechaEmision,
                referencia)
                : null;

        Nomina nomina = new Nomina();
        nomina.setStaffPerfil(staffPerfil);
        nomina.setPeriodo(periodoNormalizado);
        nomina.setSalarioBase(base);
        nomina.setBonus(bonusNormalizado);
        nomina.setDeducciones(deduccionesNormalizadas);
        nomina.setSalarioNeto(neto);
        nomina.setFechaEmision(fechaEmision);
        nomina.setEstado(estadoInicial);
        nomina.setReferencia(referencia);
        nomina.setGasto(gasto);
        return nominaRepository.save(nomina);
    }

    @Transactional
    public int generarNominasMensuales() {
        YearMonth periodoActual = YearMonth.from(operationalClockService.today());
        int generadas = 0;

        for (StaffPerfil staffPerfil : staffPerfilRepository.findByActivoTrueAndNominaAutomaticaTrue()) {
            if (staffPerfil.getSalarioBaseMensual() == null
                    || staffPerfil.getSalarioBaseMensual().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            String periodo = periodoActual.toString();
            if (nominaRepository.existsByStaffPerfilIdAndPeriodo(staffPerfil.getId(), periodo)) {
                continue;
            }
            generar(staffPerfil.getId(), periodo, staffPerfil.getSalarioBaseMensual(),
                    staffPerfil.getBonusMensual(), staffPerfil.getDeduccionesMensuales());
            staffPerfil.setFechaProximaNomina(periodoActual.plusMonths(1).atDay(1));
            staffPerfilRepository.save(staffPerfil);
            generadas++;
        }

        return generadas;
    }

    @Transactional
    public void marcarPagada(Long id) {
        Nomina nomina = buscarPorId(id);
        if (nomina.getEstado() == EstadoNomina.CANCELADA) {
            throw new BusinessValidationException("No puedes marcar como pagada una nomina cancelada.");
        }
        if (nomina.getEstado() == EstadoNomina.BORRADOR) {
            emitir(id);
            nomina = buscarPorId(id);
        }
        nomina.setEstado(EstadoNomina.PAGADA);
        if (nomina.getGasto() != null) {
            gastoService.marcarPagado(nomina.getGasto().getId());
        }
        nominaRepository.save(nomina);
    }

    private String normalizarPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            return YearMonth.from(operationalClockService.today()).toString();
        }
        return YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyy-MM")).toString();
    }

    private BigDecimal normalizarImporte(BigDecimal value, BigDecimal fallback) {
        if (value != null) {
            return value;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }

    private String buildReferencia(StaffPerfil staffPerfil, String periodo) {
        return "NOM-" + periodo.replace("-", "") + "-" + staffPerfil.getId();
    }

    private String nombreStaff(StaffPerfil staffPerfil) {
        if (staffPerfil.getUsuario() == null) {
            return "staff-" + staffPerfil.getId();
        }
        String nombre = (staffPerfil.getUsuario().getNombre() + " " + staffPerfil.getUsuario().getApellidos()).trim();
        return nombre.toLowerCase(Locale.ROOT).contains("null") ? staffPerfil.getUsuario().getNombre() : nombre;
    }
}
