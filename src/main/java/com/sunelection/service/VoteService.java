package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.model.Vote;
import com.sunelection.model.Candidate;
import com.sunelection.repository.UserRepository;
import com.sunelection.repository.VoteRepository;
import com.sunelection.repository.CandidateRepository;
import com.sunelection.security.JwtCryptoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.sunelection.service.EmailService;
import java.util.stream.Collectors;

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final JwtCryptoService cryptoService;
    private final AuditService auditService;
    private final EmailService emailService;

    public VoteService(VoteRepository voteRepository,
                      UserRepository userRepository,
                      CandidateRepository candidateRepository,
                      JwtCryptoService cryptoService,
                      AuditService auditService,
                      EmailService emailService) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.cryptoService = cryptoService;
        this.auditService = auditService;
        this.emailService = emailService;
    }

    @Transactional
    public Vote submitVote(Long userId, Long candidateId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (voteRepository.existsByUserId(userId)) {
            throw new RuntimeException("User has already voted");
        }

        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new RuntimeException("Candidate not found"));

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setCandidate(candidate);
        vote.setTimestamp(LocalDateTime.now());
        
        return voteRepository.save(vote);
    }

    public boolean hasUserVoted(String username) {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.isHasVoted();
    }

    public Map<String, Object> getResults() {
        List<Vote> votes = voteRepository.findAll();
        Map<Long, Long> candidateVotes = votes.stream()
            .collect(Collectors.groupingBy(
                vote -> vote.getCandidate().getId(),
                Collectors.counting()
            ));

        Map<String, Object> results = new HashMap<>();
        results.put("totalVotes", votes.size());
        results.put("candidateResults", candidateVotes);
        
        return results;
    }

    public Map<String, Object> getDetailedResults() {
        List<Vote> votes = voteRepository.findAll();
        Map<Candidate, Long> candidateVotes = votes.stream()
            .collect(Collectors.groupingBy(
                Vote::getCandidate,
                Collectors.counting()
            ));

        Map<String, Object> results = new HashMap<>();
        results.put("totalVotes", votes.size());
        results.put("candidateResults", candidateVotes);
        
        return results;
    }

    @Transactional
    public void castVote(Long candidateId, String encryptedVote, String username) throws Exception {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isHasVoted()) {
            throw new RuntimeException("User has already voted");
        }

        // Verify the vote signature
        String signature = cryptoService.sign(encryptedVote);
        
        // Retrieve candidate
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        // Create and save the vote
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setCandidate(candidate);
        vote.setTimestamp(LocalDateTime.now());
        vote.setEncryptedContent(encryptedVote);
        vote.setSignature(signature);
        voteRepository.save(vote);
        // Increment candidate vote count
        candidate.setVoteCount(candidate.getVoteCount() + 1);
        candidateRepository.save(candidate);

        // Mark user as voted
        user.setHasVoted(true);
        userRepository.save(user);

        // Log the vote
        auditService.logAction(username, "VOTE_CAST", "Vote cast successfully");

        // Send confirmation email
        try {
            String subject = "Confirmation de vote SunuElection";
            String text = String.format("Bonjour %s,\n\nVotre vote a bien été enregistré le %s.\n\nMerci de votre participation à cette élection.\n--\nSunuElection", user.getFullName(), java.time.LocalDateTime.now());
            emailService.send(user.getEmail(), subject, text);
        } catch (Exception ex) {
            // we log but do not fail the vote if email fails
            System.out.println("[MAIL FAIL] " + ex.getMessage());
        }
    }

    public long getTotalVotes() {
        return voteRepository.countTotalVotes();
    }

    @Transactional
    public String decryptVote(String encryptedVote) throws Exception {
        return cryptoService.decrypt(encryptedVote);
    }
} 