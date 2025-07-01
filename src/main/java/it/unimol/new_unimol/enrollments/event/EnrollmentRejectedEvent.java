package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentRejectedEvent implements Serializable {
    private String requestId;
    private String courseId;
    private String studentId;
    private String rejectedBy;
    private String reason;
    private LocalDateTime rejectionDate;

    public EnrollmentRejectedEvent() {}

    public EnrollmentRejectedEvent(String requestId, String courseId, String studentId,
                                   String rejectedBy, String reason, LocalDateTime rejectionDate) {
        this.requestId = requestId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.rejectedBy = rejectedBy;
        this.reason = reason;
        this.rejectionDate = rejectionDate;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getRejectionDate() { return rejectionDate; }
    public void setRejectionDate(LocalDateTime rejectionDate) { this.rejectionDate = rejectionDate; }

}
