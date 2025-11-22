package com.campuscross.wallet.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.campuscross.wallet.util.JwtUtil;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            log.debug("JwtAuthFilter processing: {} {}", request.getMethod(), path);

            String header = request.getHeader("Authorization");

            // Only process if Authorization header exists
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                log.debug("JWT token found, validating...");

                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);
                    log.debug("Valid JWT for user: {}", email);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, null);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.debug("Invalid JWT token");
                }
            } else {
                log.debug("No Authorization header found");
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
        }

        // Always continue the filter chain
        chain.doFilter(request, response);
    }
}