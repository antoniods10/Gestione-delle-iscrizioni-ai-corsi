package it.unimol.new_unimol.enrollments.dto;

import it.unimol.new_unimol.enrollments.util.RequestStatus;

import java.time.LocalDateTime;

public record EnrollmentRequestDto(
        String id,
        String courseId,
        String studentId,
        LocalDateTime requestDate,
        RequestStatus status,
        String rejectReason,
        String processedBy,
        LocalDateTime processedDate) {

}
