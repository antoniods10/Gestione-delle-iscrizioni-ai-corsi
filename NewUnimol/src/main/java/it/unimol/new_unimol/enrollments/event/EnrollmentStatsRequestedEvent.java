package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;

public class EnrollmentStatsRequestedEvent implements Serializable {
    private String requestId;
    private String courseId;
    private String teacherId;
    private String statsType;

    public EnrollmentStatsRequestedEvent() {}

    public EnrollmentStatsRequestedEvent(String requestId, String courseId,
                                         String teacherId, String statsType) {
        this.requestId = requestId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.statsType = statsType;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getStatsType() { return statsType; }
    public void setStatsType(String statsType) { this.statsType = statsType; }

}
