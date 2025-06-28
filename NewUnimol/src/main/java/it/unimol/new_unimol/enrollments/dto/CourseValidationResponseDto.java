package it.unimol.new_unimol.enrollments.dto;

/**
 * DTO per la risposta di validazione corso dal microservizio Corsi
 */
public record CourseValidationResponseDto(
        String courseId,
        String courseName,
        String courseDescription,
        boolean isActive,
        boolean isValid,
        String teacherId,
        Integer maxStudents,
        String errorMessage,
        String correlationId
) {
}
