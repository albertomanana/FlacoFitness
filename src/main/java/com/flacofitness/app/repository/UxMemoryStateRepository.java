package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.UxMemoryState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UxMemoryStateRepository extends JpaRepository<UxMemoryState, Long> {

    Optional<UxMemoryState> findByBrowserTokenAndAccessProfileAndModuleKey(String browserToken,
                                                                           String accessProfile,
                                                                           String moduleKey);

    List<UxMemoryState> findByBrowserTokenAndAccessProfile(String browserToken, String accessProfile);
}
