package it.unimol.new_unimol.enrollments.repository;

import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseEnrollmentSettingsRepository extends JpaRepository<CourseEnrollmentSettings, String> {

    Optional<CourseEnrollmentSettings> findByCourseId(String courseId);

    boolean existsByCourseId(String courseId);
}
