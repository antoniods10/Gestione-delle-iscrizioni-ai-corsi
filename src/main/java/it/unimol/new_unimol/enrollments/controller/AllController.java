package it.unimol.new_unimol.enrollments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.unimol.new_unimol.enrollments.service.TokenJWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AllController {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private TokenJWTService tokenJWTService;

    /**
     * Estrae il token JWT dall'header Authorization
     *
     * @param authHeader Header Authorization
     * @return Token JWT estratto
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null) {
            throw new IllegalArgumentException("Header Authorization mancante");
        }
        authHeader = authHeader.trim();
        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization non valido (manca 'Bearer')");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Token JWT mancante nell'Header Authorization");
        }
        return token;
    }

    @GetMapping("/health")
    @Operation(description = "Questa funzione verifica lo stato di salute del microservizio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Il microservizio è attivo e funzionante"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato - Token mancante o non valido"),
            @ApiResponse(responseCode = "503", description = "Il microservizio non è attivo o ha problemi")
    })
    public ResponseEntity<?> healthCheck(@RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            Status status = healthEndpoint.health().getStatus();

            if (Status.UP.equals(status)) {
                return ResponseEntity.status(HttpStatus.OK).body(true);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Errore durante la verifica " + e.getMessage());
        }
    }
}