package it.unimol.new_unimol.enrollments.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnrollmentDeletedEvent implements Serializable {
    private String enrollmentId;
    private String courseId;
    private String studentId;
    private String deletedBy;
    private LocalDateTime deletionDate;

    public EnrollmentDeletedEvent() {}

    public EnrollmentDeletedEvent(String enrollmentId, String courseId, String studentId,
                                  String deletedBy, LocalDateTime deletionDate) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.deletedBy = deletedBy;
        this.deletionDate = deletionDate;
    }

    public String getEnrollmentId() { return this.enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getCourseId() { return this.courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return this.studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getDeletedBy() { return this.deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public LocalDateTime getDeletionDate() { return this.deletionDate; }
    public void setDeletionDate(LocalDateTime deletionDate) { this.deletionDate = deletionDate; }

}
