package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.repository.UserRepository;
import com.sunelection.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import com.sunelection.model.VerificationToken;
import com.sunelection.repository.VerificationTokenRepository;
import com.sunelection.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider,
                      AuthenticationManager authenticationManager,
                      VerificationTokenRepository verificationTokenRepository,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user, Instant.now().plusSeconds(86_400));
        verificationTokenRepository.save(verificationToken);

        String link = frontendUrl + "/api/activate.html?token=" + token;
        emailService.send(user.getEmail(),
                "Activation de votre compte SunuElection",
                "Bonjour,\n\nMerci de votre inscription sur SunuElection. " +
                        "Cliquez sur le lien suivant pour activer votre compte (valide 24h):\n" + link + "\n\n--\nSunuElection");
    }

    public Map<String, Object> login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEnabled()) {
            throw new RuntimeException("Compte non activé");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return response;
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Générer un mot de passe temporaire sécurisé
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 12);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        // Envoyer le mot de passe temporaire par email (simulation)
        System.out.println("Mot de passe temporaire pour " + email + " : " + tempPassword);
        // En production, utiliser un service d'email pour envoyer le mot de passe
    }
} 