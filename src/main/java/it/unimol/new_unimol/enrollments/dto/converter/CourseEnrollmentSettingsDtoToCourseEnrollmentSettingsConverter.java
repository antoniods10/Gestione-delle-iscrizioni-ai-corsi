package it.unimol.new_unimol.enrollments.dto.converter;

import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentSettingsDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CourseEnrollmentSettingsDtoToCourseEnrollmentSettingsConverter implements Converter<CourseEnrollmentSettingsDto, CourseEnrollmentSettings> {

    @Override
    public CourseEnrollmentSettings convert(CourseEnrollmentSettingsDto source) {
        if(source == null){
            return null;
        }
        return new CourseEnrollmentSettings(
                source.id(),
                source.courseId(),
                source.enrollmentMode(),
                source.requiresApproval(),
                source.maxEnrollments(),
                source.enrollmentStartDate(),
                source.enrollmentEndDate(),
                source.allowWaitingList(),
                source.createdBy(),
                source.createdDate(),
                source.LastModifyDate());
    }
}
