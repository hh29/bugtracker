package com.example.bugtracker.Controller.DialogController;

import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.DAO.UserDAO;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Model.Entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;


public class ProjectDialogController implements Initializable {
    @FXML
    public Button okButton;
    @FXML
    public Button cancelButton;
    @FXML
    public TextField projectName;
    @FXML
    public TextField projectDescription;
    @FXML
    public ComboBox<String> priorityComboBox;
    @FXML
    public ComboBox<String> statusComboBox;
    @FXML
    public ComboBox<String> projectManagerCombo;
    @FXML
    public DatePicker Deadline;
    @FXML
    public ListView<User> assignedUserList;
    boolean okClicked = false;
    private Project project;
    private final Map<Integer, String> projectManagerHash = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusComboBox.getItems().addAll("New", "In Progress", "Closed");
        priorityComboBox.getItems().addAll("Low", "Medium", "High");

        populateProjectManagerCombo();
        // Set up the assignedUserList with custom cell factory
        try {
            populateAllUsersList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // Allow multiple selections
        assignedUserList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setProject(Project project) {
        this.project = project;
    }
    public void handleCancelButton(ActionEvent actionEvent) {
        closeDialog();
    }

    public void handleOkButton(ActionEvent actionEvent) {
        if (isValidInput()) {
            if (project == null) {
                AddProject();
            } else {
                updateProject();
            }
        } else {
            showValidationAlert();
        }
        }
        private void AddProject() {
            Project newProject = new Project();
            newProject.setProjectName(projectName.getText());
            newProject.setProjectDescription(projectDescription.getText());
            newProject.setStatus(statusComboBox.getValue());
            newProject.setPriority(priorityComboBox.getValue());
            newProject.setStartDate(LocalDate.now());
            newProject.setEndDate(Deadline.getValue());
            newProject.setProjectManagerId(getSelectedProjectManagerId());

            try {
                int newProjectID = ProjectDAO.insertProject(newProject);
                newProject.setProjectId(newProjectID);


                // Get a list of newly selected developers
                List<User> assignedUsers = assignedUserList.getItems().filtered(User::isSelected);

                // Assign the newly selected developers to the project
                ProjectDAO.assignUsersToProject(assignedUsers,newProject);

                ProjectDAO.insertProjectManager(newProject, getSelectedProjectManagerId());

                showAlert("Project Inserted", "New project has been successfully inserted.");
                okClicked = true;
                closeDialog();


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        private void updateProject(){
            project.setProjectName(projectName.getText());
            project.setProjectDescription(projectDescription.getText());
            project.setStatus(statusComboBox.getValue());
            project.setPriority(priorityComboBox.getValue());
            project.setEndDate(Deadline.getValue());
            project.setProjectManagerId(getSelectedProjectManagerId());

            try {
                ProjectDAO.updateProject(project); //update Project based on user inputs

                int selectedProjectManagerId = getSelectedProjectManagerId();
                int currentProjectManagerId = project.getProjectManagerId();

                if (selectedProjectManagerId != currentProjectManagerId) {
                    // If the selected project manager is different, update it
                    ProjectDAO.updateProjectManager(project, selectedProjectManagerId);
                }

                // Get the list of users currently assigned to the project
                List<User> currentlyAssignedUsers = ProjectDAO.getDevelopersOrTestersForProject(project);

                // Get the list of selected members from the assignedUsersList
                List<User> selectedUsers = assignedUserList.getItems().filtered(User::isSelected);

                // Create a list to track members to add
                List<User> membersToAdd = new ArrayList<>();

                // Iterate through the selected developers
                for (User selectedUser : selectedUsers) {
                    // If the selected developer is not in the list of currently assigned developers, add them to the list to add
                    if (!currentlyAssignedUsers.contains(selectedUser)) {
                        membersToAdd.add(selectedUser);
                    }
                }

                // Create a list to track developers to remove
                List<User> membersToRemove = new ArrayList<>();

                // Iterate through the currently assigned developers
                for (User assignedUser : currentlyAssignedUsers) {
                    // If the assigned developer is not in the list of selected developers, add them to the list to remove
                    if (!selectedUsers.contains(assignedUser)) {
                        membersToRemove.add(assignedUser);
                    }
                }

                // Assign the newly selected developers to the project
                ProjectDAO.assignUsersToProject(membersToAdd, project);

                // Remove the deselected developers from the project
                for (User memberToRemove : membersToRemove) {
                    ProjectDAO.removeProjectFromUser(memberToRemove, project);
                    BugDAO.removeBugsFromUserInProject(memberToRemove,project);
                }

                okClicked = true;
                showAlert("Project Updated", "The project has been successfully updated.");
                closeDialog();


            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Update Failed", "Failed to update the project. Please try again.");
            }
        }

    private void populateProjectManagerCombo() {
        List<User> projectManagers = UserDAO.getUsersWithRole("Project Manager");

        projectManagerCombo.getItems().clear(); // Clear existing items
        projectManagerHash.clear(); // Clear the map

        // Populate the ComboBox with project managers' full names
        for (User projectManager : projectManagers) {
            String fullName = projectManager.getFirstName() + " " + projectManager.getLastName();
            projectManagerCombo.getItems().add(fullName);
            projectManagerHash.put(projectManager.getUserId(), fullName); // Populate the map
        }
    }

    private int getSelectedProjectManagerId() {
        String selectedManagerFullName = projectManagerCombo.getValue();

        // Iterate through the map to find the key (ID) associated with the selected full name
        for (Map.Entry<Integer, String> entry : projectManagerHash.entrySet()) {
            if (entry.getValue().equals(selectedManagerFullName)) {
                return entry.getKey();
            }
        }

        // Return -1 if the full name is not found in the map
        return -1;
    }
    private String getProjectManagerFullName(int projectManagerId) {
        // Look up the project manager's full name based on the project manager's ID
        return projectManagerHash.getOrDefault(projectManagerId, "");
    }
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public Project getProject() {
        return project;
    }
    private void showValidationAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText("All fields must be filled.");
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean isValidInput() {
        return !projectName.getText().isEmpty() &&
                !projectDescription.getText().isEmpty() &&
                priorityComboBox.getValue() != null &&
                statusComboBox.getValue() != null &&
                projectManagerCombo.getValue() != null &&
                Deadline.getValue() != null;
    }
    public void initializeForEdit(Project projectToEdit) throws SQLException {
        project = projectToEdit;

        projectName.setText(projectToEdit.getProjectName());
        projectDescription.setText(projectToEdit.getProjectDescription());
        statusComboBox.setValue(projectToEdit.getStatus());
        priorityComboBox.setValue(projectToEdit.getPriority());
        Deadline.setValue(projectToEdit.getEndDate());
        projectManagerCombo.setValue(getProjectManagerFullName(projectToEdit.getProjectManagerId()));

        populateUserList(projectToEdit);

    }
    public void populateUserList(Project project) throws SQLException {
        List<User> users = UserDAO.getUsersWithoutExcludedRoles();

        // Get the list of developers assigned to the project
        List<User> assignedDevelopers = ProjectDAO.getDevelopersOrTestersForProject(project);

        for (User user : users) {
            // Check if the user is in the list of assigned developers based on user_id
            boolean isAssigned = assignedDevelopers.stream()
                    .anyMatch(assignedUser -> assignedUser.getUserId() == user.getUserId());
            user.setSelected(isAssigned);
        }

        ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
        assignedUserList.setItems(observableUsers);

        assignedUserList.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox(user.getFirstName() + " " + user.getLastName() + " - " + user.getRoleName());
                    checkBox.selectedProperty().bindBidirectional(user.selectedProperty());
                    setGraphic(checkBox);
                }
            }
        });
    }
    public void populateAllUsersList() throws SQLException {
        List<User> allDevelopersAndTesters = UserDAO.getAllDevelopersAndTesters();

        ObservableList<User> observableUsers = FXCollections.observableArrayList(allDevelopersAndTesters);
        assignedUserList.setItems(observableUsers);

        assignedUserList.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox(user.getFirstName() + " " + user.getLastName() + " - " + user.getRoleName());
                    checkBox.selectedProperty().bindBidirectional(user.selectedProperty());
                    setGraphic(checkBox);
                }
            }
        });
    }




    public boolean isOkClicked() {
        return okClicked;
    }
}
