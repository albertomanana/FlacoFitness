package com.flacofitness.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ux_memory_state", indexes = {
        @Index(name = "idx_ux_memory_browser_profile", columnList = "browser_token, access_profile"),
        @Index(name = "idx_ux_memory_module", columnList = "module_key")
})
@Getter
@Setter
@NoArgsConstructor
public class UxMemoryState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "browser_token", nullable = false, length = 80)
    private String browserToken;

    @Column(name = "access_profile", nullable = false, length = 40)
    private String accessProfile;

    @Column(name = "module_key", nullable = false, length = 80)
    private String moduleKey;

    @Column(name = "tooltip_seen", nullable = false)
    private Boolean tooltipSeen;

    @Column(name = "empty_state_dismissed", nullable = false)
    private Boolean emptyStateDismissed;

    @Column(name = "guide_step_state", length = 40)
    private String guideStepState;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (tooltipSeen == null) {
            tooltipSeen = false;
        }
        if (emptyStateDismissed == null) {
            emptyStateDismissed = false;
        }
    }
}
