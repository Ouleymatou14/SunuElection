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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RestoreService {
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final CandidateRepository candidateRepository;
    private final AuditLogRepository auditLogRepository;
    private final String backupDir;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestoreService(
            UserRepository userRepository,
            VoteRepository voteRepository,
            CandidateRepository candidateRepository,
            AuditLogRepository auditLogRepository,
            @Value("${backup.dir}") String backupDir) {
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.candidateRepository = candidateRepository;
        this.auditLogRepository = auditLogRepository;
        this.backupDir = backupDir;
    }

    @Transactional
    public void restoreFromBackup(String filename) throws IOException {
        Path backupPath = Paths.get(backupDir, filename);
        if (!Files.exists(backupPath)) {
            throw new FileNotFoundException("Backup file not found: " + filename);
        }
        boolean entryRead = false;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryRead = true;
                String content = readZipEntry(zis);
                switch (entry.getName()) {
                    case "users.json":
                        restoreUsers(content);
                        break;
                    case "votes.json":
                        restoreVotes(content);
                        break;
                    case "candidates.json":
                        restoreCandidates(content);
                        break;
                    case "audit_logs.json":
                        restoreAuditLogs(content);
                        break;
                }
                zis.closeEntry();
            }
            if (!entryRead) {
                throw new IllegalArgumentException("Backup file is empty");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier ZIP : " + e.getMessage(), e);
        }
    }

    private String readZipEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toString("UTF-8");
    }

    private void restoreUsers(String jsonContent) {
        try {
            userRepository.deleteAll();
            var users = objectMapper.readValue(jsonContent, new TypeReference<java.util.List<User>>(){});
            userRepository.saveAll(users);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de restauration des utilisateurs", e);
        }
    }

    private void restoreVotes(String jsonContent) {
        try {
            voteRepository.deleteAll();
            var votes = objectMapper.readValue(jsonContent, new TypeReference<java.util.List<Vote>>(){});
            voteRepository.saveAll(votes);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de restauration des votes", e);
        }
    }

    private void restoreCandidates(String jsonContent) {
        try {
            candidateRepository.deleteAll();
            var candidates = objectMapper.readValue(jsonContent, new TypeReference<java.util.List<Candidate>>(){});
            candidateRepository.saveAll(candidates);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de restauration des candidats", e);
        }
    }

    private void restoreAuditLogs(String jsonContent) {
        try {
            auditLogRepository.deleteAll();
            var logs = objectMapper.readValue(jsonContent, new TypeReference<java.util.List<AuditLog>>(){});
            auditLogRepository.saveAll(logs);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de restauration des logs d'audit", e);
        }
    }
} 