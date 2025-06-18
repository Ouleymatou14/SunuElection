package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.model.Vote;
import com.sunelection.model.Candidate;
import com.sunelection.model.AuditLog;
import com.sunelection.repository.UserRepository;
import com.sunelection.repository.VoteRepository;
import com.sunelection.repository.CandidateRepository;
import com.sunelection.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class BackupService {
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final CandidateRepository candidateRepository;
    private final AuditLogRepository auditLogRepository;
    private final String backupDir;
    private final int maxBackupFiles;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BackupService(
            UserRepository userRepository,
            VoteRepository voteRepository,
            CandidateRepository candidateRepository,
            AuditLogRepository auditLogRepository,
            @Value("${backup.dir}") String backupDir,
            @Value("${backup.max-files}") int maxBackupFiles) {
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.candidateRepository = candidateRepository;
        this.auditLogRepository = auditLogRepository;
        this.backupDir = backupDir;
        this.maxBackupFiles = maxBackupFiles;
        // Configure Jackson to handle Hibernate lazy proxies and avoid serialization errors
        this.objectMapper.registerModule(new Hibernate5Module());
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Transactional
    public String createBackup() throws IOException {
        // Create backup directory if it doesn't exist
        Files.createDirectories(Paths.get(backupDir));

        // Generate backup filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = backupDir + "/backup_" + timestamp + ".zip";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile))) {
            // Backup users
            List<User> users = userRepository.findAll();
            writeToZip(zos, "users.json", convertToJson(users));

            // Backup votes
            List<Vote> votes = voteRepository.findAll();
            writeToZip(zos, "votes.json", convertToJson(votes));

            // Backup candidates
            List<Candidate> candidates = candidateRepository.findAll();
            writeToZip(zos, "candidates.json", convertToJson(candidates));

            // Backup audit logs
            List<AuditLog> logs = auditLogRepository.findAll();
            writeToZip(zos, "audit_logs.json", convertToJson(logs));
        }

        // Clean up old backups
        cleanupOldBackups();

        return backupFile;
    }

    private void writeToZip(ZipOutputStream zos, String filename, String content) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        zos.putNextEntry(entry);
        zos.write(content.getBytes());
        zos.closeEntry();
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de sérialisation JSON", e);
        }
    }

    private void cleanupOldBackups() throws IOException {
        List<Path> backups = Files.list(Paths.get(backupDir))
                .filter(path -> path.toString().endsWith(".zip"))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        if (backups.size() > maxBackupFiles) {
            for (int i = maxBackupFiles; i < backups.size(); i++) {
                Files.delete(backups.get(i));
            }
        }
    }

    public List<String> listBackups() throws IOException {
        return Files.list(Paths.get(backupDir))
                .filter(path -> path.toString().endsWith(".zip"))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    public void deleteBackup(String filename) throws IOException {
        Path backupPath = Paths.get(backupDir, filename);
        if (Files.exists(backupPath)) {
            Files.delete(backupPath);
        } else {
            throw new FileNotFoundException("Backup file not found: " + filename);
        }
    }
} 