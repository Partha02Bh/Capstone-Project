package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Ideally, store this in application.properties, not hardcoded!
    private static final String SECRET_KEY = "super_secret_key_for_banking_system_signature_must_be_long";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 1. Generate Token (Create the Badge)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        logger.info("Generating JWT token for username: {}", username);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Hours valid
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Validate Token (Check the Badge)
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        boolean usernameMatches = extractedUsername.equals(username);
        boolean expired = isTokenExpired(token);

        if (!usernameMatches) {
            logger.warn("JwtUtil: Username mismatch! Token: {} / Expected: {}", extractedUsername, username);
        }
        if (expired) {
            logger.warn("JwtUtil: Token expired for user: {}", username);
        }

        return (usernameMatches && !expired);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}