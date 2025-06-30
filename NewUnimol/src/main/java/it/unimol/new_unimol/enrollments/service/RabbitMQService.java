package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.config.RabbitMQConfig;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationRequestDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationResponseDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.EnrollmentEventDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.EnrollmentNotificationDto;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.EntityResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * Valida un corso attraverso il microservizio Corsi
     */
    public CourseValidationResponseDto validateCourse(String courseId) {
        String correlationId = UUID.randomUUID().toString();
        CourseValidationRequestDto request = new CourseValidationRequestDto(courseId, correlationId);

        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingResponses.put(correlationId, future);

        try{
            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getMicroserviciesExchange(),
                    rabbitMQConfig.getCourseValidationRoutingKey(),
                    request,
                    message -> {
                        MessageProperties props = message.getMessageProperties();
                        props.setCorrelationId(correlationId);
                        props.setReplyTo(rabbitMQConfig.getCourseValidationResponseQueue());
                        return message;
                    }
            );

            Object response = future.get(5, TimeUnit.SECONDS);
            return (CourseValidationResponseDto) response;

        } catch (Exception e) {
            pendingResponses.remove(correlationId);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Errore nella validazione del corso: " + e.getMessage(), e);
        }
    }

    /**
     * Invia una notifica di iscrizione
     */
    public void sendEnrollmentNotification (String studentId, String courseId, String courseName,
                                            String notificationType, String message, Map<String, String> additionalData) {
        try {
            EnrollmentNotificationDto notification = new EnrollmentNotificationDto(
                    studentId, courseId, courseName, notificationType, message, LocalDateTime.now(), additionalData
            );

            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getMicroserviciesExchange(),
                    rabbitMQConfig.getNotificationRoutingKey(),
                    notification
            );
        } catch (Exception e) {
            System.err.println("Errore nell'invio della notifica: " + e.getMessage());
        }
    }

    /**
     * Pubblica un evento di iscrizione per altri microservizi
     */
    public void publishEnrollmentEvent(String eventType, String enrollmentId, String studentId,
                                       String courseId, String teacherId, String details) {
        try {
            EnrollmentEventDto event = new EnrollmentEventDto(
                    eventType,
                    enrollmentId,
                    studentId,
                    courseId,
                    teacherId,
                    LocalDateTime.now(),
                    details
            );

            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getEnrollmentExchange(),
                    "enrollment.event." + eventType.toLowerCase(),
                    event
            );
        } catch (Exception e) {
            System.err.println("Errore nella pubblicazione dell'evento: " + e.getMessage());
        }
    }

    /**
     * Gestire le risposte di validazione corso
     */
    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "#{rabbitMQConfig.courseValidationResponseQueue}")
    public void handleCourseValidationResponse(CourseValidationResponseDto response) {
        CompletableFuture<Object> future = pendingResponses.remove(response.correlationId());
        if(future != null) {
            future.complete(response);
        }
    }
}
