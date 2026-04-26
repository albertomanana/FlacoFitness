package com.flacofitness.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log", indexes = {
        @Index(name = "idx_activity_module", columnList = "module_key, occurred_at"),
        @Index(name = "idx_activity_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_activity_browser_profile", columnList = "browser_token, actor_profile")
})
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, length = 60)
    private String moduleKey;

    @Column(name = "action_key", nullable = false, length = 80)
    private String actionKey;

    @Column(name = "entity_type", nullable = false, length = 40)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "actor_profile", nullable = false, length = 40)
    private String actorProfile;

    @Column(name = "browser_token", length = 80)
    private String browserToken;

    @Column(length = 255)
    private String route;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime occurredAt;
}
