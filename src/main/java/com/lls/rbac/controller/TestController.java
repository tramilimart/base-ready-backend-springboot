package com.lls.rbac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/public/test")
    public ResponseEntity<?> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint - no authentication required");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/test")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> userEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a user endpoint - USER role required");
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/moderator/test")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<?> moderatorEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a moderator endpoint - MODERATOR or ADMIN role required");
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is an admin endpoint - ADMIN role required");
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Map<String, Object> response = new HashMap<>();
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
            response.put("authenticated", true);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Not authenticated");
        }
    }

    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> userData) {
        // This would typically call a user service to create a new user
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User creation endpoint - ADMIN role required");
        response.put("username", userData.get("username"));
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/delete-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        // This would typically call a user service to delete a user
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User deletion endpoint - ADMIN role required");
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 