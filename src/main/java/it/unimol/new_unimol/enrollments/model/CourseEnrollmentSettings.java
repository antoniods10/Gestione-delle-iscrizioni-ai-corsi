package it.unimol.new_unimol.enrollments.model;

import it.unimol.new_unimol.enrollments.util.EnrollmentMode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollment_settings")
public class CourseEnrollmentSettings {
    @Id
    @Column(name = "id", nullable = false, length = 300)
    private String id;

    @Column(name = "course_id", nullable = false, length = 300)
    private String courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_mode", nullable = false)
    private EnrollmentMode enrollmentMode;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval;

    @Column(name = "max_enrollments")
    private Integer maxEnrollments;

    @Column(name = "enrollment_start_date", nullable = false)
    private LocalDateTime enrollmentStartDate;

    @Column(name = "enroollment_end_date", nullable = false)
    private LocalDateTime enrollmentEndDate;

    @Column(name = "allow_waiting_list", nullable = false)
    private Boolean allowWaitingList;

    @Column(name = "created_by", nullable = false, length = 300)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    public CourseEnrollmentSettings() {
    }

    public CourseEnrollmentSettings(String id, String courseId, EnrollmentMode enrollmentMode, Boolean requiresApproval, Integer maxEnrollments, LocalDateTime enrollmentStartDate,
                                    LocalDateTime enrollmentEndDate, Boolean allowWaitingList, String createdBy, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.courseId = courseId;
        this.enrollmentMode = enrollmentMode;
        this.requiresApproval = requiresApproval;
        this.maxEnrollments = maxEnrollments;
        this.enrollmentStartDate = enrollmentStartDate;
        this.enrollmentEndDate = enrollmentEndDate;
        this.allowWaitingList = allowWaitingList;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
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

    public EnrollmentMode getEnrollmentMode() {
        return this.enrollmentMode;
    }

    public void setEnrollmentMode(EnrollmentMode enrollmentMode) {
        this.enrollmentMode = enrollmentMode;
    }

    public Boolean getRequiresApproval() {
        return this.requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Integer getMaxEnrollments() {
        return this.maxEnrollments;
    }

    public void setMaxEnrollments(Integer maxEnrollments) {
        this.maxEnrollments = maxEnrollments;
    }

    public LocalDateTime getEnrollmentStartDate() {
        return this.enrollmentStartDate;
    }

    public void setEnrollmentStartDate(LocalDateTime enrollmentStartDate) {
        this.enrollmentStartDate = enrollmentStartDate;
    }

    public LocalDateTime getEnrollmentEndDate() {
        return this.enrollmentEndDate;
    }

    public void setEnrollmentEndDate(LocalDateTime enrollmentEndDate) {
        this.enrollmentEndDate = enrollmentEndDate;
    }

    public Boolean getAllowWaitingList() {
        return this.allowWaitingList;
    }

    public void setAllowWaitingList(Boolean allowWaitingList) {
        this.allowWaitingList = allowWaitingList;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

}
