package it.unimol.new_unimol.enrollments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.service.StudentService;
import it.unimol.new_unimol.enrollments.service.TokenJWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

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

    @PostMapping("/courses/{courseId}/self-enroll")
    @Operation(description = "Questa funzione permette l'iscrizione self-service ad un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Iscrizione creata con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "409", description = "Già iscritto o limite raggiunto"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> selfEnrollToCourse(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            String studentId = tokenJWTService.extractUserId(token);
            CourseEnrollmentDto enrollment = studentService.selfEnrollToCourse(courseId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Iscrizione creata con successo\n" + enrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'iscrizione: " + e.getMessage());
        }
    }

    @GetMapping("/courses/isSelfService")
    @Operation(description = "Questa funzione controlla se un corso ha l'iscrizione self-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verifica completata"),
            @ApiResponse(responseCode = "400", description = "Parametro courseId mancante"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> checkSelfServiceAvailability(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            if (courseId == null || courseId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Il parametro courseId è obbligatorio");
            }

            String studentId = tokenJWTService.extractUserId(token);
            boolean canEnroll = studentService.checkSelfServiceAvailability(studentId, courseId);
            return ResponseEntity.status(HttpStatus.OK).body("Verifica completata\n" + canEnroll);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la verifica " + e.getMessage());
        }
    }

    @GetMapping("/enrollments")
    @Operation(description = "Questa funzione permette di visualizzare le proprie iscrizioni")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista iscrizioni recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getPersonalEnrollments(
            @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            String studentId = tokenJWTService.extractUserId(token);
            List<CourseEnrollmentDto> enrollments = studentService.getPersonalEnrollments(studentId);
            return ResponseEntity.status(HttpStatus.OK).body("Lista iscrizioni recuperata con successo\n" + enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle iscrizioni: " + e.getMessage());
        }
    }

    @DeleteMapping("/enrollments/{enrollmentId}/delete")
    @Operation(description = "Questa funzione permette di cancellare la propria iscrizione attiva ad un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Iscrizione cancellata con successo"),
            @ApiResponse(responseCode = "400", description = "Operazione non valida"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Iscrizione non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> cancelPersonalEnrollment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String enrollmentId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            String studentId = tokenJWTService.extractUserId(token);
            boolean cancelled = studentService.cancelPersonalEnrollment(enrollmentId, studentId);

            if (!cancelled) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Iscrizione non trovata o non appartiene a questo studente");
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Iscrizione cancellata con successo");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la cancellazione: " + e.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/enrollment-request")
    @Operation(description = "Questa funzione permette di inviare una richiesta di iscrizione ad un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Richiesta creata con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "409", description = "Richiesta già esistente"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> requestEnrollmentToCourse(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            String studentId = tokenJWTService.extractUserId(token);

            EnrollmentRequestDto request = studentService.createEnrollmentRequest(courseId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Richiesta creata con successo\n" + request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la creazione della richiesta: " + e.getMessage());
        }
    }

    @GetMapping("/enrollment-requests")
    @Operation(description = "Questa funzione permette di visualizzare le proprie iscrizioni inviate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista richieste recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getPersonalEnrollmentRequests(
            @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            String studentId = tokenJWTService.extractUserId(token);
            List<EnrollmentRequestDto> requests = studentService.getPersonalEnrollmentRequests(studentId);
            return ResponseEntity.status(HttpStatus.OK).body("Lista richieste recuperata con successo\n" + requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle richieste: " + e.getMessage());
        }
    }

    @DeleteMapping("/enrollment-request/{requestId}/pending/delete")
    @Operation(description = "Questa funzione permette di annullare una richiesta di iscrizione pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Richiesta annullata con successo"),
            @ApiResponse(responseCode = "400", description = "Operazione non valida"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Richiesta non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> cancelPendingEnrollmentRequest(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String requestId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if (!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if (!tokenJWTService.hasRole(token, "student")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo agli studenti");
            }

            String studentId = tokenJWTService.extractUserId(token);
            boolean cancelled = studentService.cancelPendingEnrollmentRequest(requestId, studentId);

            if (!cancelled) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Richiesta non trovata o non appartiene a questo studente");
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Richiesta annullata con successo");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'annullamento: " + e.getMessage());
        }
    }

}
