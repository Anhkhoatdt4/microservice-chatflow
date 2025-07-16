package com.qlda.profileservice.configuration;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenHandler jwtTokenHandler;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[JwtFilter] Incoming request URI: {}", request.getRequestURI());
        log.info("[JwtFilter] Method: {}", request.getMethod());
        log.info("[JwtFilter] Authorization Header: {}", request.getHeader("Authorization"));
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String username;


        log.info("Starting JWT filter for request URI: {}", request.getRequestURI());

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization header missing or does not start with Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwtToken = jwtTokenHandler.getToken(request);
            log.info("Extracted JWT token: {}", jwtToken);

            username = jwtTokenHandler.getUsernameFromToken(jwtToken);
            log.info("Extracted username from token: {}", username);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (username != null && (authentication == null || !authentication.isAuthenticated())) {
                log.info("No authentication found in context, loading user details for username: {}", username);


                if (username != null && (authentication == null || !authentication.isAuthenticated())) {
                    log.info("No authentication found in context. Creating authentication manually.");

                    // KHÔNG dùng UserDetailsService nữa → set auth trực tiếp
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    username, null, List.of() // nếu cần roles → lấy từ JWT claim
                            );

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    log.warn("Token validation failed");
                }
            } else {
                log.info("Authentication already exists in context or username is null");
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (JwtException e) {
            log.error("JWT token invalid: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        } catch (Exception e) {
            log.error("Unexpected error during JWT processing: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        }
    }

}
