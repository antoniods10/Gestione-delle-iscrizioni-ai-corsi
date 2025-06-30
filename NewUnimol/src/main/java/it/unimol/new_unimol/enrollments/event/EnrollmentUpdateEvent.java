package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentUpdateEvent implements Serializable {
    private String enrollmentId;
    private String courseId;
    private String studentId;
    private String updateType;
    private String updateReason;
    private LocalDateTime timestamp;

    public EnrollmentUpdateEvent() {}

    public EnrollmentUpdateEvent(String enrollmentId, String courseId, String studentId,
                                 String updateType, String updateReason, LocalDateTime timestamp) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.updateType = updateType;
        this.updateReason = updateReason;
        this.timestamp = timestamp;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
