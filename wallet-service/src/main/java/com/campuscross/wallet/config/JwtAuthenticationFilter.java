package com.campuscross.wallet.config;

import com.campuscross.wallet.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        log.debug("JwtAuthenticationFilter - request {} {}", request.getMethod(), request.getRequestURI());
        if (requestTokenHeader == null) {
            log.debug("JwtAuthenticationFilter - no Authorization header present");
        } else {
            log.debug("JwtAuthenticationFilter - Authorization header present (startsWith Bearer={})",
                    requestTokenHeader.startsWith("Bearer "));
        }

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getEmailFromToken(jwtToken);
                log.debug("JwtAuthenticationFilter - extracted username/ email from token: {}", username);
            } catch (Exception e) {
                log.warn("JwtAuthenticationFilter - Unable to parse JWT Token: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean valid = false;
            try {
                valid = jwtUtil.validateToken(jwtToken);
            } catch (Exception e) {
                log.warn("JwtAuthenticationFilter - token validation raised: {}", e.getMessage());
            }
            log.debug("JwtAuthenticationFilter - token valid: {}", valid);
            if (valid) {
                String role = jwtUtil.getRoleFromToken(jwtToken);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JwtAuthenticationFilter - authentication set for user: {} with role: {}", username, role);
            }
        }
        filterChain.doFilter(request, response);
    }
}
