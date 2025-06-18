package com.sunelection.controller;

import com.sunelection.model.Vote;
import com.sunelection.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour la gestion des votes.
 * Fournit des endpoints pour soumettre des votes et consulter les résultats.
 */
@RestController
@RequestMapping("/vote")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vote", description = "API de gestion des votes")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /**
     * Soumet un vote pour un candidat.
     *
     * @param voteRequest Requête contenant l'ID du candidat
     * @return Message de confirmation
     */
    @PostMapping
    @Operation(
        summary = "Soumettre un vote",
        description = "Permet à un utilisateur authentifié de soumettre un vote pour un candidat."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vote soumis avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Utilisateur a déjà voté")
    })
    public ResponseEntity<String> submitVote(@RequestBody VoteRequest voteRequest, Authentication authentication) {
        String username = authentication.getName();
        try {
            voteService.castVote(voteRequest.getCandidateId(), voteRequest.getEncryptedVote(), username);
            return ResponseEntity.ok("Vote soumis avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Récupère le nombre total de votes.
     *
     * @return Nombre total de votes
     */
    @GetMapping("/total")
    @Operation(
        summary = "Obtenir le total des votes",
        description = "Récupère le nombre total de votes soumis dans le système."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total des votes récupéré avec succès",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Map<String, Long>> getTotalVotes() {
        return ResponseEntity.ok(Map.of("totalVotes", voteService.getTotalVotes()));
    }

    /**
     * Récupère les résultats détaillés des votes.
     *
     * @return Résultats détaillés par candidat
     */
    @GetMapping("/results")
    @Operation(
        summary = "Obtenir les résultats détaillés",
        description = "Récupère les résultats détaillés des votes par candidat."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résultats récupérés avec succès",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Map<String, Object>> getDetailedResults() {
        return ResponseEntity.ok(voteService.getDetailedResults());
    }

    /**
     * Vérifie si l'utilisateur actuel a déjà voté.
     *
     * @return État du vote de l'utilisateur
     */
    @GetMapping("/status")
    @Operation(
        summary = "Vérifier l'état du vote",
        description = "Vérifie si l'utilisateur actuel a déjà soumis un vote."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "État du vote récupéré avec succès",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Map<String, Boolean>> checkVoteStatus(Authentication authentication) {
        String username = authentication.getName();
        boolean hasVoted = false;
        try {
            hasVoted = voteService.hasUserVoted(username);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("hasVoted", hasVoted));
    }

    /**
     * Classe interne pour la requête de vote.
     */
    @Schema(description = "Requête de vote")
    public static class VoteRequest {
        @Schema(description = "ID du candidat", required = true)
        private Long candidateId;
        private String encryptedVote;

        public Long getCandidateId() {
            return candidateId;
        }

        public void setCandidateId(Long candidateId) {
            this.candidateId = candidateId;
        }

        public String getEncryptedVote() {
            return encryptedVote;
        }

        public void setEncryptedVote(String encryptedVote) {
            this.encryptedVote = encryptedVote;
        }
    }
} 