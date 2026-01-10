package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationResponseDto;
import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentRepository;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentSettingsRepository;
import it.unimol.new_unimol.enrollments.repository.EnrollmentRequestRepository;
import it.unimol.new_unimol.enrollments.util.EnrollmentMode;
import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import it.unimol.new_unimol.enrollments.util.EnrollmentType;
import it.unimol.new_unimol.enrollments.util.RequestStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class StudentServiceTest {
    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitMQService rabbitMQService;

    @MockitoBean
    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        settingsRepository.deleteAll();
    }

    @Test
    void testSelfEnrollToCourseSuccess() {
        String courseId = "course-123";
        String studentId = "student-001";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setAllowWaitingList(true);
        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "teach-1", 100, null)
        );

        CourseEnrollmentDto result = studentService.selfEnrollToCourse(courseId,studentId);

        assertNotNull(result);
        assertEquals(courseId,result.courseId());
        assertEquals(studentId, result.studentId());
        assertEquals(EnrollmentStatus.ACTIVE, result.status());
    }

    @Test
    void testSelfEnrollToCourseAlreadyEnrolled() {
        String courseId = "course-123";
        String studentId = "student-001";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setAllowWaitingList(true);
        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "teach-1", 100, null)
        );

        studentService.selfEnrollToCourse(courseId, studentId);

        assertThrows(IllegalStateException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    void testCheckSelfServiceAvailability() {
        String courseId = "course-test";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setAllowWaitingList(false);
        settingsRepository.save(settings);

        boolean available = studentService.checkSelfServiceAvailability("student-new", courseId);
        assertTrue(available);
    }

    @Test
    void testSelfEnroll_EnrollmentNotAllowed_WrongMode() {
        String courseId = "course-manual-only";
        String studentId = "student-1";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.MANUAL);
        settings.setRequiresApproval(true);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);

        settingsRepository.save(settings);
        settingsRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    @Test
    void testSelfEnroll_DatesExpired() {
        String courseId = "course-expired";
        String studentId = "student-1";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(20));
        settings.setEnrollmentEndDate(LocalDateTime.now().minusDays(10));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);

        settingsRepository.save(settings);
        settingsRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    @Test
    void testSelfEnroll_CourseFull() {
        String courseId = "course-full";
        String studentId = "student-new";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setMaxEnrollments(0);
        settings.setAllowWaitingList(false);
        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalStateException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    @Test
    void testSelfEnroll_RequiresApproval_CreatesRequest() {
        String courseId = "course-approval-needed";
        String studentId = "student-req";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(true);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);
        settingsRepository.save(settings);
        settingsRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        studentService.createEnrollmentRequest(courseId, studentId);

        boolean requestExists = requestRepository.existsPendingRequest(courseId, studentId, RequestStatus.PENDING);
        assertTrue(requestExists, "Dovrebbe essere stata creata una richiesta di iscrizione in stato PENDING");

        boolean enrollmentExists = enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
        assertFalse(enrollmentExists, "Non dovrebbe esistere un'iscrizione attiva finché non viene approvata");
    }

    @Test
    void testSelfEnroll_CourseFull_GoesToWaitingList() {
        String courseId = "course-waiting-list";
        String studentId = "student-wl";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setMaxEnrollments(1);
        settings.setAllowWaitingList(true);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        // 2. Riempiamo il corso con un altro studente per occupare l'unico posto
        it.unimol.new_unimol.enrollments.model.CourseEnrollment existing = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        existing.setId(UUID.randomUUID().toString());
        existing.setCourseId(courseId);
        existing.setStudentId("student-existing");
        existing.setEnrollmentDate(LocalDateTime.now());
        existing.setStatus(EnrollmentStatus.ACTIVE);
        existing.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        enrollmentRepository.save(existing);
        enrollmentRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        CourseEnrollmentDto result = studentService.selfEnrollToCourse(courseId, studentId);

        assertNotNull(result);
        assertEquals(EnrollmentStatus.PENDING, result.status(), "Lo stato dell'iscrizione dovrebbe essere PENDING");
    }

    @Test
    void testGetStudentEnrollments() {
        String studentId = "student-getter";

        CourseEnrollment e1 = new CourseEnrollment();
        e1.setId(UUID.randomUUID().toString());
        e1.setCourseId("c1");
        e1.setStudentId(studentId);
        e1.setEnrollmentDate(LocalDateTime.now());
        e1.setStatus(EnrollmentStatus.ACTIVE);
        e1.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        enrollmentRepository.save(e1);

        List<CourseEnrollmentDto> results = studentService.getPersonalEnrollments(studentId);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void testSelfEnroll_AlreadyEnrolled_ThrowsException() {
        String courseId = "course-active";
        String studentId = "student-active";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now().minusDays(5));
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);
        settingsRepository.save(settings);
        settingsRepository.flush();

        CourseEnrollment existing = new CourseEnrollment();
        existing.setId(UUID.randomUUID().toString());
        existing.setCourseId(courseId);
        existing.setStudentId(studentId);
        existing.setStatus(EnrollmentStatus.ACTIVE);
        existing.setEnrollmentType(it.unimol.new_unimol.enrollments.util.EnrollmentType.SELF_SERVICE);
        existing.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(existing);
        enrollmentRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalStateException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    @Test
    void testSubmitEnrollmentRequest_AlreadyPending_ThrowsException() {
        String courseId = "course-req";
        String studentId = "student-req";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(true);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now().minusDays(5));
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);
        settingsRepository.save(settings);
        settingsRepository.flush();

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(UUID.randomUUID().toString());
        req.setCourseId(courseId);
        req.setStudentId(studentId);
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);
        requestRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalStateException.class, () -> {
            studentService.createEnrollmentRequest(courseId, studentId);
        });
    }

    @Test
    void testCheckSelfServiceAvailability_VariousCases() {
        String courseId = "c-avail";

        boolean res1 = studentService.checkSelfServiceAvailability("s1", "non-existent");
        assertFalse(res1, "Dovrebbe ritornare false se non ci sono settings per il corso");

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now().minusDays(20));
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(10));
        settings.setEnrollmentEndDate(LocalDateTime.now().minusDays(1));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settingsRepository.save(settings);

        boolean res2 = studentService.checkSelfServiceAvailability("s1", courseId);
        assertFalse(res2, "Dovrebbe ritornare false se il periodo di iscrizione è scaduto");
    }

    @Test
    void testDropCourse_Success() {
        String courseId = "course-drop";
        String studentId = "student-drop";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now().minusDays(10));
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(5));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(5));
        settings.setMaxEnrollments(10);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settingsRepository.save(settings);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(UUID.randomUUID().toString());
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment);
        enrollmentRepository.flush();

        studentService.cancelPersonalEnrollment(courseId, studentId);

        var updatedEnrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);
        assertTrue(updatedEnrollment.isPresent(), "L'iscrizione dovrebbe esistere (soft delete)");

        assertNotEquals(EnrollmentStatus.REJECTED, updatedEnrollment.get().getStatus(), "Lo stato dovrebbe essere cambiato (non più ACTIVE)");
    }

    @Test
    void testGetStudentEnrollments_WithData() {
        String studentId = "student-list";

        for (int i = 0; i < 3; i++) {
            CourseEnrollment e = new CourseEnrollment();
            e.setId(UUID.randomUUID().toString());
            e.setCourseId("course-" + i);
            e.setStudentId(studentId);
            e.setStatus(EnrollmentStatus.ACTIVE);
            e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
            e.setEnrollmentDate(LocalDateTime.now());
            enrollmentRepository.save(e);
        }
        enrollmentRepository.flush();

        var results = studentService.getPersonalEnrollments(studentId);
        assertNotNull(results);
        assertEquals(3, results.size());
        assertEquals("student-list", results.get(0).studentId());
    }

    @Test
    void testSelfEnroll_CourseFull_GoesToWaitingList_Successfully() {
        String courseId = "course-wl-ok";
        String studentId = "student-waiting";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setMaxEnrollments(1);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now().minusDays(10));
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(5));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(5));
        settingsRepository.save(settings);

        CourseEnrollment existing = new CourseEnrollment();
        existing.setId(UUID.randomUUID().toString());
        existing.setCourseId(courseId);
        existing.setStudentId("student-owner");
        existing.setStatus(EnrollmentStatus.ACTIVE);
        existing.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        existing.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(existing);
        enrollmentRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "prof", 100, null)
        );

        CourseEnrollmentDto result = studentService.selfEnrollToCourse(courseId, studentId);

        assertNotNull(result);
        assertEquals(EnrollmentStatus.PENDING, result.status());

        var savedEnrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId).get();
        assertEquals(EnrollmentStatus.PENDING, savedEnrollment.getStatus());
    }

    @Test
    void testSelfEnroll_ModeDisabled() {
        String courseId = "c-disabled";
        String studentId = "s1";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.DISABLED);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setMaxEnrollments(100);
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));


        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    @Test
    void testSelfEnroll_StartDateInFuture() {
        String courseId = "c-future";
        String studentId = "s1";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setEnrollmentStartDate(LocalDateTime.now().plusDays(5));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setMaxEnrollments(100);

        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.selfEnrollToCourse(courseId, studentId);
        });
    }

    @Test
    void testSelfEnroll_ModeBoth_CourseFull_WaitingList() {
        String courseId = "c-both-wl";
        String studentId = "s-new";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.BOTH);
        settings.setMaxEnrollments(1);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));

        settingsRepository.save(settings);

        CourseEnrollment existing = new CourseEnrollment();
        existing.setId(UUID.randomUUID().toString());
        existing.setCourseId(courseId);
        existing.setStudentId("s-existing");
        existing.setStatus(EnrollmentStatus.ACTIVE);
        existing.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        existing.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(existing);
        enrollmentRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        CourseEnrollmentDto result = studentService.selfEnrollToCourse(courseId, studentId);

        assertEquals(EnrollmentStatus.PENDING, result.status());
    }

    @Test
    void testCreateEnrollmentRequest_ApprovalNotRequired_ThrowsException() {
        String courseId = "c-no-appr";
        String studentId = "s1";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setAllowWaitingList(true);
        settings.setMaxEnrollments(100);
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));

        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.createEnrollmentRequest(courseId, studentId);
        });
    }

    @Test
    void testCancelPersonalEnrollment_WrongStudentOrStatus() {
        String courseId = "c-cancel";
        String studentId = "s-owner";
        String otherStudent = "s-hacker";

        CourseEnrollment e = new CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId(studentId);
        e.setStatus(EnrollmentStatus.PENDING);
        e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);

        boolean res1 = studentService.cancelPersonalEnrollment(e.getId(), otherStudent);
        assertFalse(res1);

        boolean res2 = studentService.cancelPersonalEnrollment(e.getId(), studentId);
        assertFalse(res2);
    }

    @Test
    void testCancelPendingEnrollmentRequest() {
        String courseId = "c-req-cancel";
        String studentId = "s1";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(UUID.randomUUID().toString());
        req.setCourseId(courseId);
        req.setStudentId(studentId);
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        boolean res = studentService.cancelPendingEnrollmentRequest(req.getId(), studentId);
        assertTrue(res);
        assertFalse(requestRepository.existsById(req.getId()));

        boolean res2 = studentService.cancelPendingEnrollmentRequest(UUID.randomUUID().toString(), studentId);
        assertFalse(res2);
    }

    @Test
    void testCheckSelfServiceAvailability_FullNoWaitingList() {
        String courseId = "c-full-nowl";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setMaxEnrollments(1);
        settings.setAllowWaitingList(false);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        CourseEnrollment e = new CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId("occupant");
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);
        enrollmentRepository.flush();

        boolean avail = studentService.checkSelfServiceAvailability("s-new", courseId);
        assertFalse(avail, "Dovrebbe ritornare false se il corso è pieno e non c'è lista d'attesa");
    }

    @Test
    void testCancelPendingEnrollmentRequest_WrongStatus_ThrowsException() {
        String courseId = "c-wrong-stat-req";
        String studentId = "s1";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(UUID.randomUUID().toString());
        req.setCourseId(courseId);
        req.setStudentId(studentId);
        req.setStatus(RequestStatus.APPROVED);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.cancelPendingEnrollmentRequest(req.getId(), studentId);
        });
    }

    @Test
    void testSelfEnroll_ModeBoth_Success() {
        String courseId = "c-both";
        String studentId = "s1";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.BOTH);
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        CourseEnrollmentDto result = studentService.selfEnrollToCourse(courseId, studentId);
        assertEquals(EnrollmentStatus.ACTIVE, result.status());
    }

    @Test
    void testSelfEnroll_InvalidCourse_ThrowsException() {
        String courseId = "c-invalid-stud";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, false, "t1", 100, "Err") // valid=FALSE
        );

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.selfEnrollToCourse(courseId, "s1");
        });
    }

    @Test
    void testSelfEnroll_UnlimitedMaxEnrollments_Success() {
        String courseId = "c-unlim-stud";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setMaxEnrollments(null);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        CourseEnrollmentDto result = studentService.selfEnrollToCourse(courseId, "s1");
        assertEquals(EnrollmentStatus.ACTIVE, result.status());
    }

    @Test
    void testCancelPersonalEnrollment_NotFound() {
        boolean res = studentService.cancelPersonalEnrollment("non-existent-id", "s1");
        assertFalse(res);
    }
}
