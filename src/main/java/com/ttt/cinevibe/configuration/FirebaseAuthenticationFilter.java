package com.ttt.cinevibe.configuration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.ttt.cinevibe.dto.request.UserRegisterRequest;
import com.ttt.cinevibe.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final UserService userService;
    
    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    // Constructor with @Autowired(required = false) for FirebaseAuth
    public FirebaseAuthenticationFilter(@Autowired(required = false) FirebaseAuth firebaseAuth, UserService userService) {
        this.firebaseAuth = firebaseAuth;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip token validation if Firebase is disabled
        if (!firebaseEnabled || firebaseAuth == null) {
            log.debug("Firebase authentication is disabled - skipping token validation");
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = extractToken(request);
        
        if (StringUtils.hasText(token)) {
            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                String uid = decodedToken.getUid();
                String email = decodedToken.getEmail();
                String name = decodedToken.getName();

                UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
                        .firebaseUid(uid)
                        .email(email)
                        .displayName(name != null ? name : email)
                        .build();
                
                // Sync user data with database
                userService.createUser(userRegisterRequest);
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    uid, 
                    token, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("User authenticated: {}", uid);
                
            } catch (FirebaseAuthException e) {
                log.error("Firebase authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}