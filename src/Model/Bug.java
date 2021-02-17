package Model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public interface Bug {
    int getId();
    int getProjectId();
    int getPriority();
    int getComplexity();
    String getDescription();
    String getUsername();
    Timestamp getDateIssued();
    BasicBug.Status getStatus();
}
