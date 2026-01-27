package com.algorena.security.security;

import com.algorena.users.domain.Role;
import com.algorena.users.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;

    @Value("${app.jwt.expiration-ms:3600000}") // default 1 hour
    private long jwtExpirationMs;

    public JwtService(@Value("${app.jwt.secret:1c17e1d043766a36dd26c58b94be13cb4c2c4c78d469663f424106c99a89a84caca3b1471948458c86104c65e5935d112db71cd11497d3f9466d3b8db763201a}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        List<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("language", user.getLanguage() != null ? user.getLanguage().name() : "EN");
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId() != null ? user.getId().toString() : user.getEmail())
                .setIssuer("swyle-backend")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateAndParse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
