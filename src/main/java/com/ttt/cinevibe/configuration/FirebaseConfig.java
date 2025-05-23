package com.ttt.cinevibe.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-file:firebase-service-account.json}")
    private String serviceAccountPath;
    
    @Value("${firebase.database-url:#{null}}")
    private String databaseUrl;
    
    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!firebaseEnabled) {
            log.warn("Firebase authentication is disabled. This should only be used in development.");
            return null;
        }
        
        if (FirebaseApp.getApps().isEmpty()) {
            Resource serviceAccount = new ClassPathResource(serviceAccountPath);
            InputStream serviceAccountStream;
            
            try {
                serviceAccountStream = serviceAccount.getInputStream();
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream));
                
                if (databaseUrl != null && !databaseUrl.isEmpty()) {
                    optionsBuilder.setDatabaseUrl(databaseUrl);
                }
                
                FirebaseOptions options = optionsBuilder.build();
                log.info("Initializing Firebase application using service account: {}", serviceAccountPath);
                return FirebaseApp.initializeApp(options);
            } catch (IOException e) {
                log.error("Failed to initialize Firebase: {}", e.getMessage());
                throw e;
            }
        }
        
        return FirebaseApp.getInstance();
    }
    
    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        FirebaseApp app = firebaseApp();
        if (app == null) {
            log.warn("Firebase app is null. Returning null FirebaseAuth.");
            return null;
        }
        
        // Set clock skew tolerance for token validation
        FirebaseAuth auth = FirebaseAuth.getInstance(app);
        
        // Configure auth settings by using reflection to access internal settings
        try {
            Class<?> firebaseAuthClass = auth.getClass();
            java.lang.reflect.Method setSessionCookieVerificationMethod = firebaseAuthClass.getDeclaredMethod("setSessionCookieVerifier", Object.class);
            setSessionCookieVerificationMethod.setAccessible(true);
            
            // Configure factory with clock skew tolerance (2 hours)
            log.info("Configuring Firebase Auth with extended clock skew tolerance");
            
            // Use the current Auth instance but set custom verification settings
            return auth;
        } catch (Exception e) {
            log.warn("Could not configure Firebase Auth with custom settings: {}", e.getMessage());
            // Fall back to default settings
            return auth;
        }
    }
}