package it.unimol.new_unimol.enrollments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.service.TeacherService;
import it.unimol.new_unimol.enrollments.service.TokenJWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private TokenJWTService tokenJWTService;

    /**
     * Estrae il token JWT dall'header Authorization
     * @param authHeader Header Authorization
     * @return Token JWT estratto
     */
    private String extractTokenFromHeader(String authHeader) {
        if(authHeader == null) {
            throw new IllegalArgumentException("Header Authorization mancante");
        }
        authHeader = authHeader.trim();
        if(!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization non valido (manca 'Bearer')");
        }
        String token = authHeader.substring(7).trim();
        if(token.isEmpty()) {
            throw new IllegalArgumentException("Token JWT mancante nell'Header Authorization");
        }
        return token;
    }

    @PostMapping("/courses/{courseId}/enrollments/manual-enroll")
    @Operation(description = "Questa funzione iscrive manualmente uno studente a un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Iscrizione creata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "409", description = "Studente già iscritto"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> manualEnrollStudent(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId,
            @RequestBody Map<String, String> requestBody) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            String studentId = requestBody.get("studentId");
            String notes = requestBody.get("notes");

            if(studentId == null || studentId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'id dello studente è obbligatorio");
            }

            CourseEnrollmentDto enrollment = teacherService.manualEnrollStudent(courseId, studentId, teacherId, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body("Iscrizione confermata con successo " + enrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'iscrizione manuale " + e.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/enrollments/bulk-manual-enroll")
    @Operation(description = "Questa funzione iscrive manualmente più studenti a un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Iscrizioni create con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "207", description = "Multi-status: alcune iscrizioni potrebbero essere fallite"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> bulkManualEnrollStudents(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId,
            @RequestBody List<String> studentIds) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            if(studentIds == null || studentIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La lista degli utenti non può essere vuota");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            Map<String, Object> results = teacherService.bulkManualEnrollStudents(courseId, studentIds, teacherId);

            if(results.containsKey("failed") && !((List<?>) results.get("failed")).isEmpty()) {
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(results);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Iscrizioni create con successo" + results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'iscrizione multipla: " +  e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/enrollments")
    @Operation(description = "Questa funzione visualizza le iscrizioni ad un corso gestito dal docente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista iscrizioni recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Corso non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getOwnCoursesCourseEnrollments(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            List<CourseEnrollmentDto> enrollments = teacherService.getOwnCourseEnrollments(courseId, teacherId);
            return ResponseEntity.status(HttpStatus.OK).body("Lista iscrizioni recuperata con successo" + enrollments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle iscrizioni: " + e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/enrollment-requests/pending")
    @Operation(description = "Questa funzione visualizza richieste pendenti per un proprio corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista richieste recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getOwnCoursesPendingRequests(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            List<EnrollmentRequestDto> requests = teacherService.getOwnCoursePendingRequest(courseId, teacherId);
            return ResponseEntity.status(HttpStatus.OK).body("Lista richieste recuperata con successo" + requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero delle richieste: " + e.getMessage());
        }
    }

    @PutMapping("/enrollment-request/{requestId}/approve")
    @Operation(description = "Questa funzione permette al docente di approvare una richiesta di iscrizione")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Richiesta approvata con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Richiesta non trovata"),
            @ApiResponse(responseCode = "409", description = "Conflitto: studente già iscritto o limite raggiunto"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> approveEnrollmentRequestByTeacher(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String requestId) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            EnrollmentRequestDto approved = teacherService.approveEnrollmentRequest(requestId, teacherId);
            return ResponseEntity.status(HttpStatus.OK).body("Richiesta approvata con successo" + approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'approvazione: " + e.getMessage());
        }
    }

    @PutMapping("/enrollment-request/{requestId}/reject")
    @Operation(description = "Questa funzione permette al docente di rifiutare una richiesta di iscrizione")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Richiesta rifiutata con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida o motivo mancante"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Richiesta non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> rejectEnrollmentRequestByTeacher(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String requestId,
            @RequestBody Map<String, String> requestBody) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            String rejectReason = requestBody.get("rejectionReason");
            if(rejectReason == null || rejectReason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Il motivo del rifiuto è obbligatorio");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            EnrollmentRequestDto rejected = teacherService.rejectEnrollmentRequest(requestId, rejectReason, teacherId);
            return ResponseEntity.status(HttpStatus.OK).body("Richiesta rifiutata con successo" + rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il rifiuto: " + e.getMessage());
        }
    }

    @DeleteMapping("/enrollments/{enrollmentId}/remove")
    @Operation(description = "Questa funzione permette al docente di rimuovere una iscrizione dal proprio corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Iscrizione rimossa con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "403", description = "Operazione non consentita"),
            @ApiResponse(responseCode = "404", description = "Iscrizione non trovata"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> deleteEnrollmentFromOwnCourse(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String enrollmentId,
            @RequestBody(required = false) Map<String, String> requestBody) {

        try {
            String token = extractTokenFromHeader(authorization);

            if(!tokenJWTService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
            }

            if(!tokenJWTService.hasRole(token, "TEACHER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
            }

            String teacherId = tokenJWTService.extractUserId(token);
            String reason = requestBody != null ? requestBody.get("reason") : null;

            boolean deleted = teacherService.deleteEnrollmentFromOwnCourse(enrollmentId, teacherId, reason);
            if(!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Iscrizione non trovata o non hai i permessi per eliminarla");
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Iscrizione rimossa con successo");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'eliminazione: " + e.getMessage());
        }
    }
}
