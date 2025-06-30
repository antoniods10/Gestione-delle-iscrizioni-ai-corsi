package it.unimol.new_unimol.enrollments.stub.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CourseDeletedEvent implements Serializable {
    private String courseId;
    private String deletedBy;
    private LocalDateTime timestamp;

    public CourseDeletedEvent() {}

    public CourseDeletedEvent(String courseId, String deletedBy, LocalDateTime timestamp) {
        this.courseId = courseId;
        this.deletedBy = deletedBy;
        this.timestamp = timestamp;
    }

    public String getCourseId() {return courseId;};
    public void setCourseId(String courseId) {this.courseId = courseId;};

    public String getDeletedBy() {return deletedBy;};
    public void setDeletedBy(String deletedBy) {this.deletedBy = deletedBy;};

    public LocalDateTime getTimestamp() {return timestamp;};
    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;};
}
