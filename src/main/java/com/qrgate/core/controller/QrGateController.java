package com.qrgate.core.controller;

import com.qrgate.core.model.QrSession;
import com.qrgate.core.repository.UserRepository;
import com.qrgate.core.service.QrCodeService;
import com.qrgate.core.service.QrGateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth/qr")
@Slf4j
@RequiredArgsConstructor
public class QrGateController {

    private final QrGateService qrGateService;
    private final QrCodeService qrCodeService;

    @Value("${qrgate.base-url:http://localhost:8080}")
    private String baseUrl;

    @GetMapping("/init")
    @ResponseBody
    public ResponseEntity<?> initSession(HttpSession session) {
        try {
            QrSession qrSession = qrGateService.createSession(session.getId());
            // Create a URL that the mobile phone can actually visit
            String mobileUrl = baseUrl + "/auth/qr/mobile?token=" + qrSession.getToken();
            log.info("Generated Mobile URL: {}", mobileUrl);
            String qrCodeDataUri = qrCodeService.generateQrCodeDataUri(mobileUrl, 300, 300);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", qrSession.getId());
            response.put("qrCode", qrCodeDataUri);
            response.put("url", mobileUrl);
            response.put("expiresAt", qrSession.getExpiresAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to init QR session", e);
            return ResponseEntity.internalServerError().body("Failed to initialize QR session");
        }
    }

    @GetMapping("/mobile")
    public String mobileConfirm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        // Automatically trigger the 'scan' status when the page is opened
        qrGateService.scanToken(token);
        return "mobile-confirm";
    }

    @GetMapping("/status/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> getStatus(@PathVariable String sessionId) {
        return qrGateService.getSession(sessionId)
                .map(session -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", session.getStatus());
                    if (session.isExpired() && session.getStatus() == QrSession.Status.PENDING) {
                        response.put("status", QrSession.Status.EXPIRED);
                    }
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/scan/{token}")
    @ResponseBody
    public ResponseEntity<?> scanQr(@PathVariable String token) {
        boolean success = qrGateService.scanToken(token);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Invalid or expired token");
    }

    @PostMapping("/confirm/{token}")
    @ResponseBody
    public ResponseEntity<?> confirmQr(@PathVariable String token, java.security.Principal principal) {
        String username = principal.getName();
        log.info("Received confirmation request for token: {} from authenticated user: {}", token, username);
        boolean success = qrGateService.confirmLogin(token, username);
        log.info("Confirmation success: {}", success);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Failed to confirm login");
    }

    private final UserRepository userRepository;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @PostMapping("/exchange/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> exchangeSession(@PathVariable String sessionId, jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 1. Get the authenticated user from the QR session
            QrSession qrSession = qrGateService.getSession(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            if (qrSession.getStatus() != QrSession.Status.CONFIRMED) {
                return ResponseEntity.badRequest().body("Session not confirmed");
            }

            com.qrgate.core.model.User user = userRepository.findById(qrSession.getAuthenticatedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Load UserDetails and create Authentication
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                    userDetailsService.loadUserByUsername(user.getUsername());
            
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            // 3. Set the Security Context for the current browser session
            org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            
            // Explicitly save the context to the session for Spring Security 6+
            new org.springframework.security.web.context.HttpSessionSecurityContextRepository()
                    .saveContext(context, request, null);
            
            // 4. Update session status to USED
            String jwt = qrGateService.exchangeSession(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("redirect", "/dashboard");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to exchange session", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
