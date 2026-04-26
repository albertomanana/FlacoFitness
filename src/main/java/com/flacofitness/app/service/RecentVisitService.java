package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.RecentVisitView;
import com.flacofitness.app.model.entity.RecentVisit;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.RecentVisitRepository;
import com.flacofitness.app.security.AccessProfile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class RecentVisitService {

    private static final Pattern DETAIL_PATTERN = Pattern.compile("^/(usuarios|staff|sesiones|pagos|gastos|maquinas|materiales|membresias|nominas|trials|clases|rutinas)/(\\d+)$");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM HH:mm", new Locale("es", "ES"));

    private final RecentVisitRepository recentVisitRepository;
    private final UsuarioService usuarioService;
    private final StaffService staffService;
    private final SesionClaseService sesionClaseService;
    private final PagoService pagoService;
    private final GastoService gastoService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final MembresiaService membresiaService;
    private final NominaService nominaService;
    private final TrialService trialService;
    private final ClaseService claseService;
    private final RutinaService rutinaService;

    public RecentVisitService(RecentVisitRepository recentVisitRepository,
                              UsuarioService usuarioService,
                              StaffService staffService,
                              SesionClaseService sesionClaseService,
                              PagoService pagoService,
                              GastoService gastoService,
                              MaquinaService maquinaService,
                              MaterialService materialService,
                              MembresiaService membresiaService,
                              NominaService nominaService,
                              TrialService trialService,
                              ClaseService claseService,
                              RutinaService rutinaService) {
        this.recentVisitRepository = recentVisitRepository;
        this.usuarioService = usuarioService;
        this.staffService = staffService;
        this.sesionClaseService = sesionClaseService;
        this.pagoService = pagoService;
        this.gastoService = gastoService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.membresiaService = membresiaService;
        this.nominaService = nominaService;
        this.trialService = trialService;
        this.claseService = claseService;
        this.rutinaService = rutinaService;
    }

    public List<RecentVisitView> listFor(String browserToken, AccessProfile profile) {
        List<RecentVisit> visits = recentVisitRepository.findTop10ByBrowserTokenAndAccessProfileOrderByVisitedAtDescIdDesc(browserToken, profile.name());
        if (visits == null) {
            return List.of();
        }
        return visits.stream()
                .map(visit -> new RecentVisitView(
                        visit.getTitle(),
                        visit.getUrl(),
                        visit.getIconKey(),
                        visit.getEntityType(),
                        visit.getVisitedAt() != null ? visit.getVisitedAt().format(FORMATTER) : "-"))
                .toList();
    }

    @Transactional
    public void registerIfTrackable(HttpServletRequest request, AccessProfile profile, String browserToken) {
        if (request == null || profile == null || browserToken == null || browserToken.isBlank()) {
            return;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String uri = request.getRequestURI();
        Matcher matcher = DETAIL_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            return;
        }

        String entityType = matcher.group(1);
        Long entityId = Long.valueOf(matcher.group(2));
        VisitDescriptor descriptor = resolveDescriptor(entityType, entityId);
        if (descriptor == null) {
            return;
        }

        var existing = recentVisitRepository
                .findByBrowserTokenAndAccessProfileAndEntityTypeAndEntityId(browserToken, profile.name(), entityType, entityId);
        RecentVisit visit = existing != null && existing.isPresent() ? existing.get() : new RecentVisit();
        visit.setBrowserToken(browserToken);
        visit.setAccessProfile(profile.name());
        visit.setEntityType(entityType);
        visit.setEntityId(entityId);
        visit.setTitle(descriptor.title());
        visit.setUrl(uri);
        visit.setIconKey(descriptor.iconKey());
        recentVisitRepository.save(visit);

        trimToTopTen(browserToken, profile);
    }

    private void trimToTopTen(String browserToken, AccessProfile profile) {
        List<RecentVisit> visits = recentVisitRepository.findByBrowserTokenAndAccessProfileOrderByVisitedAtDescIdDesc(browserToken, profile.name());
        if (visits == null || visits.size() <= 10) {
            return;
        }
        recentVisitRepository.deleteAll(visits.subList(10, visits.size()));
    }

    private VisitDescriptor resolveDescriptor(String entityType, Long entityId) {
        return switch (entityType) {
            case "usuarios" -> {
                Usuario usuario = usuarioService.buscarPorId(entityId);
                yield new VisitDescriptor(buildUserName(usuario), "user");
            }
            case "staff" -> new VisitDescriptor(
                    buildUserName(staffService.buscarPorId(entityId).getUsuario()),
                    "staff");
            case "sesiones" -> new VisitDescriptor(
                    sesionClaseService.buscarPorId(entityId).getClase() != null
                            ? sesionClaseService.buscarPorId(entityId).getClase().getNombre()
                            : "Sesion",
                    "session");
            case "pagos" -> new VisitDescriptor("Pago " + entityId, "payment");
            case "gastos" -> new VisitDescriptor(gastoService.buscarPorId(entityId).getConcepto(), "expense");
            case "maquinas" -> new VisitDescriptor(maquinaService.buscarPorId(entityId).getNombre(), "machine");
            case "materiales" -> new VisitDescriptor(materialService.buscarPorId(entityId).getNombre(), "material");
            case "membresias" -> new VisitDescriptor(membresiaService.buscarPlan(entityId).getNombre(), "membership");
            case "nominas" -> new VisitDescriptor("Nomina " + nominaService.buscarPorId(entityId).getPeriodo(), "payroll");
            case "trials" -> new VisitDescriptor(trialService.buscarPorId(entityId).getNombre(), "trial");
            case "clases" -> new VisitDescriptor(claseService.buscarPorId(entityId).getNombre(), "class");
            case "rutinas" -> new VisitDescriptor(rutinaService.buscarPorId(entityId).getNombre(), "routine");
            default -> null;
        };
    }

    private String buildUserName(Usuario usuario) {
        if (usuario == null) {
            return "Usuario";
        }
        return usuario.getNombre()
                + (usuario.getApellidos() != null && !usuario.getApellidos().isBlank() ? " " + usuario.getApellidos() : "");
    }

    private record VisitDescriptor(String title, String iconKey) {
    }
}
