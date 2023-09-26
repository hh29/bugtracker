package com.example.bugtracker.Controller.DialogController;

import com.example.bugtracker.Controller.Admin.AdminUsersController;
import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.DAO.UserDAO;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Model.Entity.Roles;
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

public class AdminUserDialogController implements Initializable {
    @FXML
    public Button okButton;
    @FXML
    public Button cancelButton;
    @FXML
    public TextField userNameLabel;
    @FXML
    public TextField firstNameLabel;
    @FXML
    public TextField surnameLabel;
    @FXML
    public TextField emailLabel;
    @FXML
    public ComboBox<Roles> roleComboBox;
    @FXML
    public DatePicker dob;
    @FXML
    public ListView<Project> assignedProjects;
    private ObservableList<Project> availableProjects;
    private User selectedUser;
    private AdminUsersController adminUsersController;
    private final Set<Project> selectedProjects = new HashSet<>();



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Roles> roles = FXCollections.observableArrayList(
                Roles.Admin,
                Roles.ProjectManager,
                Roles.Developer,
                Roles.TechSupport,
                Roles.Tester
        );
        roleComboBox.setItems(roles);

    }

    public void initData(User user, ObservableList<Project> allProjects, AdminUsersController adminUsersController) {
        this.adminUsersController = adminUsersController;
        selectedUser = user;
        availableProjects = allProjects;

        // Set available projects to the ListView
        assignedProjects.setItems(availableProjects);

        // Set selection mode to MULTIPLE
        assignedProjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedProjects.addAll(ProjectDAO.getProjectsForUser(selectedUser));



        assignedProjects.setCellFactory(projectListView -> new ListCell<Project>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = createCheckBoxForProject(project);

                    // Set the checkbox state based on whether the project is assigned
//                    checkBox.setSelected(ProjectDAO.getProjectsForUser(selectedUser).contains(project));
                    checkBox.setSelected(selectedProjects.contains(project));

                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            selectedProjects.add(project); // Add to selectedProjects
                        } else {
                            selectedProjects.remove(project); // Remove from selectedProjects
                        }

                    });




                    // Fill other user details
                    userNameLabel.setText(selectedUser.getUsername());
                    firstNameLabel.setText(selectedUser.getFirstName());
                    surnameLabel.setText(selectedUser.getLastName());
                    emailLabel.setText(selectedUser.getEmail());
                    dob.setValue(selectedUser.getDob());
                    roleComboBox.getSelectionModel().select(selectedUser.getRole());

                    setGraphic(checkBox);
                }
            }
        });
    }


    public void handleOkButton(ActionEvent actionEvent) throws SQLException {
        String newUsername = userNameLabel.getText();
        String newFirstName = firstNameLabel.getText();
        String newLastName = surnameLabel.getText();
        String newEmail = emailLabel.getText();
        LocalDate newDOB = dob.getValue();
        Roles newRole = roleComboBox.getValue();

        // Check if either the new or old role is "Admin" or "TechSupport"
        if (newRole == Roles.Admin || newRole == Roles.TechSupport || selectedUser.getRole() == Roles.Admin || selectedUser.getRole() == Roles.TechSupport) {
            // Display an alert indicating that users with these roles cannot be assigned to projects
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Role Assignment Error");
            alert.setHeaderText(null);
            alert.setContentText("Users with the 'Admin' or 'TechSupport' role cannot be assigned to projects.");
            alert.showAndWait();
            return;
        }

        // Create a list to store projects to overwrite
        List<Project> projectsToOverwrite = new ArrayList<>();
        boolean userIsProjectManager = ProjectDAO.isUserProjectManager(selectedUser);



        // Iterate through projects and check for overwrites
        for (Project project : selectedProjects) {
                if (userIsProjectManager) {

                    // Check if the project already has a different project manager assigned
                    if (selectedUser.getUserId() != (project.getProjectManagerId())) {
                        projectsToOverwrite.add(project);
                    }
                }
            }
        // Check if the project manager is being removed from any project
        if (userIsProjectManager) {
            for (Project project : assignedProjects.getItems()) {
                if (!assignedProjects.getSelectionModel().getSelectedItems().contains(project)) {
                    // Show an error message and return
                    Alert managerNotNullAlert = new Alert(Alert.AlertType.ERROR);
                    managerNotNullAlert.setTitle("Project Manager Error");
                    managerNotNullAlert.setHeaderText(null);
                    managerNotNullAlert.setContentText("The Project Manager for project '" + project.getProjectName() + "' cannot be made null. Please assign a new Project Manager.");
                    managerNotNullAlert.showAndWait();
                    return;
                }
            }
        }

        if (!projectsToOverwrite.isEmpty()) {
            // Create a StringBuilder to build the content text for the confirmation alert
            StringBuilder contentText = new StringBuilder("The following projects have different Project Managers assigned:\n\n");

            for (Project project : projectsToOverwrite) {
                contentText.append("- ").append(project.getProjectName()).append("\n");
            }

            // Display a confirmation dialog to ask if the user wants to overwrite project manager assignments
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Remove Project Manager Assignment");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText(contentText.toString() + "\nDo you want to overwrite the existing project manager assignments?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Handle overwriting logic here (update database, etc.)
                for (Project project : projectsToOverwrite) {
                    // Update project associations and project manager field as needed
                    ProjectDAO.removeProjectFromUser(ProjectDAO.getProjectManagerForProject(project), project);
                }
            }
            else {
                // "Cancel" button is clicked, return or perform any necessary actions
                return;
            }
        }


        // Check if any fields are left unfilled
        if (newUsername.isEmpty() || newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || newDOB == null || newRole == null) {
            // Display an alert if any field is empty
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Information");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields before proceeding.");
            alert.showAndWait();
            return;
        }

        // Check if the new username is already taken
        if (!newUsername.equals(selectedUser.getUsername()) && UserDAO.isUsernameTaken(newUsername)) {
            // Display an alert for duplicate username
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Username");
            alert.setHeaderText(null);
            alert.setContentText("The username " + newUsername + " is already taken. Please choose a different username.");
            alert.showAndWait();
            return;
        }


        // Add selected projects that are not in the user's current projects
        for (Project project : selectedProjects) {
            if (!ProjectDAO.getProjectsForUser(selectedUser).contains(project)) {
                ProjectDAO.addProjectToUser(selectedUser, project);
            }
        }

        // Remove unselected projects that are in the user's current projects
        Set<Project> userProjects = new HashSet<>(ProjectDAO.getProjectsForUser(selectedUser));
        for (Project project : userProjects) {
            if (!selectedProjects.contains(project)) {
                ProjectDAO.removeProjectFromUser(selectedUser, project);
                BugDAO.removeBugsFromUserInProject(selectedUser, project); // Remove bugs associated with the removed project
            }
        }


        if (newRole == Roles.ProjectManager || (newRole == Roles.Developer && selectedUser.getRole() == Roles.Tester) || (newRole == Roles.Tester && selectedUser.getRole() == Roles.Developer)) {
            // Remove all bugs associated with the user if:
            // 1. The new role is Project Manager OR
            // 2. The new role is Developer and the old role was Tester OR
            // 3. The new role is Tester and the old role was Developer
            BugDAO.removeBugsFromUser(selectedUser);
        }

        // Update user details and assigned projects
        if (newRole == selectedUser.getRole()) {
            UserDAO.updateUser(selectedUser, newUsername, newFirstName, newLastName, newEmail, newDOB);
        } else {
            UserDAO.updateUserRole(selectedUser, newRole);
            UserDAO.updateUser(selectedUser, newUsername, newFirstName, newLastName, newEmail, newDOB);
        }

        // Show success message
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("User details updated successfully.");
        successAlert.showAndWait();

        System.out.println(assignedProjects.getSelectionModel().getSelectedItems());
        closeDialog();
        // Refresh the AdminUsers page to reflect the changes
        if (adminUsersController != null) {
            adminUsersController.refreshData();

        }
    }

    public void handleCancelButton(ActionEvent actionEvent) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    private CheckBox createCheckBoxForProject(Project project) {
        return new CheckBox(project.getProjectName());
    }
}
