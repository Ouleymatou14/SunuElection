package com.sunelection.controller;

import com.sunelection.model.User;
import com.sunelection.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Contrôleur REST pour l'authentification et la gestion des utilisateurs.
 * Fournit des endpoints pour l'inscription, la connexion et la gestion des comptes.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "API d'authentification et de gestion des utilisateurs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param user Données de l'utilisateur à inscrire
     * @return Message de confirmation
     */
    @PostMapping("/register")
    @Operation(
        summary = "Inscrire un nouvel utilisateur",
        description = "Crée un nouveau compte utilisateur avec les informations fournies."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inscription réussie"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    public ResponseEntity<Map<String, String>> register(
            @Parameter(description = "Données de l'utilisateur", required = true)
            @Valid @RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok(Map.of("message", "Inscription réussie"));
    }

    /**
     * Authentifie un utilisateur.
     *
     * @param loginRequest Requête de connexion
     * @return Token JWT et informations de l'utilisateur
     */
    @PostMapping("/login")
    @Operation(
        summary = "Se connecter",
        description = "Authentifie un utilisateur et retourne un token JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connexion réussie",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @ApiResponse(responseCode = "403", description = "Compte désactivé")
    })
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Identifiants de connexion", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    /**
     * Vérifie si un email est déjà utilisé.
     *
     * @param email Email à vérifier
     * @return État de disponibilité de l'email
     */
    @GetMapping("/check-email")
    @Operation(
        summary = "Vérifier la disponibilité d'un email",
        description = "Vérifie si un email est déjà utilisé dans le système."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vérification réussie",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @Parameter(description = "Email à vérifier", required = true)
            @RequestParam String email) {
        return ResponseEntity.ok(Map.of("available", !authService.isEmailTaken(email)));
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur.
     *
     * @param resetRequest Requête de réinitialisation
     * @return Message de confirmation
     */
    @PostMapping("/reset-password")
    @Operation(
        summary = "Réinitialiser le mot de passe",
        description = "Envoie un email de réinitialisation de mot de passe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email de réinitialisation envoyé"),
        @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    public ResponseEntity<String> resetPassword(
            @Parameter(description = "Requête de réinitialisation", required = true)
            @Valid @RequestBody ResetPasswordRequest resetRequest) {
        authService.resetPassword(resetRequest.getEmail());
        return ResponseEntity.ok("Email de réinitialisation envoyé");
    }

    /**
     * Change le mot de passe d'un utilisateur.
     *
     * @param changeRequest Requête de changement de mot de passe
     * @return Message de confirmation
     */
    @PostMapping("/change-password")
    @Operation(
        summary = "Changer le mot de passe",
        description = "Change le mot de passe d'un utilisateur authentifié."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mot de passe changé avec succès"),
        @ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<String> changePassword(
            @Parameter(description = "Requête de changement de mot de passe", required = true)
            @Valid @RequestBody ChangePasswordRequest changeRequest) {
        authService.changePassword(changeRequest.getOldPassword(), changeRequest.getNewPassword());
        return ResponseEntity.ok("Mot de passe changé avec succès");
    }

    /**
     * Classe interne pour la requête de connexion.
     */
    @Schema(description = "Requête de connexion")
    public static class LoginRequest {
        @Schema(description = "Email de l'utilisateur", required = true)
        private String email;

        @Schema(description = "Mot de passe", required = true)
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Classe interne pour la requête de réinitialisation de mot de passe.
     */
    @Schema(description = "Requête de réinitialisation de mot de passe")
    public static class ResetPasswordRequest {
        @Schema(description = "Email de l'utilisateur", required = true)
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * Classe interne pour la requête de changement de mot de passe.
     */
    @Schema(description = "Requête de changement de mot de passe")
    public static class ChangePasswordRequest {
        @Schema(description = "Ancien mot de passe", required = true)
        private String oldPassword;

        @Schema(description = "Nouveau mot de passe", required = true)
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
} 