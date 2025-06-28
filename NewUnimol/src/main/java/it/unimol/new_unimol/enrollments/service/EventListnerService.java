//package it.unimol.new_unimol.enrollments.service;
//
//import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
//import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
//import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentRepository;
//import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentSettingsRepository;
//import jakarta.transaction.Transactional;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@Transactional
//public class EventListnerService {
//
//    private static final Logger logger = LoggerFactory.getLogger(EventListnerService.class);
//
//    @Autowired
//    private CourseEnrollmentRepository enrollmentRepository;
//
//    @Autowired
//    private CourseEnrollmentSettingsRepository settingsRepository;
//
//    @Autowired
//    private EventPublisherService eventPublisherService;
//
//    /**
//     * Ascolta eventi di cancellazione corsi dal microservizio corsi
//     */
//    @RabbitListener(queues = "${rabbitmq.queue.course.deleted}")
//    public void handleCourseDeleted(CourseDeletedEvent event) {
//        logger.info("[RabbitMQ] Ricevuto CourseDeletedEvent per corso: {}", event.getCourseId());
//
//        try {
//            List<CourseEnrollment> enorllments = enrollmentRepository.findByCourseId(event.getCourseId());
//            int enrollmentCount = enrollment.size();
//
//            for (CourseEnrollment enrollment : enorllments) {
//                eventPublisherService.publishEnrollmentDeleted(
//                        enrollment.getId(),
//                        enrollment.getCourseId(),
//                        enrollment.getStudentId(),
//                        "SYSTEM - Course Deleted"
//                );
//            }
//
//            enrollmentRepository.deleteAll(enorllments);
//            logger.info("Eliminate {} iscrizioni per il corso cancellato: {}", enrollmentCount,event.getCourseId());
//
//            List<EnrollmentRequest> requests = requestRepository.findByCourseId(event.getCourseId());
//            requestRespository.deleteAll(requests);
//            logger.info("Eliminate {} richieste pendenti per il corso: {}", requests.size(), event.getCourseId());
//        } catch (Exception e) {
//            logger.error("Errore nel processare CourseDeletedEvent per corso: {}", event.getCourseId(), e);
//        }
//    }
//
//    /**
//     * Ascolta eventi di disattivazione corsi
//     */
//    @RabbitListener(queues = "${rabbitmq.queue.course.deactivated}")
//    public void handleCourseDeactivated(CourseDeactivatedEvent event)
//}
