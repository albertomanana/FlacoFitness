package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.flacofitness.app.repository.*;

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
}
