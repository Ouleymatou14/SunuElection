package com.sunelection.controller;

import com.sunelection.model.Candidate;
import com.sunelection.model.User;
import com.sunelection.service.AdminService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Gestion des candidats
    @PostMapping("/candidates")
    public ResponseEntity<?> createCandidate(@RequestBody Candidate candidate) {
        try {
            Candidate created = adminService.createCandidate(candidate);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/candidates/{id}")
    public ResponseEntity<?> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {
        try {
            Candidate updated = adminService.updateCandidate(id, candidate);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        try {
            adminService.deleteCandidate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/candidates")
    public ResponseEntity<?> getAllCandidates() {
        try {
            List<Candidate> candidates = adminService.getAllCandidates();
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // Gestion des utilisateurs
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            User user = adminService.enableUser(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            User user = adminService.disableUser(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        try {
            User user = adminService.updateUserRoles(id, new java.util.HashSet<>(request.getRoles()));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @Data
    static class RoleUpdateRequest {
        private List<String> roles;
    }
}