package com.sunelection.repository;

import com.sunelection.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByUsernameAndTimestampBetween(String username, LocalDateTime start, LocalDateTime end);
} 