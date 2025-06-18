package com.sunelection.service;

import com.sunelection.model.Candidate;
import com.sunelection.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public Candidate createCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidate not found"));
    }

    @Transactional
    public Candidate updateCandidate(Long id, Candidate candidateDetails) {
        Candidate candidate = getCandidateById(id);
        candidate.setName(candidateDetails.getName());
        candidate.setParty(candidateDetails.getParty());
        candidate.setDescription(candidateDetails.getDescription());
        return candidateRepository.save(candidate);
    }

    @Transactional
    public void deleteCandidate(Long id) {
        Candidate candidate = getCandidateById(id);
        candidateRepository.delete(candidate);
    }
} 