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
        
        // Générer un token de réinitialisation
        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken(token, user, Instant.now().plusSeconds(3600)); // 1 heure
        verificationTokenRepository.save(resetToken);
        
        // Envoyer l'e-mail de réinitialisation
        String resetLink = frontendUrl + "/api/reset-password.html?token=" + token;
        emailService.send(user.getEmail(),
                "Réinitialisation de votre mot de passe SunuElection",
                "Bonjour,\n\nVous avez demandé la réinitialisation de votre mot de passe. " +
                        "Cliquez sur le lien suivant pour définir un nouveau mot de passe (valide 1h):\n" + resetLink + "\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet e-mail.\n--\nSunuElection");
    }
    
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        VerificationToken resetToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token invalide"));
        
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Token expiré");
        }
        
        if (resetToken.isUsed()) {
            throw new RuntimeException("Token déjà utilisé");
        }
        
        // Mettre à jour le mot de passe
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        verificationTokenRepository.save(resetToken);
    }
}