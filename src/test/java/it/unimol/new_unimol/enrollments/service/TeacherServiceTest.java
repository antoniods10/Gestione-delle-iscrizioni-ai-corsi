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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class TeacherServiceTest {
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @MockitoBean
    private RabbitMQService rabbitMQService;

    @MockitoBean
    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
    }

    @Test
    void testManualEnrollStudentSuccess(){
        String courseId = "c-java";
        String studentId = "s-rossi";
        String teacherId = "t-prof";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        CourseEnrollmentDto result = teacherService.manualEnrollStudent(courseId, studentId, teacherId, "Note");

        assertNotNull(result);
        assertEquals(EnrollmentType.MANUAL_BY_TEACHER, result.enrollmentType());
        assertEquals(teacherId, result.teacherId());
    }

    @Test
    void testManualEnrollStudentWrongTeacher() {
        String courseId = "c-java";
        String teacherId = "t-impostor";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "t-real", 100, null)
        );

        assertThrows(SecurityException.class, () -> {
            teacherService.manualEnrollStudent(courseId, "s-rossi", teacherId, "Note");
        });
    }

    @Test
    void testBulkManualEnrollStudents() {
        String courseId = "c-bulk";
        String teacherId = "t-prof";
        List<String> students = List.of("s1", "s2", "s3");

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        Map<String, Object> result = teacherService.bulkManualEnrollStudents(courseId, students, teacherId);

        assertNotNull(result);
        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failureCount"));
    }

    @Test
    void testGetCourseEnrollments_TeacherAuthorized() {
        String courseId = "course-teach-read";
        String teacherId = "prof-read";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        it.unimol.new_unimol.enrollments.model.CourseEnrollment e = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId("student1");
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);

        List<CourseEnrollmentDto> list = teacherService.getOwnCourseEnrollments(courseId, teacherId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void testBulkManualEnrollStudents_MixedResults() {
        String courseId = "c-bulk-mix";
        String teacherId = "prof-1";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        it.unimol.new_unimol.enrollments.model.CourseEnrollment e1 = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        e1.setId(UUID.randomUUID().toString());
        e1.setCourseId(courseId);
        e1.setStudentId("s-exists");
        e1.setStatus(EnrollmentStatus.ACTIVE);
        e1.setEnrollmentType(EnrollmentType.MANUAL_BY_TEACHER);
        e1.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e1);
        enrollmentRepository.flush();


        Map<String, Object> result = teacherService.bulkManualEnrollStudents(courseId, List.of("s-exists", "s-new"), teacherId);

        assertNotNull(result);
        assertEquals(1, result.get("successCount"));
        assertEquals(1, result.get("failureCount"));
    }

    @Test
    void testManualEnrollStudent_AlreadyEnrolled_ThrowsException() {
        String courseId = "c-man-dup";
        String teacherId = "prof-1";
        String studentId = "s-dup";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        it.unimol.new_unimol.enrollments.model.CourseEnrollment e1 = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        e1.setId(UUID.randomUUID().toString());
        e1.setCourseId(courseId);
        e1.setStudentId(studentId);
        e1.setStatus(EnrollmentStatus.ACTIVE);
        e1.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e1.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e1);

        assertThrows(IllegalStateException.class, () -> {
            teacherService.manualEnrollStudent(courseId, studentId, teacherId, "note");
        });
    }

    @Test
    void testBulkManualEnroll_EmptyList() {
        String courseId = "c-empty";
        String teacherId = "t-1";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        Map<String, Object> result = teacherService.bulkManualEnrollStudents(courseId, List.of(), teacherId);

        assertNotNull(result);
        assertEquals(0, result.get("successCount"));
        assertEquals(0, result.get("failureCount"));
    }

    @Test
    void testGetCourseEnrollments_Empty() {
        String courseId = "c-no-students";
        String teacherId = "t-1";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        var list = teacherService.getOwnCourseEnrollments(courseId, teacherId);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetCourseEnrollments_WithMultipleStudents() {
        String courseId = "c-class-list";
        String teacherId = "prof-list";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        for (int i = 1; i <= 3; i++) {
            it.unimol.new_unimol.enrollments.model.CourseEnrollment e = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
            e.setId(UUID.randomUUID().toString());
            e.setCourseId(courseId);
            e.setStudentId("s-" + i);
            e.setStatus(EnrollmentStatus.ACTIVE);
            e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
            e.setEnrollmentDate(LocalDateTime.now());
            enrollmentRepository.save(e);
        }
        enrollmentRepository.flush();

        var list = teacherService.getOwnCourseEnrollments(courseId, teacherId);

        assertEquals(3, list.size());
        assertTrue(list.stream().anyMatch(dto -> dto.studentId().equals("s-1")));
        assertTrue(list.stream().anyMatch(dto -> dto.studentId().equals("s-2")));
    }

    @Test
    void testManualEnroll_SelfServiceOnly_Throws() {
        String courseId = "c-self-only";
        String teacherId = "t1";
        String studentId = "s1";

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

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.manualEnrollStudent(courseId, studentId, teacherId, null);
        });
    }

    @Test
    void testTeacherApproveRequest_Success() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-appr";
        String teacherId = "t1";
        String studentId = "s1";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId(studentId);
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        var result = teacherService.approveEnrollmentRequest(requestId, teacherId);

        assertEquals(RequestStatus.APPROVED, result.status());
        assertEquals(teacherId, result.processedBy());
        assertTrue(enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId));
    }

    @Test
    void testTeacherRejectRequest_Success() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-rej";
        String teacherId = "t1";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        var result = teacherService.rejectEnrollmentRequest(requestId, "No", teacherId);

        assertEquals(RequestStatus.REJECTED, result.status());
    }

    @Test
    void testDeleteEnrollmentFromOwnCourse_SuccessAndFail() {
        String courseId = "c-del-own";
        String teacherId = "t1";
        String studentId = "s1";
        String enrollmentId = UUID.randomUUID().toString();

        it.unimol.new_unimol.enrollments.model.CourseEnrollment e = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        e.setId(enrollmentId);
        e.setCourseId(courseId);
        e.setStudentId(studentId);
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        boolean res = teacherService.deleteEnrollmentFromOwnCourse(enrollmentId, teacherId, "Bye");
        assertTrue(res);
        assertFalse(enrollmentRepository.existsById(enrollmentId));

        boolean res2 = teacherService.deleteEnrollmentFromOwnCourse(UUID.randomUUID().toString(), teacherId, "Bye");
        assertFalse(res2);
    }

    @Test
    void testGetOwnCoursePendingRequest() {
        String courseId = "c-pending-teach";
        String teacherId = "t1";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(UUID.randomUUID().toString());
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        var list = teacherService.getOwnCoursePendingRequest(courseId, teacherId);
        assertFalse(list.isEmpty());
    }

    @Test
    void testApproveRequest_StudentAlreadyEnrolled_ThrowsException() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-exists";
        String teacherId = "t1";
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

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        assertThrows(IllegalStateException.class, () -> {
            teacherService.approveEnrollmentRequest(requestId, teacherId);
        });
    }

    @Test
    void testApproveRequest_MaxEnrollmentsReached_ThrowsException() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-full-teach";
        String teacherId = "t1";
        String studentId = "s-new";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setMaxEnrollments(1);
        settings.setEnrollmentMode(EnrollmentMode.MANUAL);
        settings.setAllowWaitingList(true);
        settings.setRequiresApproval(true);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        it.unimol.new_unimol.enrollments.model.CourseEnrollment e = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId("s-occupant");
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.MANUAL_BY_TEACHER);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);
        enrollmentRepository.flush();

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId(studentId);
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        assertThrows(IllegalStateException.class, () -> {
            teacherService.approveEnrollmentRequest(requestId, teacherId);
        });
    }

    @Test
    void testApproveRequest_WrongStatus_ThrowsException() {
        String requestId = UUID.randomUUID().toString();
        String teacherId = "t1";
        String courseId = "c-wrong-stat";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.REJECTED);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.approveEnrollmentRequest(requestId, teacherId);
        });
    }

    @Test
    void testManualEnroll_MaxEnrollmentsReached_ThrowsException() {
        String courseId = "c-man-full";
        String teacherId = "t1";
        String studentId = "s-new";

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setMaxEnrollments(1);
        settings.setEnrollmentMode(EnrollmentMode.MANUAL);
        settings.setAllowWaitingList(false);
        settings.setRequiresApproval(false);
        settings.setCreatedBy("admin");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());
        settings.setEnrollmentStartDate(LocalDateTime.now().minusDays(1));
        settings.setEnrollmentEndDate(LocalDateTime.now().plusDays(10));
        settingsRepository.save(settings);

        it.unimol.new_unimol.enrollments.model.CourseEnrollment e = new it.unimol.new_unimol.enrollments.model.CourseEnrollment();
        e.setId(UUID.randomUUID().toString());
        e.setCourseId(courseId);
        e.setStudentId("s-occupant");
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentType(EnrollmentType.MANUAL_BY_TEACHER);
        e.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(e);
        enrollmentRepository.flush();

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        assertThrows(IllegalStateException.class, () -> {
            teacherService.manualEnrollStudent(courseId, studentId, teacherId, "notes");
        });
    }

    @Test
    void testManualEnroll_NoSettings_Success() {
        String courseId = "c-no-settings";
        String teacherId = "t1";

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, teacherId, 100, null)
        );

        CourseEnrollmentDto result = teacherService.manualEnrollStudent(courseId, "s1", teacherId, "Note");
        assertNotNull(result);
        assertEquals(EnrollmentStatus.ACTIVE, result.status());
    }

    @Test
    void testApproveRequest_Security_WrongTeacher() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-sec";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "REAL_TEACHER", 100, null)
        );

        assertThrows(SecurityException.class, () -> {
            teacherService.approveEnrollmentRequest(requestId, "HACKER_TEACHER");
        });
    }

    @Test
    void testRejectRequest_NotFound_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.rejectEnrollmentRequest("fake-id", "reason", "t1");
        });
    }

    @Test
    void testRejectRequest_Security_WrongTeacher() {
        String requestId = UUID.randomUUID().toString();
        String courseId = "c-sec-rej";

        EnrollmentRequest req = new EnrollmentRequest();
        req.setId(requestId);
        req.setCourseId(courseId);
        req.setStudentId("s1");
        req.setStatus(RequestStatus.PENDING);
        req.setRequestDate(LocalDateTime.now());
        requestRepository.save(req);

        when(rabbitMQService.validateCourse(courseId)).thenReturn(
                new CourseValidationResponseDto(courseId, "Java", "Desc", true, true, "REAL_TEACHER", 100, null)
        );

        assertThrows(SecurityException.class, () -> {
            teacherService.rejectEnrollmentRequest(requestId, "No", "HACKER_TEACHER");
        });
    }
}
