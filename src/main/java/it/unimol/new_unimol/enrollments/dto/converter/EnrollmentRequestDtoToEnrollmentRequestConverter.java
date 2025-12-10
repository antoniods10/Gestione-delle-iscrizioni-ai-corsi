package it.unimol.new_unimol.enrollments.dto.converter;

import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentRequestDtoToEnrollmentRequestConverter implements Converter<EnrollmentRequestDto, EnrollmentRequest> {

    @Override
    public EnrollmentRequest convert(EnrollmentRequestDto source) {
        if (source == null) {
            return null;
        }
        return new EnrollmentRequest(
                source.id(),
                source.courseId(),
                source.studentId(),
                source.requestDate(),
                source.status(),
                source.processedBy(),
                source.processedDate());
    }
}
