package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.GlobalSearchGroupItem;
import com.flacofitness.app.model.dto.GlobalSearchResponse;
import com.flacofitness.app.model.dto.GlobalSearchResultItem;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GlobalSearchService {

    private static final int GROUP_LIMIT = 5;

    private final UsuarioRepository usuarioRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final SesionClaseRepository sesionClaseRepository;

    public GlobalSearchService(UsuarioRepository usuarioRepository,
                               StaffPerfilRepository staffPerfilRepository,
                               SesionClaseRepository sesionClaseRepository) {
        this.usuarioRepository = usuarioRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.sesionClaseRepository = sesionClaseRepository;
    }

    public GlobalSearchResponse search(String query) {
        String normalized = normalize(query);
        if (normalized.length() < 2) {
            return new GlobalSearchResponse(query, Collections.emptyList(), 0);
        }

        PageRequest limit = PageRequest.of(0, GROUP_LIMIT * 3);
        List<GlobalSearchResultItem> usuarios = usuarioRepository.searchTopForGlobal(normalized, limit).stream()
                .flatMap(usuario -> mapUsuario(usuario, normalized))
                .sorted(Comparator.comparingLong(GlobalSearchResultItem::score).reversed()
                        .thenComparing(GlobalSearchResultItem::title))
                .limit(GROUP_LIMIT)
                .toList();

        List<GlobalSearchResultItem> staff = staffPerfilRepository.searchTopForGlobal(normalized, limit).stream()
                .flatMap(perfil -> mapStaff(perfil, normalized))
                .sorted(Comparator.comparingLong(GlobalSearchResultItem::score).reversed()
                        .thenComparing(GlobalSearchResultItem::title))
                .limit(GROUP_LIMIT)
                .toList();

        List<GlobalSearchResultItem> sesiones = sesionClaseRepository.searchTopForGlobal(normalized, limit).stream()
                .flatMap(sesion -> mapSesion(sesion, normalized))
                .sorted(Comparator.comparingLong(GlobalSearchResultItem::score).reversed()
                        .thenComparing(GlobalSearchResultItem::title))
                .limit(GROUP_LIMIT)
                .toList();

        List<GlobalSearchGroupItem> groups = Stream.of(
                        new GlobalSearchGroupItem("Usuarios", usuarios),
                        new GlobalSearchGroupItem("Staff", staff),
                        new GlobalSearchGroupItem("Sesiones", sesiones))
                .filter(group -> !group.items().isEmpty())
                .toList();

        int total = groups.stream().mapToInt(group -> group.items().size()).sum();
        return new GlobalSearchResponse(query, groups, total);
    }

    private Stream<GlobalSearchResultItem> mapUsuario(Usuario usuario, String query) {
        String title = usuario.getNombre() + (usuario.getApellidos() != null && !usuario.getApellidos().isBlank() ? " " + usuario.getApellidos() : "");
        String subtitle = usuario.getEmail() != null ? usuario.getEmail() : "Usuario";
        long score = score(title, subtitle, query);
        if (score <= 0) {
            return Stream.empty();
        }
        return Stream.of(new GlobalSearchResultItem(title, subtitle, "/usuarios/" + usuario.getId(), "Usuarios", "user", score));
    }

    private Stream<GlobalSearchResultItem> mapStaff(StaffPerfil perfil, String query) {
        Usuario usuario = perfil.getUsuario();
        if (usuario == null) {
            return Stream.empty();
        }
        String title = usuario.getNombre() + (usuario.getApellidos() != null && !usuario.getApellidos().isBlank() ? " " + usuario.getApellidos() : "");
        String subtitle = perfil.getRolStaff() != null ? perfil.getRolStaff().name().replace('_', ' ') : "Staff";
        long score = score(title, subtitle, query);
        if (score <= 0) {
            return Stream.empty();
        }
        return Stream.of(new GlobalSearchResultItem(title, subtitle, "/staff/" + perfil.getId(), "Staff", "staff", score));
    }

    private Stream<GlobalSearchResultItem> mapSesion(SesionClase sesion, String query) {
        String clase = sesion.getClase() != null ? sesion.getClase().getNombre() : "Sesion";
        String staff = sesion.getStaffResponsable() != null && sesion.getStaffResponsable().getUsuario() != null
                ? sesion.getStaffResponsable().getUsuario().getNombre()
                : "Sin staff";
        String title = clase;
        String subtitle = (sesion.getFecha() != null ? sesion.getFecha() : LocalDate.now()) + " - " + staff;
        long score = score(title, subtitle, query);
        if (score <= 0) {
            return Stream.empty();
        }
        return Stream.of(new GlobalSearchResultItem(title, subtitle, "/sesiones/" + sesion.getId(), "Sesiones", "session", score));
    }

    private long score(String primary, String secondary, String query) {
        if (query == null || query.isBlank()) {
            return 0;
        }
        String primaryNormalized = normalize(primary);
        String secondaryNormalized = normalize(secondary);
        if (primaryNormalized.equals(query)) {
            return 100;
        }
        if (primaryNormalized.startsWith(query)) {
            return 80;
        }
        if (secondaryNormalized.startsWith(query)) {
            return 70;
        }
        if (primaryNormalized.contains(query)) {
            return 60;
        }
        if (secondaryNormalized.contains(query)) {
            return 40;
        }
        return 0;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
