package com.flacofitness.app.security;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public enum AccessProfile {
    ADMIN("ADMIN", "Administrador", "AD", "Control total del sistema"),
    STAFF_ENTRENADOR("ENTRENADOR", "Entrenador", "EN", "Clases, rutinas y asistencias"),
    STAFF_RECEPCION("RECEPCION", "Recepcion", "RE", "Usuarios, trials, pagos y check-in"),
    STAFF_GERENTE("GERENTE", "Gerente", "GE", "Gestion, finanzas e inventario"),
    CLIENTE("CLIENTE", "Cliente", "CL", "Panel personal limitado");

    private final String displayName;
    private final String label;
    private final String initials;
    private final String description;

    AccessProfile(String displayName, String label, String initials, String description) {
        this.displayName = displayName;
        this.label = label;
        this.initials = initials;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLabel() {
        return label;
    }

    public String getInitials() {
        return initials;
    }

    public String getDescription() {
        return description;
    }

    public static AccessProfile from(String rawProfile) {
        if (rawProfile == null || rawProfile.isBlank()) {
            return ADMIN;
        }

        String normalized = rawProfile.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(profile -> profile.name().equals(normalized) || profile.displayName.equals(normalized))
                .findFirst()
                .orElse(ADMIN);
    }

    public String entryPoint() {
        return dashboardEntryPoint();
    }

    public boolean canSeeSection(String section) {
        return switch (this) {
            case ADMIN -> true;
            case STAFF_ENTRENADOR -> Set.of(
                    "inicio", "usuarios", "rutinas", "clases", "sesiones", "asistencias", "maquinas", "materiales"
            ).contains(section);
            case STAFF_RECEPCION -> Set.of(
                    "inicio", "usuarios", "membresias", "trials", "pagos", "asistencias", "sesiones"
            ).contains(section);
            case STAFF_GERENTE -> Set.of(
                    "inicio", "usuarios", "staff", "membresias", "trials", "finanzas", "pagos", "gastos", "recurrentes", "nominas",
                    "maquinas", "materiales", "clases", "sesiones", "rutinas", "asistencias"
            ).contains(section);
            case CLIENTE -> "cliente".equals(section);
        };
    }

    public boolean canAccess(String rawPath, String method) {
        String path = normalizePath(rawPath);
        boolean write = isWriteMethod(method);

        if (path.equals("/salir") || path.startsWith("/notificaciones")) {
            return true;
        }

        if (path.startsWith("/cuenta/password")) {
            return true;
        }

        if (path.contains("/reset-password")) {
            return this == ADMIN;
        }

        if (path.startsWith("/busqueda") || path.startsWith("/api/busqueda/global")) {
            return this != CLIENTE;
        }

        if (path.startsWith("/chat") || path.startsWith("/api/chat")) {
            return true;
        }

        if (path.startsWith("/stats/finanzas")) {
            return this == ADMIN || this == STAFF_GERENTE;
        }

        if (this == ADMIN) {
            return true;
        }

        return switch (this) {
            case STAFF_ENTRENADOR -> canAccessAsTrainer(path, write);
            case STAFF_RECEPCION -> canAccessAsReception(path, write);
            case STAFF_GERENTE -> canAccessAsManager(path, write);
            case CLIENTE -> canAccessAsClient(path, write);
            case ADMIN -> true;
        };
    }

    public String dashboardEntryPoint() {
        return switch (this) {
            case ADMIN -> "/";
            case STAFF_ENTRENADOR -> "/staff/dashboard";
            case STAFF_RECEPCION -> "/staff/dashboard";
            case STAFF_GERENTE -> "/staff/dashboard";
            case CLIENTE -> "/cliente";
        };
    }

    private boolean canAccessAsTrainer(String path, boolean write) {
        if (path.equals("/") || path.startsWith("/stats")) {
            return true;
        }
        if (path.startsWith("/staff/dashboard")
                || path.startsWith("/staff/nominas")
                || path.startsWith("/api/staff")) {
            return true;
        }
        if (path.startsWith("/rutinas") || path.startsWith("/clases")
                || path.startsWith("/sesiones") || path.startsWith("/asistencias")) {
            return true;
        }
        if (path.startsWith("/maquinas") || path.startsWith("/materiales")) {
            return !write;
        }
        return path.startsWith("/usuarios")
                && !path.contains("/nuevo")
                && !path.contains("/editar")
                && !write;
    }

    private boolean canAccessAsReception(String path, boolean write) {
        if (path.equals("/") || path.startsWith("/stats")) {
            return true;
        }
        if (path.startsWith("/staff/dashboard")
                || path.startsWith("/staff/nominas")
                || path.startsWith("/api/staff")) {
            return true;
        }

        if (path.startsWith("/sesiones")) {
            return !isCreateOrEditRoute(path)
                    && (!write || path.contains("/reservas") || path.contains("/asistencias"));
        }

        return path.startsWith("/usuarios")
                || path.startsWith("/membresias")
                || path.startsWith("/trials")
                || path.startsWith("/finanzas")
                || path.startsWith("/pagos")
                || path.startsWith("/asistencias");
    }

    private boolean canAccessAsManager(String path, boolean write) {
        if (path.equals("/") || path.startsWith("/stats")) {
            return true;
        }
        if (path.startsWith("/staff/dashboard")
                || path.startsWith("/staff/nominas")
                || path.startsWith("/api/staff")) {
            return true;
        }
        if (path.startsWith("/automatizaciones")) {
            return true;
        }
        if (path.startsWith("/clases") || path.startsWith("/sesiones")
                || path.startsWith("/rutinas") || path.startsWith("/asistencias")) {
            return !write && !isCreateOrEditRoute(path);
        }
        return path.startsWith("/usuarios")
                || path.startsWith("/staff")
                || path.startsWith("/membresias")
                || path.startsWith("/trials")
                || path.startsWith("/pagos")
                || path.startsWith("/gastos")
                || path.startsWith("/nominas")
                || path.startsWith("/maquinas")
                || path.startsWith("/materiales");
    }

    private boolean canAccessAsClient(String path, boolean write) {
        return path.startsWith("/cliente") || path.startsWith("/api/cliente");
    }

    private boolean isCreateOrEditRoute(String path) {
        return path.contains("/nueva")
                || path.contains("/nuevo")
                || path.contains("/editar");
    }

    private static boolean isWriteMethod(String method) {
        return method != null
                && !method.equalsIgnoreCase("GET")
                && !method.equalsIgnoreCase("HEAD")
                && !method.equalsIgnoreCase("OPTIONS");
    }

    private static String normalizePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "/";
        }
        String path = rawPath.startsWith("/") ? rawPath : "/" + rawPath;
        int queryIndex = path.indexOf('?');
        return queryIndex >= 0 ? path.substring(0, queryIndex) : path;
    }
}
