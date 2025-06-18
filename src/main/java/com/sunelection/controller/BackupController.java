package com.sunelection.controller;

import com.sunelection.service.BackupService;
import com.sunelection.service.RestoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Contrôleur REST pour la gestion des sauvegardes du système.
 * Fournit des endpoints pour créer, lister, télécharger et restaurer des sauvegardes.
 */
@RestController
@RequestMapping("/admin/backup")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sauvegarde", description = "API de gestion des sauvegardes")
public class BackupController {
    private final BackupService backupService;
    private final RestoreService restoreService;
    private static final String BACKUP_DIR = "backups";
    private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+\\.zip$");
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    public BackupController(BackupService backupService, RestoreService restoreService) {
        this.backupService = backupService;
        this.restoreService = restoreService;
    }

    private void validateFileName(String fileName) {
        if (fileName == null || !VALID_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid backup filename");
        }
    }

    /**
     * Crée une nouvelle sauvegarde du système.
     *
     * @return Le nom du fichier de sauvegarde créé
     * @throws IOException si une erreur survient lors de la création de la sauvegarde
     */
    @PostMapping("/create")
    @Operation(
        summary = "Créer une sauvegarde",
        description = "Crée une nouvelle sauvegarde complète du système, incluant les utilisateurs, votes et logs."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sauvegarde créée avec succès",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la création de la sauvegarde")
    })
    public ResponseEntity<String> createBackup() throws IOException {
        String backupFile = backupService.createBackup();
        return ResponseEntity.ok(backupFile);
    }

    /**
     * Liste toutes les sauvegardes disponibles.
     *
     * @return Liste des noms de fichiers de sauvegarde
     */
    @GetMapping("/list")
    @Operation(
        summary = "Lister les sauvegardes",
        description = "Récupère la liste de toutes les sauvegardes disponibles dans le système."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des sauvegardes récupérée avec succès",
            content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la récupération de la liste")
    })
    public ResponseEntity<?> listBackups() {
        try {
            return ResponseEntity.ok(backupService.listBackups());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération de la liste des sauvegardes : " + e.getMessage());
        }
    }

    /**
     * Télécharge une sauvegarde spécifique.
     *
     * @param fileName Nom du fichier de sauvegarde à télécharger
     * @return Le fichier de sauvegarde en tant que ressource
     * @throws IOException si le fichier n'est pas trouvé ou inaccessible
     */
    @GetMapping("/download/{fileName}")
    @Operation(
        summary = "Télécharger une sauvegarde",
        description = "Télécharge un fichier de sauvegarde spécifique."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sauvegarde téléchargée avec succès"),
        @ApiResponse(responseCode = "404", description = "Fichier de sauvegarde non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur lors du téléchargement")
    })
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName) throws IOException {
        validateFileName(fileName);
        Path filePath = Paths.get(BACKUP_DIR).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new IOException("Backup file not found: " + fileName);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    /**
     * Restaure le système à partir d'une sauvegarde.
     *
     * @param fileName Nom du fichier de sauvegarde à restaurer
     * @return Message de confirmation
     * @throws IOException si une erreur survient lors de la restauration
     */
    @PostMapping("/restore/{fileName}")
    @Operation(
        summary = "Restaurer une sauvegarde",
        description = "Restaure le système à partir d'un fichier de sauvegarde spécifique."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Système restauré avec succès"),
        @ApiResponse(responseCode = "404", description = "Fichier de sauvegarde non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la restauration")
    })
    public ResponseEntity<String> restoreBackup(@PathVariable String fileName) throws IOException {
        validateFileName(fileName);
        restoreService.restoreFromBackup(fileName);
        return ResponseEntity.ok("Système restauré avec succès");
    }

    /**
     * Supprime une sauvegarde spécifique.
     *
     * @param fileName Nom du fichier de sauvegarde à supprimer
     * @return Message de confirmation
     * @throws IOException si une erreur survient lors de la suppression
     */
    @DeleteMapping("/{fileName}")
    @Operation(
        summary = "Supprimer une sauvegarde",
        description = "Supprime un fichier de sauvegarde spécifique du système."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sauvegarde supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Fichier de sauvegarde non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la suppression")
    })
    public ResponseEntity<String> deleteBackup(
            @Parameter(description = "Nom du fichier de sauvegarde", required = true)
            @PathVariable String fileName) throws IOException {
        backupService.deleteBackup(fileName);
        return ResponseEntity.ok("Sauvegarde supprimée avec succès");
    }
} 