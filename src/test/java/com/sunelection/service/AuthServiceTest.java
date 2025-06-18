package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.repository.UserRepository;
import com.sunelection.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import com.sunelection.repository.VerificationTokenRepository;
import com.sunelection.service.EmailService;
import com.sunelection.model.VerificationToken;
import java.time.Instant;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, authenticationManager, verificationTokenRepository, emailService);
    }

    @Test
    void register_Success() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.register(user);

        // Assert
        verify(userRepository).existsByEmail(user.getEmail());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).send(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void register_EmailExists() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(user));
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwtToken");

        // Act
        Map<String, Object> result = authService.login(email, password);

        // Assert
        assertNotNull(result);
        assertEquals("jwtToken", result.get("token"));
        assertEquals(user, result.get("user"));
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(email);
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void isEmailTaken_True() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = authService.isEmailTaken(email);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void isEmailTaken_False() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = authService.isEmailTaken(email);

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }
} 