package it.unimol.new_unimol.enrollments.stub.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CourseDeactivatedEvent implements Serializable {
    private String courseId;
    private String deactivatedBy;
    private LocalDateTime timestamp;

    public CourseDeactivatedEvent() {}

    public CourseDeactivatedEvent(String courseId, String deactivatedBy, LocalDateTime timestamp) {
        this.courseId = courseId;
        this.deactivatedBy = deactivatedBy;
        this.timestamp = timestamp;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getDeactivatedBy() { return deactivatedBy; }
    public void setDeactivatedBy(String deactivatedBy) { this.deactivatedBy = deactivatedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
