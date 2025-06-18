package com.sunelection.service;

import com.sunelection.model.Candidate;
import com.sunelection.model.User;
import com.sunelection.repository.CandidateRepository;
import com.sunelection.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AdminService(CandidateRepository candidateRepository,
                       UserRepository userRepository,
                       AuditService auditService) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    // Gestion des candidats
    @Transactional
    public Candidate createCandidate(Candidate candidate) {
        Candidate saved = candidateRepository.save(candidate);
        auditService.logAction("ADMIN", "CANDIDATE_CREATE", "Created candidate: " + candidate.getName());
        return saved;
    }

    @Transactional
    public Candidate updateCandidate(Long id, Candidate candidate) {
        Candidate existing = candidateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidate not found"));
        
        existing.setName(candidate.getName());
        existing.setDescription(candidate.getDescription());
        
        Candidate updated = candidateRepository.save(existing);
        auditService.logAction("ADMIN", "CANDIDATE_UPDATE", "Updated candidate: " + candidate.getName());
        return updated;
    }

    @Transactional
    public void deleteCandidate(Long id) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidate not found"));
        
        candidateRepository.delete(candidate);
        auditService.logAction("ADMIN", "CANDIDATE_DELETE", "Deleted candidate: " + candidate.getName());
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    // Gestion des utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User enableUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(true);
        User updated = userRepository.save(user);
        auditService.logAction("ADMIN", "USER_ENABLE", "Enabled user: " + user.getEmail());
        return updated;
    }

    @Transactional
    public User disableUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(false);
        User updated = userRepository.save(user);
        auditService.logAction("ADMIN", "USER_DISABLE", "Disabled user: " + user.getEmail());
        return updated;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        auditService.logAction("ADMIN", "USER_DELETE", "Deleted user: " + user.getEmail());
    }

    @Transactional
    public User updateUserRoles(Long id, Set<String> roles) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRoles(roles);
        User updated = userRepository.save(user);
        auditService.logAction("ADMIN", "USER_ROLES_UPDATE", "Updated roles for user: " + user.getEmail());
        return updated;
    }
} 