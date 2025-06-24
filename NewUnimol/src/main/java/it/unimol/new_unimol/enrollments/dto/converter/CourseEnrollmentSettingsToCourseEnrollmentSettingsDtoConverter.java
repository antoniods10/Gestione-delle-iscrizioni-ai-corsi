package it.unimol.new_unimol.enrollments.dto.converter;

import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import it.unimol.new_unimol.enrollments.dto.CourseEnrollmentSettingsDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CourseEnrollmentSettingsToCourseEnrollmentSettingsDtoConverter implements Converter<CourseEnrollmentSettings, CourseEnrollmentSettingsDto> {

    @Override
    public CourseEnrollmentSettingsDto convert(CourseEnrollmentSettings courseEnrollmentSettings) {
        if(courseEnrollmentSettings == null) {
            return null;
        }
        return new CourseEnrollmentSettingsDto(
                courseEnrollmentSettings.getId(),
                courseEnrollmentSettings.getCourseId(),
                courseEnrollmentSettings.getEnrollmentMode(),
                courseEnrollmentSettings.getRequiresApproval(),
                courseEnrollmentSettings.getMaxEnrollments(),
                courseEnrollmentSettings.getEnrollmentStartDate(),
                courseEnrollmentSettings.getEnrollmentEndDate(),
                courseEnrollmentSettings.getAllowWaitingList(),
                courseEnrollmentSettings.getCreatedBy(),
                courseEnrollmentSettings.getCreatedDate(),
                courseEnrollmentSettings.getLastModifiedDate());
    }
}
