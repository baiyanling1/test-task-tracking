package com.testtracking.repository;

import com.testtracking.entity.AlertNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertNotificationConfigRepository extends JpaRepository<AlertNotificationConfig, Long> {
    Optional<AlertNotificationConfig> findByAlertType(String alertType);
}
