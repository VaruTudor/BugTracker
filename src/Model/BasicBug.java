package Model;

import java.sql.Timestamp;

public class BasicBug implements Bug{
    private final int id, projectId,priority, complexity;
    private final String description, username;
    private final Timestamp dateIssued;
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        SOLVED
    }
    private final Status status;

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getPriority() {
        return priority;
    }

    public int getComplexity() {
        return complexity;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getDateIssued() {
        return dateIssued;
    }

    public Status getStatus() {
        return status;
    }

    public BasicBug(int id, int projectId, int priority, int complexity, String description, String username, Timestamp dateIssued, Status status) {
        this.id = id;
        this.projectId = projectId;
        this.priority = priority;
        this.complexity = complexity;
        this.description = description;
        this.username = username;
        this.dateIssued = dateIssued;
        this.status = status;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", priority=" + priority +
                ", complexity=" + complexity +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                ", dateIssued=" + dateIssued +
                ", status=" + status;
    }
}
