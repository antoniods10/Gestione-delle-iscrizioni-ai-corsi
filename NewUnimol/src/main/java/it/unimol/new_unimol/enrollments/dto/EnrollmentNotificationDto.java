package it.unimol.new_unimol.enrollments.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO per inviare notifiche al microservizio Notifiche
 */
public record EnrollmentNotificationDto(
        String userId,
        String courseId,
        String courseName,
        String notificationType,
        String message,
        LocalDateTime timestamp,
        Map<String, String> additionalData
) {
}
