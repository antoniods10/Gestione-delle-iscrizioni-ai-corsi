package it.unimol.new_unimol.enrollments.dto;

/**
 * DTO per richiedere la validazione di un corso al microservizio Corsi
 */
public record CourseValidationRequestDto(String courseId, String correlationId) {
}
