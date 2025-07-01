//package it.unimol.new_unimol.enrollments.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
//import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
//import it.unimol.new_unimol.enrollments.service.TeacherService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/teachers")
//public class TeacherController {
//    @Autowired
//    private TeacherService teacherService;
//
//    @PostMapping("/courses/{courseId}/enrollments/manual-enroll")
//    @Operation(description = "Questa funzione iscrive manualmente uno studente a un corso")
//    public ResponseEntity<CourseEnrollmentDto> manualEnrollStrudent() {}
//
//    @PostMapping("/courses/{courseId}/enrollments/bulk-manual-enroll")
//    @Operation(description = "Questa funzione iscrive manualmente più studenti a un corso")
//    public ResponseEntity<List<CourseEnrollmentDto>> bulkManualEnrollStrudents() {}
//
//    @GetMapping("/courses/{courseId}/enrollments")
//    @Operation(description = "Questa funzione visualizza le iscrizioni ad un corso gestito dal docente")
//    public ResponseEntity<List<CourseEnrollmentDto>> getOwnCoursesCourseEnrollments() {}
//
//    @GetMapping("/courses/{courseId}/enrollment-request")
//    @Operation(description = "Questa funzione visualizza richieste pendenti per un proprio corso")
//    public ResponseEntity<List<EnrollmentRequestDto>> getOwnCoursesPendingRequests() {}
//
//    @PutMapping("/enrollment-request/{requestId}/approve")
//    @Operation(description = "Questa funzione permette al docente di approvare una richiesta di iscrizione")
//    public ResponseEntity<EnrollmentRequestDto> approveEnrollmentRequestByTeacher() {}
//
//    @PutMapping("/enrollment-request/{requestId}/reject")
//    @Operation(description = "Questa funzione permette al docente di rifiutare una richiesta di iscrizione")
//    public ResponseEntity<EnrollmentRequestDto> rejectEnrollmentRequestByTeacher() {}
//
//    @DeleteMapping("/enrollments/{enrollmentId}")
//    @Operation(description = "Questa funzione permette al docente di rimuovere una iscrizione dal proprio corso")
//    public ResponseEntity<Void> deleteEnrollmentFromOwnCourse() {}
//
//    //Vedere se toglierla o meno
//    @GetMapping("/courses/{courseId}/enrollment-stats")
//    @Operation(description = "Questa funzione restituisce le statistiche di iscrizione per un corso gestitio")
//    public ResponseEntity<Map<String, Object>> getOwnCourseEnrollment() {}
//
//}
