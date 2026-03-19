package com.qrgate.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_sessions")
@Data
@NoArgsConstructor
public class QrSession {

    public enum Status {
        PENDING, SCANNED, CONFIRMED, EXPIRED, USED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private String browserSessionId;

    private Long authenticatedUserId;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
