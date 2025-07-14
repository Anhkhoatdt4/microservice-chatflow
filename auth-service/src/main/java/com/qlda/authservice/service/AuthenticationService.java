package com.qlda.authservice.service;

import com.qlda.authservice.dto.request.LogoutRequest;
import com.qlda.authservice.dto.response.TokenResponse;
import com.qlda.authservice.entity.InvalidatedToken;
import com.qlda.authservice.entity.User;
import com.qlda.authservice.repository.InvalidatedTokenRepository;
import com.qlda.authservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    @Value("${spring.jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${spring.jwt.issuer}")
    private String ISSUER;

    @Value("${spring.jwt.expiration}")
    private int EXPIRATION_TIME;

    @Value("${spring.jwt.refreshable-duration}")
    private int REFRESH_EXPIRATION_TIME;

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public String generateToken(String username){
        return Jwts.builder().issuer(ISSUER)
                .subject(username)
                .issuedAt(new Date())
                .expiration(generateExpirationDate())
                .signWith(getSigningKey())
                .claim("scope", buildScope(userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found")))) // Thêm claim tùy chỉnh nếu cần
                .compact(); // bien token thanh String jwt ( header + payload + signature)

    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(",");
        if (!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add(role.getName()); // Giả sử Role có phương thức getName() để lấy tên
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString(); // Trả về chuỗi chứa các quyền, phân tách bằng dấu phẩy
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000L);
    }
    private Date getExpirationDate(String token){
        try{
            Claims claims = getAllClaimsFromToken(token);
            if (claims != null) {
                return claims.getExpiration();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Key getSigningKey() {
        byte[] keyBytes = SIGNER_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String getToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Loai bo "Bearer "
        }
        return null; // Neu khong co token
    }

    public String getUsernameFromToken(String token){
        String username = null;
        try {
            Claims claims = getAllClaimsFromToken(token);
            if(claims != null){
                username = claims.getSubject(); // Lấy tên người dùng từ claims
                log.debug("Username from token: {}", username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return username;
    }

    private Claims getAllClaimsFromToken(String token) {
        Claims claims;
        try{
            claims= Jwts.parser()
                    .setSigningKey(getSigningKey())  // Cung cấp khóa ký để xác minh chữ ký của token.
                    .build()
                    .parseClaimsJws(token)  // Giải mã và xác minh chữ ký của JWT.
                    .getBody();  // Lấy phần thân (claims) của JWT.
            log.debug("Claims from token: {}", claims);
        }
        catch (Exception e){
            e.printStackTrace();
            claims =null;
        }
        return claims;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            boolean isTokenExpired = claims.getExpiration().before(new Date());
            boolean isTokenInvalidated = invalidatedTokenRepository.existsById(token);
            return !isTokenExpired && !isTokenInvalidated;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDate(token);
        return expiration.before(new Date());
    }

    public boolean isTokenValid(String authToken, UserDetails userDetails){
        String username = getUsernameFromToken(authToken);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(authToken));
    }

    public String generateRefreshToken(String username){
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME * 1000L)) // Thời gian hết hạn gấp đôi
                .signWith(getSigningKey())
                .compact(); // Biến token thành String jwt (header + payload + signature)
    }

    public TokenResponse generateTokenAndRefreshToken(String username) {
        String token = generateToken(username);
        String refreshToken = generateRefreshToken(username);
        return new TokenResponse(token, refreshToken,
                getExpirationDate(token), getExpirationDate(refreshToken));
    }
    public void logOut(LogoutRequest logoutRequest){
        String token = logoutRequest.getToken();
        if (token != null && !token.isEmpty()) {
            Date expirationDate = getExpirationDate(token);
            if (expirationDate != null) {
                // Lưu trữ token đã bị hủy trong cơ sở dữ liệu hoặc bộ nhớ cache
                InvalidatedToken invalidatedToken = new InvalidatedToken();
                invalidatedToken.setId(token);
                invalidatedToken.setExpiryTime(expirationDate);
                // Lưu invalidatedToken vào cơ sở dữ liệu hoặc bộ nhớ cache
                invalidatedTokenRepository.save(invalidatedToken);
                log.info("Token invalidated: {}", token);
            } else {
                log.warn("Invalid token: {}", token);
            }
        } else {
            log.warn("Logout request does not contain a valid token.");
        }
    }
}
