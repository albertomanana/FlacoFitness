package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.RecentVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentVisitRepository extends JpaRepository<RecentVisit, Long> {

    Optional<RecentVisit> findByBrowserTokenAndAccessProfileAndEntityTypeAndEntityId(String browserToken,
                                                                                      String accessProfile,
                                                                                      String entityType,
                                                                                      Long entityId);

    List<RecentVisit> findTop10ByBrowserTokenAndAccessProfileOrderByVisitedAtDescIdDesc(String browserToken,
                                                                                         String accessProfile);

    List<RecentVisit> findByBrowserTokenAndAccessProfileOrderByVisitedAtDescIdDesc(String browserToken,
                                                                                   String accessProfile);
}
