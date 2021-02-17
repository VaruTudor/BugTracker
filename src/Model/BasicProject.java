package Model;

import Exceptions.UserAlreadyExistsException;
import Exceptions.UserNotFoundException;

import java.util.List;

public class BasicProject implements Project{
    private final int id;
    private final String leader,name,description;
    private final List<String> users;

    public int getId() {
        return id;
    }
    public String getLeader() {
        return leader;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public List<String> getUsers() {   return users; }

    public BasicProject(int id, String leader, String name, String description, List<String> users) {
        this.id = id;
        this.leader = leader;
        this.users = users;
        this.name = name;
        this.description = description;
    }

    public void addUser(String username){
        for(String eachUsername : users){
            if (eachUsername.equals(username)){
                throw new UserAlreadyExistsException();
            }
        }
        users.add(username);
    }
    public void removeUser(String username){

        if(!users.contains(username)) throw new UserNotFoundException();
        users.remove(username);
    }


    @Override
    public String toString() {
        return name + " - " + leader;
    }
}
