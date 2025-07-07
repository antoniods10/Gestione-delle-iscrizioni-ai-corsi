package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentToCourseEnrollmentDtoConverter;
import it.unimol.new_unimol.enrollments.dto.converter.EnrollmentRequestToEnrollmentRequestDtoConverter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherService {
    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private EventPublisherService eventPublisherService;

    @Autowired
    private CourseEnrollmentToCourseEnrollmentDtoConverter enrollmentToDto;

    @Autowired
    private EnrollmentRequestToEnrollmentRequestDtoConverter requestToDto;

    /**
     * Iscrive manualmente uno studente ad un corso
     */
    public CourseEnrollmentDto manualEnrollStudent(String courseId, String studentId, String teacherId, String notes) {
        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        if(!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }

        if(enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalStateException("Lo studente è già iscritto a questo corso");
        }

        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);
        if(settings != null) {
            if (settings.getEnrollmentMode() == EnrollmentMode.SELF_SERVICE) {
                throw new IllegalArgumentException("Questo corso accetta solo iscrizioni self-service");
            }

            if (settings.getMaxEnrollments() != null) {
                int currentEnrollments = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
                if (currentEnrollments >= settings.getMaxEnrollments()) {
                    throw new IllegalStateException("Raggiunto il limite massimo di iscrizioni per questo corso");
                }
            }
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(UUID.randomUUID().toString());
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setTheacherId(teacherId);
        enrollment.setEnrollmentType(EnrollmentType.MANUAL_BY_TEACHER);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setApprovedDate(LocalDateTime.now());
        enrollment.setNotes(notes != null ? notes : "Iscrizione manuale da docente");

        CourseEnrollment saved = enrollmentRepository.save(enrollment);

        eventPublisherService.publishEnrollmentCreated(
                saved.getId(),
                courseId,
                studentId,
                EnrollmentType.MANUAL_BY_TEACHER.toString(),
                EnrollmentStatus.ACTIVE.toString(),
                saved.getEnrollmentDate(),
                teacherId
        );

        rabbitMQService.sendEnrollmentNotification(
                studentId,
                courseId,
                courseValidation.courseName(),
                "ENROLLMENT_CREATED_BY_TEACHER",
                "Sei stato iscritto al corso " + courseValidation.courseName() + "dal docente",
                Map.of("courseId", courseId, "teacherId", teacherId)
        );

        return enrollmentToDto.convert(saved);
    }

    /**
     * Iscrive manualmente più studenti ad un corso
     */
    public Map<String, Object> bulkManualEnrollStudents(String courseId, List<String> studentIds, String teacherId) {
        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        if(!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }
        List<CourseEnrollmentDto> successfulEnrollments = new ArrayList<>();
        List<Map<String, String>> failedEnrollments = new ArrayList<>();

        for(String studentId : studentIds) {
            try {
                CourseEnrollmentDto enrollment = manualEnrollStudent(courseId, studentId, teacherId, "Iscrizione multipla da docente");
                successfulEnrollments.add(enrollment);
            } catch (Exception e) {
                failedEnrollments.add(Map.of("studentId", studentId, "error", e.getMessage()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successful", successfulEnrollments);
        result.put("failed", failedEnrollments);
        result.put("totalProcessed", studentIds.size());
        result.put("successCount", successfulEnrollments.size());
        result.put("failureCount", failedEnrollments.size());

        return result;
    }

    /**
     * Restituisce le iscrizioni per un corso del docente
     */
    public List<CourseEnrollmentDto> getOwnCourseEnrollments(String courseId, String teacherId) {
        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        if(!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }

        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollmentToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce le richieste pendenti per un corso del docente
     */
    public List<EnrollmentRequestDto> getOwnCoursePendingRequest(String courseId, String teacherId) {
        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        if(!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }

        return requestRepository.findByCourseIdAndStatus(courseId, RequestStatus.PENDING).stream()
                .map(requestToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Approva una richiesta di iscrizione
     */
    public EnrollmentRequestDto approveEnrollmentRequest(String requestId, String teacherId) {
        EnrollmentRequest request = requestRepository.findById(requestId).orElse(null);

        if(request == null) {
            throw new IllegalArgumentException("Richiesta non trovata: " + requestId);
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(request.getCourseId());
        if(!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("La richiesta non è in stato PENDING");
        }

        if(enrollmentRepository.existsByCourseIdAndStudentId(request.getCourseId(), request.getStudentId())) {
            throw new IllegalStateException("Lo studente è già iscritto a questo corso");
        }

        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(request.getCourseId()).orElse(null);

        if(settings != null && settings.getMaxEnrollments() != null) {
            int currentEnrollments = enrollmentRepository.countByCourseIdAndStatus(request.getCourseId(), EnrollmentStatus.ACTIVE);
            if(currentEnrollments >= settings.getMaxEnrollments()) {
                throw new IllegalStateException("Raggiunto il limite massimo di iscrizioni per questo corso");
            }
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedBy(teacherId);
        request.setProcessedDate(LocalDateTime.now());

        EnrollmentRequest updatedRequest = requestRepository.save(request);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(UUID.randomUUID().toString());
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStudentId(request.getStudentId());
        enrollment.setTheacherId(teacherId);
        enrollment.setEnrollmentType(EnrollmentType.MANUAL_BY_TEACHER);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setApprovedDate(LocalDateTime.now());
        enrollment.setNotes("Richiesta approvata dal docente");

        enrollmentRepository.save(enrollment);

        eventPublisherService.publishEnrollmentApproved(
                enrollment.getId(),
                requestId,
                request.getCourseId(),
                request.getStudentId(),
                teacherId
        );

        rabbitMQService.sendEnrollmentNotification(
                request.getStudentId(),
                request.getCourseId(),
                courseValidation.courseName(),
                "ENROLLMENT_APPROVED_BY_TEACHER",
                "La tua richiesta di iscrizione al corso " + courseValidation.courseName() + " è stata approvata",
                Map.of("courseId", request.getCourseId(), "approvedBy", teacherId)
        );

        return requestToDto.convert(updatedRequest);
    }

    /**
     * Rifiuta una richiesta di iscrizione
     */
    public EnrollmentRequestDto rejectEnrollmentRequest(String requestId, String rejectReason, String teacherId) {
        EnrollmentRequest request = requestRepository.findById(requestId).orElse(null);

        if(request == null) {
            throw new IllegalArgumentException("Richiesta non trovata: " + requestId);
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(request.getCourseId());
        if (!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("La richiesta non è in stato PENDING");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectReason(rejectReason);
        request.setProcessedBy(teacherId);
        request.setProcessedDate(LocalDateTime.now());

        EnrollmentRequest updatedRequest = requestRepository.save(request);

        eventPublisherService.publishEnrollmentRejected(
                requestId,
                request.getCourseId(),
                request.getStudentId(),
                teacherId,
                rejectReason
        );

        rabbitMQService.sendEnrollmentNotification(
                request.getStudentId(),
                request.getCourseId(),
                courseValidation.courseName(),
                "ENROLLMENT_REJECTED_BY_TEACHER",
                "La tua richiesta di iscrizione al corso " + courseValidation.courseName() + " è stata rifiutata: " + rejectReason,
                Map.of("courseId", request.getCourseId(), "rejectedBy", teacherId, "reason", rejectReason)
        );

        return requestToDto.convert(updatedRequest);
    }

    /**
     * Rimuove uno studente dal proprio corso
     */
    public boolean deleteEnrollmentFromOwnCourse(String enrollmentId, String teacherId, String reason) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElse(null);

        if (enrollment == null) {
            return false;
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(enrollment.getCourseId());
        if (!courseValidation.teacherId().equals(teacherId)) {
            throw new SecurityException("Non sei il docente di questo corso");
        }

        enrollmentRepository.delete(enrollment);

        eventPublisherService.publishEnrollmentDeleted(
                enrollmentId,
                enrollment.getCourseId(),
                enrollment.getStudentId(),
                teacherId
        );

        rabbitMQService.sendEnrollmentNotification(
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                courseValidation.courseName(),
                "ENROLLMENT_DELETED_BY_TEACHER",
                "Sei stato rimosso dal corso " + courseValidation.courseName() +
                        (reason != null ? ". Motivo: " + reason : ""),
                Map.of("courseId", enrollment.getCourseId(), "deletedBy", teacherId)
        );

        return true;
    }
}
