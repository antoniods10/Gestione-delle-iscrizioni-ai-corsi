package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.config.RabbitMQConfig;
import it.unimol.new_unimol.enrollments.dto.corsi.CourseResponseDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationResponseDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.EnrollmentNotificationDto;
import it.unimol.new_unimol.enrollments.service.corsi.CourseServiceClient;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;


@Service
@Transactional
public class RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Value("${admin.token}")
    private String adminToken;

    public CourseValidationResponseDto validateCourse(String courseId) {
        try {
            if (!courseServiceClient.courseExists(courseId, adminToken)) {
                return new CourseValidationResponseDto(
                        courseId, null, null, false, false,null, null, "Corso non trovato"
                );
            }

            CourseResponseDto course = courseServiceClient.getCourseById(courseId, adminToken);

            return new CourseValidationResponseDto(
                    course.id(),
                    course.nome(),
                    course.descrizione(),
                    true,
                    true,
                    course.teacherId(),
                    250,
                    null
            );
        } catch (Exception e) {
            return new CourseValidationResponseDto(
                    courseId, null, null, false, false,null, null, "Errore nella validazione" + e.getMessage()
            );
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
                    rabbitMQConfig.getEnrollmentExchange(),
                    rabbitMQConfig.getEnrollmentNotificationRoutingKey(),
                    notification
            );
        } catch (Exception e) {
            System.err.println("Errore nell'invio della notifica: " + e.getMessage());
        }
    }

}
