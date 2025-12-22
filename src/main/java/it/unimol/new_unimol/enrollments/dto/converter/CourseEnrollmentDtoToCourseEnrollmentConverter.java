package it.unimol.new_unimol.enrollments.dto.converter;

import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentDto;
import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CourseEnrollmentDtoToCourseEnrollmentConverter implements Converter<CourseEnrollmentDto, CourseEnrollment> {

    @Override
    public CourseEnrollment convert(CourseEnrollmentDto source) {
        if (source == null) {
            return null;
        }
        return new CourseEnrollment(
                source.id(),
                source.courseId(),
                source.studentId(),
                source.teacherId(),
                source.enrollmentType(),
                source.status(),
                source.enrollmentDate(),
                source.approvedDate(),
                source.notes());
    }


}
