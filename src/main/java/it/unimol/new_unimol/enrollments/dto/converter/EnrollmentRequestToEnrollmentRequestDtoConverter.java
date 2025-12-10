package it.unimol.new_unimol.enrollments.dto.converter;

import it.unimol.new_unimol.enrollments.dto.EnrollmentRequestDto;
import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentRequestToEnrollmentRequestDtoConverter implements Converter<EnrollmentRequest, EnrollmentRequestDto> {

    @Override
    public EnrollmentRequestDto convert(EnrollmentRequest enrollmentRequest) {
        if(enrollmentRequest == null) {
            return null;
        }
        return new EnrollmentRequestDto(
                enrollmentRequest.getId(),
                enrollmentRequest.getCourseId(),
                enrollmentRequest.getStudentId(),
                enrollmentRequest.getRequestDate(),
                enrollmentRequest.getStatus(),
                enrollmentRequest.getRejectReason(),
                enrollmentRequest.getProcessedBy(),
                enrollmentRequest.getProcessedDate());
    }
}
