package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.model.Vote;
import com.sunelection.model.Candidate;
import com.sunelection.model.AuditLog;
import com.sunelection.repository.UserRepository;
import com.sunelection.repository.VoteRepository;
import com.sunelection.repository.CandidateRepository;
import com.sunelection.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private BackupService backupService;
    private String testBackupDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        testBackupDir = "test_backups";
        Files.createDirectories(Path.of(testBackupDir));
        Files.walk(Path.of(testBackupDir))
            .filter(p -> !p.equals(Path.of(testBackupDir)))
            .map(Path::toFile)
            .forEach(File::delete);
        backupService = new BackupService(userRepository, voteRepository, candidateRepository, auditLogRepository, testBackupDir, 10);
    }

    @Test
    void createBackup_Success() throws IOException {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        Vote vote = new Vote();
        vote.setId(1L);
        Candidate candidate = new Candidate();
        candidate.setName("Test Candidate");
        AuditLog log = new AuditLog();
        log.setAction("TEST_ACTION");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        when(voteRepository.findAll()).thenReturn(Arrays.asList(vote));
        when(candidateRepository.findAll()).thenReturn(Arrays.asList(candidate));
        when(auditLogRepository.findAll()).thenReturn(Arrays.asList(log));

        // Act
        String backupFile = backupService.createBackup();

        // Assert
        assertNotNull(backupFile);
        assertTrue(new File(backupFile).exists());
        verify(userRepository).findAll();
        verify(voteRepository).findAll();
        verify(candidateRepository).findAll();
        verify(auditLogRepository).findAll();
    }

    @Test
    void listBackups_Success() throws IOException {
        // Arrange
        String backupFile = testBackupDir + "/backup_test_list.zip";
        Files.createFile(Path.of(backupFile));

        // Act
        List<String> backups = backupService.listBackups();

        // Assert
        assertNotNull(backups);
        assertTrue(backups.contains("backup_test_list.zip"));
    }

    @Test
    void deleteBackup_Success() throws IOException {
        // Arrange
        String backupFile = testBackupDir + "/backup_20230101_120000.zip";
        Files.createFile(Path.of(backupFile));

        // Act
        backupService.deleteBackup("backup_20230101_120000.zip");

        // Assert
        assertFalse(Files.exists(Path.of(backupFile)));
    }

    @Test
    void deleteBackup_FileNotFound() {
        // Act & Assert
        assertThrows(IOException.class, () -> backupService.deleteBackup("nonexistent.zip"));
    }

    @Test
    void cleanupOldBackups_Success() throws IOException {
        // Arrange
        String backup1 = testBackupDir + "/backup1.zip";
        String backup2 = testBackupDir + "/backup2.zip";
        String backup3 = testBackupDir + "/backup3.zip";
        Files.createFile(Path.of(backup1));
        Files.createFile(Path.of(backup2));
        Files.createFile(Path.of(backup3));

        ReflectionTestUtils.setField(backupService, "maxBackupFiles", 2);

        // Act
        backupService.createBackup();

        // Assert
        long zipCount = Files.list(Path.of(testBackupDir)).filter(p -> p.toString().endsWith(".zip")).count();
        assertEquals(2, zipCount);
    }
} 