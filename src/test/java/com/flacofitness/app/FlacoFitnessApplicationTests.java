package com.flacofitness.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flacofitness.app.repository.AsistenciaRepository;
import com.flacofitness.app.repository.ClaseRepository;
import com.flacofitness.app.repository.GastoRepository;
import com.flacofitness.app.repository.MaquinaRepository;
import com.flacofitness.app.repository.MaterialRepository;
import com.flacofitness.app.repository.MembresiaUsuarioRepository;
import com.flacofitness.app.repository.PagoRepository;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.ReservaSesionRepository;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.repository.RutinaRepository;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.TrialRepository;
import com.flacofitness.app.repository.UsuarioRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "spring.sql.init.mode=never",
        "app.pagos.scheduler.enabled=false"
})
class FlacoFitnessApplicationTests {

    @MockBean
    private RolRepository rolRepository;

    @MockBean
    private PlanRepository planRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private RutinaRepository rutinaRepository;

    @MockBean
    private AsistenciaRepository asistenciaRepository;

    @MockBean
    private PagoRepository pagoRepository;

    @MockBean
    private StaffPerfilRepository staffPerfilRepository;

    @MockBean
    private MembresiaUsuarioRepository membresiaUsuarioRepository;

    @MockBean
    private TrialRepository trialRepository;

    @MockBean
    private ClaseRepository claseRepository;

    @MockBean
    private SesionClaseRepository sesionClaseRepository;

    @MockBean
    private ReservaSesionRepository reservaSesionRepository;

        @MockBean
        private GastoRepository gastoRepository;

        @MockBean
        private MaquinaRepository maquinaRepository;

        @MockBean
        private MaterialRepository materialRepository;

    @Test
    void contextLoads() {
    }
}
