package Model;

import java.util.List;

public interface Project {
    void addUser(String username);
    void removeUser(String username);
    List<String> getUsers();
    int getId();
    String getLeader();
    String getName();
    String getDescription();
}
