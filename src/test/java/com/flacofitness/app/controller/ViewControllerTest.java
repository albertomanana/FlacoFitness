package com.flacofitness.app.controller;

import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.flacofitness.app.repository.*;

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
}
