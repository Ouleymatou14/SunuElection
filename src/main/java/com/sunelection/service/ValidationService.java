package com.sunelection.service;

import com.sunelection.model.User;
import com.sunelection.model.Vote;
import com.sunelection.model.Candidate;
import com.sunelection.model.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
public class ValidationService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    public void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (user.getPassword() == null || user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (user.getFullName().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Full name must not exceed " + MAX_NAME_LENGTH + " characters");
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }

    public void validateVote(Vote vote) {
        if (vote == null) {
            throw new IllegalArgumentException("Vote cannot be null");
        }

        if (vote.getUser() == null) {
            throw new IllegalArgumentException("User is required for vote");
        }

        if (vote.getCandidate() == null) {
            throw new IllegalArgumentException("Candidate is required for vote");
        }

        if (vote.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp is required for vote");
        }
    }

    public void validateCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate cannot be null");
        }

        if (candidate.getName() == null || candidate.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate name is required");
        }

        if (candidate.getName().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Candidate name must not exceed " + MAX_NAME_LENGTH + " characters");
        }

        if (candidate.getDescription() != null && candidate.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        if (candidate.getVoteCount() < 0) {
            throw new IllegalArgumentException("Vote count cannot be negative");
        }
    }

    public void validateAuditLog(AuditLog log) {
        if (log == null) {
            throw new IllegalArgumentException("Audit log cannot be null");
        }

        if (log.getUsername() == null || log.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required for audit log");
        }

        if (log.getAction() == null || log.getAction().trim().isEmpty()) {
            throw new IllegalArgumentException("Action is required for audit log");
        }

        if (log.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp is required for audit log");
        }

        if (!StringUtils.hasText(log.getHash())) {
            throw new IllegalArgumentException("Hash is required");
        }
    }

    public void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password is required");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
} 