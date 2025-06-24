package it.unimol.new_unimol.enrollments.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentSettingsDto;
import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("/courses/{courseId}/enrollment-settings")
    @Operation(description = "Questa funzione crea una nuova configurazione di iscrizione per un corso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Configurazione creata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "409", description = "Configurazione già esistente")
    })
    public ResponseEntity<CourseEnrollmentSettingsDto> createEnrollmentSettings(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String courseId,
            @Valid @RequestBody CourseEnrollmentSettingsDto settingsDto) {

        CourseEnrollmentSettingsDto created = adminService.createEnrollmentSettings(courseId, settingsDto, authorization);
        return
    }

    @GetMapping("/enrollment-settings")
    @Operation(description = "Questa funzione restituisce la lista di tutte le configurazioni di iscrizione ad un corso")
    public ResponseEntity<List<CourseEnrollmentSettingsDto>> getAllEnrollmentSettings() {}

    @PutMapping("/courses/{courseId}/enrollment-settings")
    @Operation(description = "Questa funzione aggiorna la configurazione di iscrizione per un corso")
    public ResponseEntity<CourseEnrollmentSettingsDto> updateEnrollmentSettings() {}

    @GetMapping("/courses/{courseId}/enrollment-settings")
    @Operation(description = "Questa funzione restituisce i dettagli della configurazione di iscrizione per un corso")
    public ResponseEntity<CourseEnrollmentSettingsDto> getEnrollmentSettingsByCourse() {}

    @DeleteMapping("/courses/{courseId}/enrollment-settings")
    @Operation(description = "Questa funzione elimina la configurazione di iscrizione per un corso")
    public ResponseEntity<Void> deleteEnrollmentSettings() {}

    @GetMapping("/courses/{courseId}/enrollments")
    @Operation(description = "Questa funzione visualizza tutte le iscrizioni di un corso")
    public ResponseEntity<List<CourseEnrollmentDto>> getCourseEnrollments() {}

    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(description = "Questa funzione cancella una specifica iscrizione di uno studente")
    public ResponseEntity<Void> deleteEnrollment() {}

    @GetMapping("/courses/{courseId}/enrollment-request")
    @Operation(description = "Questa funzione visualizza le richieste di iscrizione pendenti per un corso")
    public ResponseEntity<List<EnrollmentRequestDto>> getPendingEnrollmentRequests() {}

    @PutMapping("/enrollment-request/{requestId}/approve")
    @Operation(description = "Questa funzione approva una richiesta di iscrizione")
    public ResponseEntity<EnrollmentRequestDto> approveEnrollmentRequest() {}

    @PutMapping("/enrollment-request/{requestId}/reject")
    @Operation(description = "Questa funzione rifiuta una richiesta di iscrizione specificando il motivo")
    public ResponseEntity<EnrollmentRequestDto> rejectEnrollmentRequest() {}
}
