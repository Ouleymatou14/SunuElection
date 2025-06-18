package com.sunelection.service;

import com.sunelection.model.*;
import com.sunelection.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestoreServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ValidationService validationService;

    private RestoreService restoreService;

    private String testBackupDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        testBackupDir = "test_backups";
        restoreService = new RestoreService(userRepository, voteRepository, candidateRepository, auditLogRepository, testBackupDir);
        Files.createDirectories(Path.of(testBackupDir));
    }

    @Test
    void restoreFromBackup_Success() throws IOException {
        // Créer un fichier de sauvegarde temporaire
        Path backupPath = Paths.get(testBackupDir, "test_backup.zip");
        Files.createDirectories(backupPath.getParent());
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(backupPath))) {
            ZipEntry entry = new ZipEntry("users.json");
            zos.putNextEntry(entry);
            zos.write("[]".getBytes());
            zos.closeEntry();
        }
        // Act
        restoreService.restoreFromBackup("test_backup.zip");
        // Assert
        verify(userRepository, atLeastOnce()).saveAll(any());
        // Nettoyer
        Files.deleteIfExists(backupPath);
    }

    @Test
    void restoreFromBackup_FileNotFound() {
        // Tester la restauration avec un fichier inexistant
        try {
            restoreService.restoreFromBackup("nonexistent.zip");
        } catch (IOException e) {
            // Vérifier que l'exception est levée
            assert e.getMessage().contains("Backup file not found");
        }
    }

    @Test
    void restoreFromBackup_ValidationError() throws IOException {
        // Simuler une erreur de validation
        doThrow(new IllegalArgumentException("Invalid user data"))
            .when(validationService).validateUser(any(User.class));
        // Créer un fichier de sauvegarde temporaire
        Path backupPath = Paths.get(testBackupDir, "test_backup.zip");
        Files.createDirectories(backupPath.getParent());
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(backupPath))) {
            ZipEntry entry = new ZipEntry("users.json");
            zos.putNextEntry(entry);
            zos.write("[]".getBytes());
            zos.closeEntry();
        }
        try {
            restoreService.restoreFromBackup("test_backup.zip");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Invalid user data");
        }
        // Nettoyer
        Files.deleteIfExists(backupPath);
    }

    @Test
    void restoreFromBackup_CorruptedFile() throws IOException {
        // Créer un fichier ZIP corrompu
        Path backupPath = Paths.get(testBackupDir, "corrupted_backup.zip");
        Files.createDirectories(backupPath.getParent());
        Files.write(backupPath, "This is not a valid ZIP file".getBytes());
        // Assert
        assertThrows(RuntimeException.class, () -> restoreService.restoreFromBackup("corrupted_backup.zip"));
        // Nettoyer
        Files.deleteIfExists(backupPath);
    }

    @Test
    void restoreFromBackup_DataConflict() throws IOException {
        // Simuler un conflit de données
        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(new User())); // Utilisateur existant
        // Créer un fichier de sauvegarde
        Path backupPath = Paths.get(testBackupDir, "conflict_backup.zip");
        Files.createDirectories(backupPath.getParent());
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(backupPath))) {
            ZipEntry entry = new ZipEntry("users.json");
            zos.putNextEntry(entry);
            zos.write("[]".getBytes());
            zos.closeEntry();
        }
        try {
            restoreService.restoreFromBackup("conflict_backup.zip");
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("Data conflict detected");
        }
        // Nettoyer
        Files.deleteIfExists(backupPath);
    }

    @Test
    void restoreFromBackup_InvalidDataFormat() throws IOException {
        // Créer un fichier avec un format de données invalide
        Path backupPath = Paths.get(testBackupDir, "invalid_format_backup.zip");
        Files.createDirectories(backupPath.getParent());
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupPath))) {
            ZipEntry entry = new ZipEntry("users.json");
            zos.putNextEntry(entry);
            zos.write("invalid,csv,format\n".getBytes());
            zos.closeEntry();
        }
        // Assert
        assertThrows(RuntimeException.class, () -> restoreService.restoreFromBackup("invalid_format_backup.zip"));
        // Nettoyer
        Files.deleteIfExists(backupPath);
    }

    @Test
    void restoreFromBackup_EmptyBackup() throws IOException {
        // Créer un fichier ZIP vide
        Path backupPath = Paths.get(testBackupDir, "empty_backup.zip");
        Files.createDirectories(backupPath.getParent());
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupPath))) {
            // Aucune entrée ajoutée
        }
        try {
            restoreService.restoreFromBackup("empty_backup.zip");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Backup file is empty");
        }
        // Nettoyer
        Files.deleteIfExists(backupPath);
    }
} 