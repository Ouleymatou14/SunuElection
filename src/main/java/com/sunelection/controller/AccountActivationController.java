package com.sunelection.controller;

import com.sunelection.model.VerificationToken;
import com.sunelection.repository.VerificationTokenRepository;
import com.sunelection.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AccountActivationController {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    public AccountActivationController(VerificationTokenRepository verificationTokenRepository,
                                       UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElse(null);
        if (vt == null || vt.isUsed() || vt.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide ou expiré");
        }
        vt.setUsed(true);
        vt.getUser().setEnabled(true);
        userRepository.save(vt.getUser());
        verificationTokenRepository.save(vt);
        return ResponseEntity.ok("Compte activé avec succès");
    }
}
