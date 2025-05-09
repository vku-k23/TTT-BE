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
            
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            
            Map<String, Object> claims = decodedToken.getClaims();
            long expirationTime = ((Number) claims.getOrDefault("exp", 0)).longValue() * 1000; // Convert to milliseconds
            long currentTime = System.currentTimeMillis();
            long timeToLive = expirationTime - currentTime;
            
            if (timeToLive <= 0) {
                log.warn("Token has expired for user: {}", decodedToken.getUid());
                return null;
            }
            
            // Log custom claims for debugging
            if (claims.containsKey("username")) {
                log.debug("Token contains username claim: {}", claims.get("username"));
            } else {
                log.debug("Token does not contain username claim");
            }
            
            log.debug("Token verified successfully for user: {}. Token expires in {} minutes", 
                decodedToken.getUid(), 
                TimeUnit.MILLISECONDS.toMinutes(timeToLive));
                
            return decodedToken;
        } catch (Exception e) {
            log.error("Error verifying Firebase token: {}", e.getMessage());
            return null;
        }
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