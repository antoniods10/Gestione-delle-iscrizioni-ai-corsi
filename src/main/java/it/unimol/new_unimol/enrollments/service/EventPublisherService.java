package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.event.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    public EventPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEnrollmentCreated(String enrollmentId, String courseId, String studentId,
                                         String enrollmentType, String status, LocalDateTime enrollmentDate,
                                         String createdBy) {
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                enrollmentId, courseId, studentId, enrollmentType, status, enrollmentDate, createdBy
        );

        Map<String, Object> message = createEnrollmentMessage(event, "ENROLLMENT_CREATED");

        rabbitTemplate.convertAndSend(enrollmentExchange, "enrollment.created", message);
    }

    public void publishEnrollmentApproved(String enrollmentId, String requestId, String courseId,
                                          String studentId, String approvedBy) {
        EnrollmentApprovedEvent event = new EnrollmentApprovedEvent(
                enrollmentId, requestId, courseId, studentId, approvedBy, LocalDateTime.now()
        );
        Map<String, Object> message = createEnrollmentMessage(event, "ENROLLMENT_APPROVED");

        rabbitTemplate.convertAndSend(enrollmentExchange, "enrollment.approved", message);
    }

    public void publishEnrollmentRejected(String requestId, String courseId, String studentId,
                                          String rejectedBy, String reason) {
        EnrollmentRejectedEvent event = new EnrollmentRejectedEvent(
                requestId, courseId, studentId, rejectedBy, reason, LocalDateTime.now()
        );
        Map<String, Object> message = createEnrollmentMessage(event, "ENROLLMENT_REJECTED");

        rabbitTemplate.convertAndSend(enrollmentExchange, "enrollment.rejected", message);
    }

    public void publishEnrollmentDeleted(String enrollmentId, String courseId, String studentId,
                                         String deletedBy) {
        EnrollmentDeletedEvent event = new EnrollmentDeletedEvent(
                enrollmentId, courseId, studentId, deletedBy, LocalDateTime.now()
        );
        Map<String, Object> message = new HashMap<>();
        message.put("eventType","ENROLLMENT_DELETED");
        message.put("enrollmentId", enrollmentId);

        rabbitTemplate.convertAndSend(enrollmentExchange, "enrollment.deleted", message);
    }

    public void publishEnrollmentUpdated(String enrollmentId, String courseId, String studentId,
                                         String updateType, String updateReason) {
        EnrollmentUpdateEvent event = new EnrollmentUpdateEvent(
                enrollmentId, courseId, studentId, updateType, updateReason, LocalDateTime.now()
        );
        Map<String, Object> message = createEnrollmentMessage(event, "ENROLLMENT_UPDATED");

        rabbitTemplate.convertAndSend(enrollmentExchange, "enrollment.updated", message);
    }

    public void publishEnrollmentRequestSubmitted (String requestId, String courseId, String studentId) {
        EnrollmentRequestSubmittedEvent event = new EnrollmentRequestSubmittedEvent(
                requestId, courseId, studentId, LocalDateTime.now()
        );
        Map<String, Object> message = createEnrollmentMessage(event, "ENROLLMENT_REQUEST_SUBMITTED");

        rabbitTemplate.convertAndSend(enrollmentExchange, "enrollment.request.submitted", message);

    }

    private Map<String, Object> createEnrollmentMessage(EnrollmentCreatedEvent event, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("enrollmentId", event.getEnrollmentId());
        message.put("courseId", event.getCourseId());
        message.put("studentId", event.getStudentId());
        message.put("enrollmentType", event.getEnrollmentType());
        message.put("status", event.getStatus());
        message.put("enrollmentDate", event.getEnrollmentDate());
        message.put("createdBy", event.getCreatedBy());
        return message;
    }

    private Map<String, Object> createEnrollmentMessage(EnrollmentApprovedEvent event, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("enrollmentId", event.getEnrollmentId());
        message.put("requestId", event.getRequestId());
        message.put("courseId", event.getCourseId());
        message.put("studentId", event.getStudentId());
        message.put("approvedBy", event.getApprovedBy());
        message.put("approvalDate", event.getApprovalDate());
        return message;
    }

    private Map<String, Object> createEnrollmentMessage(EnrollmentRejectedEvent event, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("requestId", event.getRequestId());
        message.put("courseId", event.getCourseId());
        message.put("studentId", event.getStudentId());
        message.put("rejectedBy", event.getRejectedBy());
        message.put("reason", event.getReason());
        message.put("rejectionDate", event.getRejectionDate());
        return message;
    }

    private Map<String, Object> createEnrollmentMessage(EnrollmentDeletedEvent event, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("enrollmentId", event.getEnrollmentId());
        message.put("courseId", event.getCourseId());
        message.put("studentId", event.getStudentId());
        message.put("deletedBy", event.getDeletedBy());
        message.put("deletionDate", event.getDeletionDate());
        return message;
    }

    private Map<String, Object> createEnrollmentMessage(EnrollmentUpdateEvent event, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("enrollmentId", event.getEnrollmentId());
        message.put("courseId", event.getCourseId());
        message.put("updateType", event.getUpdateType());
        message.put("updateReason", event.getUpdateReason());
        message.put("timestamp", event.getTimestamp());
        return message;
    }

    private Map<String, Object> createEnrollmentMessage(EnrollmentRequestSubmittedEvent event, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("requestId", event.getRequestId());
        message.put("courseId", event.getCourseId());
        message.put("studentId", event.getStudentId());
        message.put("requestDate", event.getRequestDate());
        return message;
    }

}
