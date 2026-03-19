package com.qrgate.core.repository;

import com.qrgate.core.model.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrSessionRepository extends JpaRepository<QrSession, String> {

    Optional<QrSession> findByToken(String token);
}
