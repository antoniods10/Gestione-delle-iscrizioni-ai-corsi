package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentCreatedEvent implements Serializable {
    private String enrollmentId;
    private String courseId;
    private String studentId;
    private String enrollmentType;
    private String status;
    private LocalDateTime enrollmentDate;
    private String createdBy;

    public EnrollmentCreatedEvent() {}

    public EnrollmentCreatedEvent(String enrollmentId, String courseId, String studentId,
                                  String enrollmentType, String status, LocalDateTime enrollmentDate,
                                  String createdBy) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.enrollmentType = enrollmentType;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.createdBy = createdBy;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getEnrollmentType() { return enrollmentType; }
    public void setEnrollmentType(String enrollmentType) { this.enrollmentType = enrollmentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
