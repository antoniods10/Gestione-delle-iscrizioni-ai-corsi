package it.unimol.new_unimol.enrollments.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenJWTService {
    @Value("${jwt.public-key}")
    private String publicKeyString;
    private PublicKey publicKey;

    private PublicKey getPublicKey() {
        if (this.publicKey == null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(this.publicKeyString);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.publicKey = keyFactory.generatePublic(spec);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Chiave pubblica non è in formato Base64 valido", e);
            } catch (Exception e) {
                throw new RuntimeException("Errore pub_key, controlla application.properties", e);
            }
        }
        return publicKey;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    private String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Boolean hasRole(String token, String role) {
        return extractRole(token).equals(role);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }
}