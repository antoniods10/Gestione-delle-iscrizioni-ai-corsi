package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentStatsGeneratedEvent implements Serializable {
    private String requestId;
    private String courseId;
    private Integer totalEnrollments;
    private Integer activeEnrollments;
    private Integer pendingRequests;
    private Integer availableSlots;
    private LocalDateTime timestamp;

    public EnrollmentStatsGeneratedEvent() {}

    public EnrollmentStatsGeneratedEvent(String requestId, String courseId, Integer totalEnrollments,
                                         Integer activeEnrollments, Integer pendingRequests,
                                         Integer availableSlots, LocalDateTime timestamp) {
        this.requestId = requestId;
        this.courseId = courseId;
        this.totalEnrollments = totalEnrollments;
        this.activeEnrollments = activeEnrollments;
        this.pendingRequests = pendingRequests;
        this.availableSlots = availableSlots;
        this.timestamp = timestamp;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public Integer getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(Integer totalEnrollments) { this.totalEnrollments = totalEnrollments; }

    public Integer getActiveEnrollments() { return activeEnrollments; }
    public void setActiveEnrollments(Integer activeEnrollments) { this.activeEnrollments = activeEnrollments; }

    public Integer getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(Integer pendingRequests) { this.pendingRequests = pendingRequests; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
