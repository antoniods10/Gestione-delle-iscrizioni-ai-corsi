package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.event.EnrollmentStatsGeneratedEvent;
import it.unimol.new_unimol.enrollments.event.EnrollmentStatsRequestedEvent;
import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentRepository;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentSettingsRepository;
import it.unimol.new_unimol.enrollments.repository.EnrollmentRequestRepository;
import it.unimol.new_unimol.enrollments.stub.event.CourseDeactivatedEvent;
import it.unimol.new_unimol.enrollments.stub.event.CourseDeletedEvent;
import it.unimol.new_unimol.enrollments.stub.event.TeacherChangedEvent;
import it.unimol.new_unimol.enrollments.stub.event.UserDeletedEvent;
import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import it.unimol.new_unimol.enrollments.util.EnrollmentType;
import it.unimol.new_unimol.enrollments.util.RequestStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventListenerService {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @Autowired
    private EventPublisherService eventPublisherService;

    /**
     * Ascolta eventi di cancellazione corsi dal microservizio corsi
     */
    @RabbitListener(queues = "${rabbitmq.queue.course.deleted}")
    public void handleCourseDeleted(CourseDeletedEvent event) {
        logger.info("[RabbitMQ] Ricevuto CourseDeletedEvent per corso: {}", event.getCourseId());

        try {
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseId(event.getCourseId());
            int enrollmentCount = enrollments.size();

            for (CourseEnrollment enrollment : enrollments) {
                eventPublisherService.publishEnrollmentDeleted(
                        enrollment.getId(),
                        enrollment.getCourseId(),
                        enrollment.getStudentId(),
                        "SYSTEM - Course Deleted"
                );
            }

            enrollmentRepository.deleteAll(enrollments);
            logger.info("Eliminate {} iscrizioni per il corso cancellato: {}", enrollmentCount,event.getCourseId());

            List<EnrollmentRequest> requests = requestRepository.findByCourseId(event.getCourseId());
            requestRepository.deleteAll(requests);
            logger.info("Eliminate {} richieste pendenti per il corso: {}", requests.size(), event.getCourseId());
        } catch (Exception e) {
            logger.error("Errore nel processare CourseDeletedEvent per corso: {}", event.getCourseId(), e);
        }
    }

    /**
     * Ascolta eventi di disattivazione corsi
     */
    @RabbitListener(queues = "${rabbitmq.queue.course.deactivated}")
    public void handleCourseDeactivated(CourseDeactivatedEvent event) {
        logger.info("[RabbitMQ] Ricevuto CourseDeactivatedEvent per corso: {}", event.getCourseId());

        try {
            settingsRepository.findByCourseId(event.getCourseId())
                    .ifPresent(settings -> {
                        settings.setEnrollmentEndDate(LocalDateTime.now());
                        settingsRepository.save(settings);
                        logger.info("Bloccate nuove iscrizioni per corso disattivato: {}", event.getCourseId());
                    });
            List<EnrollmentRequest> pendingRequests = requestRepository.findByCourseIdAndStatus(event.getCourseId(), RequestStatus.PENDING);

            for(EnrollmentRequest request : pendingRequests) {
                request.setStatus(RequestStatus.REJECTED);
                request.setRejectReason("Corso Disattivato");
                request.setProcessedBy("SYSTEM");
                request.setProcessedDate(LocalDateTime.now());
                requestRepository.save(request);

                eventPublisherService.publishEnrollmentRejected(
                        request.getId(),
                        request.getCourseId(),
                        request.getStudentId(),
                        "SYSTEM",
                        "Corso Disattivato"
                );
            }

            logger.info("Rifiutate {} richieste pendenti per corso disattivato: {}", pendingRequests.size(), event.getCourseId());
        } catch (Exception e) {
            logger.error("Errore nel processare CourseDeactivatedEvent per corso: {}", event.getCourseId(), e);
        }
    }

    /**
     * Ascolta eventi di cancellazione utenti dal microservizio Utenti
     */
    @RabbitListener(queues = "${rabbitmq.queue.user.deleted}")
    public void handleUserDeleted(UserDeletedEvent event) {
        logger.info("[RabbitMQ] Ricevuto UserDeletedEvent per utente: {} con ruolo: {}",
                event.getUserId(), event.getUserRole());

        try {
            if ("STUDENT".equals(event.getUserRole())) {
                List<CourseEnrollment> enrollments = enrollmentRepository.findByStudentId(event.getUserId());

                for (CourseEnrollment enrollment : enrollments) {
                    eventPublisherService.publishEnrollmentDeleted(
                            enrollment.getId(),
                            enrollment.getCourseId(),
                            enrollment.getStudentId(),
                            "SYSTEM - User Deleted"
                    );
                }

                enrollmentRepository.deleteAll(enrollments);
                logger.info("Eliminate {} iscrizioni per lo studente eliminato: {}",
                        enrollments.size(), event.getUserId());

                List<EnrollmentRequest> requests = requestRepository.findByStudentId(event.getUserId());
                requestRepository.deleteAll(requests);
                logger.info("Eliminate {} richieste per lo studente: {}", requests.size(), event.getUserId());
            }
        } catch (Exception e) {
            logger.error("Errore nel processare UserDeletedEvent per utente: {}", event.getUserId(), e);
        }
    }

    /**
     * Ascolta eventi di cambio docente per un corso
     */
    @RabbitListener(queues = "${rabbitmq.queue.course.teacher.changed}")
    public void handleTeacherChanged(TeacherChangedEvent event) {
        logger.info("[RabbitMQ] Ricevuto TeacherChangedEvent per corso: {} - vecchio docente: {}, nuovo docente: {}",
                event.getCourseId(), event.getOldTeacherId(), event.getNewTeacherId());

        try {
            List<CourseEnrollment> manualEnrollments = enrollmentRepository.findByCourseId(event.getCourseId())
                    .stream()
                    .filter(e -> e.getEnrollmentType() == EnrollmentType.MANUAL_BY_TEACHER)
                    .filter(e -> e.getTheacherId() != null && e.getTheacherId().equals(event.getOldTeacherId()))
                    .collect(Collectors.toList());

            for (CourseEnrollment enrollment : manualEnrollments) {
                enrollment.setTheacherId(event.getNewTeacherId());
                enrollmentRepository.save(enrollment);
            }

            logger.info("Aggiornate {} iscrizioni manuali con il nuovo docente", manualEnrollments.size());

            // Pubblica evento di notifica per informare gli studenti del cambio docente
            if (!manualEnrollments.isEmpty()) {
                for (CourseEnrollment enrollment : manualEnrollments) {
                    eventPublisherService.publishEnrollmentUpdated(
                            enrollment.getId(),
                            enrollment.getCourseId(),
                            enrollment.getStudentId(),
                            "TEACHER_CHANGED",
                            "Il docente del corso è cambiato"
                    );
                }
            }

        } catch (Exception e) {
            logger.error("Errore nel processare TeacherChangedEvent per corso: {}", event.getCourseId(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.enrollment.stats.requested}")
    public void handleStatsRequested(EnrollmentStatsRequestedEvent event) {
        logger.info("[RabbitMQ] Ricevuto EnrollmentStatsRequestedEvent: requestId={}, courseId={}, type={}",
                event.getRequestId(), event.getCourseId(), event.getStatsType());

        try {
            // Calcola statistiche
            int totalEnrollments = enrollmentRepository.findByCourseId(event.getCourseId()).size();
            int activeEnrollments = enrollmentRepository.countByCourseIdAndStatus(event.getCourseId(), EnrollmentStatus.ACTIVE);
            int pendingRequests = requestRepository.findByCourseIdAndStatus(event.getCourseId(), RequestStatus.PENDING).size();

            // Calcola posti disponibili
            Integer availableSlots = null;
            Optional<CourseEnrollmentSettings> settingOptional = settingsRepository.findByCourseId(event.getCourseId());
            if(settingOptional.isPresent()) {
                CourseEnrollmentSettings settings = settingOptional.get();
                if(settings.getMaxEnrollments() != null) {
                    int available = settings.getMaxEnrollments() - activeEnrollments;
                    availableSlots = Math.max(0, available);
                }
            }

            // Pubblica le statistiche
            EnrollmentStatsGeneratedEvent statsEvent = new EnrollmentStatsGeneratedEvent(
                    event.getRequestId(),
                    event.getCourseId(),
                    totalEnrollments,
                    activeEnrollments,
                    pendingRequests,
                    availableSlots,
                    LocalDateTime.now()
            );

            eventPublisherService.publishStatsGenerated(statsEvent);
            logger.info("[RabbitMQ] Statistiche pubblicate per requestId={}", event.getRequestId());

        } catch (Exception e) {
            logger.error("[RabbitMQ] Errore nel processare EnrollmentStatsRequestedEvent: {}",
                    event.getRequestId(), e);
        }
    }

}
