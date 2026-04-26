package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop12ByOrderByOccurredAtDescIdDesc();

    List<ActivityLog> findTop12ByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(String entityType, Long entityId);
}
