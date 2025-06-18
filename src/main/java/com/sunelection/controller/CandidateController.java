package com.sunelection.controller;

import com.sunelection.model.Candidate;
import com.sunelection.service.CandidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur public permettant de récupérer la liste des candidats.
 * Accessible sans privilège administrateur afin que les votants puissent voir les candidats disponibles.
 */
@RestController
@RequestMapping("/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    /**
     * Retourne la liste de tous les candidats.
     * @return liste des candidats
     */
    @GetMapping
    public ResponseEntity<List<Candidate>> getAll() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }
}
