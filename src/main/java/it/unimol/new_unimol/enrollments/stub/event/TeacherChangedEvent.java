package it.unimol.new_unimol.enrollments.stub.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TeacherChangedEvent implements Serializable {
    private String courseId;
    private String oldTeacherId;
    private String newTeacherId;
    private LocalDateTime timestamp;

    public TeacherChangedEvent() {}

    public TeacherChangedEvent(String courseId, String oldTeacherId, String newTeacherId, LocalDateTime timestamp) {
        this.courseId = courseId;
        this.oldTeacherId = oldTeacherId;
        this.newTeacherId = newTeacherId;
        this.timestamp = timestamp;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getOldTeacherId() { return oldTeacherId; }
    public void setOldTeacherId(String oldTeacherId) { this.oldTeacherId = oldTeacherId; }

    public String getNewTeacherId() { return newTeacherId; }
    public void setNewTeacherId(String newTeacherId) { newTeacherId = newTeacherId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
