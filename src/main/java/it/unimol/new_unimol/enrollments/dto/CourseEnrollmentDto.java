package it.unimol.new_unimol.enrollments.dto;

import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import it.unimol.new_unimol.enrollments.util.EnrollmentType;

import java.time.LocalDateTime;

public record CourseEnrollmentDto(
        String id,
        String courseId,
        String studentId,
        String teacherId,
        EnrollmentType enrollmentType,
        EnrollmentStatus status,
        LocalDateTime enrollmentDate,
        LocalDateTime approvedDate,
        String notes) {
}
