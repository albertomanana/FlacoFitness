package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.dto.AsistenciaDiariaStatsItem;
import com.flacofitness.app.model.dto.AsistenciaMensualStatsItem;
import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.AsistenciaRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository, UsuarioRepository usuarioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAll();
    }

    public List<Asistencia> listarPorUsuario(Long usuarioId) {
        return asistenciaRepository.findByUsuarioId(usuarioId);
    }

    public long contarTodas() {
        return asistenciaRepository.count();
    }

    public List<AsistenciaDiariaStatsItem> obtenerAsistenciasPorDia() {
        return asistenciaRepository.countGroupedByFecha().stream()
                .map(item -> new AsistenciaDiariaStatsItem(item.getFecha(), item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public List<AsistenciaMensualStatsItem> obtenerAsistenciasMensuales() {
        return asistenciaRepository.countGroupedByMes().stream()
                .map(item -> new AsistenciaMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public Asistencia buscarPorId(Long id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada con id: " + id));
    }

    @Transactional
    public Asistencia registrar(Asistencia asistencia) {
        asistencia.setUsuario(obtenerUsuarioValido(asistencia.getUsuario()));
        return asistenciaRepository.save(asistencia);
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("La asistencia debe estar asociada a un usuario valido");
        }

        return usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuario.getId()));
    }
}
