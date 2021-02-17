package View;

import Controller.Controller;
import Exceptions.BugAlreadyExistsException;
import Exceptions.ProjectAlreadyExistsException;
import Exceptions.UserAlreadyExistsException;
import Exceptions.UserNotFoundException;
import Model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GUI {

    private static final Controller controller = new Controller();

    private static JFrame newBugFrame;
    private static JFrame openProjectFrame;
    private static JFrame loginFrame;

    private static JTable bugsTable;
    private static DefaultTableModel bugsTableModel;
    private static DefaultListModel<String> projectsListModel;

    private static JTextField usernameText;
    private static JPasswordField passwordText;

    /**
     * Creates a new table(type JTable) which will contain all bugs(type Bug) with project id(type int)
     * equal to the id of the project(type Project) given as parameter.
     * @param selectedProject - the project which will give the id to be compared
     * @return a new JTable
     */
    public static JTable refreshBugsTable(Project selectedProject){
        List<Bug> bugs = controller.getBugsWithProjectId(selectedProject.getId());
        String[] columnNames ={"ID","STATUS","PRIORITY","COMPLEXITY","ISSUED BY"};
        bugsTableModel = new DefaultTableModel(columnNames,0);
        for (Bug bug : bugs) {
            Object[] o = new Object[5];
            o[0] = bug.getId();
            o[1] = bug.getStatus();
            o[2] = bug.getPriority();
            o[3] = bug.getComplexity();
            o[4] = bug.getUsername();
            bugsTableModel.addRow(o);
        }

        return new JTable(bugsTableModel);
    }

    /**
     * Creates a new button(type JButton) which will be used in order to login.
     * Both the username(type String) and password(type String) take values from the text boxes(type JTextField and JPasswordField)
     * found in the frame and will be passed to the controller to try to find a match. On success, a new frame(type JFrame) is created and shown
     * to the user and the old frame is hidden. On failure, the error message will be shown in a pop up window.
     * @param mainFrame - the frame which opens when app is main method is called
     * @return a new JButton
     */
    public static JButton setupLoginButton(JFrame mainFrame){
        JButton loginButton = new JButton(new AbstractAction("Login") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameText.getText();
                String password = String.valueOf(passwordText.getPassword());

                if (controller.lookUpUsername(username)) {
                    // the username exists in the database
                    if (controller.getPasswordWithUsername(username).equals(password)) {
                        // the password matches
                        // create the new window
                        loginFrame = new JFrame(username);
                        loginFrame.setSize(400, 300);
                        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        loginFrame.setLocationRelativeTo(null);
                        loginFrame.setLayout(new GridBagLayout());

                        GridBagConstraints c = new GridBagConstraints();

                        // populating the list of projects
                        List<Project> projectList = controller.getProjectsWithUsername(username);

                        projectsListModel = new DefaultListModel<>();
                        for (Project p : projectList) {
                            projectsListModel.addElement(p.toString());
                        }

                        JList<String> projects = new JList<>(projectsListModel);

                        c.fill = GridBagConstraints.BOTH;
                        c.gridwidth = 2;
                        c.gridx = 0;
                        c.gridy = 0;
                        c.weightx = 1;
                        c.weighty = 1;
                        loginFrame.add(projects,c);

                        // building the new project button
                        JButton newProjectButton = setupNewProjectButton(username,password);
                        c.anchor = GridBagConstraints.LINE_END;
                        c.fill = GridBagConstraints.NONE;
                        c.gridwidth = 1;
                        c.gridx = 0;
                        c.gridy = 1;
                        c.insets = new Insets(3,3,3,3);
                        c.weightx = 0;
                        c.weighty = 0;
                        loginFrame.add(newProjectButton,c);

                        // building the open project button
                        JButton openProjectButton = setupOpenProjectButton(username,projectList,projects);
                        c.anchor = GridBagConstraints.LINE_START;
                        c.gridx = 1;
                        c.gridy = 1;
                        loginFrame.add(openProjectButton,c);

                        loginFrame.setVisible(true);
                        // hide the mainFrame when a login is successful
                        mainFrame.dispose();
                    } else {
                        // no password match
                        JOptionPane.showMessageDialog(new JFrame(), "Wrong password", "Warning", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // no username match
                    JOptionPane.showMessageDialog(new JFrame(), "There is no account with such username", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginButton.setBounds(10,80,80,25);
        return loginButton;
    }

    /**
     * Creates a new button(type JButton) which will be used to create a new account.
     * Both the username(type String) and password(type String) take values from the text boxes(type JTextField and JPasswordField)
     * found in the frame and will be passed to the controller. The controller will create a new account if possible.
     * On failure, the error message will be shown in a pop up window.
     * @return a new JButton
     */
    public static JButton setupCreateAccountButton(){
        JButton createAccountButton = new JButton(new AbstractAction("Create Account") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                String username = usernameText.getText();
                String password = String.valueOf(passwordText.getPassword());

                try {
                    controller.addUser(
                            new BasicUser(username,password)
                    );

                    controller.refreshDatabaseBasicUsers();
                }catch (UserAlreadyExistsException exception){
                    JOptionPane.showMessageDialog(new JFrame(), exception.toString(), "Warning", JOptionPane.ERROR_MESSAGE);
                } catch (RuntimeException exception){
                    controller.removeFromNewUsers(username,password);
                    JOptionPane.showMessageDialog(new JFrame(), "there has been an error", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        createAccountButton.setBounds(100,80,165,25);

        return createAccountButton;
    }

    /**
     * Creates a new button(type JButton) which will be used to create a new project.
     * @param username the username who will be the project leader
     * @param password the password of the user
     * @return a new JButton
     */
    public static JButton setupNewProjectButton(String username, String password){
        JButton newProjectButton = new JButton(new AbstractAction("New Project") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // create the new window
                JFrame frame = new JFrame("New Project");
                frame.setSize(360, 360);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                JPanel panel = new JPanel();
                frame.add(panel);
                panel.setLayout(null);

                // name
                JLabel projectIdLabel = new JLabel("id");
                projectIdLabel.setBounds(10,20,80,25);
                panel.add(projectIdLabel);

                JTextField projectIdText = new JTextField(20);
                projectIdText.setBounds(100,20,200,25);
                panel.add(projectIdText);

                // name
                JLabel projectNameLabel = new JLabel("name");
                projectNameLabel.setBounds(10,50,80,25);
                panel.add(projectNameLabel);

                JTextField projectNameText = new JTextField(20);
                projectNameText.setBounds(100,50,200,25);
                panel.add(projectNameText);

                // description
                JLabel descriptionLabel = new JLabel("description");
                descriptionLabel.setBounds(10,80,80,25);
                panel.add(descriptionLabel);

                JTextField descriptionText = new JTextField(20);
                descriptionText.setBounds(100,80,200,200);
                panel.add(descriptionText);

                JButton createProjectButton = setupCreateProjectButton(
                        projectIdText,
                        projectNameText,
                        descriptionText,
                        username,
                        password
                );
                panel.add(createProjectButton);

                frame.setVisible(true);
            }
        });

        newProjectButton.setBounds(10, 215, 150, 20);
        return newProjectButton;
    }

    /**
     * Creates a new button(type JButton) which will be used to open a project.
     * If the opener is the leader of the project, they will have the option of adding/removing users
     * from the project. Otherwise, they will only see the bugs of the opened project.
     * @param username the username who will be the project leader
     * @param projectList the list of projects
     * @param projects the list of projects string representation as a JList
     * @return a new JButton
     */
    public static JButton setupOpenProjectButton(String username, List<Project> projectList, JList<String> projects){
        JButton openProjectButton = new JButton(new AbstractAction("Open Project") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = projects.getSelectedIndex();
                if(selectedIndex != -1){
                    // a project is selected
                    Project selectedProject = projectList.get(selectedIndex);

                    if(selectedProject.getLeader().equals(username)){
                        openProjectFrame = new JFrame("Project " + selectedProject.getName());
                        openProjectFrame.setLayout(new GridBagLayout());
                        openProjectFrame.setLocationRelativeTo(null);
                        openProjectFrame.setSize(720,370);

                        GridBagConstraints c = new GridBagConstraints();

                        bugsTable = refreshBugsTable(selectedProject);
                        JScrollPane panel = new JScrollPane(bugsTable);
                        c.fill = GridBagConstraints.BOTH;
                        c.gridwidth = 3;
                        c.gridx = 0;
                        c.gridy = 0;
                        c.weightx = 1;
                        c.weighty = 1;
                        openProjectFrame.add(panel,c);

                        // open bug
                        List<Bug> data = controller.getBugsWithProjectId(selectedProject.getId());
                        JButton openBugButton = setupOpenBugButton(bugsTable,data);
                        c.anchor = GridBagConstraints.LINE_END;
                        c.fill = GridBagConstraints.HORIZONTAL;
                        c.gridwidth = 1;
                        c.gridx = 1;
                        c.gridy = 1;
                        c.insets = new Insets(3,3,3,3);
                        c.weightx = 0;
                        c.weighty = 0;
                        openProjectFrame.add(openBugButton,c);

                        // new bug
                        JButton newBugButton = setupNewBugButton(selectedProject, username);
                        c.anchor = GridBagConstraints.LINE_START;
                        c.gridx = 2;
                        c.gridy = 1;
                        openProjectFrame.add(newBugButton,c);

                        JTextField usernameTextField = new JTextField(20);
                        usernameTextField.setBounds(10,10,150,25);
                        c.gridx = 0;
                        c.gridy = 2;
                        c.weightx = 0.075;
                        openProjectFrame.add(usernameTextField,c);

                        // add users as leader
                        JButton addUserToProjectButton = setupAddUserButton(usernameTextField, selectedProject);
                        c.anchor = GridBagConstraints.LINE_END;
                        c.gridx = 1;
                        c.gridy = 2;
                        c.weightx = 0;
                        openProjectFrame.add(addUserToProjectButton,c);

                        // remove user
                        JButton removeUserToProjectButton = setupRemoveUserButton(usernameTextField, selectedProject);
                        c.anchor = GridBagConstraints.LINE_START;
                        c.gridx = 2;
                        c.gridy = 2;
                        openProjectFrame.add(removeUserToProjectButton,c);

                        openProjectFrame.setVisible(true);
                    }else{
                        openProjectFrame = new JFrame("Project " + selectedProject.getName());
                        openProjectFrame.setLayout(new GridBagLayout());
                        openProjectFrame.setLocationRelativeTo(null);
                        openProjectFrame.setSize(720,370);

                        GridBagConstraints c = new GridBagConstraints();

                        bugsTable = refreshBugsTable(selectedProject);
                        JScrollPane panel = new JScrollPane(bugsTable);
                        c.fill = GridBagConstraints.BOTH;
                        c.gridwidth = 2;
                        c.gridx = 0;
                        c.gridy = 0;
                        c.weightx = 1;
                        c.weighty = 1;
                        openProjectFrame.add(panel,c);

                        // open bug
                        List<Bug> data = controller.getBugsWithProjectId(selectedProject.getId());
                        JButton openBugButton = setupOpenBugButton(bugsTable, data);
                        c.anchor = GridBagConstraints.LINE_END;
                        c.fill = GridBagConstraints.NONE;
                        c.gridwidth = 1;
                        c.gridx = 0;
                        c.gridy = 1;
                        c.insets = new Insets(3,3,3,3);
                        c.weightx = 0;
                        c.weighty = 0;
                        openProjectFrame.add(openBugButton,c);

                        // new bug
                        JButton newBugButton = setupNewBugButton(selectedProject, username);
                        c.anchor = GridBagConstraints.LINE_START;
                        c.gridx = 1;
                        c.gridy = 1;
                        openProjectFrame.add(newBugButton,c);

                        openProjectFrame.setVisible(true);
                    }

                }else {
                    JOptionPane.showMessageDialog(new JFrame(), "No program selected", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        openProjectButton.setBounds(10, 240, 150, 20);
        return openProjectButton;
    }

    /**
     * Creates a new button(type JButton) which will be used to add a bug.
     * The bug(type Bug) will be added to the database if there is no other with the same id.
     * Otherwise an error message will be shown.
     * @param bugIdTextField text field for bug id
     * @param selectedProject the project which was opened
     * @param priorityTextField text field for priority
     * @param complexityTextField text field for complexity
     * @param descriptionTextField text field for description
     * @param username the username of who opened the project
     * @return a new JButton
     */
    public static JButton setupAddBugButton(JTextField bugIdTextField, Project selectedProject,JTextField priorityTextField, JTextField complexityTextField, JTextField descriptionTextField, String username){
        JButton addBugButton = new JButton(new AbstractAction("Add Bug") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                try{
                    int bugId =  Integer.parseInt(bugIdTextField.getText());
                    int priority = Integer.parseInt(priorityTextField.getText());
                    int complexity = Integer.parseInt(complexityTextField.getText());

                    try{
                        controller.addBug(
                                new BasicBug(
                                        bugId,
                                        selectedProject.getId(),
                                        Integer.parseInt(priorityTextField.getText()),
                                        Integer.parseInt(complexityTextField.getText()),
                                        descriptionTextField.getText(),
                                        username,
                                        Timestamp.valueOf(LocalDateTime.now()),
                                        BasicBug.Status.NOT_STARTED)
                        );
                        controller.refreshDatabaseBugs();
                    }catch (BugAlreadyExistsException exception){
                        JOptionPane.showMessageDialog(new JFrame(), exception.toString(), "Warning", JOptionPane.ERROR_MESSAGE);
                    }

                    Object[] o = new Object[5];
                    o[0] = bugId;
                    o[1] = BasicBug.Status.NOT_STARTED;
                    o[2] = priority;
                    o[3] = complexity;
                    o[4] = username;
                    bugsTableModel.addRow(o);

                    openProjectFrame.revalidate();
                    openProjectFrame.repaint();
                }catch (NumberFormatException exception){
                    JOptionPane.showMessageDialog(new JFrame(), "bug id,priority or complexity not set", "Warning", JOptionPane.ERROR_MESSAGE);
                }


            }
        });

        addBugButton.setBounds(10,320,290,25);
        return addBugButton;
    }

    /**
     * Creates a new button(type JButton) which will open the window for creating a new bug.
     * @param selectedProject the project which was opened
     * @param username the username of who opened the project
     * @return a new JButton
     */
    public static JButton setupNewBugButton(Project selectedProject, String username){
        JButton newBugButton = new JButton(new AbstractAction("New Bug") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                newBugFrame = new JFrame("New Bug");
                newBugFrame.setLayout(new GridBagLayout());
                newBugFrame.setLocationRelativeTo(null);
                GridBagConstraints c = new GridBagConstraints();

                // id
                JLabel bugIdLabel = new JLabel("id");
                c.anchor = GridBagConstraints.LINE_END;
                c.insets = new Insets(0,5,0,0);
                c.gridx = 0;
                c.gridy = 0;
                newBugFrame.add(bugIdLabel,c);

                // priority
                JLabel priorityLabel = new JLabel("priority");
                c.gridx = 0;
                c.gridy = 1;
                newBugFrame.add(priorityLabel,c);

                // complexity
                JLabel complexityLabel = new JLabel("complexity");
                c.gridx = 0;
                c.gridy = 2;
                newBugFrame.add(complexityLabel,c);

                // description
                JLabel descriptionLabel = new JLabel("description");
                c.gridx = 0;
                c.gridy = 3;
                newBugFrame.add(descriptionLabel,c);


                JTextField bugIdTextField = new JTextField(20);
                c.anchor = GridBagConstraints.LINE_START;
                c.insets = new Insets(5,5,0,5);
                c.gridx = 1;
                c.gridy = 0;
                newBugFrame.add(bugIdTextField,c);

                JTextField priorityTextField = new JTextField(20);
                c.gridx = 1;
                c.gridy = 1;
                newBugFrame.add(priorityTextField,c);

                JTextField complexityTextField = new JTextField(20);
                c.gridx = 1;
                c.gridy = 2;
                newBugFrame.add(complexityTextField,c);

                JTextField descriptionTextField = new JTextField(20);
                c.gridx = 1;
                c.gridy = 3;
                c.weightx = c.weighty = 3;
                c.fill = GridBagConstraints.VERTICAL;
                newBugFrame.add(descriptionTextField,c);

                JButton addBugButton = setupAddBugButton(
                        bugIdTextField,
                        selectedProject,
                        priorityTextField,
                        complexityTextField,
                        descriptionTextField,
                        username
                );
                c.gridx = 1;
                c.gridy = 4;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = c.weighty = 0;
                c.insets = new Insets(5,5,5,5);
                newBugFrame.add(addBugButton,c);

                newBugFrame.setSize(300,420);
                newBugFrame.setVisible(true);
            }
        });
        newBugButton.setBounds(115,295,100,25);

        return newBugButton;
    }

    /**
     * Creates a new button(type JButton) which will open the window
     * containing additional information about the selected bug.
     * @param table the table containing bugs
     * @param bugList the list of bugs
     * @return a new JButton
     */
    public static JButton setupOpenBugButton(JTable table, List<Bug> bugList){
        JButton openBugButton = new JButton(new AbstractAction("Open Bug") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                JFrame frame = new JFrame();
                frame.setLayout(new GridBagLayout());
                frame.setLocationRelativeTo(null);
                GridBagConstraints c = new GridBagConstraints();

                int indexOfBugSelected = table.getSelectedRow();
                if(indexOfBugSelected == -1){
                    JOptionPane.showMessageDialog(new JFrame(), "No bug selected", "Warning", JOptionPane.ERROR_MESSAGE);
                }else{
                    try {
                        Bug selectedBug = bugList.get(indexOfBugSelected);

                        // id
                        JLabel bugIdLabel = new JLabel("id: " + selectedBug.getId());
                        c.anchor = GridBagConstraints.LINE_START;
                        c.insets = new Insets(0,5,0,0);
                        c.gridx = 0;
                        c.gridy = 0;
                        frame.add(bugIdLabel,c);

                        // project id
                        // id
                        JLabel projectIdLabel = new JLabel("project id: " + selectedBug.getProjectId());
                        c.gridx = 0;
                        c.gridy = 1;
                        frame.add(projectIdLabel,c);

                        // priority
                        JLabel priorityLabel = new JLabel("priority: " + selectedBug.getPriority());
                        c.gridx = 0;
                        c.gridy = 2;
                        frame.add(priorityLabel,c);

                        // complexity
                        JLabel complexityLabel = new JLabel("complexity: " + selectedBug.getComplexity());
                        c.gridx = 0;
                        c.gridy = 3;
                        frame.add(complexityLabel,c);

                        // description
                        JTextArea textArea = new JTextArea(2, 20);
                        textArea.setText("description: " + selectedBug.getDescription());
                        textArea.setWrapStyleWord(true);
                        textArea.setLineWrap(true);
                        textArea.setOpaque(false);
                        textArea.setEditable(false);
                        textArea.setFocusable(false);
                        textArea.setBackground(UIManager.getColor("Label.background"));
                        textArea.setFont(UIManager.getFont("Label.font"));
                        textArea.setBorder(UIManager.getBorder("Label.border"));
                        c.gridx = 0;
                        c.gridy = 4;
                        frame.add(textArea, c);

                        // username
                        JLabel usernameLabel = new JLabel("user who issued: " + selectedBug.getUsername());
                        c.gridx = 0;
                        c.gridy = 5;
                        frame.add(usernameLabel,c);

                        // description
                        JLabel dateLabel = new JLabel("date issued: " + selectedBug.getDateIssued());
                        c.gridx = 0;
                        c.gridy = 6;
                        frame.add(dateLabel,c);

                        // status
                        JLabel statusLabel = new JLabel("status: " + selectedBug.getStatus());
                        c.gridx = 0;
                        c.gridy = 7;
                        frame.add(statusLabel,c);

                        frame.setSize(300,300);
                        frame.setVisible(true);
                    }catch (IndexOutOfBoundsException exception){
                        //todo make opening a bug after adding it work without the need to reopen the window
                        JOptionPane.showMessageDialog(new JFrame(), "Please reopen the bugs window", "Warning", JOptionPane.ERROR_MESSAGE);
                    }


                }

            }
        });
        openBugButton.setBounds(10,295,100,25);

        return openBugButton;
    }

    /**
     * Creates a new button(type JButton) which will create a new project and add it to the project list.
     * @param projectIdText text field for project id
     * @param projectNameText text field for project name
     * @param descriptionText text field for description
     * @param username the username of who opened the project
     * @param password the password of the user
     * @return a new JButton
     */
    public static JButton setupCreateProjectButton(JTextField projectIdText, JTextField projectNameText, JTextField descriptionText, String username, String password){
        JButton createProjectButton = new JButton(new AbstractAction("Create Project") {
            @Override
            public void actionPerformed( ActionEvent e ) {

                try{
                    Project newProject = new BasicProject(
                            Integer.parseInt(projectIdText.getText()),
                            username,
                            projectNameText.getText(),
                            descriptionText.getText()
                            ,new ArrayList<>()
                    );

                    try{
                        controller.addProject(newProject);
                    }catch (ProjectAlreadyExistsException exception){
                        JOptionPane.showMessageDialog(new JFrame(), exception.toString(), "Warning", JOptionPane.ERROR_MESSAGE);
                    }

                    try {
                        controller.refreshDatabaseProjects();

                        projectsListModel.addElement(newProject.toString());

                        loginFrame.revalidate();
                        loginFrame.repaint();
                    }catch (RuntimeException exception){
                        controller.removeFromNewUsers(username,password);
                        JOptionPane.showMessageDialog(new JFrame(), "there has been an error", "Warning", JOptionPane.ERROR_MESSAGE);
                    }
                }catch (NumberFormatException exception){
                    JOptionPane.showMessageDialog(new JFrame(), "project id not set", "Warning", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        createProjectButton.setBounds(10,285,290,25);

        return createProjectButton;
    }

    /**
     * Creates a new button(type JButton) which will add a user to a project.
     * @param usernameTextField the username text field of who opened the project
     * @param selectedProject the project which was opened
     * @return a new JButton
     */
    public static JButton setupAddUserButton(JTextField usernameTextField, Project selectedProject){
        JButton addUserToProjectButton = new JButton(new AbstractAction("Add User") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                try{
                    selectedProject.addUser(usernameTextField.getText());
                    controller.updateDatabaseProjectUserAssociationsGivenId(selectedProject.getId());
                    JOptionPane.showMessageDialog(new JFrame(), "user added", "INFO", JOptionPane.INFORMATION_MESSAGE);
                }catch (UserAlreadyExistsException exception){
                    JOptionPane.showMessageDialog(new JFrame(), "user already exists in the project", "Warning", JOptionPane.ERROR_MESSAGE);
                }catch (Exception exception){
                    JOptionPane.showMessageDialog(new JFrame(), "there has been an error", "Warning", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        addUserToProjectButton.setBounds(10,70,150,25);

        return addUserToProjectButton;
    }

    /**
     * Creates a new button(type JButton) which will remove a user to a project.
     * @param usernameTextField the username text field of who opened the project
     * @param selectedProject the project which was opened
     * @return a new JButton
     */
    public static JButton setupRemoveUserButton(JTextField usernameTextField, Project selectedProject){
        JButton removeUserToProjectButton = new JButton(new AbstractAction("Remove User") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                try{
                    selectedProject.removeUser(usernameTextField.getText());
                    controller.updateDatabaseProjectUserAssociationsGivenId(selectedProject.getId());
                    JOptionPane.showMessageDialog(new JFrame(), "user removed", "INFO", JOptionPane.INFORMATION_MESSAGE);
                }catch (UserNotFoundException exception){
                    JOptionPane.showMessageDialog(new JFrame(), exception.toString(), "Warning", JOptionPane.ERROR_MESSAGE);
                }catch (Exception exception){
                    JOptionPane.showMessageDialog(new JFrame(), "there has been an error", "Warning", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        removeUserToProjectButton.setBounds(10,70,150,25);

        return removeUserToProjectButton;
    }

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("BugTracker");

        mainFrame.setSize(300,200);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();

        mainFrame.add(panel);
        panel.setLayout(null);

        // username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setBounds(10,20,80,25);
        panel.add(usernameLabel);

        usernameText = new JTextField(20);
        usernameText.setBounds(100,20,165,25);
        panel.add(usernameText);

        // password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10,50,80,25);
        panel.add(passwordLabel);

        passwordText = new JPasswordField(20);
        passwordText.setBounds(100,50,165,25);
        panel.add(passwordText);

        // login
        JButton loginButton = setupLoginButton(mainFrame);
        panel.add(loginButton);

        // create account
        JButton createAccountButton = setupCreateAccountButton();
        panel.add(createAccountButton);

        mainFrame.setVisible(true);
    }

}
