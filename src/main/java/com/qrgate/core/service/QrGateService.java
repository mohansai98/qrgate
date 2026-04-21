package com.qrgate.core.service;

import com.qrgate.core.model.QrSession;
import com.qrgate.core.model.User;
import com.qrgate.core.repository.QrSessionRepository;
import com.qrgate.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrGateService {

    private final QrSessionRepository qrSessionRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${qrgate.token.expiry-seconds:90}")
    private long tokenExpirySeconds;

    @Transactional
    public QrSession createSession(String browserSessionId) {
        QrSession session = new QrSession();
        session.setToken(UUID.randomUUID().toString());
        session.setBrowserSessionId(browserSessionId);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(tokenExpirySeconds));
        session.setStatus(QrSession.Status.PENDING);
        return qrSessionRepository.save(session);
    }

    public Optional<QrSession> getSession(String sessionId) {
        return qrSessionRepository.findById(sessionId);
    }

    public Optional<QrSession> getSessionByToken(String token) {
        return qrSessionRepository.findByToken(token);
    }

    @Transactional
    public boolean scanToken(String token) {
        return qrSessionRepository.findByToken(token)
                .map(session -> {
                    if (session.isExpired() || session.getStatus() != QrSession.Status.PENDING) {
                        return false;
                    }
                    session.setStatus(QrSession.Status.SCANNED);
                    qrSessionRepository.save(session);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public boolean confirmLogin(String token, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return qrSessionRepository.findByToken(token)
                .map(session -> {
                    if (session.isExpired() || session.getStatus() != QrSession.Status.SCANNED) {
                        return false;
                    }
                    session.setStatus(QrSession.Status.CONFIRMED);
                    session.setAuthenticatedUserId(user.getId());
                    session.setConfirmedAt(LocalDateTime.now());
                    qrSessionRepository.save(session);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public String exchangeSession(String sessionId) {
        QrSession session = qrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != QrSession.Status.CONFIRMED) {
            throw new RuntimeException("Session not confirmed");
        }

        if (session.isExpired()) {
            session.setStatus(QrSession.Status.EXPIRED);
            qrSessionRepository.save(session);
            throw new RuntimeException("Session expired");
        }

        User user = userRepository.findById(session.getAuthenticatedUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        session.setStatus(QrSession.Status.USED);
        qrSessionRepository.save(session);

        return jwtService.generateToken(user.getUsername(), user.getId().toString());
    }
}
