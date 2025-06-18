package com.sunelection.config;

import com.sunelection.model.User;
import com.sunelection.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Initialise quelques comptes par défaut au démarrage de l'application afin
 * de faciliter les tests (ADMIN / SCRUTATEUR).
 * <p>
 * Les comptes sont créés uniquement s'ils n'existent pas déjà.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUserIfAbsent("admin1@gmail.com", "Admin", "Passer123", Set.of("ADMIN"));
        createUserIfAbsent("scrutateur@gmail.com", "Scrutateur", "Passer123", Set.of("SCRUTATEUR"));
    }

    private void createUserIfAbsent(String email, String fullName, String rawPassword, Set<String> roles) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
        logger.info("Created default user [{}] with roles {}", email, roles);
    }
}
