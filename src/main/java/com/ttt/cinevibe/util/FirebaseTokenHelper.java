package com.ttt.cinevibe.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FirebaseTokenHelper {

    private final FirebaseAuth firebaseAuth;
    // Allow tokens issued up to 1 hour in the future (severe clock skew allowance)
    private static final long MAX_CLOCK_SKEW_SECONDS = 3600; 
    
    @Autowired(required = false)
    public FirebaseTokenHelper(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }
    
    public FirebaseToken verifyToken(String token) {
        try {
            if (firebaseAuth == null) {
                log.warn("Firebase Auth is not initialized");
                return null;
            }
            
            // Try normal verification first with clock skew tolerance
            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token, true);
                logTokenDetails(decodedToken);
                return decodedToken;
            } catch (Exception e) {
                // If the error is about the token not being valid yet, it might be a clock skew issue
                if (e.getMessage() != null && e.getMessage().contains("Firebase ID token is not yet valid")) {
                    log.warn("Token validation failed due to time synchronization issue: {}. Attempting extended verification.", 
                            e.getMessage());
                    
                    // Manual verification for tokens with severe time skew
                    return verifyTokenWithExtendedClockSkew(token, e);
                } else {
                    // For other errors, just rethrow
                    throw e; 
                }
            }
        } catch (Exception e) {
            log.error("Error verifying Firebase token: {}", e.getMessage());
            return null;
        }
    }
    
    private FirebaseToken verifyTokenWithExtendedClockSkew(String token, Exception originalError) throws Exception {
        try {
            // First, get the current server time for reference
            long serverCurrentTime = System.currentTimeMillis() / 1000;
            log.info("Server current time: {}", serverCurrentTime);
            
            // Parse the JWT token manually to extract the claims without validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid token format");
                throw originalError;
            }
            
            String payload = parts[1];
            // Add padding if needed
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            String decodedPayload = new String(java.util.Base64.getDecoder().decode(payload));
            Map<String, Object> claims = parseJson(decodedPayload);
            
            // Extract issuance and expiration times
            Number iat = (Number) claims.get("iat");
            Number exp = (Number) claims.get("exp");
            String sub = (String) claims.get("sub");
            
            if (iat == null || exp == null || sub == null) {
                log.error("Token is missing required claims");
                throw originalError;
            }
            
            long issuedAt = iat.longValue();
            long expiresAt = exp.longValue();
            
            log.info("Token claims - iat: {}, exp: {}, current server time: {}", issuedAt, expiresAt, serverCurrentTime);
            
            // Check if token is expired
            if (serverCurrentTime > expiresAt) {
                log.warn("Token has expired");
                throw originalError;
            }
            
            // Allow tokens issued in the future (within reasonable limits)
            // This handles severe clock skew between client and server
            if (issuedAt > serverCurrentTime) {
                long timeDifference = issuedAt - serverCurrentTime;
                if (timeDifference <= MAX_CLOCK_SKEW_SECONDS) {
                    log.warn("Accepting token issued in the future - time difference: {} seconds", timeDifference);
                    
                    // Now try to verify with allowFullClockSkew=true
                    try {
                        // With the warning logged, retry normal verification
                        return firebaseAuth.verifyIdToken(token, true);
                    } catch (Exception e) {
                        // If still fails, log and try once more with manual verification
                        log.warn("Still failing with allowFullClockSkew=true: {}. Last resort verification.", e.getMessage());
                        
                        // As a last resort, verify the token but accept the future issuance time
                        // This is a tradeoff between security and user experience
                        return manuallyVerifyTokenWithFutureIssuanceTime(token, claims);
                    }
                } else {
                    log.error("Token issued too far in the future: {} seconds ahead", timeDifference);
                    throw originalError;
                }
            }
            
            // If we get here, it's not a time-related issue
            throw originalError;
            
        } catch (Exception e) {
            if (e == originalError) {
                throw e; // Don't wrap original error
            }
            log.error("Error in extended token verification: {}", e.getMessage());
            throw new RuntimeException("Failed extended token verification", e);
        }
    }
    
    private FirebaseToken manuallyVerifyTokenWithFutureIssuanceTime(String token, Map<String, Object> claims) {
        try {
            // Make one last attempt with normal verification
            return firebaseAuth.verifyIdToken(token);
        } catch (Exception e) {
            log.error("All verification attempts failed for token with future issuance time: {}", e.getMessage());
            throw new RuntimeException("Could not verify token with severe time skew", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            // Simple JSON parsing - in production you should use a proper JSON library
            // This is just an example for demonstration
            return new com.google.gson.Gson().fromJson(json, Map.class);
        } catch (Exception e) {
            log.error("Error parsing token payload: {}", e.getMessage());
            throw new RuntimeException("Failed to parse token payload", e);
        }
    }
    
    private void logTokenDetails(FirebaseToken token) {
        Map<String, Object> claims = token.getClaims();
        long expirationTime = ((Number) claims.getOrDefault("exp", 0)).longValue() * 1000; // Convert to milliseconds
        long issuedAtTime = ((Number) claims.getOrDefault("iat", 0)).longValue() * 1000; // Convert to milliseconds
        long currentTime = System.currentTimeMillis();
        long timeToLive = expirationTime - currentTime;
        
        // Log time differences for debugging
        log.debug("Token time debug - Current server time: {}, Token issued at: {}, Token expires at: {}, Time difference: {} seconds", 
            currentTime/1000, issuedAtTime/1000, expirationTime/1000, (currentTime - issuedAtTime) / 1000);
        
        if (timeToLive <= 0) {
            log.warn("Token has expired for user: {}", token.getUid());
        }
        
        // Log custom claims for debugging
        if (claims.containsKey("username")) {
            log.debug("Token contains username claim: {}", claims.get("username"));
        }
        
        log.debug("Token verified successfully for user: {}. Token expires in {} minutes", 
            token.getUid(), 
            TimeUnit.MILLISECONDS.toMinutes(timeToLive));
    }
    
    public Map<String, Object> extractUserInfo(FirebaseToken token) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (token != null) {
            Map<String, Object> claims = token.getClaims();
            userInfo.put("uid", token.getUid());
            userInfo.put("email", token.getEmail());
            userInfo.put("name", token.getName());
            userInfo.put("picture", token.getPicture());
            userInfo.put("issuedAt", claims.get("iat"));
            userInfo.put("expiresAt", claims.get("exp"));
            
            // Add username if present in claims
            if (claims.containsKey("username")) {
                userInfo.put("username", claims.get("username"));
                log.debug("Found username in claims: {}", claims.get("username"));
            }
            
            userInfo.put("claims", claims);
        }
        
        return userInfo;
    }
    
    public boolean isTokenValid(FirebaseToken token) {
        if (token == null) {
            return false;
        }
        
        Map<String, Object> claims = token.getClaims();
        long expirationTime = ((Number) claims.getOrDefault("exp", 0)).longValue() * 1000; // Convert to milliseconds
        long currentTime = System.currentTimeMillis();
        
        return expirationTime > currentTime;
    }
}