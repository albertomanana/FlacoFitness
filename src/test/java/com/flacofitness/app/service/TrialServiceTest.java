package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.TrialConversionResult;
import com.flacofitness.app.model.entity.Rol;
import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.repository.MembresiaUsuarioRepository;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.TrialRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialServiceTest {

    @Mock private TrialRepository trialRepository;
    @Mock private StaffPerfilRepository staffPerfilRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private MembresiaUsuarioRepository membresiaUsuarioRepository;

    private TrialService trialService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        trialService = new TrialService(
                trialRepository,
                staffPerfilRepository,
                usuarioRepository,
                rolRepository,
                membresiaUsuarioRepository,
                new OperationalClockService(),
                passwordEncoder);
    }

    @Test
    void convertirAUsuarioNuevoGeneraCredencialesTemporalesUsables() {
        Trial trial = new Trial();
        trial.setId(15L);
        trial.setNombre("Lead");
        trial.setApellidos("Demo");
        trial.setEmail("lead.demo@flacofitness.test");
        trial.setTelefono("600000000");
        trial.setFechaPrueba(LocalDate.now());
        trial.setEstado(EstadoTrial.ASISTIO);

        Rol rolCliente = new Rol();
        rolCliente.setNombre("CLIENTE");

        when(trialRepository.findById(15L)).thenReturn(Optional.of(trial));
        when(usuarioRepository.findByEmailIgnoreCase("lead.demo@flacofitness.test")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByUsernameIgnoreCase("lead.demo")).thenReturn(false);
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rolCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(91L);
            return usuario;
        });

        TrialConversionResult result = trialService.convertirAUsuarioConCredenciales(15L);

        assertThat(result.nuevoUsuario()).isTrue();
        assertThat(result.temporalPassword()).startsWith("FF-");
        assertThat(result.usuario().getUsername()).isEqualTo("lead.demo");
        assertThat(result.usuario().getMustChangePassword()).isTrue();
        assertThat(passwordEncoder.matches(result.temporalPassword(), result.usuario().getPasswordHash())).isTrue();
        assertThat(trial.getEstado()).isEqualTo(EstadoTrial.CONVERTIDO);
        assertThat(trial.getUsuarioConvertido()).isSameAs(result.usuario());
    }
}
