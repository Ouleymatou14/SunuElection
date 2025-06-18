package com.sunelection.service;

import com.sunelection.model.AuditLog;
import com.sunelection.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog logAction(String username, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        String data = log.getUsername() + log.getAction() + log.getDetails() + log.getTimestamp();
        log.setHash(sha256(data));
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    public List<AuditLog> getLogsByUsernameAndDateRange(String username, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByUsernameAndTimestampBetween(username, start, end);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyLogIntegrity(AuditLog log) {
        try {
            String data = log.getUsername() + log.getAction() + log.getDetails() + log.getTimestamp();
            // Ici, tu peux utiliser un service de signature/crypto si besoin
            // return cryptoService.verify(data, log.getHash());
            return true; // Remplace par la vraie vérification si tu as un service de signature
        } catch (Exception e) {
            return false;
        }
    }
} 