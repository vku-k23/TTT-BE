package com.ttt.cinevibe.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Test API", description = "Development testing endpoints - disable in production")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;
    
    private final FirebaseAuth firebaseAuth;

    @Operation(summary = "Generate test token", description = "Development only: generates a test authentication for the specified UID")
    @GetMapping("/token/{uid}")
    public ResponseEntity<Map<String, String>> generateTestToken(@PathVariable String uid) {
        Map<String, String> response = new HashMap<>();
        
        if (!firebaseEnabled || firebaseAuth == null) {
            response.put("message", "Firebase authentication is disabled. Token cannot be generated.");
            return ResponseEntity.ok(response);
        }
        
        try {
            String customToken = firebaseAuth.createCustomToken(uid);
            response.put("customToken", customToken);
            response.put("message", "Note: This is a Firebase custom token. For testing API endpoints, you need to exchange this for an ID token.");
            response.put("instructions", "Use Firebase Auth REST API to exchange this custom token for an ID token: https://firebase.google.com/docs/reference/rest/auth#section-verify-custom-token");
            
            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            log.error("Error creating custom token: {}", e.getMessage());
            response.put("error", "Failed to create custom token: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "Test endpoint", description = "Development only: returns information about Firebase configuration")
    @GetMapping("/firebase-status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("firebaseEnabled", firebaseEnabled);
        response.put("firebaseAuthInitialized", firebaseAuth != null);
        return ResponseEntity.ok(response);
    }
}