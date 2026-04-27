package com.flacofitness.app.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.flacofitness.app.model.enums.EstadoMembresia;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import com.flacofitness.app.model.enums.EstadoSesion;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.model.enums.MetodoPago;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.model.enums.TipoMembresia;
import com.flacofitness.app.model.enums.TipoRutina;

@Component
@Profile({"local", "demo"})
@ConditionalOnProperty(name = "app.demo-seeder.enabled", havingValue = "true")
@Order(2)
public class DemoDataSeeder implements ApplicationRunner {

    private static final int TARGET_TOTAL_USERS = 80;
    private static final int TARGET_PHOTO_USERS = 50;
    private static final long RANDOM_SEED = 20260414L;

    private static final List<PlanSpec> PLAN_SPECS = List.of(
            new PlanSpec("Basico", "Plan base para acceso estable y frecuente al gimnasio.", new BigDecimal("29.00"), 30, true),
            new PlanSpec("Premium", "Plan historico completo mantenido solo para compatibilidad.", new BigDecimal("49.90"), 30, false),
            new PlanSpec("Plus", "Plan historico intermedio mantenido solo para compatibilidad.", new BigDecimal("59.90"), 30, false),
            new PlanSpec("Estudiante", "Cuota reducida pensada para perfiles jovenes.", new BigDecimal("19.00"), 30, true),
            new PlanSpec("Trimestral", "Plan historico trimestral mantenido solo para compatibilidad.", new BigDecimal("129.90"), 90, false)
    );

    private static final List<RoutineSpec> GENERAL_ROUTINES = List.of(
            new RoutineSpec("Full Body Base", "Rutina de cuerpo completo para construir constancia.", TipoRutina.GENERAL, true),
            new RoutineSpec("Cardio Salud", "Bloques de cardio moderado para continuidad semanal.", TipoRutina.GENERAL, true),
            new RoutineSpec("Fuerza Funcional", "Trabajo funcional para usuarios con ritmo medio y alto.", TipoRutina.GENERAL, true),
            new RoutineSpec("HIIT Metabolico", "Intervalos intensos para gasto calorico y condicion fisica.", TipoRutina.GENERAL, true),
            new RoutineSpec("Tren Superior", "Empuje, traccion y estabilidad del torso.", TipoRutina.GENERAL, true),
            new RoutineSpec("Tren Inferior", "Pierna y gluteo con progresion sencilla y realista.", TipoRutina.GENERAL, true),
            new RoutineSpec("Movilidad y Core", "Sesiones de movilidad articular y control del core.", TipoRutina.GENERAL, true),
            new RoutineSpec("Recuperacion Activa", "Sesion suave de retorno o descarga.", TipoRutina.GENERAL, true),
            new RoutineSpec("Base Archivo Otono", "Rutina historica archivada para temporadas de ajuste.", TipoRutina.GENERAL, false),
            new RoutineSpec("Base Archivo Verano", "Rutina historica archivada para periodos de menor carga.", TipoRutina.GENERAL, false)
    );

    private static final List<String> MALE_FIRST_NAMES = List.of(
            "Carlos", "David", "Javier", "Miguel", "Alberto", "Sergio", "Daniel", "Raul",
            "Pablo", "Adrian", "Lucas", "Hugo", "Ivan", "Oscar", "Fernando", "Marcos",
            "Victor", "Andres", "Hector", "Jorge", "Rafael", "Nicolas", "Tomas", "Cesar",
            "Antonio", "Luis", "Pedro", "Samuel", "Mario", "Diego"
    );

    private static final List<String> FEMALE_FIRST_NAMES = List.of(
            "Ana", "Marta", "Laura", "Elena", "Sara", "Paula", "Lucia", "Carmen",
            "Irene", "Claudia", "Alba", "Sonia", "Patricia", "Noelia", "Cristina", "Silvia",
            "Veronica", "Rocio", "Ines", "Andrea", "Daniela", "Sofia", "Eva", "Teresa",
            "Alicia", "Julia", "Lorena", "Nuria", "Marina", "Mireia"
    );

    private static final List<String> SURNAMES = List.of(
            "Garcia", "Martinez", "Lopez", "Sanchez", "Perez", "Gomez", "Fernandez", "Rodriguez",
            "Gonzalez", "Diaz", "Moreno", "Romero", "Navarro", "Molina", "Torres", "Ruiz",
            "Vazquez", "Ortega", "Castro", "Serrano", "Rivas", "Delgado", "Moya", "Gil",
            "Leon", "Campos", "Cruz", "Vera", "Cano", "Ibanez"
    );

    private static final List<String> CITIES = List.of(
            "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza", "Malaga", "Murcia", "Bilbao",
            "Alicante", "Valladolid", "Cordoba", "Gijon", "Salamanca", "Granada", "Oviedo", "Pamplona"
    );

    private static final List<String> STREETS = List.of(
            "Mayor", "del Prado", "de la Luna", "de la Estacion", "del Sol", "de la Rivera",
            "de las Acacias", "del Mercado", "de los Olivos", "de la Sierra", "del Cid", "Nueva",
            "de la Plaza", "del Canal", "del Parque", "de la Higuera"
    );

    private static final List<String> OBSERVACIONES = List.of(
            "Llego puntual y completo la sesion.",
            "Entrenamiento intenso con buena tecnica.",
            "Trabajo de movilidad antes de fuerza.",
            "Sesiones continuadas durante la semana.",
            "Retomo actividad tras unos dias de pausa.",
            "Buen ritmo en cardio y core.",
            "Asistencia correcta antes del turno de tarde.",
            "Se mantuvo muy estable durante la sesion.",
            "Entreno corto pero consistente.",
            "Buen progreso en la carga de trabajo.",
            "Recuperacion activa con estiramientos.",
            "Bloque de HIIT completado sin incidencias."
    );

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private JdbcTemplate jdbcTemplate;
    private final Path usersUploadPath;
    private final Random random = new Random(RANDOM_SEED);

    public DemoDataSeeder(ObjectProvider<JdbcTemplate> jdbcTemplateProvider, @Value("${upload.dir}") String uploadDir) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.usersUploadPath = Paths.get(uploadDir, "users").toAbsolutePath().normalize();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (this.jdbcTemplate == null) {
            return;
        }
        seedReferenceData();
    }

    private void seedReferenceData() throws IOException {
        Map<String, Long> roleIds = ensureRoles();
        Map<String, Long> planIds = ensurePlans();

        List<UserSpec> users = buildUsers();
        ensureAvatars(users);
        upsertUsers(users, roleIds, planIds);

        Map<String, Long> userIds = loadUserIds(users);
        cleanupSeedDependencies(userIds.values());

        Map<String, Long> routineIds = ensureRoutines(users);
        assignRoutines(users, userIds, routineIds);
        seedAttendances(users, userIds);
        seedPayments(users, userIds, planIds);
        seedCoreSaasModules();
    }

    private Map<String, Long> ensureRoles() {
        ensureRole("STAFF");
        ensureRole("CLIENTE");

        Map<String, Long> roles = new LinkedHashMap<>();
        roles.put("STAFF", loadSingleId("roles", "nombre", "STAFF"));
        roles.put("CLIENTE", loadSingleId("roles", "nombre", "CLIENTE"));
        return roles;
    }

    private void ensureRole(String nombre) {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles WHERE nombre = ?", Integer.class, nombre);
        if (total == null || total == 0) {
            jdbcTemplate.update("INSERT INTO roles (nombre) VALUES (?)", nombre);
        }
    }

    private Map<String, Long> ensurePlans() {
        for (PlanSpec spec : PLAN_SPECS) {
            Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM planes WHERE nombre = ?", Integer.class, spec.nombre());
            if (total == null || total == 0) {
                jdbcTemplate.update(
                        "INSERT INTO planes (nombre, descripcion, precio_mensual, duracion_dias, activo) VALUES (?, ?, ?, ?, ?)",
                        spec.nombre(), spec.descripcion(), spec.precioMensual(), spec.duracionDias(), spec.activo());
            } else {
                jdbcTemplate.update(
                        "UPDATE planes SET descripcion = ?, precio_mensual = ?, duracion_dias = ?, activo = ? WHERE nombre = ?",
                        spec.descripcion(), spec.precioMensual(), spec.duracionDias(), spec.activo(), spec.nombre());
            }
        }

        Map<String, Long> plans = new LinkedHashMap<>();
        for (PlanSpec spec : PLAN_SPECS) {
            plans.put(spec.nombre(), loadSingleId("planes", "nombre", spec.nombre()));
        }
        return plans;
    }

    private List<UserSpec> buildUsers() {
        List<UserSpec> users = new ArrayList<>();

        users.add(new UserSpec(
                "carlos@demo.com", "carlos.martinez@flacofitness.es", "Carlos", "Martinez", generateDni(1001),
                generatePhone(1001, true), LocalDate.now().minusYears(34).minusDays(22), buildAddress(1001),
                LocalDateTime.now().minusDays(220), true, nextPaymentDate("Premium", FinancialProfile.CLEAN),
                "CLIENTE", "Premium", photoPath(1), ActivityProfile.VERY_ACTIVE, FinancialProfile.CLEAN,
                Gender.MALE, 1));

        users.add(new UserSpec(
                "ana@demo.com", "ana.garcia@flacofitness.es", "Ana", "Garcia", generateDni(1002),
                generatePhone(1002, false), LocalDate.now().minusYears(29).minusDays(145), buildAddress(1002),
                LocalDateTime.now().minusDays(190), true, nextPaymentDate("Basico", FinancialProfile.PENDING),
                "CLIENTE", "Basico", photoPath(2), ActivityProfile.ACTIVE, FinancialProfile.PENDING,
                Gender.FEMALE, 2));

        users.add(new UserSpec(
                "david@demo.com", "david.lopez@flacofitness.es", "David", "Lopez", generateDni(1003),
                generatePhone(1003, true), LocalDate.now().minusYears(38).minusDays(34), buildAddress(1003),
                LocalDateTime.now().minusDays(260), true, nextPaymentDate("Premium", FinancialProfile.CLEAN),
                "CLIENTE", "Premium", photoPath(3), ActivityProfile.VERY_ACTIVE, FinancialProfile.CLEAN,
                Gender.MALE, 3));

        users.add(new UserSpec(
                "maria@demo.com", "maria.sanchez@flacofitness.es", "Maria", "Sanchez", generateDni(1004),
                generatePhone(1004, false), LocalDate.now().minusYears(31).minusDays(80), buildAddress(1004),
                LocalDateTime.now().minusDays(150), true, nextPaymentDate("Basico", FinancialProfile.OVERDUE),
                "CLIENTE", "Basico", photoPath(4), ActivityProfile.MODERATE, FinancialProfile.OVERDUE,
                Gender.FEMALE, 4));

        users.add(new UserSpec(
                "jorge@demo.com", "jorge.perez@flacofitness.es", "Jorge", "Perez", generateDni(1005),
                generatePhone(1005, true), LocalDate.now().minusYears(45).minusDays(19), buildAddress(1005),
                LocalDateTime.now().minusDays(320), false, null,
                "CLIENTE", "Estudiante", photoPath(5), ActivityProfile.INACTIVE, FinancialProfile.OVERDUE,
                Gender.MALE, 5));

        for (int index = 6; index <= TARGET_PHOTO_USERS; index++) {
            Gender gender = index % 2 == 0 ? Gender.MALE : Gender.FEMALE;
            ActivityProfile activity = activityProfileForIndex(index, true);
            FinancialProfile financial = financialProfileForIndex(index, activity);
            String role = isPhotoStaff(index) ? "STAFF" : "CLIENTE";
            String plan = selectPlanByActivity(activity, index);
            String firstName = pickFirstName(index, gender);
            String[] surnames = pickSurnames(index);

            users.add(new UserSpec(
                    null,
                    buildEmail(firstName, surnames[0], surnames[1], index),
                    firstName,
                    surnames[0] + " " + surnames[1],
                    generateDni(1000 + index),
                    generatePhone(1000 + index, gender == Gender.MALE),
                    birthDateFor(index, activity),
                    buildAddress(1000 + index),
                    registrationDateFor(index, true),
                    activity != ActivityProfile.INACTIVE,
                    nextPaymentDate(plan, financial),
                    role,
                    plan,
                    photoPath(index),
                    activity,
                    financial,
                    gender,
                    index));
        }

        for (int index = TARGET_PHOTO_USERS + 1; index <= TARGET_TOTAL_USERS; index++) {
            Gender gender = index % 2 == 0 ? Gender.MALE : Gender.FEMALE;
            ActivityProfile activity = activityProfileForIndex(index, false);
            FinancialProfile financial = financialProfileForIndex(index, activity);
            String role = isExtraStaff(index) ? "STAFF" : "CLIENTE";
            String plan = selectPlanByActivity(activity, index);
            String firstName = pickFirstName(index, gender);
            String[] surnames = pickSurnames(index + 7);

            users.add(new UserSpec(
                    null,
                    buildEmail(firstName, surnames[0], surnames[1], index),
                    firstName,
                    surnames[0] + " " + surnames[1],
                    generateDni(1000 + index),
                    generatePhone(1000 + index, gender == Gender.MALE),
                    birthDateFor(index, activity),
                    buildAddress(1000 + index),
                    registrationDateFor(index, false),
                    activity != ActivityProfile.INACTIVE,
                    nextPaymentDate(plan, financial),
                    role,
                    plan,
                    null,
                    activity,
                    financial,
                    gender,
                    index));
        }

        return users;
    }

    private boolean isPhotoStaff(int index) {
        return index == 8 || index == 23;
    }

    private boolean isExtraStaff(int index) {
        return index == 51;
    }

    private void ensureAvatars(List<UserSpec> users) throws IOException {
        Files.createDirectories(usersUploadPath);
        for (UserSpec user : users) {
            if (!StringUtils.hasText(user.fotoPath())) {
                continue;
            }

            Path target = usersUploadPath.resolve(fileNameFromPath(user.fotoPath())).normalize();
            if (Files.exists(target)) {
                continue;
            }

            String initials = user.nombre().substring(0, 1).toUpperCase(Locale.ROOT)
                    + (StringUtils.hasText(user.apellidos()) ? user.apellidos().substring(0, 1).toUpperCase(Locale.ROOT) : "X");
            Files.writeString(target, buildAvatarSvg(initials, user.gender(), user.seedIndex()), StandardCharsets.UTF_8);
        }
    }

    private String buildAvatarSvg(String initials, Gender gender, int seedIndex) {
        String[] backgrounds = {"#0f172a", "#123c4c", "#243b55", "#3c2d64", "#1f4d3a", "#5b342f"};
        String[] skinTones = {"#f1c7a7", "#e7b48f", "#d79a71", "#c8865a"};
        String[] hairColors = {"#2c1e1a", "#5a3f2d", "#8a5a3c", "#1d2939"};
        String[] shirtColors = {"#7fc8f8", "#f47c7c", "#8dd3c7", "#f9c74f", "#9d7cfe", "#45b69c"};

        String bgA = backgrounds[seedIndex % backgrounds.length];
        String bgB = backgrounds[(seedIndex + 2) % backgrounds.length];
        String skin = skinTones[seedIndex % skinTones.length];
        String hair = hairColors[(seedIndex + (gender == Gender.FEMALE ? 1 : 0)) % hairColors.length];
        String shirt = shirtColors[seedIndex % shirtColors.length];
        String shirtShadow = shade(shirt, 0.82);
        String hairShape = gender == Gender.FEMALE
                ? "<path d=\"M67 100c0-28 24-50 53-50s53 22 53 50c0 18-7 29-14 39-8-15-20-21-39-21s-31 6-39 21c-7-10-14-21-14-39z\" fill=\"" + hair + "\" opacity=\"0.95\"/>"
                : "<path d=\"M68 95c4-27 25-45 52-45s48 18 52 45c-8-7-18-11-27-11-10 0-15 3-25 3s-15-3-25-3c-9 0-19 4-27 11z\" fill=\"" + hair + "\" opacity=\"0.95\"/>";

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <linearGradient id="shirt" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                  </defs>
                  <rect width="256" height="256" rx="30" fill="url(#bg)"/>
                  <circle cx="128" cy="102" r="43" fill="%s"/>
                  %s
                  <circle cx="111" cy="102" r="5" fill="#1f2937"/>
                  <circle cx="145" cy="102" r="5" fill="#1f2937"/>
                  <path d="M112 123c8 7 24 7 32 0" fill="none" stroke="#1f2937" stroke-width="4" stroke-linecap="round"/>
                  <path d="M70 214c14-28 33-42 58-42s44 14 58 42" fill="url(#shirt)"/>
                  <text x="128" y="202" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="28" font-weight="700" fill="rgba(255,255,255,0.92)">%s</text>
                </svg>
                """.formatted(bgA, bgB, shirt, shirtShadow, skin, hairShape, initials);
    }

    private String shade(String color, double factor) {
        int[] rgb = hexToRgb(color);
        return String.format("#%02x%02x%02x",
                clamp((int) Math.round(rgb[0] * factor)),
                clamp((int) Math.round(rgb[1] * factor)),
                clamp((int) Math.round(rgb[2] * factor)));
    }

    private int[] hexToRgb(String color) {
        String normalized = color.startsWith("#") ? color.substring(1) : color;
        return new int[] {
                Integer.parseInt(normalized.substring(0, 2), 16),
                Integer.parseInt(normalized.substring(2, 4), 16),
                Integer.parseInt(normalized.substring(4, 6), 16)
        };
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void upsertUsers(List<UserSpec> users, Map<String, Long> roleIds, Map<String, Long> planIds) {
        for (UserSpec user : users) {
            Long roleId = roleIds.get(user.roleName());
            Long planId = planIds.get(user.planName());
            Long userId = findUserId(user.email(), user.legacyEmail()).orElse(null);

            if (userId == null) {
                jdbcTemplate.update(
                        "INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, fecha_nacimiento, direccion, foto_path, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        user.nombre(), user.apellidos(), user.dni(), user.email(), user.telefono(),
                        user.fechaNacimiento() != null ? java.sql.Date.valueOf(user.fechaNacimiento()) : null,
                        user.direccion(), user.fotoPath(), user.activo(),
                        user.fechaRegistro() != null ? Timestamp.valueOf(user.fechaRegistro()) : null,
                        user.fechaProximoPago() != null ? java.sql.Date.valueOf(user.fechaProximoPago()) : null,
                        roleId, planId);
            } else {
                jdbcTemplate.update(
                        "UPDATE usuarios SET nombre = ?, apellidos = ?, dni = ?, email = ?, telefono = ?, fecha_nacimiento = ?, direccion = ?, foto_path = ?, activo = ?, fecha_registro = ?, fecha_proximo_pago = ?, rol_id = ?, plan_id = ? WHERE id = ?",
                        user.nombre(), user.apellidos(), user.dni(), user.email(), user.telefono(),
                        user.fechaNacimiento() != null ? java.sql.Date.valueOf(user.fechaNacimiento()) : null,
                        user.direccion(), user.fotoPath(), user.activo(),
                        user.fechaRegistro() != null ? Timestamp.valueOf(user.fechaRegistro()) : null,
                        user.fechaProximoPago() != null ? java.sql.Date.valueOf(user.fechaProximoPago()) : null,
                        roleId, planId, userId);
            }
        }
    }

    private Map<String, Long> loadUserIds(List<UserSpec> users) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (UserSpec user : users) {
            findUserId(user.email(), user.legacyEmail()).ifPresent(value -> ids.put(user.email(), value));
        }
        return ids;
    }

    private Optional<Long> findUserId(String primaryEmail, String legacyEmail) {
        List<String> candidates = new ArrayList<>();
        if (StringUtils.hasText(primaryEmail)) {
            candidates.add(primaryEmail);
        }
        if (StringUtils.hasText(legacyEmail)) {
            candidates.add(legacyEmail);
        }

        for (String candidate : candidates) {
            Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, candidate);
            if (total != null && total > 0) {
                return Optional.of(loadSingleId("usuarios", "email", candidate));
            }
        }

        return Optional.empty();
    }

    private void cleanupSeedDependencies(Iterable<Long> userIds) {
        List<Long> ids = new ArrayList<>();
        userIds.forEach(ids::add);
        if (ids.isEmpty()) {
            return;
        }

        deleteByIds("DELETE FROM reservas_sesion WHERE usuario_id IN (%s)", ids);
        deleteByIds("DELETE FROM asistencias WHERE usuario_id IN (%s)", ids);
        deleteByIds("DELETE FROM pagos WHERE usuario_id IN (%s)", ids);
        deleteByIds("DELETE FROM membresias_usuario WHERE usuario_id IN (%s)", ids);
        deleteByIds("DELETE FROM usuario_rutina WHERE usuario_id IN (%s)", ids);
    }

    private Map<String, Long> ensureRoutines(List<UserSpec> users) {
        Map<String, Long> routines = new LinkedHashMap<>();

        for (RoutineSpec spec : GENERAL_ROUTINES) {
            upsertRoutine(spec.nombre(), spec.descripcion(), spec.tipoRutina(), spec.activa(), spec.createdAt());
            routines.put(spec.nombre(), loadSingleId("rutinas", "nombre", spec.nombre()));
        }

        Map<String, String> personalized = buildPersonalizedRoutineNames(users);
        for (Map.Entry<String, String> entry : personalized.entrySet()) {
            UserSpec user = users.stream().filter(candidate -> candidate.email().equals(entry.getKey())).findFirst().orElse(null);
            if (user == null) {
                continue;
            }

            upsertRoutine(entry.getValue(), "Rutina personalizada para " + user.nombre() + " " + user.apellidos() + ".",
                    TipoRutina.PERSONALIZADA, true,
                    LocalDateTime.now().minusDays(12 + Math.abs(entry.getKey().hashCode() % 90)));
            routines.put(entry.getValue(), loadSingleId("rutinas", "nombre", entry.getValue()));
        }

        return routines;
    }

    private Map<String, String> buildPersonalizedRoutineNames(List<UserSpec> users) {
        return users.stream()
                .filter(user -> user.activo() && ("Premium".equalsIgnoreCase(user.planName()) || "Plus".equalsIgnoreCase(user.planName())))
                .sorted(Comparator.comparingInt((UserSpec user) -> activityWeight(user.activityProfile())).reversed()
                        .thenComparing(UserSpec::fechaRegistro, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(10)
                .collect(Collectors.toMap(
                        UserSpec::email,
                        user -> "Plan " + user.nombre() + " " + personalizedFocus(user),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private int activityWeight(ActivityProfile profile) {
        return switch (profile) {
            case VERY_ACTIVE -> 80;
            case ACTIVE -> 70;
            case MODERATE -> 55;
            case LOW -> 30;
            case INACTIVE -> 10;
        };
    }

    private String personalizedFocus(UserSpec user) {
        List<String> focus = List.of("Definicion", "Fuerza", "Rendimiento", "Equilibrio", "Resistencia", "Volumen", "Hipertrofia", "Reactivacion", "Tonificacion", "Cambio");
        return focus.get(Math.floorMod(user.seedIndex(), focus.size()));
    }

    private void upsertRoutine(String nombre, String descripcion, TipoRutina tipo, boolean activa, LocalDateTime fechaCreacion) {
        Long id = findRoutineId(nombre);
        if (id == null) {
            jdbcTemplate.update(
                    "INSERT INTO rutinas (nombre, descripcion, fecha_creacion, tipo_rutina, activa) VALUES (?, ?, ?, ?, ?)",
                    nombre, descripcion, Timestamp.valueOf(fechaCreacion), tipo.name(), activa);
            return;
        }

        jdbcTemplate.update(
                "UPDATE rutinas SET descripcion = ?, fecha_creacion = ?, tipo_rutina = ?, activa = ? WHERE id = ?",
                descripcion, Timestamp.valueOf(fechaCreacion), tipo.name(), activa, id);
    }

    private Long findRoutineId(String nombre) {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rutinas WHERE nombre = ?", Integer.class, nombre);
        if (total == null || total == 0) {
            return null;
        }
        return loadSingleId("rutinas", "nombre", nombre);
    }

    private void assignRoutines(List<UserSpec> users, Map<String, Long> userIds, Map<String, Long> routineIds) {
        Map<String, String> personalized = buildPersonalizedRoutineNames(users);
        Set<String> noRoutineUsers = users.stream()
                .filter(user -> !user.activo() && user.activityProfile() == ActivityProfile.INACTIVE)
                .limit(4)
                .map(UserSpec::email)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (UserSpec user : users) {
            Long userId = userIds.get(user.email());
            if (userId == null || noRoutineUsers.contains(user.email())) {
                continue;
            }

            Set<Long> assigned = new LinkedHashSet<>();
            assigned.add(routineIds.get("Movilidad y Core"));

            if ("Basico".equalsIgnoreCase(user.planName()) || "Estudiante".equalsIgnoreCase(user.planName())) {
                assigned.add(routineIds.get("Full Body Base"));
            } else {
                assigned.add(routineIds.get("Fuerza Funcional"));
            }

            if (user.activityProfile() == ActivityProfile.VERY_ACTIVE || user.activityProfile() == ActivityProfile.ACTIVE) {
                assigned.add(routineIds.get("HIIT Metabolico"));
                assigned.add(routineIds.get("Cardio Salud"));
            }

            if (user.gender() == Gender.FEMALE) {
                assigned.add(routineIds.get("Tren Inferior"));
            } else {
                assigned.add(routineIds.get("Tren Superior"));
            }

            if (!user.activo() || user.financialProfile() == FinancialProfile.OVERDUE) {
                assigned.add(routineIds.get("Recuperacion Activa"));
            }

            String personalizedName = personalized.get(user.email());
            if (personalizedName != null && routineIds.containsKey(personalizedName)) {
                assigned.add(routineIds.get(personalizedName));
            }

            if (user.activityProfile() == ActivityProfile.LOW && user.financialProfile() == FinancialProfile.CLEAN) {
                assigned.add(routineIds.get("Cardio Salud"));
            }

            for (Long routineId : assigned) {
                if (routineId != null && !existsUserRoutine(userId, routineId)) {
                    jdbcTemplate.update("INSERT INTO usuario_rutina (rutina_id, usuario_id) VALUES (?, ?)", routineId, userId);
                }
            }
        }
    }

    private boolean existsUserRoutine(Long userId, Long routineId) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario_rutina WHERE usuario_id = ? AND rutina_id = ?",
                Integer.class, userId, routineId);
        return total != null && total > 0;
    }

    private void seedAttendances(List<UserSpec> users, Map<String, Long> userIds) {
        for (UserSpec user : users) {
            Long userId = userIds.get(user.email());
            if (userId == null) {
                continue;
            }

            int target = switch (user.activityProfile()) {
                case VERY_ACTIVE -> 24 + random.nextInt(5);
                case ACTIVE -> 14 + random.nextInt(5);
                case MODERATE -> 7 + random.nextInt(4);
                case LOW -> 2 + random.nextInt(3);
                case INACTIVE -> random.nextInt(2);
            };

            LocalDate lowerBound = user.fechaRegistro() != null
                    ? user.fechaRegistro().toLocalDate().plusDays(7)
                    : LocalDate.now().minusDays(180);
            LocalDate startDate = lowerBound.isAfter(LocalDate.now().minusDays(180)) ? lowerBound : LocalDate.now().minusDays(180);
            Set<LocalDate> usedDates = new HashSet<>();

            for (int i = 0; i < target; i++) {
                LocalDate date = generateAttendanceDate(startDate, user.activityProfile(), usedDates);
                LocalTime time = generateAttendanceTime(user.activityProfile(), date);
                String note = random.nextDouble() < 0.25 ? OBSERVACIONES.get(random.nextInt(OBSERVACIONES.size())) : null;

                jdbcTemplate.update(
                        "INSERT INTO asistencias (fecha, hora_entrada, observaciones, usuario_id) VALUES (?, ?, ?, ?)",
                        java.sql.Date.valueOf(date), time, note, userId);
                usedDates.add(date);
            }
        }
    }

    private LocalDate generateAttendanceDate(LocalDate lowerBound, ActivityProfile profile, Set<LocalDate> usedDates) {
        int maxDays = Math.max(30, (int) ChronoUnit.DAYS.between(lowerBound, LocalDate.now()));
        for (int attempt = 0; attempt < 120; attempt++) {
            LocalDate candidate = lowerBound.plusDays(random.nextInt(maxDays + 1));
            if (candidate.isAfter(LocalDate.now())) {
                candidate = LocalDate.now().minusDays(random.nextInt(3));
            }

            DayOfWeek dow = candidate.getDayOfWeek();
            boolean allowed = switch (profile) {
                case VERY_ACTIVE, ACTIVE -> dow != DayOfWeek.SUNDAY;
                case MODERATE -> dow != DayOfWeek.SUNDAY && (dow != DayOfWeek.SATURDAY || random.nextDouble() < 0.35);
                case LOW -> dow != DayOfWeek.SUNDAY && random.nextDouble() < 0.6;
                case INACTIVE -> random.nextDouble() < 0.15;
            };

            if (allowed && !usedDates.contains(candidate)) {
                return candidate;
            }
        }

        LocalDate fallback = lowerBound;
        while (usedDates.contains(fallback) || fallback.isAfter(LocalDate.now())) {
            fallback = fallback.plusDays(1);
        }
        return fallback;
    }

    private LocalTime generateAttendanceTime(ActivityProfile profile, LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return LocalTime.of(10 + random.nextInt(4), random.nextBoolean() ? 0 : 30);
        }

        return switch (profile) {
            case VERY_ACTIVE -> LocalTime.of(7 + random.nextInt(2), random.nextBoolean() ? 0 : 30);
            case ACTIVE -> random.nextBoolean()
                    ? LocalTime.of(7 + random.nextInt(2), random.nextBoolean() ? 0 : 30)
                    : LocalTime.of(18 + random.nextInt(3), random.nextBoolean() ? 0 : 15);
            case MODERATE -> random.nextBoolean()
                    ? LocalTime.of(8 + random.nextInt(2), random.nextBoolean() ? 0 : 30)
                    : LocalTime.of(19 + random.nextInt(2), random.nextBoolean() ? 0 : 15);
            case LOW -> LocalTime.of(18 + random.nextInt(2), random.nextBoolean() ? 0 : 30);
            case INACTIVE -> LocalTime.of(19, 0);
        };
    }

    private void seedPayments(List<UserSpec> users, Map<String, Long> userIds, Map<String, Long> planIds) {
        Map<String, PlanSpec> planByName = PLAN_SPECS.stream().collect(Collectors.toMap(PlanSpec::nombre, spec -> spec));

        for (UserSpec user : users) {
            Long userId = userIds.get(user.email());
            Long planId = planIds.get(user.planName());
            PlanSpec plan = planByName.get(user.planName());
            if (userId == null || plan == null || planId == null) {
                continue;
            }

            int cycles = paymentCyclesFor(user);
            LocalDate dueDate = paymentStartDate(user, plan, cycles);
            int paidCycles = switch (user.financialProfile()) {
                case CLEAN -> cycles;
                case PENDING, OVERDUE -> Math.max(1, cycles - 1);
            };

            for (int cycle = 1; cycle <= cycles; cycle++) {
                boolean last = cycle == cycles;
                EstadoPago estado;
                LocalDate fechaPago = null;

                if (cycle <= paidCycles) {
                    estado = EstadoPago.PAGADO;
                    fechaPago = dueDate.minusDays(1 + random.nextInt(3));
                } else if (user.financialProfile() == FinancialProfile.PENDING && last) {
                    estado = EstadoPago.PENDIENTE;
                } else {
                    estado = EstadoPago.VENCIDO;
                }

                String referencia = buildReference(user, cycle, dueDate);
                if (!existsPayment(referencia)) {
                    jdbcTemplate.update(
                            "INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            fechaPago != null ? java.sql.Date.valueOf(fechaPago) : null,
                            java.sql.Date.valueOf(dueDate),
                            plan.precioMensual(),
                            randomMetodoPago(user, cycle).name(),
                            estado.name(),
                            referencia,
                            userId,
                            planId);
                }

                dueDate = dueDate.plusDays(plan.duracionDias());
            }

            LocalDate nextPayment = switch (user.financialProfile()) {
                case CLEAN -> dueDate;
                case PENDING, OVERDUE -> dueDate.minusDays(plan.duracionDias());
            };

            if (user.activo()) {
                jdbcTemplate.update("UPDATE usuarios SET fecha_proximo_pago = ? WHERE id = ?",
                        java.sql.Date.valueOf(nextPayment), userId);
            } else {
                jdbcTemplate.update("UPDATE usuarios SET fecha_proximo_pago = NULL WHERE id = ?", userId);
            }
        }
    }

    private void seedCoreSaasModules() {
        updatePlanCatalogMetadata();
        List<Long> staffProfileIds = seedStaffProfiles();
        seedMembershipContracts();
        seedTrials(staffProfileIds);
        Map<String, Long> classIds = seedClasses();
        seedSessionsAndReservations(classIds, staffProfileIds);
        linkPaymentsToMembershipContracts();
        assignStaffToRoutines(staffProfileIds);
    }

    private void updatePlanCatalogMetadata() {
        updatePlanCatalog("Basico", TipoMembresia.MENSUAL, "Acceso general, registro de asistencias y rutinas base.");
        updatePlanCatalog("Premium", TipoMembresia.PREMIUM, "Plan historico, ya no se ofrece como alta nueva.");
        updatePlanCatalog("Plus", TipoMembresia.PREMIUM, "Plan historico, mantenido para contratos antiguos.");
        updatePlanCatalog("Estudiante", TipoMembresia.ESTUDIANTE, "Tarifa reducida para perfiles jovenes con acceso completo.");
        updatePlanCatalog("Trimestral", TipoMembresia.TRIMESTRAL, "Plan historico de larga duracion mantenido para compatibilidad.");
    }

    private void updatePlanCatalog(String nombre, TipoMembresia tipo, String beneficios) {
        jdbcTemplate.update(
                "UPDATE planes SET tipo_membresia = ?, beneficios = ? WHERE nombre = ?",
                tipo.name(), beneficios, nombre);
    }

    private List<Long> seedStaffProfiles() {
        List<Map<String, Object>> staffUsers = jdbcTemplate.queryForList("""
                SELECT u.id, u.nombre, u.apellidos, u.email
                FROM usuarios u
                JOIN roles r ON r.id = u.rol_id
                WHERE r.nombre = 'STAFF'
                ORDER BY u.id ASC
                """);

        List<Long> staffProfileIds = new ArrayList<>();
        for (int index = 0; index < staffUsers.size(); index++) {
            Map<String, Object> row = staffUsers.get(index);
            Long userId = ((Number) row.get("ID")).longValue();
            RolStaff rolStaff = index == 0 ? RolStaff.GERENTE : (index == 1 ? RolStaff.ENTRENADOR : RolStaff.RECEPCION);
            String especialidad = index == 0 ? "Direccion operativa" : (index == 1 ? "Fuerza y recomposicion" : "Recepcion y atencion");
            String observaciones = "Perfil staff demo ligado al usuario " + row.get("EMAIL") + ".";
            boolean puedeImpartirClases = rolStaff == RolStaff.ENTRENADOR;

            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM staff_perfiles WHERE usuario_id = ?",
                    Integer.class, userId);

            if (total == null || total == 0) {
                jdbcTemplate.update(
                        "INSERT INTO staff_perfiles (usuario_id, especialidad, rol_staff, activo, puede_impartir_clases, fecha_alta, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        userId,
                        especialidad,
                        rolStaff.name(),
                        true,
                        puedeImpartirClases,
                        java.sql.Date.valueOf(LocalDate.now().minusDays(90L + index * 15L)),
                        observaciones);
            } else {
                jdbcTemplate.update(
                        "UPDATE staff_perfiles SET especialidad = ?, rol_staff = ?, activo = ?, puede_impartir_clases = ?, observaciones = ? WHERE usuario_id = ?",
                        especialidad,
                        rolStaff.name(),
                        true,
                        puedeImpartirClases,
                        observaciones,
                        userId);
            }

            staffProfileIds.add(loadStaffProfileIdByUserId(userId));
        }

        return staffProfileIds;
    }

    private void seedMembershipContracts() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                SELECT u.id, u.plan_id, u.activo, u.fecha_registro, u.fecha_proximo_pago, p.precio_mensual, p.duracion_dias
                FROM usuarios u
                LEFT JOIN planes p ON p.id = u.plan_id
                WHERE u.plan_id IS NOT NULL
                ORDER BY u.id ASC
                """);

        for (Map<String, Object> row : users) {
            Long userId = ((Number) row.get("ID")).longValue();
            Long planId = ((Number) row.get("PLAN_ID")).longValue();
            BigDecimal precio = (BigDecimal) row.get("PRECIO_MENSUAL");
            Integer duracion = row.get("DURACION_DIAS") != null ? ((Number) row.get("DURACION_DIAS")).intValue() : 30;
            LocalDate fechaInicio = toLocalDate(row.get("FECHA_REGISTRO")).orElse(LocalDate.now().minusDays(duracion));
            LocalDate fechaFin = toLocalDate(row.get("FECHA_PROXIMO_PAGO")).orElse(fechaInicio.plusDays(Math.max(duracion, 1)));
            boolean activo = Boolean.TRUE.equals(row.get("ACTIVO"));
            EstadoMembresia estado = estadoMembresiaDemo(activo, fechaFin);

            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM membresias_usuario WHERE usuario_id = ? AND plan_id = ? AND fecha_inicio = ?",
                    Integer.class,
                    userId,
                    planId,
                    java.sql.Date.valueOf(fechaInicio));

            if (total != null && total > 0) {
                continue;
            }

            jdbcTemplate.update(
                    "INSERT INTO membresias_usuario (usuario_id, plan_id, fecha_inicio, fecha_fin, estado, precio_snapshot, origen, observaciones, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    userId,
                    planId,
                    java.sql.Date.valueOf(fechaInicio),
                    java.sql.Date.valueOf(fechaFin),
                    estado.name(),
                    precio,
                    "Seed local",
                    "Contrato demo generado desde el plan actual del usuario.",
                    Timestamp.valueOf(LocalDateTime.now().minusDays(12)));
        }
    }

    private EstadoMembresia estadoMembresiaDemo(boolean usuarioActivo, LocalDate fechaFin) {
        if (!usuarioActivo) {
            return EstadoMembresia.CANCELADA;
        }
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            return EstadoMembresia.VENCIDA;
        }
        return EstadoMembresia.ACTIVA;
    }

    private void seedTrials(List<Long> staffProfileIds) {
        Long staffId = firstOrNull(staffProfileIds);
        List<TrialSpec> trials = List.of(
                new TrialSpec("Laura", "Navas", "690123445", "laura.navas.trial@demo.es", "Instagram", LocalDate.now(), EstadoTrial.PENDIENTE),
                new TrialSpec("Hugo", "Campos", "691223344", "hugo.campos.trial@demo.es", "Referido", LocalDate.now().plusDays(1), EstadoTrial.PENDIENTE),
                new TrialSpec("Claudia", "Moya", "692334455", "claudia.moya.trial@demo.es", "Web", LocalDate.now().minusDays(1), EstadoTrial.ASISTIO),
                new TrialSpec("Ivan", "Rivas", "693445566", "ivan.rivas.trial@demo.es", "Flyer local", LocalDate.now().minusDays(3), EstadoTrial.NO_ASISTIO),
                new TrialSpec("Sofia", "Cano", "694556677", "sofia.cano.trial@demo.es", "Instagram", LocalDate.now().plusDays(3), EstadoTrial.PENDIENTE)
        );

        for (TrialSpec trial : trials) {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM trials WHERE email = ?",
                    Integer.class,
                    trial.email());

            if (total == null || total == 0) {
                jdbcTemplate.update(
                        "INSERT INTO trials (nombre, apellidos, telefono, email, origen, fecha_prueba, estado, staff_responsable_id, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        trial.nombre(),
                        trial.apellidos(),
                        trial.telefono(),
                        trial.email(),
                        trial.origen(),
                        java.sql.Date.valueOf(trial.fechaPrueba()),
                        trial.estado().name(),
                        staffId,
                        "Lead demo para explicar el embudo comercial.",
                        Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
            } else {
                jdbcTemplate.update(
                        "UPDATE trials SET fecha_prueba = ?, estado = ?, staff_responsable_id = ? WHERE email = ?",
                        java.sql.Date.valueOf(trial.fechaPrueba()),
                        trial.estado().name(),
                        staffId,
                        trial.email());
            }
        }
    }

    private Map<String, Long> seedClasses() {
        List<ClassSpec> classes = List.of(
                new ClassSpec("HIIT Controlado", "Sesion intensa de intervalos con aforo reducido.", 12, true),
                new ClassSpec("Yoga Movilidad", "Movilidad, respiracion y recuperacion activa.", 16, true),
                new ClassSpec("Spinning Base", "Cardio guiado para grupos mixtos.", 14, true),
                new ClassSpec("Fuerza Tecnica", "Trabajo guiado de fuerza con supervision de entrenador.", 10, true)
        );

        Map<String, Long> ids = new LinkedHashMap<>();
        for (ClassSpec spec : classes) {
            Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clases WHERE nombre = ?", Integer.class, spec.nombre());
            if (total == null || total == 0) {
                jdbcTemplate.update(
                        "INSERT INTO clases (nombre, descripcion, capacidad_sugerida, activa, observaciones) VALUES (?, ?, ?, ?, ?)",
                        spec.nombre(),
                        spec.descripcion(),
                        spec.capacidadSugerida(),
                        spec.activa(),
                        "Clase demo del nucleo vendible.");
            } else {
                jdbcTemplate.update(
                        "UPDATE clases SET descripcion = ?, capacidad_sugerida = ?, activa = ? WHERE nombre = ?",
                        spec.descripcion(),
                        spec.capacidadSugerida(),
                        spec.activa(),
                        spec.nombre());
            }
            ids.put(spec.nombre(), loadSingleId("clases", "nombre", spec.nombre()));
        }
        return ids;
    }

    private void seedSessionsAndReservations(Map<String, Long> classIds, List<Long> staffProfileIds) {
        List<Long> activeUsers = jdbcTemplate.queryForList(
                "SELECT id FROM usuarios WHERE activo = TRUE ORDER BY id ASC LIMIT 24",
                Long.class);
        if (activeUsers.isEmpty() || classIds.isEmpty()) {
            return;
        }

        List<Long> instructorProfileIds = findInstructorProfileIds();
        Long staff1 = firstOrNull(instructorProfileIds);
        Long staff2 = instructorProfileIds.size() > 1 ? instructorProfileIds.get(1) : staff1;
        Long rutinaHiit = findRoutineId("HIIT Metabolico");
        Long rutinaMovilidad = findRoutineId("Movilidad y Core");

        List<SessionSpec> sessions = List.of(
                new SessionSpec("HIIT Controlado", LocalDate.now(), LocalTime.of(18, 30), LocalTime.of(19, 15), 12, EstadoSesion.PROGRAMADA, staff2, rutinaHiit),
                new SessionSpec("Yoga Movilidad", LocalDate.now(), LocalTime.of(20, 0), LocalTime.of(20, 45), 16, EstadoSesion.PROGRAMADA, staff1, rutinaMovilidad),
                new SessionSpec("Spinning Base", LocalDate.now().plusDays(1), LocalTime.of(9, 30), LocalTime.of(10, 15), 14, EstadoSesion.PROGRAMADA, staff1, null),
                new SessionSpec("Fuerza Tecnica", LocalDate.now().plusDays(2), LocalTime.of(19, 0), LocalTime.of(20, 0), 10, EstadoSesion.PROGRAMADA, staff2, findRoutineId("Fuerza Funcional")),
                new SessionSpec("Yoga Movilidad", LocalDate.now().minusDays(1), LocalTime.of(19, 30), LocalTime.of(20, 15), 16, EstadoSesion.FINALIZADA, staff1, rutinaMovilidad)
        );

        int userOffset = 0;
        for (SessionSpec session : sessions) {
            Long classId = classIds.get(session.className());
            if (classId == null) {
                continue;
            }

            Long sessionId = upsertSession(session, classId);
            int reservations = Math.min(session.aforo() - 1, 5 + Math.floorMod(session.className().hashCode(), 5));
            for (int index = 0; index < reservations && index < activeUsers.size(); index++) {
                Long userId = activeUsers.get(Math.floorMod(userOffset + index, activeUsers.size()));
                upsertReservation(sessionId, userId, session.estado() == EstadoSesion.FINALIZADA ? EstadoReservaSesion.ASISTIO : EstadoReservaSesion.RESERVADA);
                if (session.estado() == EstadoSesion.FINALIZADA || (session.fecha().isEqual(LocalDate.now()) && index < 3)) {
                    upsertSessionAttendance(sessionId, userId, session.fecha(), session.horaInicio().plusMinutes(5), "Asistencia vinculada a sesion demo.");
                }
            }
            userOffset += 4;
        }
    }

    private Long upsertSession(SessionSpec session, Long classId) {
        Long sessionId = findSessionId(classId, session.fecha(), session.horaInicio());
        if (sessionId == null) {
            jdbcTemplate.update(
                    "INSERT INTO sesiones_clase (clase_id, fecha, hora_inicio, hora_fin, aforo, estado, rutina_id, staff_responsable_id, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    classId,
                    java.sql.Date.valueOf(session.fecha()),
                    session.horaInicio(),
                    session.horaFin(),
                    session.aforo(),
                    session.estado().name(),
                    session.rutinaId(),
                    session.staffId(),
                    "Sesion demo programada para validar agenda, reservas y asistencia.");
            return findSessionId(classId, session.fecha(), session.horaInicio());
        }

        jdbcTemplate.update(
                "UPDATE sesiones_clase SET hora_fin = ?, aforo = ?, estado = ?, rutina_id = ?, staff_responsable_id = ?, observaciones = ? WHERE id = ?",
                session.horaFin(),
                session.aforo(),
                session.estado().name(),
                session.rutinaId(),
                session.staffId(),
                "Sesion demo programada para validar agenda, reservas y asistencia.",
                sessionId);
        return sessionId;
    }

    private Long findSessionId(Long classId, LocalDate fecha, LocalTime horaInicio) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM sesiones_clase WHERE clase_id = ? AND fecha = ? AND hora_inicio = ? ORDER BY id ASC LIMIT 1",
                Long.class,
                classId,
                java.sql.Date.valueOf(fecha),
                horaInicio);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void upsertReservation(Long sessionId, Long userId, EstadoReservaSesion estado) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservas_sesion WHERE sesion_id = ? AND usuario_id = ?",
                Integer.class,
                sessionId,
                userId);
        if (total == null || total == 0) {
            jdbcTemplate.update(
                    "INSERT INTO reservas_sesion (sesion_id, usuario_id, estado, fecha_reserva) VALUES (?, ?, ?, ?)",
                    sessionId,
                    userId,
                    estado.name(),
                    Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        } else {
            jdbcTemplate.update(
                    "UPDATE reservas_sesion SET estado = ?, fecha_reserva = ? WHERE sesion_id = ? AND usuario_id = ?",
                    estado.name(),
                    Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                    sessionId,
                    userId);
        }
    }

    private void upsertSessionAttendance(Long sessionId, Long userId, LocalDate fecha, LocalTime horaEntrada, String observaciones) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM asistencias WHERE sesion_clase_id = ? AND usuario_id = ?",
                Integer.class,
                sessionId,
                userId);
        if (total != null && total > 0) {
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO asistencias (fecha, hora_entrada, observaciones, usuario_id, sesion_clase_id) VALUES (?, ?, ?, ?, ?)",
                java.sql.Date.valueOf(fecha),
                horaEntrada,
                observaciones,
                userId,
                sessionId);
    }

    private void linkPaymentsToMembershipContracts() {
        List<Map<String, Object>> contracts = jdbcTemplate.queryForList("""
                SELECT id, usuario_id, plan_id
                FROM membresias_usuario
                ORDER BY fecha_inicio DESC, id DESC
                """);

        for (Map<String, Object> contract : contracts) {
            jdbcTemplate.update(
                    "UPDATE pagos SET membresia_usuario_id = ? WHERE usuario_id = ? AND plan_id = ? AND membresia_usuario_id IS NULL",
                    ((Number) contract.get("ID")).longValue(),
                    ((Number) contract.get("USUARIO_ID")).longValue(),
                    ((Number) contract.get("PLAN_ID")).longValue());
        }
    }

    private void assignStaffToRoutines(List<Long> staffProfileIds) {
        List<Long> instructorProfileIds = findInstructorProfileIds();
        if (instructorProfileIds.isEmpty()) {
            return;
        }
        List<Long> routineIds = jdbcTemplate.queryForList("SELECT id FROM rutinas WHERE activa = TRUE ORDER BY id ASC", Long.class);
        for (int index = 0; index < routineIds.size(); index++) {
            jdbcTemplate.update(
                    "UPDATE rutinas SET staff_responsable_id = ? WHERE id = ?",
                    instructorProfileIds.get(index % instructorProfileIds.size()),
                    routineIds.get(index));
        }
    }

    private List<Long> findInstructorProfileIds() {
        return jdbcTemplate.queryForList("""
                SELECT id
                FROM staff_perfiles
                WHERE activo = TRUE
                  AND (rol_staff = 'ENTRENADOR' OR puede_impartir_clases = TRUE)
                ORDER BY id ASC
                """, Long.class);
    }

    private Long loadStaffProfileIdByUserId(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM staff_perfiles WHERE usuario_id = ? ORDER BY id ASC LIMIT 1",
                Long.class,
                userId);
    }

    private Long firstOrNull(List<Long> ids) {
        return ids.isEmpty() ? null : ids.get(0);
    }

    private Optional<LocalDate> toLocalDate(Object value) {
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof LocalDate localDate) {
            return Optional.of(localDate);
        }
        if (value instanceof java.sql.Date date) {
            return Optional.of(date.toLocalDate());
        }
        if (value instanceof Timestamp timestamp) {
            return Optional.of(timestamp.toLocalDateTime().toLocalDate());
        }
        if (value instanceof LocalDateTime localDateTime) {
            return Optional.of(localDateTime.toLocalDate());
        }
        return Optional.empty();
    }

    private int paymentCyclesFor(UserSpec user) {
        return switch (user.financialProfile()) {
            case CLEAN -> 4 + Math.floorMod(user.seedIndex(), 3);
            case PENDING -> 4 + Math.floorMod(user.seedIndex(), 2);
            case OVERDUE -> 3 + Math.floorMod(user.seedIndex(), 2);
        };
    }

    private LocalDate paymentStartDate(UserSpec user, PlanSpec plan, int cycles) {
        LocalDate base = user.fechaRegistro() != null ? user.fechaRegistro().toLocalDate().plusDays(plan.duracionDias()) : LocalDate.now().minusMonths(6);
        LocalDate lower = LocalDate.now().minusDays((long) plan.duracionDias() * cycles);
        return base.isAfter(lower) ? base : lower;
    }

    private MetodoPago randomMetodoPago(UserSpec user, int cycle) {
        int value = Math.abs(Objects.hash(user.email(), cycle, RANDOM_SEED)) % 100;
        if (value < 55) {
            return MetodoPago.TARJETA;
        }
        if (value < 80) {
            return MetodoPago.TRANSFERENCIA;
        }
        return MetodoPago.EFECTIVO;
    }

    private String buildReference(UserSpec user, int cycle, LocalDate dueDate) {
        return "FF-" + user.seedIndex() + "-" + cycle + "-" + dueDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private ActivityProfile activityProfileForIndex(int index, boolean photoUser) {
        if (photoUser) {
            if (index <= 10) {
                return ActivityProfile.VERY_ACTIVE;
            }
            if (index <= 24) {
                return ActivityProfile.ACTIVE;
            }
            if (index <= 38) {
                return ActivityProfile.MODERATE;
            }
            if (index <= 46) {
                return ActivityProfile.LOW;
            }
            return ActivityProfile.INACTIVE;
        }

        if (index <= 56) {
            return ActivityProfile.ACTIVE;
        }
        if (index <= 68) {
            return ActivityProfile.MODERATE;
        }
        if (index <= 74) {
            return ActivityProfile.LOW;
        }
        return ActivityProfile.INACTIVE;
    }

    private FinancialProfile financialProfileForIndex(int index, ActivityProfile activity) {
        return switch (activity) {
            case VERY_ACTIVE, ACTIVE -> index % 6 == 0 ? FinancialProfile.PENDING : FinancialProfile.CLEAN;
            case MODERATE -> index % 4 == 0 ? FinancialProfile.OVERDUE : FinancialProfile.PENDING;
            case LOW -> index % 3 == 0 ? FinancialProfile.OVERDUE : FinancialProfile.PENDING;
            case INACTIVE -> FinancialProfile.OVERDUE;
        };
    }

    private String selectPlanByActivity(ActivityProfile activity, int index) {
        return switch (activity) {
            case VERY_ACTIVE, ACTIVE, MODERATE -> List.of("Basico", "Estudiante").get(index % 2);
            case LOW -> List.of("Basico", "Estudiante").get(index % 2);
            case INACTIVE -> List.of("Basico", "Estudiante").get(index % 2);
        };
    }

    private Long loadSingleId(String table, String column, String value) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM " + table + " WHERE " + column + " = ? ORDER BY id ASC LIMIT 1",
                Long.class,
                value);
    }

    private boolean existsPayment(String referencia) {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pagos WHERE referencia = ?", Integer.class, referencia);
        return total != null && total > 0;
    }

    private String pickFirstName(int index, Gender gender) {
        List<String> pool = gender == Gender.FEMALE ? FEMALE_FIRST_NAMES : MALE_FIRST_NAMES;
        return pool.get(Math.floorMod(index * 3, pool.size()));
    }

    private String[] pickSurnames(int index) {
        String surname1 = SURNAMES.get(Math.floorMod(index * 5, SURNAMES.size()));
        String surname2 = SURNAMES.get(Math.floorMod(index * 7 + 3, SURNAMES.size()));
        if (surname1.equalsIgnoreCase(surname2)) {
            surname2 = SURNAMES.get(Math.floorMod(index * 11 + 1, SURNAMES.size()));
        }
        return new String[] {surname1, surname2};
    }

    private String buildEmail(String firstName, String surname1, String surname2, int index) {
        return slug(firstName) + "." + slug(surname1) + "." + slug(surname2) + String.format(Locale.ROOT, "%02d", index) + "@flacofitness.es";
    }

    private String buildAddress(int index) {
        String street = STREETS.get(Math.floorMod(index * 4, STREETS.size()));
        String city = CITIES.get(Math.floorMod(index * 6, CITIES.size()));
        int number = 3 + Math.floorMod(index * 13, 97);
        return "Calle " + street + " " + number + ", " + city;
    }

    private LocalDate birthDateFor(int index, ActivityProfile activityProfile) {
        int age = switch (activityProfile) {
            case VERY_ACTIVE -> 22 + Math.floorMod(index, 10);
            case ACTIVE -> 25 + Math.floorMod(index, 12);
            case MODERATE -> 28 + Math.floorMod(index, 14);
            case LOW -> 31 + Math.floorMod(index, 15);
            case INACTIVE -> 38 + Math.floorMod(index, 17);
        };

        return LocalDate.now().minusYears(age).minusDays(Math.floorMod(index * 17, 300));
    }

    private LocalDateTime registrationDateFor(int index, boolean photoUser) {
        long daysBack = photoUser ? 70L + Math.floorMod(index * 17, 430) : 20L + Math.floorMod(index * 11, 240);
        return LocalDateTime.now().minusDays(daysBack)
                .withHour(9 + Math.floorMod(index, 6))
                .withMinute(Math.floorMod(index * 7, 4) * 15)
                .withSecond(0)
                .withNano(0);
    }

    private LocalDate nextPaymentDate(String planName, FinancialProfile financialProfile) {
        PlanSpec plan = PLAN_SPECS.stream().filter(spec -> spec.nombre().equals(planName)).findFirst().orElse(PLAN_SPECS.get(0));
        return switch (financialProfile) {
            case CLEAN -> LocalDate.now().plusDays(plan.duracionDias());
            case PENDING -> LocalDate.now().plusDays(7 + random.nextInt(10));
            case OVERDUE -> LocalDate.now().minusDays(5 + random.nextInt(25));
        };
    }

    private String generateDni(int seed) {
        int number = 10_000_000 + Math.floorMod(seed * 73_291, 89_000_000);
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        char control = letters.charAt(number % letters.length());
        return String.format(Locale.ROOT, "%08d%c", number, control);
    }

    private String generatePhone(int seed, boolean male) {
        int prefix = male ? 6 : 7;
        int suffix = Math.floorMod(seed * 97_531, 100_000_000);
        return String.format(Locale.ROOT, "%d%08d", prefix, suffix);
    }

    private String slug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "").replaceAll("(^-|-$)", "");
    }

    private String photoPath(int index) {
        return "uploads/users/portrait-" + String.format(Locale.ROOT, "%02d", index) + ".svg";
    }

    private String fileNameFromPath(String photoPath) {
        String normalized = photoPath.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private void deleteByIds(String sqlTemplate, Iterable<Long> ids) {
        List<Long> list = new ArrayList<>();
        ids.forEach(list::add);
        if (list.isEmpty()) {
            return;
        }

        String placeholders = list.stream().map(value -> "?").collect(Collectors.joining(","));
        jdbcTemplate.update(String.format(sqlTemplate, placeholders), list.toArray());
    }

    private boolean allowedPhotoRoutine(UserSpec user) {
        return user.activo() && ("Premium".equalsIgnoreCase(user.planName()) || "Plus".equalsIgnoreCase(user.planName()));
    }

    private int activityPriority(ActivityProfile profile) {
        return switch (profile) {
            case VERY_ACTIVE -> 80;
            case ACTIVE -> 70;
            case MODERATE -> 55;
            case LOW -> 30;
            case INACTIVE -> 10;
        };
    }

    private void loadRoutineAssignments(List<UserSpec> users, Map<String, String> personalized, Map<String, Long> userIds, Map<String, Long> routineIds) {
        // Helper kept intentionally small: assignments are handled in assignRoutines.
    }

    private enum ActivityProfile {
        VERY_ACTIVE,
        ACTIVE,
        MODERATE,
        LOW,
        INACTIVE
    }

    private enum FinancialProfile {
        CLEAN,
        PENDING,
        OVERDUE
    }

    private enum Gender {
        MALE,
        FEMALE
    }

    private record PlanSpec(String nombre, String descripcion, BigDecimal precioMensual, int duracionDias, boolean activo) {
    }

    private record RoutineSpec(String nombre, String descripcion, TipoRutina tipoRutina, boolean activa) {
        private LocalDateTime createdAt() {
            return LocalDateTime.now().minusDays(20L + Math.abs(nombre.hashCode() % 120));
        }
    }

    private record TrialSpec(
            String nombre,
            String apellidos,
            String telefono,
            String email,
            String origen,
            LocalDate fechaPrueba,
            EstadoTrial estado) {
    }

    private record ClassSpec(String nombre, String descripcion, int capacidadSugerida, boolean activa) {
    }

    private record SessionSpec(
            String className,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int aforo,
            EstadoSesion estado,
            Long staffId,
            Long rutinaId) {
    }

    private record UserSpec(
            String legacyEmail,
            String email,
            String nombre,
            String apellidos,
            String dni,
            String telefono,
            LocalDate fechaNacimiento,
            String direccion,
            LocalDateTime fechaRegistro,
            boolean activo,
            LocalDate fechaProximoPago,
            String roleName,
            String planName,
            String fotoPath,
            ActivityProfile activityProfile,
            FinancialProfile financialProfile,
            Gender gender,
            int seedIndex) {
    }
}
