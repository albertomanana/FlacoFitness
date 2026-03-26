package com.flacofitness.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flacofitness.app.repository.AsistenciaRepository;
import com.flacofitness.app.repository.PagoRepository;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.repository.RutinaRepository;
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

    @Test
    void contextLoads() {
    }
}
