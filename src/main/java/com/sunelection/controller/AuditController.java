package com.sunelection.controller;

import com.sunelection.model.AuditLog;
import com.sunelection.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "*")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<AuditLog> logs;
            if (username != null) {
                logs = auditService.getLogsByUsername(username);
            } else if (start != null && end != null) {
                logs = auditService.getLogsByDateRange(start, end);
            } else {
                logs = auditService.getAllLogs();
            }
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyLogIntegrity(@RequestBody AuditLog log) {
        try {
            boolean isValid = auditService.verifyLogIntegrity(log);
            return ResponseEntity.ok(new VerificationResponse(isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    static class VerificationResponse {
        private boolean valid;

        public VerificationResponse(boolean valid) {
            this.valid = valid;
        }
    }
} 