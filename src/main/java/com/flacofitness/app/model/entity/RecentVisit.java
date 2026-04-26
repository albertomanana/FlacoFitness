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
@Table(name = "recent_visit", indexes = {
        @Index(name = "idx_recent_visit_browser_profile", columnList = "browser_token, access_profile"),
        @Index(name = "idx_recent_visit_visited_at", columnList = "visited_at")
})
@Getter
@Setter
@NoArgsConstructor
public class RecentVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "browser_token", nullable = false, length = 80)
    private String browserToken;

    @Column(name = "access_profile", nullable = false, length = 40)
    private String accessProfile;

    @Column(name = "entity_type", nullable = false, length = 40)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "icon_key", length = 40)
    private String iconKey;

    @CreationTimestamp
    @Column(name = "visited_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime visitedAt;
}
