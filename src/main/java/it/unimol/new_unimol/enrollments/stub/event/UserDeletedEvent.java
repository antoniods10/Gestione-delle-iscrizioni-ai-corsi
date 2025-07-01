package it.unimol.new_unimol.enrollments.stub.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserDeletedEvent implements Serializable {
    private String userId;
    private String userRole;
    private String deletedBy;
    private LocalDateTime timestamp;

    public UserDeletedEvent() {}

    public UserDeletedEvent(String userId, String userRole, String deletedBy, LocalDateTime timestamp) {
        this.userId = userId;
        this.userRole = userRole;
        this.deletedBy = deletedBy;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
