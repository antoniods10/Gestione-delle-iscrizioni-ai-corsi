package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.*;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentSettingsDtoToCourseEnrollmentSettingsConverter;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentSettingsToCourseEnrollmentSettingsDtoConverter;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentToCourseEnrollmentDtoConverter;
import it.unimol.new_unimol.enrollments.dto.converter.EnrollmentRequestToEnrollmentRequestDtoConverter;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationResponseDto;
import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentRepository;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentSettingsRepository;
import it.unimol.new_unimol.enrollments.repository.EnrollmentRequestRepository;
import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import it.unimol.new_unimol.enrollments.util.EnrollmentType;
import it.unimol.new_unimol.enrollments.util.RequestStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {
    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private EventPublisherService eventPublisherService;

    @Autowired
    private CourseEnrollmentSettingsToCourseEnrollmentSettingsDtoConverter settingsToDto;

    @Autowired
    private CourseEnrollmentSettingsDtoToCourseEnrollmentSettingsConverter dtoToSettings;

    @Autowired
    private CourseEnrollmentToCourseEnrollmentDtoConverter enrollmentToDto;

    @Autowired
    private EnrollmentRequestToEnrollmentRequestDtoConverter requestToDto;


    /**
     * Crea una nuova configurazione di iscrizione per un corso
     */
    public CourseEnrollmentSettingsDto createEnrollmentSettings(String courseId, CourseEnrollmentSettingsDto settingsDto, String adminId) {
        if(settingsRepository.existsByCourseId(courseId)) {
            throw new IllegalStateException("Esiste già una configurazione per il corso: " + courseId);
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        LocalDateTime now = LocalDateTime.now();

        if(settingsDto.enrollmentStartDate() != null) {
            if(settingsDto.enrollmentStartDate().isBefore(now)) {
                throw new IllegalArgumentException("La data di inizio iscrizioni deve essere successiva alla data di creazione della configurazione di iscrizione al corso");
            }
        }

        if(settingsDto.enrollmentEndDate() != null) {
            if(settingsDto.enrollmentEndDate().isBefore(now)) {
                throw new IllegalArgumentException("La data di fine iscrizioni deve essere successiva alla data di creazione della configurazione di iscrizione al corso");
            }
        }

        if(settingsDto.enrollmentStartDate() != null && settingsDto.enrollmentEndDate() != null) {
            if(settingsDto.enrollmentStartDate().isAfter(settingsDto.enrollmentEndDate())) {
                throw new IllegalArgumentException("La data di inizio iscrizioni deve essere precedente alla data di fine");
            }
        }

        if(settingsDto.maxEnrollments() != null && settingsDto.maxEnrollments() <= 0) {
            throw new IllegalArgumentException("Il numero massimo di iscrizioni deve essere maggiore di 0");
        }

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(settingsDto.enrollmentMode());
        settings.setRequiresApproval(settingsDto.requiresApproval());
        settings.setMaxEnrollments(courseValidation.maxStudents());
        settings.setEnrollmentStartDate(settingsDto.enrollmentStartDate());
        settings.setEnrollmentEndDate(settingsDto.enrollmentEndDate());
        settings.setAllowWaitingList(settingsDto.allowWaitingList());
        settings.setCreatedBy(adminId);
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());

        CourseEnrollmentSettings saved = settingsRepository.save(settings);

        rabbitMQService.sendEnrollmentNotification(
                adminId,
                courseId,
                courseValidation.courseName(),
                "ENROLLMENT_SETTINGS_CREATED",
                "Configurazione iscrizione creata per il corso" + courseValidation.courseName(),
                Map.of("courseId", courseId, "enrollmentMode", settings.getEnrollmentMode().toString())
        );

        return settingsToDto.convert(saved);
    }

    /**
     * Restituisce tutte le configurazioni di iscrizione ad un corso
     */
    public List<CourseEnrollmentSettingsDto> getAllEnrollmentSettings() {
        return settingsRepository.findAll().stream()
                .map(settingsToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Aggiorna la configurazione di iscrizione per un corso
     */
    public CourseEnrollmentSettingsDto updateEnrollmentSettings(String courseId, CourseEnrollmentSettingsDto settingsDto) {
        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);

        if(settings == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        if(settingsDto.enrollmentStartDate() != null) {
            if(settingsDto.enrollmentStartDate().isBefore(now)) {
                throw new IllegalArgumentException("La data di inizio iscrizioni deve essere successiva alla data di creazione della configurazione di iscrizione al corso");
            }
        }

        if(settingsDto.enrollmentEndDate() != null) {
            if(settingsDto.enrollmentEndDate().isBefore(now)) {
                throw new IllegalArgumentException("La data di fine iscrizioni deve essere successiva alla data di creazione della configurazione di iscrizione al corso");
            }
        }

        if(settingsDto.enrollmentStartDate() != null && settingsDto.enrollmentEndDate() != null) {
            if(settingsDto.enrollmentStartDate().isAfter(settingsDto.enrollmentEndDate())){
                throw new IllegalArgumentException("La data di inizio iscrizioni deve essere precedente alla data di fine");
            }
        }

        settings.setEnrollmentMode(settingsDto.enrollmentMode());
        settings.setRequiresApproval(settingsDto.requiresApproval());
        settings.setMaxEnrollments(settingsDto.maxEnrollments());
        settings.setEnrollmentStartDate(settingsDto.enrollmentStartDate());
        settings.setEnrollmentEndDate(settingsDto.enrollmentEndDate());
        settings.setAllowWaitingList(settingsDto.allowWaitingList());
        settings.setLastModifiedDate(LocalDateTime.now());

        CourseEnrollmentSettings updated = settingsRepository.save(settings);

        return settingsToDto.convert(updated);
    }

    /**
     * Restituisce la configurazione di iscrizione per un corso
     */
    public CourseEnrollmentSettingsDto getEnrollmentSettingsByCourseId(String courseId) {
        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);

        if(settings == null) {
            return null;
        }

        return settingsToDto.convert(settings);
    }


    /**
     * Elimina la configurazione di iscrizione per un corso
     */
    public boolean deleteEnrollmentSettings(String courseId) {
        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);

        if(settings == null) {
            return false;
        }

        settingsRepository.delete(settings);
        return true;
    }

    /**
     * Restituisce tutte le iscrizioni per un corso
     */
    public List<CourseEnrollmentDto> getCourseEnrollments(String courseId) {
        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);

        if (!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido" + courseValidation.errorMessage());
        }

        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollmentToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Elimina una specifica iscrizione
     */
    public boolean deleteEnrollment(String enrollmentId, String adminId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);

        if(enrollment == null) {
            return false;
        }

        enrollmentRepository.delete(enrollment);

        eventPublisherService.publishEnrollmentDeleted(
                enrollmentId,
                enrollment.getCourseId(),
                enrollment.getStudentId(),
                adminId
        );

        rabbitMQService.sendEnrollmentNotification(
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                null,
                "ENROLLMENT_DELETED",
                "La tua iscrizione al corso è stata cancellata dall'amministratore",
                Map.of("courseId", enrollment.getCourseId(), "deletedBy", adminId)
        );

        return true;
    }

    /**
     * Restituisce le richieste di iscrizione pendenti per un corso
     */
    public List<EnrollmentRequestDto> getPendingEnrollmentRequests(String courseId) {
        return requestRepository.findByCourseIdAndStatus(courseId, RequestStatus.PENDING).stream()
                .map(requestToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Approva una richiesta di iscrizione
     */
    public EnrollmentRequestDto approveEnrollmentRequest(String requestId, String adminId) {
        EnrollmentRequest request = requestRepository.findById(requestId).orElse(null);

        if(request == null) {
            return null;
        }

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("La richiesta non è in stato di PENDING");
        }

        if(enrollmentRepository.existsByCourseIdAndStudentId(request.getCourseId(), request.getStudentId())) {
            throw new IllegalStateException("Lo studente è già iscritto a questo corso");
        }

        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(request.getCourseId()).orElse(null);
        if(settings == null) {
            throw new IllegalArgumentException("Configurazione non trovata per il corso");
        }

        if(settings.getMaxEnrollments() != null) {
            int currentEnrollments = enrollmentRepository.countByCourseIdAndStatus(request.getCourseId(), EnrollmentStatus.ACTIVE);
            if(currentEnrollments >= settings.getMaxEnrollments()) {
                throw new IllegalStateException("Raggiunto il limite massimo di iscrizioni per questo corso");
            }
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedBy(adminId);
        request.setProcessedDate(LocalDateTime.now());

        EnrollmentRequest updatedRequest = requestRepository.save(request);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(UUID.randomUUID().toString());
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStudentId(request.getStudentId());
        enrollment.setEnrollmentType(EnrollmentType.MANUAL_BY_TEACHER);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setApprovedDate(LocalDateTime.now());
        enrollment.setNotes("Approvata da admin");

        enrollmentRepository.save(enrollment);

        eventPublisherService.publishEnrollmentApproved(
                enrollment.getId(),
                requestId,
                request.getCourseId(),
                request.getStudentId(),
                adminId
        );

        rabbitMQService.sendEnrollmentNotification(
                request.getStudentId(),
                request.getCourseId(),
                null,
                "ENROLLMENT_APPROVED",
                "La tua richiesta di iscrizione è stata approvata",
                Map.of("courseId", request.getCourseId(), "approvedBy", adminId)
        );

        return requestToDto.convert(updatedRequest);
    }

    /**
     * Rifiuta una richiesta di iscrizione
     */
    public EnrollmentRequestDto rejectEnrollmentRequest(String requestId, String rejectreason, String adminId) {
        EnrollmentRequest request = requestRepository.findById(requestId).orElse(null);

        if(request == null) {
            throw new IllegalArgumentException("Richiesta non trovata: " + requestId);
        }

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("La richiesta non è in stato PENDING");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectReason(rejectreason);
        request.setProcessedBy(adminId);
        request.setProcessedDate(LocalDateTime.now());

        EnrollmentRequest updatedRequest = requestRepository.save(request);

        eventPublisherService.publishEnrollmentRejected(
                requestId,
                request.getCourseId(),
                request.getStudentId(),
                adminId,
                rejectreason
        );

        rabbitMQService.sendEnrollmentNotification(
                request.getStudentId(),
                request.getCourseId(),
                null,
                "ENROLLMENT_REJECTED",
                "La tua richiesta di iscrizione è stata rifiutata: " + rejectreason,
                Map.of("courseId", request.getCourseId(), "rejectedBy", adminId, "reason", rejectreason)
        );

        return requestToDto.convert(updatedRequest);
    }
}
