package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentApprovedEvent implements Serializable {
    private String enrollmentId;
    private String requestId;
    private String courseId;
    private String studentId;
    private String approvedBy;
    private LocalDateTime approvalDate;

    public EnrollmentApprovedEvent() {}

    public EnrollmentApprovedEvent(String enrollmentId, String requestId, String courseId,
                                   String studentId, String approvedBy, LocalDateTime approvalDate) {
        this.enrollmentId = enrollmentId;
        this.requestId = requestId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate;
    }

    public String getEnrollmentId() { return this.enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getRequestId() { return this.requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCourseId() { return this.courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return this.studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getApprovedBy() { return this.approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovalDate() { return this.approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }
}
