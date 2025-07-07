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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

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
     * Iscrizione self-service ad un corso
     */
    public CourseEnrollmentDto selfEnrollToCourse(String courseId, String studentId) {
        if(enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalStateException("Sei già iscritto a questo corso");
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);
        if(settings == null) {
            throw new IllegalArgumentException("Configurazione iscrizione non trovata per il corso");
        }

        if(settings.getEnrollmentMode() == EnrollmentMode.DISABLED) {
            throw new IllegalArgumentException("Le iscrizioni per questo corso sono disabilitate");
        }

        if(settings.getEnrollmentMode() == EnrollmentMode.MANUAL) {
            throw new IllegalArgumentException("Questo corso accetta solo iscrizioni da parte del docente");
        }

        LocalDateTime now = LocalDateTime.now();
        if(settings.getEnrollmentStartDate() != null && now.isBefore(settings.getEnrollmentStartDate())) {
            throw new IllegalArgumentException("Le iscrizioni non sono ancora aperte");
        }

        if(settings.getEnrollmentEndDate() != null && now.isAfter(settings.getEnrollmentEndDate())) {
            throw new IllegalArgumentException("Il periodo di iscrizione è terminato");
        }

        if(settings.getEnrollmentMode() == EnrollmentMode.BOTH && settings.getMaxEnrollments() != null) {
            int currentEnrollments = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
            if(currentEnrollments >= settings.getMaxEnrollments()) {
                if(!settings.getAllowWaitingList()) {
                    throw new IllegalStateException("Raggiunto il limite massimo di iscrizioni per questo corso");
                }
                //Se c'è la lista d'attesa procedi ma con lo status pending
                CourseEnrollment enrollment = new CourseEnrollment();
                enrollment.setId(UUID.randomUUID().toString());
                enrollment.setCourseId(courseId);
                enrollment.setStudentId(studentId);
                enrollment.setEnrollmentType(EnrollmentType.SELF_SERVICE);
                enrollment.setStatus(EnrollmentStatus.PENDING);
                enrollment.setEnrollmentDate(LocalDateTime.now());
                enrollment.setNotes("In lista d'attesa");

                CourseEnrollment saved = enrollmentRepository.save(enrollment);

                rabbitMQService.sendEnrollmentNotification(
                        studentId,
                        courseId,
                        courseValidation.courseName(),
                        "WAITING_LIST",
                        "Sei stato inserito in lista d'attesa per il corso " + courseValidation.courseName(),
                        Map.of("courseId", courseId, "enrollmentId", saved.getId())
                );

                return enrollmentToDto.convert(saved);
            }
        }

        if(settings.getEnrollmentMode() == EnrollmentMode.SELF_SERVICE && settings.getMaxEnrollments() != null) {
            int currentEnrollments = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
            if(currentEnrollments >= settings.getMaxEnrollments()) {
                if(!settings.getAllowWaitingList()) {
                    throw new IllegalStateException("Raggiunto il limite massimo di iscrizioni per questo corso");
                }
                //Se c'è la lista d'attesa procedi ma con lo status pending
                CourseEnrollment enrollment = new CourseEnrollment();
                enrollment.setId(UUID.randomUUID().toString());
                enrollment.setCourseId(courseId);
                enrollment.setStudentId(studentId);
                enrollment.setEnrollmentType(EnrollmentType.SELF_SERVICE);
                enrollment.setStatus(EnrollmentStatus.PENDING);
                enrollment.setEnrollmentDate(LocalDateTime.now());
                enrollment.setNotes("In lista d'attesa");

                CourseEnrollment saved = enrollmentRepository.save(enrollment);

                rabbitMQService.sendEnrollmentNotification(
                        studentId,
                        courseId,
                        courseValidation.courseName(),
                        "WAITING_LIST",
                        "Sei stato inserito in lista d'attesa per il corso " + courseValidation.courseName(),
                        Map.of("courseId", courseId, "enrollmentId", saved.getId())
                );

                return enrollmentToDto.convert(saved);
            }
        }

        if(settings.getRequiresApproval()){
            throw new IllegalArgumentException("Questo corso richiede approvazione. Usa l'endpoint per creare una richiesta di iscrizione");
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(UUID.randomUUID().toString());
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setEnrollmentType(EnrollmentType.SELF_SERVICE);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        CourseEnrollment saved = enrollmentRepository.save(enrollment);

        eventPublisherService.publishEnrollmentCreated(
                saved.getId(),
                courseId,
                studentId,
                EnrollmentType.SELF_SERVICE.toString(),
                EnrollmentStatus.ACTIVE.toString(),
                saved.getEnrollmentDate(),
                studentId
        );

        rabbitMQService.sendEnrollmentNotification(
                studentId,
                courseId,
                courseValidation.courseName(),
                "ENROLLMENT_CREATED",
                "TI sei iscritto con successo al corso " + courseValidation.courseName(),
                Map.of("courseId", courseId, "enrollmentId", saved.getId())
        );

        return enrollmentToDto.convert(saved);
    }

    /**
     * Verifica se il corso ha iscrizione self-service disponibile
     */
    public boolean checkSelfServiceAvailability(String studentId, String courseId) {
        if(enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            return false;
        }

        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);
        if (settings == null) {
            return false;
        }

        if(settings.getEnrollmentMode() != EnrollmentMode.SELF_SERVICE && settings.getEnrollmentMode() != EnrollmentMode.BOTH) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if(settings.getEnrollmentStartDate() != null && now.isBefore(settings.getEnrollmentStartDate())) {
            return false;
        }

        if(settings.getEnrollmentEndDate() != null && now.isAfter(settings.getEnrollmentEndDate())) {
            return false;
        }

        if(settings.getMaxEnrollments() != null && !settings.getAllowWaitingList()) {
            int currentEnrollments = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
            if(currentEnrollments >= settings.getMaxEnrollments()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Resituisce tutte le iscrizioni dello studente
     */
    public List<CourseEnrollmentDto> getPersonalEnrollments(String studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollmentToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Cancella l'iscrizione dello studente
     */
    public boolean cancelPersonalEnrollment(String enrollmentId, String studentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);

        if(enrollment == null || !enrollment.getStudentId().equals(studentId)) {
            return false;
        }

        if(!(enrollment.getStatus() == EnrollmentStatus.ACTIVE)) {
            return false;
        }

        enrollmentRepository.delete(enrollment);

        eventPublisherService.publishEnrollmentDeleted(
                enrollmentId,
                enrollment.getCourseId(),
                studentId,
                studentId
        );

        rabbitMQService.sendEnrollmentNotification(
                studentId,
                enrollment.getCourseId(),
                null,
                "ENROLLMENT_CANCELLED",
                "Hai cancellato la tua iscrizione al corso",
                Map.of("courseId", enrollment.getCourseId())
        );

        return true;
    }

    /**
     * Crea una richiesta di iscrizione per corsi che richiedono approvazione
     */
    public EnrollmentRequestDto createEnrollmentRequest(String courseId, String studentId, String requestNote) {
        if(enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalStateException("Sei già iscritto a questo corso");
        }

        if(requestRepository.existsPendingRequest(courseId, studentId, RequestStatus.PENDING)) {
            throw new IllegalStateException("Hai già una richiesta di iscrizione pendente per questo corso");
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        CourseEnrollmentSettings settings = settingsRepository.findByCourseId(courseId).orElse(null);
        if(settings == null) {
            throw new IllegalArgumentException("Configurazione iscrizione non trovata per il corso");
        }

        if(!settings.getRequiresApproval()) {
            throw new IllegalArgumentException("Questo corso non richiede approvazione. Usa l'endpoint di iscrizione diretta");
        }

        EnrollmentRequest request = new EnrollmentRequest();
        request.setId(UUID.randomUUID().toString());
        request.setCourseId(courseId);
        request.setStudentId(studentId);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        request.setRejectReason(requestNote);

        EnrollmentRequest saved = requestRepository.save(request);

        eventPublisherService.publishEnrollmentRequestSubmitted(
                saved.getId(),
                courseId,
                studentId
        );

        rabbitMQService.sendEnrollmentNotification(
                null,
                courseId,
                courseValidation.courseName(),
                "ENROLLMENT_REQUEST_CREATED",
                "Nuova richiesta di iscrizione per il corso" + courseValidation.courseName(),
                Map.of("courseId", courseId, "studentId", studentId, "requestId", saved.getId())
        );
        return requestToDto.convert(saved);
    }

    /**
     * Restituisce tutte le richieste di iscrizione dello studente
     */
    public List<EnrollmentRequestDto> getPersonalEnrollmentRequests(String studentId) {
        return requestRepository.findByStudentId(studentId).stream()
                .map(requestToDto::convert)
                .collect(Collectors.toList());
    }

    /**
     * Cancella una richiesta di iscrizione pendente
     */
    public boolean cancelPendingEnrollmentRequest(String requestId, String studentId) {
        EnrollmentRequest request = requestRepository.findById(requestId).orElse(null);

        if(request == null || !request.getStudentId().equals(studentId)) {
            return false;
        }

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Puoi cancellare solo richieste pendenti");
        }

        requestRepository.delete(request);

        rabbitMQService.sendEnrollmentNotification(
                studentId,
                request.getCourseId(),
                null,
                "ENROLLMENT_REQUESTED_CANCELLED",
                "Hai cancellato la tua richiesta di iscrizione",
                Map.of("courseId", request.getCourseId())
        );

        return true;
    }
}
