package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.event.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsabile della pubblicazione di eventi RabbitMQ per le iscrizioni.
 */
@Service
@Transactional
public class EventPublisherService {

    public static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.enrollment}")
    private String enrollmentExchange;

    @Value("${rabbitmq.routing.enrollment.created}")
    private String enrollmentCreatedRouting;

    @Value("${rabbitmq.routing.enrollment.approved}")
    private String enrollmentApprovedRouting;

    @Value("${rabbitmq.routing.enrollment.rejected}")
    private String enrollmentRejectedRouting;

    @Value("${rabbitmq.routing.enrollment.deleted}")
    private String enrollmentDeletedRouting;

    @Value("${rabbitmq.routing.enrollment.request.submitted}")
    private String enrollmentRequestSubmittedRouting;

    @Value("${rabbitmq.routing.enrollment.updated}")
    private String enrollmentUpdatedRouting;

    @Value("${rabbitmq.routing.enrollment.stats.requested}")
    private String enrollmentStatsRequestedRouting;

    @Value("${rabbitmq.routing.enrollment.stats.generated}")
    private String enrollmentStatsGeneratedRouting;

    public EventPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEnrollmentCreated(String enrollmentId, String courseId, String studentId,
                                         String enrollmentType, String status, LocalDateTime enrollmentDate,
                                         String createdBy) {
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                enrollmentId, courseId, studentId, enrollmentType, status, enrollmentDate, createdBy
        );

        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentCreatedRouting, event);
            logger.info("Pubblicato EnrollmentCreaatedEvent per l'enrollment: {}", enrollmentId);
        }catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentCreatedEvent per l'enrollment: {}", enrollmentId, e);
        }
    }

    public void publishEnrollmentApproved(String enrollmentId, String requestId, String courseId,
                                          String studentId, String approvedBy) {
        EnrollmentApprovedEvent event = new EnrollmentApprovedEvent(
                enrollmentId, requestId, courseId, studentId, approvedBy, LocalDateTime.now()
        );

        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentApprovedRouting, event);
            logger.info("Pubblicato EnrollmentApprovedEvent per l'enrollment: {}", enrollmentId);
        }catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentApprovedEvent per l'enrollment: {}", enrollmentId, e);
        }
    }

    public void publishEnrollmentRejected(String requestId, String courseId, String studentId,
                                          String rejectedBy, String reason) {
        EnrollmentRejectedEvent event = new EnrollmentRejectedEvent(
                requestId, courseId, studentId, rejectedBy, reason, LocalDateTime.now()
        );

        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentRejectedRouting, event);
            logger.info("Pubblicato EnrollmentRejectedEvent per l'enrollment: {}", requestId);
        } catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentRejectedEvent per l'enrollment: {}", requestId, e);
        }
    }

    public void publishEnrollmentDeleted(String enrollmentId, String courseId, String studentId,
                                         String deletedBy) {
        EnrollmentDeletedEvent event = new EnrollmentDeletedEvent(
                enrollmentId, courseId, studentId, deletedBy, LocalDateTime.now()
        );

        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentDeletedRouting, event);
            logger.info("Pubblicato EnrollmentDeletedEvent per l'enrollment: {}", enrollmentId);
        } catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentDeletedEvent per l'enrollment: {} ", enrollmentId, e);
        }
    }

    public void publishEnrollmentUpdated(String enrollmentId, String courseId, String studentId,
                                         String updateType, String updateReason) {
        EnrollmentUpdateEvent event = new EnrollmentUpdateEvent(
                enrollmentId, courseId, studentId, updateType, updateReason, LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentUpdatedRouting, event);
            logger.info("Pubblicato EnrollmentUpdateEvent per l'enrollment: {}", enrollmentId);
        } catch (Exception e) {
            logger.error("Errore nel pubblicare EnrollmentUpdateEvent per l'enrollment: {}", enrollmentId, e);
        }
    }

    public void publishEnrollmentRequestSubmitted (String requestId, String courseId, String studentId) {
        EnrollmentRequestSubmittedEvent event = new EnrollmentRequestSubmittedEvent(
                requestId, courseId, studentId, LocalDateTime.now()
        );

        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentRequestSubmittedRouting, event);
            logger.info("Pubblicato EnrollmentRequestSubmitted per l'enrollment: {}", requestId);
        } catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentRequestSubmitted per l'enrollment: {}", requestId, e);
        }
    }

    public void publishStatsRequested (String requestId, String courseId, String teacherId, String statsType) {
        EnrollmentStatsRequestedEvent event = new EnrollmentStatsRequestedEvent(
                requestId, courseId, teacherId, statsType
        );

        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentStatsRequestedRouting, event);
            logger.info("Pubblicato EnrollmentStatsRequestedEvent per l'enrollment: {}", requestId);
        } catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentStatsRequestedEvent per l'enrollment: {}", requestId, e);
        }
    }

    public void publishStatsGenerated(EnrollmentStatsGeneratedEvent event) {
        try{
            rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentStatsGeneratedRouting, event);
            logger.info("Pubblicato EnrollmentStatsGeneratedEvent per l'enrollment: {}", event.getRequestId());
        } catch (Exception e){
            logger.error("Errore nel pubblicare EnrollmentStatsGeneratedEvent per l'enrollment: {}", event.getRequestId(), e);
        }
    }
}
