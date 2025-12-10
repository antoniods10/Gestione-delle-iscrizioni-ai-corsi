package it.unimol.new_unimol.enrollments.repository;

import it.unimol.new_unimol.enrollments.model.EnrollmentRequest;
import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import it.unimol.new_unimol.enrollments.util.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, String> {

    List<EnrollmentRequest> findByCourseId(String courseId);

    List<EnrollmentRequest> findByStudentId(String studentId);

    List<EnrollmentRequest> findByCourseIdAndStatus(String courseId, RequestStatus status);

    List<EnrollmentRequest> findByStudentIdAndStatus(String studentId, RequestStatus status);

    //verifica se esiste richiesta pendente
    @Query("SELECT COUNT(er) > 0 FROM EnrollmentRequest er WHERE er.courseId = :courseId AND er.studentId = :studentId AND er.status = :status")
    boolean existsPendingRequest(@Param("courseId") String courseId,
                                 @Param("studentId") String studentId,
                                 @Param("status") RequestStatus status);

    Optional<EnrollmentRequest> findByCourseIdAndStudentIdAndStatus(String courseId, String studentId, RequestStatus status);

}
