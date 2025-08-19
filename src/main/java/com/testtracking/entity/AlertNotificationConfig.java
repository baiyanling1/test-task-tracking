package com.testtracking.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "alert_notification_config")
public class AlertNotificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_type", length = 100, nullable = false, unique = true)
    private String alertType;

    @Column(name = "alert_name", length = 200, nullable = false)
    private String alertName;

    @Column(name = "dingtalk_enabled")
    private Boolean dingtalkEnabled = false;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
