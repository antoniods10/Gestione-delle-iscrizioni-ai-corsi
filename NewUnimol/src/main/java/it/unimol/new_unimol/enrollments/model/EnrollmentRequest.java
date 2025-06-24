package it.unimol.new_unimol.enrollments.model;

import it.unimol.new_unimol.enrollments.util.RequestStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment_request")
public class EnrollmentRequest {
    @Id
    @Column(name = "id", nullable = false, length = 300)
    private String id;

    @Column(name = "course_id", nullable = false, length = 300)
    private String courseId;

    @Column(name = "student_id", nullable = false, length = 300)
    private String studentId;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    public EnrollmentRequest() {}

    public EnrollmentRequest(String id, String courseId, String studentId, LocalDateTime requestDate, RequestStatus status, String processedBy, LocalDateTime processedDate) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.requestDate = requestDate;
        this.status = status;
        this.rejectReason = null;
        this.processedBy = processedBy;
        this.processedDate = processedDate;
    }

    public String getId() {return this.id;}
    public void setId(String id) {this.id = id;}

    public String getCourseId() {return this.courseId;}
    public void setCourseId(String courseId) {this.courseId = courseId;}

    public String getStudentId() {return this.studentId;}
    public void setStudentId(String studentId) {this.studentId = studentId;}

    public LocalDateTime getRequestDate() {return this.requestDate;}
    public void setRequestDate(LocalDateTime requestDate) {this.requestDate = requestDate;}

    public RequestStatus getStatus() {return this.status;}
    public void setStatus(RequestStatus status) {this.status = status;}

    public String getRejectReason() {return this.rejectReason;}
    public void setRejectReason(String rejectReason) {this.rejectReason = rejectReason;}

    public String getProcessedBy() {return this.processedBy;}
    public void setProcessedBy(String processedBy) {this.processedBy = processedBy;}

    public LocalDateTime getProcessedDate() {return this.processedDate;}
    public void setProcessedDate(LocalDateTime processedDate) {this.processedDate = processedDate;}

}
