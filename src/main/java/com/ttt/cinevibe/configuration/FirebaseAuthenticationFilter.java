package com.ttt.cinevibe.configuration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ttt.cinevibe.dto.request.UserRegisterRequest;
import com.ttt.cinevibe.service.UserService;
import com.ttt.cinevibe.util.FirebaseTokenHelper;
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
    private final FirebaseTokenHelper tokenHelper;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    public FirebaseAuthenticationFilter(
            @Autowired(required = false) FirebaseAuth firebaseAuth,
            UserService userService,
            FirebaseTokenHelper tokenHelper) {
        this.firebaseAuth = firebaseAuth;
        this.userService = userService;
        this.tokenHelper = tokenHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!firebaseEnabled || firebaseAuth == null) {
            log.debug("Firebase authentication is disabled - skipping token validation");
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                handleFirebaseToken(token);
            } catch (Exception e) {
                log.error("Authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleFirebaseToken(String token) {
        FirebaseToken decodedToken = tokenHelper.verifyToken(token);

        if (decodedToken == null || !tokenHelper.isTokenValid(decodedToken)) {
            log.warn("Invalid or expired Firebase token");
            SecurityContextHolder.clearContext();
            return;
        }

        try {
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();

            log.debug("Firebase token validated for user: {}", uid);

            UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
                    .firebaseUid(uid)
                    .email(email != null ? email : uid + "@firebase.com")
                    .displayName(name != null ? name : (email != null ? email : uid))
                    .build();

            userService.createUser(userRegisterRequest);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    uid,
                    token,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("Error processing Firebase token: {}", e.getMessage());
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