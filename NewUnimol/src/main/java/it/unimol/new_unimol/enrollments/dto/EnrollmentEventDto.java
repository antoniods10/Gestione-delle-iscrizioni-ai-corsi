package it.unimol.new_unimol.enrollments.dto;

import java.time.LocalDateTime;

public record EnrollmentEventDto(
        String eventType,
        String enrollmentId,
        String studentId,
        String courseId,
        String teacherId,
        LocalDateTime timpestamp,
        String details
) {
}
