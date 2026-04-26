package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.entity.Rol;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.model.enums.EstadoNomina;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.flacofitness.app.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "spring.sql.init.mode=never",
        "app.pagos.scheduler.enabled=false"
})
@AutoConfigureMockMvc
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessSessionService accessSessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean private RolRepository rolRepository;
    @MockBean private PlanRepository planRepository;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private RutinaRepository rutinaRepository;
    @MockBean private AsistenciaRepository asistenciaRepository;
    @MockBean private PagoRepository pagoRepository;
    @MockBean private StaffPerfilRepository staffPerfilRepository;
    @MockBean private MembresiaUsuarioRepository membresiaUsuarioRepository;
    @MockBean private TrialRepository trialRepository;
    @MockBean private ClaseRepository claseRepository;
    @MockBean private SesionClaseRepository sesionClaseRepository;
    @MockBean private ReservaSesionRepository reservaSesionRepository;
    @MockBean private GastoRepository gastoRepository;
    @MockBean private GastoRecurrenteRepository gastoRecurrenteRepository;
    @MockBean private NominaRepository nominaRepository;
    @MockBean private MaquinaRepository maquinaRepository;
    @MockBean private MaterialRepository materialRepository;
    @MockBean private ActivityLogRepository activityLogRepository;
    @MockBean private RecentVisitRepository recentVisitRepository;
    @MockBean private UxMemoryStateRepository uxMemoryStateRepository;

    @Test
    void rutasProtegidasRedirigenAAccesoSiNoHaySesion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acceso"));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acceso"));
    }

    @Test
    void perfilClienteNoPuedeAccederAUsuarios() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.CLIENTE);

        mockMvc.perform(get("/usuarios").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeAccederATodo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        mockMvc.perform(get("/").session(session))
            .andExpect(status().isOk());

        mockMvc.perform(get("/usuarios").session(session))
            .andExpect(status().isOk());
    }

    @Test
    void adminPuedeVerListadoGastos() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        mockMvc.perform(get("/gastos").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeVerCentroFinanciero() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        stubFinancialCenterStats();

        mockMvc.perform(get("/finanzas").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("finanzas/index"));

        mockMvc.perform(get("/stats/finanzas").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeAbrirFormularioNomina() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        when(staffPerfilRepository.findByActivoTrue()).thenReturn(List.of(buildStaff()));

        mockMvc.perform(get("/nominas/nueva").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("nominas/form"));
    }

    @Test
    void adminPuedeAbrirFormularioUsuarioConCtaYPreview() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        when(rolRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of());
        when(planRepository.findByActivoTrue()).thenReturn(List.of());

        mockMvc.perform(get("/usuarios/nuevo").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/form"));
    }

    @Test
    void adminPuedeAbrirFormularioTrialConDtoSeguro() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        when(staffPerfilRepository.findByActivoTrue()).thenReturn(List.of());

        mockMvc.perform(get("/trials/nuevo").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("trials/form"));
    }

    @Test
    void adminPuedeCrearNominaConFormularioMinimo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        StaffPerfil staff = buildStaff();

        when(staffPerfilRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(nominaRepository.existsByStaffPerfilIdAndPeriodo(1L, "2026-04")).thenReturn(false);
        when(nominaRepository.save(any(com.flacofitness.app.model.entity.Nomina.class)))
                .thenAnswer(invocation -> {
                    com.flacofitness.app.model.entity.Nomina nomina = invocation.getArgument(0);
                    nomina.setId(44L);
                    return nomina;
                });

        mockMvc.perform(post("/nominas")
                        .session(session)
                        .param("staffPerfilId", "1")
                        .param("periodo", "2026-04")
                        .param("salarioBase", "1200.00")
                        .param("bonus", "50.00")
                        .param("deducciones", "10.00")
                        .param("action", "borrador"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nominas/44"));
    }

    @Test
    void accionesNominaInvalidasRedirigenConFlashSinError500() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        com.flacofitness.app.model.entity.Nomina nomina = new com.flacofitness.app.model.entity.Nomina();
        nomina.setId(88L);
        nomina.setEstado(EstadoNomina.CANCELADA);
        nomina.setPeriodo("2026-04");
        nomina.setSalarioNeto(new BigDecimal("1200.00"));
        when(nominaRepository.findById(88L)).thenReturn(Optional.of(nomina));

        mockMvc.perform(post("/nominas/88/pagar").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nominas/88"));
    }

    @Test
    void loginTemporalRedirigeACambioYPermiteGuardarNuevaPassword() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(31L);
        usuario.setNombre("Admin");
        usuario.setEmail("admin@flacofitness.test");
        usuario.setUsername("admin-test");
        usuario.setActivo(true);
        usuario.setPasswordHash(passwordEncoder.encode("Temp1234!"));
        usuario.setMustChangePassword(true);
        Rol rol = new Rol();
        rol.setNombre("ADMIN");
        usuario.setRol(rol);

        when(usuarioRepository.findByEmailIgnoreCase("admin@flacofitness.test")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(31L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var loginResult = mockMvc.perform(post("/acceso")
                        .param("login", "admin@flacofitness.test")
                        .param("password", "Temp1234!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cuenta/password"))
                .andReturn();

        mockMvc.perform(post("/cuenta/password")
                        .session((MockHttpSession) loginResult.getRequest().getSession(false))
                        .param("currentPassword", "Temp1234!")
                        .param("newPassword", "Nueva1234!")
                        .param("confirmPassword", "Nueva1234!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        org.mockito.Mockito.verify(usuarioRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                Boolean.FALSE.equals(saved.getMustChangePassword())
                        && passwordEncoder.matches("Nueva1234!", saved.getPasswordHash())));
    }

    @Test
    void adminPuedeCrearTrialSinError500() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        when(trialRepository.save(any(Trial.class))).thenAnswer(invocation -> {
            Trial trial = invocation.getArgument(0);
            trial.setId(77L);
            return trial;
        });

        mockMvc.perform(post("/trials")
                        .session(session)
                        .param("nombre", "Lead")
                        .param("apellidos", "Demo")
                        .param("email", "lead@flacofitness.test")
                        .param("fechaPrueba", "2026-04-26")
                        .param("estado", EstadoTrial.PENDIENTE.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trials/77"));
    }

    @Test
    void adminVeErrorAlCrearNominaDuplicada() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);
        StaffPerfil staff = buildStaff();

        when(staffPerfilRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffPerfilRepository.findByActivoTrue()).thenReturn(List.of(staff));
        when(nominaRepository.existsByStaffPerfilIdAndPeriodo(1L, "2026-04")).thenReturn(true);

        mockMvc.perform(post("/nominas")
                        .session(session)
                        .param("staffPerfilId", "1")
                        .param("periodo", "2026-04")
                        .param("salarioBase", "1200.00")
                        .param("bonus", "0")
                        .param("deducciones", "0")
                        .param("action", "emitir"))
                .andExpect(status().isOk())
                .andExpect(view().name("nominas/form"));
    }

    /**
     * Regression test for EL1008E: gasto.frecuencia does not exist on Gasto.
     * The correct access is gasto.gastoRecurrente.frecuencia with null guards.
     * This test would have caught the bug before it reached production.
     */
    @Test
    void adminPuedeVerDetalleGasto() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        Gasto gasto = new Gasto();
        gasto.setId(1L);
        gasto.setConcepto("Test gasto regression");
        gasto.setImporte(BigDecimal.TEN);
        gasto.setEstado(EstadoGasto.PENDIENTE);
        // gastoRecurrente=null, staffResponsable=null, maquina=null, material=null
        // Template must handle all null relations without EL1008E

        when(gastoRepository.findById(1L)).thenReturn(Optional.of(gasto));

        mockMvc.perform(get("/gastos/1").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeVerDetalleStaff() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        Usuario usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombre("Laura");
        usuario.setApellidos("Trainer");
        usuario.setEmail("laura@flacofitness.test");

        StaffPerfil staff = new StaffPerfil();
        staff.setId(1L);
        staff.setUsuario(usuario);
        staff.setActivo(true);
        staff.setFechaAlta(LocalDate.of(2026, 1, 10));

        when(staffPerfilRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(sesionClaseRepository.findByFechaAndStaffResponsableIdOrderByHoraInicioAscIdAsc(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(List.of());
        when(sesionClaseRepository.findByStaffResponsableIdOrderByFechaDescHoraInicioDescIdDesc(1L)).thenReturn(List.of());
        when(gastoRepository.findByActivoTrueAndStaffResponsableIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of());
        when(nominaRepository.findByStaffPerfilIdOrderByPeriodoDescIdDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/staff/1").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeVerDetalleMaquina() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        Maquina maquina = new Maquina();
        maquina.setId(1L);
        maquina.setNombre("Bike Pro");
        maquina.setActivo(true);

        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(gastoRepository.findByActivoTrueAndMaquinaIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/maquinas/1").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeVerDetalleMaterial() throws Exception {
        MockHttpSession session = new MockHttpSession();
        accessSessionService.grantAccess(session, AccessProfile.ADMIN);

        Material material = new Material();
        material.setId(1L);
        material.setNombre("Bandas elasticas");
        material.setActivo(true);
        material.setStock(12);
        material.setStockMinimo(3);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(gastoRepository.findByActivoTrueAndMaterialIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/materiales/1").session(session))
                .andExpect(status().isOk());
    }

    private StaffPerfil buildStaff() {
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombre("Laura");
        usuario.setApellidos("Trainer");
        usuario.setEmail("laura@flacofitness.test");

        StaffPerfil staff = new StaffPerfil();
        staff.setId(1L);
        staff.setUsuario(usuario);
        staff.setActivo(true);
        staff.setRolStaff(RolStaff.ENTRENADOR);
        staff.setFechaAlta(LocalDate.of(2026, 1, 10));
        staff.setSalarioBaseMensual(new BigDecimal("1200.00"));
        return staff;
    }

    private void stubFinancialCenterStats() {
        when(pagoRepository.sumMontoByEstado(com.flacofitness.app.model.enums.EstadoPago.PAGADO))
                .thenReturn(BigDecimal.ZERO);
        when(pagoRepository.sumMontoByEstados(org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(BigDecimal.ZERO);
        when(pagoRepository.sumMontoGroupedByMes(com.flacofitness.app.model.enums.EstadoPago.PAGADO))
                .thenReturn(List.of());
        when(gastoRepository.sumImporteGroupedByMes()).thenReturn(List.of());
        when(gastoRepository.sumImporteGroupedByCategoria(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of());
        when(nominaRepository.sumSalarioNetoByEstado(EstadoNomina.EMITIDA)).thenReturn(BigDecimal.ZERO);
    }
}
