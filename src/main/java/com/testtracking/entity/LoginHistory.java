package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_history")
@EqualsAndHashCode(callSuper = false)
public class LoginHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginStatus status = LoginStatus.SUCCESS;

    public enum LoginStatus {
        SUCCESS,
        FAILED,
        LOCKED
    }
}
