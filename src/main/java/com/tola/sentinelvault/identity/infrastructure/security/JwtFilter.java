package com.tola.sentinelvault.identity.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepts every request, extracts the Bearer token from the
 * Authorization header, validates it, and populates the
 * SecurityContext if valid.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            log.debug("No Authorization header or invalid Bearer format for request: {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        if (!jwtProvider.isValid(token)) {
            log.debug("Invalid JWT token for request: {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtProvider.parse(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);

            if (role == null || role.isEmpty()) {
                log.warn("JWT token missing 'role' claim for user: {} ({})", userId, email);
                chain.doFilter(request, response);
                return;
            }

            log.debug("Authenticated user {} with role {} for request: {}", userId, role, request.getRequestURI());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            log.error("Error processing JWT token: {}", ex.getMessage(), ex);
        }

        chain.doFilter(request, response);
    }
}