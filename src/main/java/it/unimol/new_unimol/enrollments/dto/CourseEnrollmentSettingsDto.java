package it.unimol.new_unimol.enrollments.dto;

import it.unimol.new_unimol.enrollments.util.EnrollmentMode;

import java.time.LocalDateTime;

public record CourseEnrollmentSettingsDto(
        String id,
        String courseId,
        EnrollmentMode enrollmentMode,
        Boolean requiresApproval,
        Integer maxEnrollments,
        LocalDateTime enrollmentStartDate,
        LocalDateTime enrollmentEndDate,
        Boolean allowWaitingList,
        String createdBy,
        LocalDateTime createdDate,
        LocalDateTime LastModifyDate) {
}
