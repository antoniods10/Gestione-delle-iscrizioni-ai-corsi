package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentRequestSubmittedEvent implements Serializable {
    private String requestId;
    private String courseId;
    private String studentId;
    private LocalDateTime requestDate;

    public EnrollmentRequestSubmittedEvent() {}

    public EnrollmentRequestSubmittedEvent(String requestId, String courseId,
                                           String studentId, LocalDateTime requestDate) {
        this.requestId = requestId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.requestDate = requestDate;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

}
