package Controller;

import Exceptions.BugAlreadyExistsException;
import Exceptions.ProjectAlreadyExistsException;
import Exceptions.UserAlreadyExistsException;
import Model.*;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private final List<User> users;
    private final List<User> newUsers;

    private final List<Project> projects;
    private final List<Project> newProjects;

    private final List<Bug> bugs;
    private final List<Bug> newBugs;

    private String databaseURL,user,password;


    private void initializeDB(){

        databaseURL = "jdbc:sqlserver://DESKTOP-5K2EO8O\\MSSQLSERVER01;databaseName=SimpleUserLoginWithDatabase_DB";
        user = "sa";
        password = "pestele";


        try {
            Connection connection = DriverManager.getConnection(databaseURL,user,password);

            Statement statementUsers = connection.createStatement();
            ResultSet resultSetUsers = statementUsers.executeQuery("SELECT * FROM BasicUsers");

            while (resultSetUsers.next()){
                String username = resultSetUsers.getString(1);
                String password = resultSetUsers.getString(2);

                users.add(new BasicUser(username,password));
            }

            Statement statementProjects = connection.createStatement();
            ResultSet resultSetProjects = statementProjects.executeQuery("SELECT * FROM Projects");

            while (resultSetProjects.next()){
                int id = resultSetProjects.getInt(1);
                String leader = resultSetProjects.getString(2);
                String name = resultSetProjects.getString(3);
                String description = resultSetProjects.getString(4);
                Project newProject = new BasicProject(id,leader,name,description,new ArrayList<>());

                Statement statementProjectsUserAssociations = connection.createStatement();
                ResultSet resultSetProjectsUserAssociations = statementProjectsUserAssociations.executeQuery("SELECT * FROM UserProjectAssociations");
                while (resultSetProjectsUserAssociations.next()){
                    if (resultSetProjectsUserAssociations.getInt(2) == id){
                        // the id is same as newProject
                        newProject.addUser(resultSetProjectsUserAssociations.getString(1));
                    }
                }
                projects.add(newProject);
            }

            Statement statementBugs = connection.createStatement();
            ResultSet resultSetBugs = statementBugs.executeQuery("SELECT * FROM Bugs");

            while (resultSetBugs.next()){
                int id = resultSetBugs.getInt(1);
                int projectId = resultSetBugs.getInt(2);
                int priority = resultSetBugs.getInt(3);
                int complexity = resultSetBugs.getInt(4);
                String description = resultSetBugs.getString(5);
                String username = resultSetBugs.getString(6);
                Timestamp dateIssued = resultSetBugs.getTimestamp(7);
                String status = resultSetBugs.getString(8);

                Bug newBug = new BasicBug(
                        id,
                        projectId,
                        priority,
                        complexity,
                        description,
                        username,
                        dateIssued,
                        BasicBug.Status.valueOf(status)
                        );

                bugs.add(newBug);
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public Controller() {
        users = new ArrayList<>();
        newUsers = new ArrayList<>();
        projects = new ArrayList<>();
        newProjects = new ArrayList<>();
        bugs = new ArrayList<>();
        newBugs = new ArrayList<>();
        initializeDB();
    }

    public boolean lookUpUsername(String username){
        for(User user : users){
            if (user.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    public String getPasswordWithUsername(String username){
        for(User user : users){
            if (user.getUsername().equals(username)){
                return user.getPassword();
            }
        }
        return "";
    }

    public void addUser(User user){
        for(User eachUser : users){
            if(eachUser.getUsername().equals(user.getUsername())){
                throw new UserAlreadyExistsException();
            }
        }
        users.add(user);
        newUsers.add(user);
    }

    public void addProject(Project project){
        for(Project eachProject : projects){
            if(eachProject.getId() == project.getId()){
                throw new ProjectAlreadyExistsException();
            }
        }
        projects.add(project);
        newProjects.add(project);
    }

    public void addBug(Bug bug){
        for(Bug eachBug : bugs){
            if (eachBug.getId() == bug.getId()){
                throw new BugAlreadyExistsException();
            }
        }
        bugs.add(bug);
        newBugs.add(bug);
    }

    public void removeFromNewUsers(String username, String password){
        newUsers.remove(new BasicUser(username,password));
    }

    public void refreshDatabaseBasicUsers(){
        try {
            Connection connection = DriverManager.getConnection(databaseURL,user,password);

            Statement statement = connection.createStatement();
            for(User user : newUsers){
                try{
                    statement.executeUpdate("INSERT INTO BasicUsers VALUES ('" + user.getUsername() + "','" + user.getPassword() + "')");
                }catch (SQLServerException e){
                    throw new RuntimeException();
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void refreshDatabaseBugs(){
        try {
            Connection connection = DriverManager.getConnection(databaseURL,user,password);

            Statement statement = connection.createStatement();
            for(Bug bug : newBugs){
                try{
                    statement.executeUpdate("INSERT INTO Bugs VALUES ('" + bug.getId() + "','"
                            + bug.getProjectId() + "','"
                            + bug.getPriority() + "','"
                            + bug.getComplexity() + "','"
                            + bug.getDescription() + "','"
                            + bug.getUsername() + "',"
                            + "CURRENT_TIMESTAMP,'"
                            + bug.getStatus() + "')");
                }catch (SQLServerException e){
                    e.printStackTrace();
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateDatabaseProjectUserAssociationsGivenId(int id){
        try {
            Connection connection = DriverManager.getConnection(databaseURL,user,password);

            Statement statementDelete = connection.createStatement();
            Statement statementInsert = connection.createStatement();

            try {
                statementDelete.executeUpdate(
                        "DELETE FROM UserProjectAssociations WHERE project_id=" + id + ";"
                );
            }catch (SQLServerException e){
                throw new RuntimeException();
            }

                for(Project project : projects){
                    if(project.getId() == id){
                        for (String user : project.getUsers()){
                            try{
                                statementInsert.executeUpdate(
                                        "INSERT INTO UserProjectAssociations VALUES('"
                                                + user + "',"
                                                + project.getId() + ")"
                                );
                            }catch (SQLServerException e){
                                throw new RuntimeException();
                            }
                        }
                    }
                }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void refreshDatabaseProjects(){
        try {
            Connection connection = DriverManager.getConnection(databaseURL,user,password);

            Statement statementInsertProjects = connection.createStatement();
            Statement statementInsertUserProjectAssociations = connection.createStatement();
            for(Project project : newProjects){
                try{
                    statementInsertProjects.executeUpdate("INSERT INTO Projects VALUES ("
                            + project.getId() + ",'"
                            + project.getLeader() + "','"
                            + project.getName() + "','"
                            + project.getDescription() + "')"
                    );

                    statementInsertUserProjectAssociations.executeUpdate(
                            "INSERT INTO UserProjectAssociations VALUES('"
                                    + project.getLeader() + "',"
                                    + project.getId() + ")"
                    );

                }catch (SQLServerException e){
                    throw new RuntimeException();
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Bug> getBugsWithProjectId(int id){
        List<Bug> bugsWithId = new ArrayList<>();
        for(Bug bug : bugs){
            if(bug.getProjectId() == id){
                bugsWithId.add(bug);
            }
        }
        return bugsWithId;
    }

    public List<Project> getProjectsWithUsername (String username){
        List<Project> projectsOfUser = new ArrayList<>();
        for (Project project : projects){
            if(project.getUsers().contains(username)){
                // the user is found in the user pool of the project
                projectsOfUser.add(project);
            }
        }
        return projectsOfUser;
    }

    @Override
    public String toString() {
        return users.toString();
    }
}
