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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import com.google.firebase.auth.FirebaseToken;

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
    
    @Operation(summary = "Get direct auth token for testing", description = "Development only: creates a direct auth token for testing that can be used immediately")
    @GetMapping("/direct-token/{uid}")
    public ResponseEntity<Map<String, String>> getDirectTestToken(@PathVariable String uid) {
        Map<String, String> response = new HashMap<>();
        
        if (!firebaseEnabled || firebaseAuth == null) {
            response.put("message", "Firebase authentication is disabled. Token cannot be generated.");
            return ResponseEntity.ok(response);
        }
        
        try {
            // Tạo một token trực tiếp cho UID được cung cấp
            // Đây là một mô phỏng cho ID token Firebase thực
            String directToken = "test_direct_token_" + uid;
            
            response.put("token", directToken);
            response.put("tokenType", "Bearer");
            response.put("usage", "Copy the token value and use it in the Authorization header as: Bearer token_value");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating direct token: {}", e.getMessage());
            response.put("error", "Failed to create direct token: " + e.getMessage());
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
    
    @Operation(summary = "Firebase Auth Guide", description = "Instructions for proper Firebase authentication flow")
    @GetMapping("/auth-guide")
    public ResponseEntity<Map<String, Object>> getFirebaseAuthGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        guide.put("title", "Hướng dẫn xác thực Firebase");
        
        Map<String, String> clientSideSteps = new HashMap<>();
        clientSideSteps.put("step1", "Thêm Firebase Authentication vào ứng dụng Android");
        clientSideSteps.put("step2", "Cho phép người dùng đăng nhập (email/password hoặc phương thức khác)");
        clientSideSteps.put("step3", "Lấy ID token sau khi đăng nhập: FirebaseUser.getIdToken(false).addOnSuccessListener(token -> {})");
        clientSideSteps.put("step4", "Thêm token vào header cho mọi request API: Authorization: Bearer {token}");
        guide.put("client_side", clientSideSteps);
        
        Map<String, String> serverSideSteps = new HashMap<>();
        serverSideSteps.put("note", "Server side đã được cấu hình để xác thực token Firebase trong FirebaseAuthenticationFilter");
        serverSideSteps.put("step1", "Token từ client sẽ được xác thực với Firebase Admin SDK");
        serverSideSteps.put("step2", "Nếu token hợp lệ, người dùng sẽ được tự động đăng ký hoặc đăng nhập");
        serverSideSteps.put("step3", "Request sẽ được xử lý với thông tin người dùng đã xác thực");
        guide.put("server_side", serverSideSteps);
        
        Map<String, String> testingInstructions = new HashMap<>();
        testingInstructions.put("option1", "Sử dụng Firebase Console để tạo user và Firebase Auth Rest API để lấy token");
        testingInstructions.put("option2", "Sử dụng Firebase Test Lab và một ứng dụng Android đơn giản để lấy token");
        testingInstructions.put("option3", "Sử dụng endpoint /api/test/token/{uid} để lấy custom token, sau đó đổi thành ID token");
        testingInstructions.put("custom_token_exchange_url", "https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=YOUR_WEB_API_KEY");
        testingInstructions.put("custom_token_exchange_body", "{\"token\": \"CUSTOM_TOKEN\", \"returnSecureToken\": true}");
        
        guide.put("testing", testingInstructions);
        
        return ResponseEntity.ok(guide);
    }
    
    @Operation(summary = "Verify Firebase Token", description = "Verifies a Firebase ID token and shows its contents")
    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");
        
        if (!firebaseEnabled || firebaseAuth == null) {
            response.put("error", "Firebase authentication is disabled");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (token == null || token.isEmpty()) {
            response.put("error", "Token is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Xác thực token và lấy thông tin
            var decodedToken = firebaseAuth.verifyIdToken(token);
            
            response.put("valid", true);
            response.put("uid", decodedToken.getUid());
            response.put("email", decodedToken.getEmail());
            response.put("name", decodedToken.getName());
            response.put("claims", decodedToken.getClaims());
            
            // Hướng dẫn sử dụng token này trong các request khác
            response.put("usage", "Sử dụng token này trong header: Authorization: Bearer " + token);
            
            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/validateToken")
    @Operation(summary = "Validate Firebase Token", description = "Validates a Firebase token and shows detailed information about clock synchronization")
    public Map<String, Object> validateToken(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        long serverTime = System.currentTimeMillis() / 1000;
        response.put("serverTime", serverTime);
        response.put("serverTimeReadable", new java.util.Date());
        
        if (!firebaseEnabled || firebaseAuth == null) {
            response.put("status", "error");
            response.put("message", "Firebase authentication is disabled");
            return response;
        }
        
        // If token is not provided, try to extract from request
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        if (token == null || token.isEmpty()) {
            response.put("status", "error");
            response.put("message", "No token provided");
            return response;
        }
        
        // Decode the token without verification first for diagnostic purposes
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = parts[1];
                // Add padding if needed
                while (payload.length() % 4 != 0) {
                    payload += "=";
                }
                
                String decoded = new String(java.util.Base64.getDecoder().decode(payload));
                Map<String, Object> claims = new com.google.gson.Gson().fromJson(decoded, Map.class);
                
                response.put("tokenDecoded", claims);
                
                // Extract and provide timing information
                Number issuedAt = (Number) claims.get("iat");
                Number expiresAt = (Number) claims.get("exp");
                
                if (issuedAt != null && expiresAt != null) {
                    long iat = issuedAt.longValue();
                    long exp = expiresAt.longValue();
                    
                    response.put("tokenIssuedAt", iat);
                    response.put("tokenExpiresAt", exp);
                    response.put("tokenIssuedAtReadable", new java.util.Date(iat * 1000));
                    response.put("tokenExpiresAtReadable", new java.util.Date(exp * 1000));
                    
                    // Calculate time differences
                    long timeDiff = serverTime - iat;
                    response.put("serverTimeMinusIssuedAt", timeDiff);
                    
                    if (iat > serverTime) {
                        response.put("timeWarning", "⚠️ TOKEN ISSUED IN THE FUTURE - Server time is behind by " + (iat - serverTime) + " seconds");
                    }
                    
                    if (serverTime > exp) {
                        response.put("expirationWarning", "⚠️ TOKEN EXPIRED - Token expired " + (serverTime - exp) + " seconds ago");
                    }
                }
            }
        } catch (Exception e) {
            response.put("decodingError", "Failed to decode token: " + e.getMessage());
        }
        
        // Now try to actually verify the token
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token, true);
            response.put("status", "success");
            response.put("message", "Token is valid");
            response.put("uid", decodedToken.getUid());
            response.put("email", decodedToken.getEmail());
            response.put("name", decodedToken.getName());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Token validation failed: " + e.getMessage());
            
            if (e.getMessage() != null && e.getMessage().contains("Firebase ID token is not yet valid")) {
                response.put("clockSkewError", true);
                response.put("recommendation", "Check device and server clock synchronization");
            }
        }
        
        return response;
    }
}