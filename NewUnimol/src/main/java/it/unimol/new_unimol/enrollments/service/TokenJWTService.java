package it.unimol.new_unimol.enrollments.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.unimol.new_unimol.enrollments.dto.TokenJWTDto;
import org.springframework.beans.factory.annotation.Value;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TokenJWTService {

    @Value("${jwt.private-key}")
    private String privateKeyString;

    @Value("${jwt.public-key}")
    private String publicKeyString;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Decifra e restituisce la chiave privata per la firma dei token JWT.
     *
     * @return La chiave privata come oggetto PrivateKey.
     * @throws RuntimeException Se la chiave privata non è in formato Base64 valido o se si verifica un errore durante la generazione della chiave.
     */
    private PrivateKey getPrivateKey() {
        if (this.privateKey == null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(this.privateKeyString);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.privateKey = keyFactory.generatePrivate(spec);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Chiave privata non è in formato Base64 valido", e);
            } catch (Exception e) {
                throw new RuntimeException("Errore prv_key, controlla application.properties", e);
            }
        }
        return privateKey;
    }

    /**
     * Decifra e restituisce la chiave pubblica per la verifica dei token JWT.
     *
     * @return La chiave pubblica come oggetto PublicKey.
     * @throws RuntimeException Se la chiave pubblica non è in formato Base64 valido o se si verifica un errore durante la generazione della chiave.
     */
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

    public <T> T extractClaim (String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Estrae tutti i claims dal token JWT.
     *
     * @param token Il token JWT da cui estrarre i claims.
     * @return Un oggetto Claims contenente tutti i claims del token.
     * @throws RuntimeException Se il token non è valido o se si verifica un errore durante l'estrazione dei claims.
     */
    private Claims extractAllClaims (String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Estrae l'ID utente dal token JWT.
     *
     * @param token Il token JWT da cui estrarre l'ID utente.
     * @return L'ID utente come stringa.
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Estrae il nome utente dal token JWT.
     *
     * @param token Il token JWT da cui estrarre il nome utente.
     * @return Il nome utente come stringa.
     */
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    /**
     * Estrae il ruolo dell'utente dal token JWT.
     *
     * @param token Il token JWT da cui estrarre il ruolo.
     * @return Il ruolo come stringa.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public boolean hasRole(String token, String requiredRole) {
        String role = extractRole(token);
        return requiredRole.equalsIgnoreCase(role);
    }

    /**
     * Estrae la data di scadenza dal token JWT.
     *
     * @param token Il token JWT da cui estrarre la data di scadenza.
     * @return La data di scadenza come oggetto Date.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica se il token JWT è scaduto.
     *
     * @param token Il token JWT da verificare.
     * @return true se il token è scaduto, false altrimenti.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Verifica se il token JWT è valido.
     *
     * @param token Il token JWT da verificare.
     * @return true se il token è valido, false altrimenti.
     */
    public boolean isTokenValid (String token) {
        return !isTokenExpired(token);
    }

    public TokenJWTDto generateToken (String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();

        long now = System.currentTimeMillis();
        long expiration = now + (this.jwtExpiration * 1000);

        String token = Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .claim("username", username)
                .claim("role", role)
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
                .compact();

        return new TokenJWTDto(token, userId, username, role, now / 1000, expiration / 1000);
    }

    public TokenJWTDto parseToken(String token) {
        Claims claims = extractAllClaims(token);
        return new TokenJWTDto(
                token,
                claims.getSubject(),
                claims.get("username", String.class),
                claims.get("role", String.class),
                claims.getIssuedAt().getTime() / 1000,
                claims.getExpiration().getTime() / 1000
        );
    }
}
