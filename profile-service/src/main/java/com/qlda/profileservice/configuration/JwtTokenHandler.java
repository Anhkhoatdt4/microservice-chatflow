package com.qlda.profileservice.configuration;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenHandler {

    @Value("${spring.jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${spring.jwt.issuer}")
    private String ISSUER;

    @Value("${spring.jwt.expiration}")
    private int EXPIRATION_TIME;

    @Value("${spring.jwt.refreshable-duration}")
    private int REFRESH_EXPIRATION_TIME;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SIGNER_KEY.getBytes());
    }

    public String generateToken(String username, String scope) {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(new Date())
                .expiration(generateExpirationDate())
                .signWith(getSigningKey())
                .claim("scope", scope)
                .compact();
    }
    public boolean isTokenValid(String authToken, UserDetails userDetails){
        String username = getUsernameFromToken(authToken);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(authToken));
    }

    public String getToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Loai bo "Bearer "
        }
        return null; // Neu khong co token
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000L);
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME * 1000L))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username", e);
            return null;
        }
    }

    public Date getExpirationDate(String token) {
        try {
            return getAllClaimsFromToken(token).getExpiration();
        } catch (Exception e) {
            log.error("Failed to extract expiration", e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDate(token);
        return expiration != null && expiration.before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationTime(String token) {
        Date expirationDate = getExpirationDate(token);
        return expirationDate != null ? expirationDate.getTime() - System.currentTimeMillis() : 0;
    }

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
