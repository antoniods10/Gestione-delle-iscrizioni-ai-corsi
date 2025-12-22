package it.unimol.new_unimol.enrollments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentSettingsDto;
import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.service.AdminService;
import it.unimol.new_unimol.enrollments.service.TokenJWTService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

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

    @PostMapping("/courses/{courseId}/enrollment-settings")
    @Operation(description = "Questa funzione crea una nuova configurazione di iscrizione per un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurazione creata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione consentitato solo agli admin"),
            @ApiResponse(responseCode = "409", description = "Configurazione già esistente"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> createEnrollmentSettings(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId,
            @Valid @RequestBody CourseEnrollmentSettingsDto settingsDto) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            String adminId = tokenJWTService.extractUserId(token);
            CourseEnrollmentSettingsDto created = adminService.createEnrollmentSettings(courseId, settingsDto, adminId);
            return ResponseEntity.status(HttpStatus.OK).body("Configurazione creata con successo\n" + created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la creazione della configurazione " + e.getMessage());
        }

    }

    @GetMapping("/enrollment-settings")
    @Operation(description = "Questa funzione restituisce la lista di tutte le configurazioni di iscrizione di tutti i corsi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista configurazioni recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getAllEnrollmentSettings(
            @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }
            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            List<CourseEnrollmentSettingsDto> settings = adminService.getAllEnrollmentSettings();
            return ResponseEntity.status(HttpStatus.OK).body("Lista configurazioni recuperata con successo\n" + settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle configurazione " + e.getMessage());
        }
    }

    @PutMapping("/courses/{courseId}/enrollment-settings-update")
    @Operation(description = "Questa funzione aggiorna la configurazione di iscrizione per un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurazione aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> updateEnrollmentSettings(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId,
            @Valid @RequestBody CourseEnrollmentSettingsDto settingsDto) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }
            CourseEnrollmentSettingsDto updated = adminService.updateEnrollmentSettings(courseId, settingsDto);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Configurazione non trovata per il corso: " + courseId);
            }
            return ResponseEntity.status(HttpStatus.OK).body("Configurazione aggiornata con successo\n" + updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'aggiornamento della configurazione " + e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/enrollment-settings-details")
    @Operation(description = "Questa funzione restituisce i dettagli della configurazione di iscrizione per un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurazione recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getEnrollmentSettingsByCourse(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            CourseEnrollmentSettingsDto settings = adminService.getEnrollmentSettingsByCourseId(courseId);
            if (settings == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Configurazione non trovata per il corso: " + courseId);
            }
            return ResponseEntity.status(HttpStatus.OK).body("Configurazione recuperata con successo\n" + settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero della configurazione: " + e.getMessage());
        }
    }

    @DeleteMapping("/courses/{courseId}/enrollment-settings-delete")
    @Operation(description = "Questa funzione elimina la configurazione di iscrizione per un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configurazione eliminata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> deleteEnrollmentSettings(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            boolean deleted = adminService.deleteEnrollmentSettings(courseId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Configurazione non trovata per il corso: " + courseId);
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Configurazione eliminata con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'eliminazione della configurazione: " + e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/enrollments")
    @Operation(description = "Questa funzione visualizza tutte le iscrizioni di un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista iscrizioni recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "400", description = "Corso non valido"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getCourseEnrollments(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            List<CourseEnrollmentDto> enrollments = adminService.getCourseEnrollments(courseId);
            return ResponseEntity.status(HttpStatus.OK).body("Lista iscrizioni recuperata con successo\n" + enrollments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle iscrizioni: " + e.getMessage());
        }
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(description = "Questa funzione cancella una specifica iscrizione di uno studente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Iscrizione eliminata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Iscrizione non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> deleteEnrollment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String enrollmentId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            String adminId = tokenJWTService.extractUserId(token);
            boolean deleted = adminService.deleteEnrollment(enrollmentId, adminId);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Iscrizione non trovata: " + enrollmentId);
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Iscrizione eliminata con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'eliminazione dell'iscrizione: " + e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/enrollment-request")
    @Operation(description = "Questa funzione visualizza le richieste di iscrizione pendenti per un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista richieste recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getPendingEnrollmentRequests(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            List<EnrollmentRequestDto> requests = adminService.getPendingEnrollmentRequests(courseId);
            return ResponseEntity.status(HttpStatus.OK).body("Lista richieste recuperata con successo\n" + requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle richieste " + e.getMessage());
        }
    }

    @PutMapping("/enrollment-request/{requestId}/approve")
    @Operation(description = "Questa funzione approva una richiesta di iscrizione")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Richiesta approvata con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida o già processata"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Richiesta non trovata"),
            @ApiResponse(responseCode = "409", description = "Studente già iscritto o limite iscrizioni raggiunto"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> approveEnrollmentRequest(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String requestId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            String adminId = tokenJWTService.extractUserId(token);
            EnrollmentRequestDto approved = adminService.approveEnrollmentRequest(requestId, adminId);
            return ResponseEntity.status(HttpStatus.OK).body("Richiesta approvata con successo\n" + approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'approvazione della richiesta: " + e.getMessage());
        }
    }

    @PutMapping("/enrollment-request/{requestId}/reject")
    @Operation(description = "Questa funzione rifiuta una richiesta di iscrizione specificando il motivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Richiesta rifiutata con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida o motivo mancante"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Richiesta non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> rejectEnrollmentRequest(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String requestId,
            @RequestBody Map<String, String> requestBody) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli admin");
            }

            String rejectReason = requestBody.get("rejectionReason");
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Il motivo del rifiuto è obbligatorio");
            }

            String adminId = tokenJWTService.extractUserId(token);
            EnrollmentRequestDto rejected = adminService.rejectEnrollmentRequest(requestId, rejectReason, adminId);
            return ResponseEntity.status(HttpStatus.OK).body("Richiesta rifiutata con successo\n" + rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il rifiuto della richiesta: " + e.getMessage());
        }
    }

}
