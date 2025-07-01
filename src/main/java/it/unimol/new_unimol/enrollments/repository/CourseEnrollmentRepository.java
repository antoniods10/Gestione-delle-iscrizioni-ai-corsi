package it.unimol.new_unimol.enrollments.repository;

import it.unimol.new_unimol.enrollments.model.CourseEnrollment;
import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, String> {

    List<CourseEnrollment> findByCourseId(String courseId);

    List<CourseEnrollment> findByStudentId(String studentId);

    boolean existsByCourseIdAndStudentId(String courseId, String studentId);

    Optional<CourseEnrollment> findByCourseIdAndStudentId(String courseId, String studentId);

    //conta le iscrizioni attive per un corso
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.courseId = :courseId AND ce.status = :status")
    int countByCourseIdAndStatus(@Param("courseId") String courseId, @Param("status") EnrollmentStatus status);

    List<CourseEnrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);
}
