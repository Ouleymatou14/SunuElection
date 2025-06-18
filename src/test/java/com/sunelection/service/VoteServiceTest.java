package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.model.Vote;
import com.sunelection.repository.UserRepository;
import com.sunelection.repository.VoteRepository;
import com.sunelection.repository.CandidateRepository;
import com.sunelection.model.Candidate;
import com.sunelection.security.JwtCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private JwtCryptoService cryptoService;

    @Mock
    private AuditService auditService;

    private VoteService voteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        voteService = new VoteService(voteRepository, userRepository, candidateRepository, cryptoService, auditService);
        when(candidateRepository.findById(anyLong())).thenReturn(Optional.of(new Candidate()));
    }

    @Test
    void castVote_Success() throws Exception {
        // Arrange
        String username = "test@example.com";
        String encryptedVote = "encryptedVote";
        User user = new User();
        user.setEmail(username);
        user.setHasVoted(false);

        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
        when(cryptoService.sign(anyString())).thenReturn("signature");
        when(candidateRepository.findById(anyLong())).thenReturn(Optional.of(new Candidate()));

        // Act
        voteService.castVote(1L, encryptedVote, username);

        // Assert
        verify(voteRepository).save(any(Vote.class));
        verify(userRepository).save(user);
        verify(auditService).logAction(eq(username), eq("VOTE_CAST"), anyString());
        assertTrue(user.isHasVoted());
    }

    @Test
    void castVote_UserNotFound() {
        // Arrange
        String username = "nonexistent@example.com";
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> voteService.castVote(1L, "vote", username));

    }

    @Test
    void castVote_AlreadyVoted() {
        // Arrange
        String username = "test@example.com";
        User user = new User();
        user.setEmail(username);
        user.setHasVoted(true);

        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> voteService.castVote(1L, "vote", username));

    }

    @Test
    void getTotalVotes() {
        // Arrange
        when(voteRepository.countTotalVotes()).thenReturn(5L);

        // Act
        long totalVotes = voteService.getTotalVotes();

        // Assert
        assertEquals(5L, totalVotes);
        verify(voteRepository).countTotalVotes();
    }

    @Test
    void decryptVote_Success() throws Exception {
        // Arrange
        String encryptedVote = "encryptedVote";
        String decryptedVote = "decryptedVote";
        when(cryptoService.decrypt(encryptedVote)).thenReturn(decryptedVote);

        // Act
        String result = voteService.decryptVote(encryptedVote);

        // Assert
        assertEquals(decryptedVote, result);
        verify(cryptoService).decrypt(encryptedVote);
    }
} 