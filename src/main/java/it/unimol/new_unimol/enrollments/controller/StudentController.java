//package it.unimol.new_unimol.enrollments.controller;
//
//import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
//import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
//import it.unimol.new_unimol.enrollments.service.StudentService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import io.swagger.v3.oas.annotations.Operation;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/students")
//public class StudentController {
//    @Autowired
//    private StudentService studentService;
//
//    @PostMapping("/courses/{courseId}/enroll")
//    @Operation(description = "Questa funzione permette l'iscrizione self-service ad un corso")
//    public ResponseEntity<CourseEnrollmentDto> selfEnrollToCourse() {}
//
//    @GetMapping("/courses/isSelfSerivce")
//    @Operation(description = "Questa funzione controlla se un corso ha l'iscrizione self-service")
//    public ResponseEntity<Boolean> checkSelfServiceAvailability() {}
//
//    @GetMapping("/enrollments")
//    @Operation(description = "Questa funzione permette di visualizzare le proprie iscrizioni")
//    public ResponseEntity<List<CourseEnrollmentDto>> getPersonalEnrollments() {}
//
//    @DeleteMapping("/enrollments/{enrollmentId}")
//    @Operation(description = "Questa funzione permette di cancellare la propria iscrizione attiva ad un corso")
//    public ResponseEntity<Void> cancelPersonalEnrollment() {}
//
//    @PostMapping("/courses/{courseId}/enrollment-request")
//    @Operation(description = "Questa funzione permette di inviare una richiesta di iscrizione ad un corso")
//    public ResponseEntity<EnrollmentRequestDto> requestEnrollmentToCourse() {}
//
//    @GetMapping("/enrollment-requests")
//    @Operation(description = "Questa funzione permette di visualizzare le proprie iscrizioni inviate")
//    public ResponseEntity<List<EnrollmentRequestDto>> getPersonalEnrollmentRequests() {}
//
//    @DeleteMapping("/enrollment-request/{requestId}")
//    @Operation(description = "Questa funzione permette di annullare una richiesta di iscrizione pendente")
//    public ResponseEntity<Void> cancelPendingEnrollmentRequest() {}
//
//}
