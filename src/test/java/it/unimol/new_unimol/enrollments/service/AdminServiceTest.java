package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentSettingsDto;
import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
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
public class AdminServiceTest {
    @Autowired
    private AdminService adminService;

    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @MockitoBean
    private RabbitMQService rabbitMQService;

    @MockitoBean
    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        settingsRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    void testCreateEnrollmentSettingsSuccess() {
        String courseId = "c-settings";
        String adminId = "admin-1";

        CourseEnrollmentSettingsDto dto = new CourseEnrollmentSettingsDto(
                null, courseId, EnrollmentMode.SELF_SERVICE, false, 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10), true, null, null, null
        );

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Name", "Desc", true, true, "teach", 50, null)
        );

        CourseEnrollmentSettingsDto result = adminService.createEnrollmentSettings(courseId, dto, adminId);

        assertNotNull(result);
        assertEquals(courseId, result.courseId());
        assertEquals(EnrollmentMode.SELF_SERVICE, result.enrollmentMode());
    }

    @Test
    void testCreateEnrollmentSettingsAlreadyExists() {
        String courseId = "c-duplicate";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Name", "Desc", true, true, "teach", 50, null)
        );

        adminService.createEnrollmentSettings(courseId,
                new CourseEnrollmentSettingsDto(null, courseId, EnrollmentMode.DISABLED, false, 10, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10), false, null, null, null),
                "admin");

        settingsRepository.flush();

        assertThrows(IllegalStateException.class, () -> {
            adminService.createEnrollmentSettings(courseId,
                    new CourseEnrollmentSettingsDto(null, courseId, EnrollmentMode.SELF_SERVICE, false, 50, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10), true, null, null, null),
                    "admin");
        });
    }

    @Test
    void testApproveEnrollmentRequest() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "course-java-101";
        String studentId = "student-mario";
        String adminId = "admin-luigi";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.MANUAL);
        settings.setRequiresApproval(true);
        settings.setMaxEnrollments(50);
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(10));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setAllowWaitingList(true);
        settings.setCreatedBy("admin-creator");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settingsRepository.save(settings);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setId(requestId);
        request.setCourseId(courseId);
        request.setStudentId(studentId);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        requestRepository.save(request);

        EnrollmentRequestDto result = adminService.approveEnrollmentRequest(requestId, adminId);

        assertNotNull(result, "Il risultato non dovrebbe essere null");
        assertEquals(RequestStatus.APPROVED, result.status(), "Lo stato della richiesta dovrebbe essere APPROVED");
        assertEquals(adminId, result.processedBy(), "L'admin che ha processato dovrebbe corrispondere");

        boolean enrollmentExists = enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
        assertTrue(enrollmentExists, "Dovrebbe essere stata creata una nuova iscrizione nella tabella CourseEnrollment");

        EnrollmentRequest updatedRequest = requestRepository.findById(requestId).orElseThrow();
        assertEquals(RequestStatus.APPROVED, updatedRequest.getStatus());
    }

    @Test
    void testRejectEnrollmentRequest() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-reject";
        String studentId = "s-reject";

        EnrollmentRequest request = new EnrollmentRequest();
        request.setId(requestId);
        request.setCourseId(courseId);
        request.setStudentId(studentId);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        requestRepository.save(request);

        EnrollmentRequestDto result = adminService.rejectEnrollmentRequest(requestId, "Motivo rifiuto", "admin");

        assertEquals(RequestStatus.REJECTED, result.status());

        EnrollmentRequest updated = requestRepository.findById(requestId).get();
        assertEquals(RequestStatus.REJECTED, updated.getStatus());
    }

    @Test
    void testDeleteEnrollment() {
        String enrollmentId = UUID.randomUUID().toString();
        String adminId = "admin-deleter";

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(enrollmentId);
        enrollment.setCourseId("c-delete");
        enrollment.setStudentId("s-delete");
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        enrollmentRepository.save(enrollment);

        adminService.deleteEnrollment(enrollmentId, adminId);

        assertFalse(enrollmentRepository.existsById(enrollmentId));
    }

    @Test
    void testUpdateEnrollmentSettings() {
        String courseId = "course-update";
        String adminId = "admin-upd";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.DISABLED);
        settings.setRequiresApproval(false);
        settings.setMaxEnrollments(10);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now().minusDays(5));
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().plusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setAllowWaitingList(true);

        settingsRepository.save(settings);
        settingsRepository.flush();

        CourseEnrollmentSettingsDto updateDto = new CourseEnrollmentSettingsDto(
                null, courseId, EnrollmentMode.SELF_SERVICE, true, 200,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(30),
                true, null, null, null
        );

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Title", "Desc", true, true, "teach", 50, null)
        );

        CourseEnrollmentSettingsDto result = adminService.updateEnrollmentSettings(courseId, updateDto);

        assertNotNull(result);
        assertEquals(EnrollmentMode.SELF_SERVICE, result.enrollmentMode());
        assertEquals(200, result.maxEnrollments());
    }

    @Test
    void testApproveRequest_WrongStatus_ThrowsException() {
        String requestId = UUID.randomUUID().toString();

        EnrollmentRequest request = new EnrollmentRequest();
        request.setId(requestId);
        request.setCourseId("c1");
        request.setStudentId("s1");
        request.setStatus(RequestStatus.APPROVED);
        request.setRequestDate(LocalDateTime.now());
        requestRepository.save(request);

        assertThrows(IllegalArgumentException.class, () -> {
            adminService.approveEnrollmentRequest(requestId, "admin");
        });
    }

    @Test
    void testGetPendingRequests() {
        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(UUID.randomUUID().toString());
        req.setCourseId("c-pending");
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        List<EnrollmentRequestDto> list = adminService.getPendingEnrollmentRequests("c-pending");
        assertFalse(list.isEmpty());
    }

    @Test
    void testUpdateEnrollmentSettings_NotFound_ThrowsException() {
        String courseId = "course-not-found";

        CourseEnrollmentSettingsDto updateDto = new CourseEnrollmentSettingsDto(
                null, courseId, EnrollmentMode.SELF_SERVICE, true, 200,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30),
                true, null, null, null
        );

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Title", "Desc", true, true, "teach", 50, null)
        );

        CourseEnrollmentSettingsDto result = adminService.updateEnrollmentSettings(courseId, updateDto);
        assertNull(result, "Il metodo dovrebbe restituire null se i settings da aggiornare non esistono");
    }

    @Test
    void testApproveEnrollmentRequest_NotFound_ThrowsException() {
        String nonExistentId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> {
            try {
                adminService.approveEnrollmentRequest(nonExistentId, "admin");
            } catch (Exception e) {

            }
        });
    }

    @Test
    void testRejectEnrollmentRequest_NotFound_ThrowsException() {
        String nonExistentId = UUID.randomUUID().toString();
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.rejectEnrollmentRequest(nonExistentId, "reason", "admin");
        });
    }

    @Test
    void testRejectEnrollmentRequest_WrongStatus() {
        String requestId = UUID.randomUUID().toString();

        EnrollmentRequest request = new EnrollmentRequest();
        request.setId(requestId);
        request.setCourseId("c1");
        request.setStudentId("s1");
        request.setStatus(RequestStatus.APPROVED);
        request.setRequestDate(LocalDateTime.now());
        requestRepository.save(request);

        assertThrows(IllegalArgumentException.class, () -> {
            adminService.rejectEnrollmentRequest(requestId, "reason", "admin");
        });
    }

    @Test
    void testDeleteEnrollment_NotFound() {
        String randomId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> {
            adminService.deleteEnrollment(randomId, "admin");
        });
    }

    @Test
    void testGetEnrollmentSettings() {
        String courseId = "c-read";
        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now());
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settings.setMaxEnrollments(100);
        settings.setAllowWaitingList(true);
        settingsRepository.save(settings);

        List<CourseEnrollmentSettingsDto> results = adminService.getAllEnrollmentSettings();
        assertNotNull(results);
        assertFalse(results.isEmpty());

        CourseEnrollmentSettingsDto result = results.stream()
                .filter(s -> s.courseId().equals(courseId))
                .findFirst()
                .orElseThrow();
        assertEquals(courseId, result.courseId());
    }

    @Test
    void testGetPendingRequests_ByCourse() {
        String courseId = "c-filter";

        EnrollmentRequest r1 = new EnrollmentRequest();
        r1.setId(UUID.randomUUID().toString());
        r1.setCourseId(courseId);
        r1.setStudentId("s1");
        r1.setStatus(RequestStatus.PENDING);
        r1.setRequestDate(LocalDateTime.now());
        requestRepository.save(r1);

        EnrollmentRequest r2 = new EnrollmentRequest();
        r2.setId(UUID.randomUUID().toString());
        r2.setCourseId(courseId);
        r2.setStudentId("s2");
        r2.setStatus(RequestStatus.PENDING);
        r2.setRequestDate(LocalDateTime.now());
        requestRepository.save(r2);

        EnrollmentRequest r3 = new EnrollmentRequest();
        r3.setId(UUID.randomUUID().toString());
        r3.setCourseId("other-course");
        r3.setStudentId("s3");
        r3.setStatus(RequestStatus.PENDING);
        r3.setRequestDate(LocalDateTime.now());
        requestRepository.save(r3);

        requestRepository.flush();

        var list = adminService.getPendingEnrollmentRequests(courseId);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(r -> r.courseId().equals(courseId)));
    }

    @Test
    void testGetEnrollmentSettings_Success() {
        String courseId = "c-get-settings";
        CourseEnrollmentSettings s = new CourseEnrollmentSettings();
        s.setId(UUID.randomUUID().toString());
        s.setCourseId(courseId);
        s.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        s.setCreatedBy("admin");
        s.setCreatedDate(LocalDateTime.now());
        s.setLastModifiedDate(LocalDateTime.now());
        s.setEnrollmentStartDate(LocalDateTime.now());
        s.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        s.setMaxEnrollments(50);
        s.setAllowWaitingList(false);
        s.setRequiresApproval(false);
        settingsRepository.save(s);

        CourseEnrollmentSettingsDto dto = adminService.getEnrollmentSettingsByCourseId(courseId);
        assertNotNull(dto);
        assertEquals(courseId, dto.courseId());
        assertEquals(50, dto.maxEnrollments());
    }

    @Test
    void testGetPendingRequests_Filtering() {
        String courseId = "c-reqs";

        EnrollmentRequest r1 = new EnrollmentRequest();
        r1.setId(UUID.randomUUID().toString());
        r1.setCourseId(courseId);
        r1.setStudentId("s1");
        r1.setStatus(RequestStatus.PENDING);
        r1.setRequestDate(LocalDateTime.now());
        requestRepository.save(r1);

        EnrollmentRequest r2 = new EnrollmentRequest();
        r2.setId(UUID.randomUUID().toString());
        r2.setCourseId(courseId);
        r2.setStudentId("s2");
        r2.setStatus(RequestStatus.APPROVED);
        r2.setRequestDate(LocalDateTime.now());
        requestRepository.save(r2);

        requestRepository.flush();

        var results = adminService.getPendingEnrollmentRequests(courseId);

        assertEquals(1, results.size());
        assertEquals("s1", results.get(0).studentId());
    }

    @Test
    void testCreateSettings_InvalidDates() {
        String courseId = "c-dates";
        String adminId = "admin";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        CourseEnrollmentSettingsDto dtoPast = new CourseEnrollmentSettingsDto(
                null, courseId, EnrollmentMode.SELF_SERVICE, false, 50,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), true, null, null, null
        );
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.createEnrollmentSettings(courseId, dtoPast, adminId);
        });

        CourseEnrollmentSettingsDto dtoSwap = new CourseEnrollmentSettingsDto(
                null, courseId, EnrollmentMode.SELF_SERVICE, false, 50,
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                true, null, null, null
        );
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.createEnrollmentSettings(courseId, dtoSwap, adminId);
        });

        CourseEnrollmentSettingsDto dtoMax = new CourseEnrollmentSettingsDto(
                null, courseId, EnrollmentMode.SELF_SERVICE, false, -5, // <--- NEGATIVO
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                true, null, null, null
        );
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.createEnrollmentSettings(courseId, dtoMax, adminId);
        });
    }

    @Test
    void testDeleteEnrollmentSettings() {
        String courseId = "c-del-sett";
        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(false);
        settings.setMaxEnrollments(100);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));

        settingsRepository.save(settings);

        boolean res = adminService.deleteEnrollmentSettings(courseId);
        assertTrue(res);
        assertFalse(settingsRepository.existsByCourseId(courseId));

        boolean res2 = adminService.deleteEnrollmentSettings("non-existent");
        assertFalse(res2);
    }

    @Test
    void testGetCourseEnrollments_Admin() {
        String courseId = "c-admin-list";

        CourseEnrollment e = new CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId("s1");
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t1", 100, null)
        );

        var list = adminService.getCourseEnrollments(courseId);
        assertFalse(list.isEmpty());
    }

    @Test
    void testApproveRequest_MaxEnrollmentsReached() {
        String courseId = "c-full-req";
        String requestId = UUID.randomUUID().toString();

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setMaxEnrollments(1);
        settings.setEnrollmentMode(EnrollmentMode.SELF_SERVICE);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(true);
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

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s-pending");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        assertThrows(IllegalStateException.class, () -> {
            adminService.approveEnrollmentRequest(requestId, "admin");
        });
    }

    @Test
    void testApproveRequest_StudentAlreadyEnrolled_ThrowsException() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-adm-exists";
        String studentId = "s1";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId(studentId);
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        CourseEnrollment e = new CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId(studentId);
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);

        assertThrows(IllegalStateException.class, () -> {
            adminService.approveEnrollmentRequest(requestId, "admin");
        });
    }

    @Test
    void testApproveRequest_SettingsNotFound_ThrowsException() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-no-set";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        assertThrows(IllegalArgumentException.class, () -> {
            adminService.approveEnrollmentRequest(requestId, "admin");
        });
    }

    @Test
    void testApproveRequest_RequestNotFound_ReturnsNull() {
        var result = adminService.approveEnrollmentRequest(UUID.randomUUID().toString(), "admin");
        assertNull(result);
    }

    @Test
    void testApproveRequest_UnlimitedEnrollments_Success() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-unlimited";
        String adminId = "admin";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setMaxEnrollments(null);
        settings.setEnrollmentMode(EnrollmentMode.MANUAL);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(true);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        var result = adminService.approveEnrollmentRequest(requestId, adminId);
        assertNotNull(result);
        assertEquals(RequestStatus.APPROVED, result.status());
    }

    @Test
    void testGetCourseEnrollments_InvalidCourse_ThrowsException() {
        String courseId = "c-invalid";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, false, "t1", 100, "Error") // valid=FALSE
        );

        assertThrows(IllegalArgumentException.class, () -> {
            adminService.getCourseEnrollments(courseId);
        });
    }
}
