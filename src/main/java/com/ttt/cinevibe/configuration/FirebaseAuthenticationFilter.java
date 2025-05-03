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

    // Cho phép development mode để sử dụng test token
    @Value("${app.development-mode:true}")
    private boolean developmentMode;

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
            // Kiểm tra xem token có phải là test token không
            if (developmentMode && token.startsWith("test_direct_token_")) {
                // Xử lý test token
                handleTestToken(token);
            } else {
                // Xử lý token Firebase thật
                handleFirebaseToken(token);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void handleTestToken(String token) {
        try {
            // Trích xuất UID từ test token
            String uid = token.substring("test_direct_token_".length());
            
            log.info("Processing test token for UID: {}", uid);
            
            // Tạo nếu người dùng không tồn tại
            UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
                    .firebaseUid(uid)
                    .email(uid + "@test.com")
                    .displayName("Test User " + uid)
                    .build();
            
            // Sync user data with database
            userService.createUser(userRegisterRequest);
            
            // Set xác thực
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                uid, 
                token, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Test user authenticated: {}", uid);
        } catch (Exception e) {
            log.error("Test token authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
    
    private void handleFirebaseToken(String token) {
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
            log.info("Firebase user authenticated: {}", uid);
            
        } catch (FirebaseAuthException e) {
            log.error("Firebase authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}