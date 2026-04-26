package com.flacofitness.app.service;

import com.flacofitness.app.model.entity.Clase;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoSesion;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSearchServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StaffPerfilRepository staffPerfilRepository;

    @Mock
    private SesionClaseRepository sesionClaseRepository;

    private GlobalSearchService globalSearchService;

    @BeforeEach
    void setUp() {
        globalSearchService = new GlobalSearchService(
                usuarioRepository,
                staffPerfilRepository,
                sesionClaseRepository);
    }

    @Test
    void search_ignoraTerminosCortosSinConsultarRepositorios() {
        var response = globalSearchService.search("a");

        assertThat(response.totalResults()).isZero();
        assertThat(response.groups()).isEmpty();
        verify(usuarioRepository, never()).searchTopForGlobal(isA(String.class), isA(Pageable.class));
        verify(staffPerfilRepository, never()).searchTopForGlobal(isA(String.class), isA(Pageable.class));
        verify(sesionClaseRepository, never()).searchTopForGlobal(isA(String.class), isA(Pageable.class));
    }

    @Test
    void search_devuelveResultadosAgrupadosDesdeConsultasLimitadas() {
        when(usuarioRepository.searchTopForGlobal(eq("ana"), isA(Pageable.class)))
                .thenReturn(List.of(usuario(1L, "Ana", "Lopez", "ana@demo.com")));
        when(staffPerfilRepository.searchTopForGlobal(eq("ana"), isA(Pageable.class)))
                .thenReturn(List.of(staff(2L, "Ana", "Coach")));
        when(sesionClaseRepository.searchTopForGlobal(eq("ana"), isA(Pageable.class)))
                .thenReturn(List.of(sesion(3L, "Ana Yoga")));

        var response = globalSearchService.search("Ana");

        assertThat(response.totalResults()).isEqualTo(3);
        assertThat(response.groups()).extracting("label")
                .containsExactly("Usuarios", "Staff", "Sesiones");
        assertThat(response.groups().get(0).items().get(0).url()).isEqualTo("/usuarios/1");
        assertThat(response.groups().get(1).items().get(0).url()).isEqualTo("/staff/2");
        assertThat(response.groups().get(2).items().get(0).url()).isEqualTo("/sesiones/3");
    }

    private Usuario usuario(Long id, String nombre, String apellidos, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setActivo(true);
        return usuario;
    }

    private StaffPerfil staff(Long id, String nombre, String especialidad) {
        StaffPerfil perfil = new StaffPerfil();
        perfil.setId(id);
        perfil.setUsuario(usuario(20L + id, nombre, "Staff", nombre.toLowerCase() + "@demo.com"));
        perfil.setEspecialidad(especialidad);
        perfil.setRolStaff(RolStaff.ENTRENADOR);
        perfil.setActivo(true);
        perfil.setPuedeImpartirClases(true);
        return perfil;
    }

    private SesionClase sesion(Long id, String nombreClase) {
        Clase clase = new Clase();
        clase.setId(id);
        clase.setNombre(nombreClase);
        clase.setCapacidadSugerida(12);
        clase.setActiva(true);

        SesionClase sesion = new SesionClase();
        sesion.setId(id);
        sesion.setClase(clase);
        sesion.setFecha(LocalDate.now());
        sesion.setHoraInicio(LocalTime.of(18, 0));
        sesion.setAforo(12);
        sesion.setEstado(EstadoSesion.PROGRAMADA);
        return sesion;
    }
}
