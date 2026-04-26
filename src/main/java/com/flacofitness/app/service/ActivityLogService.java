package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.ActivityLogItemView;
import com.flacofitness.app.model.entity.ActivityLog;
import com.flacofitness.app.repository.ActivityLogRepository;
import com.flacofitness.app.security.AccessProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ActivityLogService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("es", "ES"));

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLogItemView> recentActivity() {
        List<ActivityLog> logs = activityLogRepository.findTop12ByOrderByOccurredAtDescIdDesc();
        if (logs == null) {
            return List.of();
        }
        return logs.stream()
                .map(this::toView)
                .toList();
    }

    public List<ActivityLogItemView> recentByEntity(String entityType, Long entityId) {
        List<ActivityLog> logs = activityLogRepository.findTop12ByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(entityType, entityId);
        if (logs == null) {
            return List.of();
        }
        return logs.stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public void log(String moduleKey,
                    String actionKey,
                    String entityType,
                    Long entityId,
                    String title,
                    String description,
                    AccessProfile actorProfile,
                    String browserToken,
                    String route) {
        ActivityLog log = new ActivityLog();
        log.setModuleKey(moduleKey);
        log.setActionKey(actionKey);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setTitle(title);
        log.setDescription(description);
        log.setActorProfile(actorProfile != null ? actorProfile.name() : AccessProfile.ADMIN.name());
        log.setBrowserToken(browserToken);
        log.setRoute(route);
        activityLogRepository.save(log);
    }

    private ActivityLogItemView toView(ActivityLog log) {
        String occurredAtLabel = log.getOccurredAt() != null ? log.getOccurredAt().format(FORMATTER) : "-";
        String actorProfileLabel = log.getActorProfile() == null
                ? "Sistema"
                : AccessProfile.from(log.getActorProfile()).getLabel();
        return new ActivityLogItemView(
                log.getTitle(),
                log.getDescription(),
                log.getModuleKey(),
                log.getActionKey(),
                log.getRoute(),
                occurredAtLabel,
                actorProfileLabel);
    }
}
