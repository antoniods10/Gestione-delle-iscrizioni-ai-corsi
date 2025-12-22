package it.unimol.new_unimol.enrollments.model;

import it.unimol.new_unimol.enrollments.util.EnrollmentStatus;
import it.unimol.new_unimol.enrollments.util.EnrollmentType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CourseEnrollment")
public class CourseEnrollment {
    @Id
    @Column(name = "id", nullable = false, length = 300)
    private String id;

    @Column(name = "course_id", nullable = false, length = 300)
    private String courseId;

    @Column(name = "student_id", nullable = false, length = 300)
    private String studentId;

    @Column(name = "teacher_id", length = 300)
    private String theacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_type", nullable = false)
    private EnrollmentType enrollmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public CourseEnrollment() {
    }

    public CourseEnrollment(String id, String courseId, String studentId, String theacherId, EnrollmentType enrollmentType, EnrollmentStatus status, LocalDateTime enrollmentDate, LocalDateTime approvedDate) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.theacherId = theacherId;
        this.enrollmentType = enrollmentType;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.approvedDate = approvedDate;
        this.notes = null;
    }

    public CourseEnrollment(String id, String courseId, String studentId, String theacherId, EnrollmentType enrollmentType, EnrollmentStatus status, LocalDateTime enrollmentDate, LocalDateTime approvedDate, String notes) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.theacherId = theacherId;
        this.enrollmentType = enrollmentType;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.approvedDate = approvedDate;
        this.notes = notes;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return this.courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getStudentId() {
        return this.studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTheacherId() {
        return this.theacherId;
    }

    public void setTheacherId(String theacherId) {
        this.theacherId = theacherId;
    }

    public EnrollmentType getEnrollmentType() {
        return this.enrollmentType;
    }

    public void setEnrollmentType(EnrollmentType type) {
        this.enrollmentType = type;
    }

    public EnrollmentStatus getStatus() {
        return this.status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrollmentDate() {
        return this.enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public LocalDateTime getApprovedDate() {
        return this.approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
