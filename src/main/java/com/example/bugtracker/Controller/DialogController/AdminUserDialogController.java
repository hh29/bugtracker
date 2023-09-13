package com.example.bugtracker.Controller.DialogController;

import com.example.bugtracker.Controller.Admin.AdminUsersController;
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
        assignedProjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }

    public void initData(User user, ObservableList<Project> allProjects, AdminUsersController adminUsersController) {
        this.adminUsersController = adminUsersController;
        selectedUser = user;
        availableProjects = allProjects;

        // Set available projects to the ListView
        assignedProjects.setItems(availableProjects);

        // Set selection mode to MULTIPLE
        assignedProjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
                    checkBox.setSelected(ProjectDAO.getProjectsForUser(selectedUser).contains(project));

                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            selectedProjects.add(project); // Add to selectedProjects
                        } else {
                            selectedProjects.remove(project); // Remove from selectedProjects
                        }
                        System.out.println("Number of selected items: " + selectedProjects.size());

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


        // Iterate through projects and check for overwrites
        for (Project project : selectedProjects) {
            boolean userIsProjectManager = ProjectDAO.isUserProjectManager(selectedUser);

                if (userIsProjectManager) {

                    // Check if the project already has a different project manager assigned
                    if (selectedUser.getUserId() != (project.getProjectManagerId())) {
                        projectsToOverwrite.add(project);
                    }
                }
            }
        // Check if any initially checked box is unchecked
        for (Project project : ProjectDAO.getProjectsForUser(selectedUser)) {
            if (selectedProjects.contains(project) && !assignedProjects.getSelectionModel().getSelectedItems().contains(project)) {
                // Display an alert if an initially checked box is unchecked
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Project Manager Cannot Be Null");
                alert.setHeaderText(null);
                alert.setContentText("The Project Manager for project '" + project.getProjectName() + "' cannot be made null.");
                alert.showAndWait();
                return; // Abort the operation
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
                    ProjectDAO.addProjectToUser(selectedUser, project);
                    ProjectDAO.updateProjectManager(project,selectedUser);
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


//        if (!selectedProjects.contains(se) && assignedProjects.getSelectionModel().getSelectedItems().contains(project)) {
//            // The project was initially unchecked but is now checked
//            // Create the project/user association here
//            ProjectDAO.addProjectToUser(selectedUser, project);
//        }
//        else if (selectedProjects.contains(project) && !assignedProjects.getSelectionModel().getSelectedItems().contains(project)) {
//            // The project was initially checked but is now unchecked
//            // Remove the project/user association here
//            ProjectDAO.removeProjectFromUser(selectedUser, project);
//        }


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
