package it.unimol.new_unimol.enrollments.dto.converter;

import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CourseEnrollmentToCourseEnrollmentDtoConverter implements Converter<CourseEnrollment, CourseEnrollmentDto> {

    @Override
    public CourseEnrollmentDto convert(CourseEnrollment courseEnrollment) {
        if(courseEnrollment == null) {
            return null;
        }
        return new CourseEnrollmentDto(
                courseEnrollment.getId(),
                courseEnrollment.getCourseId(),
                courseEnrollment.getStudentId(),
                courseEnrollment.getTheacherId(),
                courseEnrollment.getEnrollmentType(),
                courseEnrollment.getStatus(),
                courseEnrollment.getEnrollmentDate(),
                courseEnrollment.getApprovedDate(),
                courseEnrollment.getNotes());
    }
}
