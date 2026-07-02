package me.fatihenes.mywatchlist.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private SecretKey signInKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, Duration.ofDays(1));
    }

    public String generateRememberMeToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, Duration.ofDays(30));
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails,
            Duration duration) {
        Instant now = Instant.now();

        return Jwts.builder().claims(extraClaims).subject(userDetails.getUsername())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(duration)))
                .signWith(getSignInKey(), Jwts.SIG.HS256).compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return signInKey;
    }
}
