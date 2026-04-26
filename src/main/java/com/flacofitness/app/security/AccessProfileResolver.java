package com.flacofitness.app.security;

import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.repository.StaffPerfilRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AccessProfileResolver {

    private final StaffPerfilRepository staffPerfilRepository;

    public AccessProfileResolver(StaffPerfilRepository staffPerfilRepository) {
        this.staffPerfilRepository = staffPerfilRepository;
    }

    public AccessProfile resolveFor(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            return AccessProfile.CLIENTE;
        }

        String roleName = usuario.getRol().getNombre().trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(roleName)) {
            return AccessProfile.ADMIN;
        }
        if ("CLIENTE".equals(roleName)) {
            return AccessProfile.CLIENTE;
        }
        if (!"STAFF".equals(roleName)) {
            return AccessProfile.CLIENTE;
        }

        StaffPerfil staffPerfil = staffPerfilRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (staffPerfil == null || staffPerfil.getRolStaff() == null) {
            return AccessProfile.STAFF_RECEPCION;
        }

        return switch (staffPerfil.getRolStaff()) {
            case ENTRENADOR -> AccessProfile.STAFF_ENTRENADOR;
            case RECEPCION -> AccessProfile.STAFF_RECEPCION;
            case GERENTE, ADMINISTRACION -> AccessProfile.STAFF_GERENTE;
        };
    }

    public boolean canTeach(Usuario usuario) {
        return staffPerfilRepository.findByUsuarioId(usuario.getId())
                .map(perfil -> Boolean.TRUE.equals(perfil.getPuedeImpartirClases())
                        && perfil.getRolStaff() == RolStaff.ENTRENADOR)
                .orElse(false);
    }
}
