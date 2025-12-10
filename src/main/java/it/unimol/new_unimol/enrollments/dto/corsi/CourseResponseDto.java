package it.unimol.new_unimol.enrollments.dto.corsi;

public record CourseResponseDto(
        String id,
        String nome,
        String codice,
        String descrizione,
        Integer cfu,
        String teacherId
) {
}
