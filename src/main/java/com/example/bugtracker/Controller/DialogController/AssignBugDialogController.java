package com.example.bugtracker.Controller.DialogController;

import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Model.Entity.User;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class AssignBugDialogController implements Initializable {
    @FXML
    public Button okButton;
    @FXML
    public Button cancelButton;
    @FXML
    public ComboBox<String> statusComboBox;
    @FXML
    public ComboBox<String> priorityComboBox;
    @FXML
    public ComboBox<String> severityComboBox;

    @FXML
    public TextField bugTitle;
    @FXML
    public TextField bugDescription;
    @FXML
    public TextField estimatedTime;
   @FXML
   public ComboBox<User> assignedToComboBox;
    boolean okClicked = false;
    private Bug bug;
    private Project project;

    public void setProject(Project project) throws SQLException {
        this.project = project;
        populateComboBox(project);
    }

    public void setBug(Bug bug) {
        this.bug = bug;
    }

    public Bug getBug() {
        return bug;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize and populate the ComboBoxes with values
        statusComboBox.getItems().addAll("New", "In Progress", "Testing","Reopened", "Closed");
        priorityComboBox.getItems().addAll("Low", "Medium", "High");
        severityComboBox.getItems().addAll("Minor", "Major", "Critical");

    }
    public void populateComboBox(Project project) throws SQLException {
        ObservableList<User> users = ProjectDAO.getDevelopersOrTestersForProject(project);
        assignedToComboBox.setItems(users);

        assignedToComboBox.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                } else {
                    // Set the cell's text to the developer's first and last name
                    setText(user.getFirstName() + " " + user.getLastName() + " - " +
                            user.getRoleName());
                }
            }
        });
        assignedToComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                } else {
                    // Set the selected item's text to the developer's first and last name
                    setText(user.getFirstName() + " " + user.getLastName());
                }
            }
        });

        if (bug != null) {
            // Select the assigned user if it exists
            User assignedUser = BugDAO.getAssignedUserForBug(bug);
            assignedToComboBox.getSelectionModel().select(assignedUser);
        } else {
            // Bug is not assigned, set the ComboBox to an empty value
            assignedToComboBox.getSelectionModel().clearSelection();
        }
    }

        public void handleOkButton(ActionEvent actionEvent) throws SQLException {
            // Get the selected user from the ComboBox
            User selectedUser = assignedToComboBox.getValue();


            // Check if all required fields are filled
            if (!isValidInput()) {
                showValidationAlert();
                return; // Don't proceed further if input is not valid
            }

            if (bug == null) {
                // Create a new Bug object with the provided data
                Bug newBug = new Bug();
                // Set the Bug object's properties from the dialog fields
                newBug.setBugTitle(bugTitle.getText());
                newBug.setBugDescription(bugDescription.getText());
                newBug.setStatus(statusComboBox.getValue());
                newBug.setPriority(priorityComboBox.getValue());
                newBug.setSeverity(severityComboBox.getValue());
                newBug.setEstimatedTimeToComplete(estimatedTime.getText());

                // Set the project and reporter IDs accordingly
                newBug.setProjectId(project.getProjectId());
                newBug.setReporterId(loggedInUser.getUserId());
                newBug.setCreatedDate(Date.valueOf(LocalDate.now()).toLocalDate());

                // Set the updatedDate
                newBug.setUpdatedDate(Date.valueOf(LocalDate.now()).toLocalDate());
                // Insert the new bug into the database
               int newBugId =  BugDAO.insertBug(newBug);
               newBug.setBugId(newBugId);

                // Handle assignment only if selectedUser is not null
                if (selectedUser != null) {
                    // Insert the bug_id and user_id into the bug_user table
                    BugDAO.assignBugToUser(newBug, selectedUser);
                }
                // Close the dialog and show a success message
                closeDialog();
                showAlert("Success", "Bug has been successfully inserted.");
            } else {

                boolean bugFieldsChanged = !bugTitle.getText().equals(bug.getBugTitle()) ||
                        !bugDescription.getText().equals(bug.getBugDescription()) ||
                        !statusComboBox.getValue().equals(bug.getStatus()) ||
                        !priorityComboBox.getValue().equals(bug.getPriority()) ||
                        !severityComboBox.getValue().equals(bug.getSeverity()) ||
                        !estimatedTime.getText().equals(bug.getEstimatedTimeToComplete());


                // Bug is not null, indicating an existing bug to be updated
                if (bugFieldsChanged) {
                    bug.setBugTitle(bugTitle.getText());
                    bug.setBugDescription(bugDescription.getText());
                    bug.setStatus(statusComboBox.getValue());
                    bug.setPriority(priorityComboBox.getValue());
                    bug.setSeverity(severityComboBox.getValue());
                    bug.setEstimatedTimeToComplete(estimatedTime.getText());
                    bug.setUpdatedDate(Date.valueOf(LocalDate.now()).toLocalDate());

                    // Check if the status has changed to "Testing" and the user is not a tester
                    if ("Testing".equals(bug.getStatus()) && !selectedUser.getRoleName().equals("Tester")) {
                        showAlert("Error", "For a status with 'Testing', the ticket must be assigned to a tester.");
                        return; // Don't proceed further
                    }

                    BugDAO.updateBug(bug);
                }

                User previouslyAssignedUser = BugDAO.getAssignedUserForBug(bug);
                // Check if there's a change in assignment or if no assignment exists
                if (previouslyAssignedUser == null) {
                    // Bug has no previous assignment, so simply assign to the selected user
                    BugDAO.assignBugToUser(bug, selectedUser);
                } else if (!selectedUser.equals(previouslyAssignedUser)) {
                    // Remove the previous assignment and assign to the selected user
                    BugDAO.removeBugFromUser(bug, previouslyAssignedUser);
                    BugDAO.assignBugToUser(bug, selectedUser);
                }  // Close the dialog and show a success message
                closeDialog();
                showAlert("Success", "Bug has been successfully updated.");
                }

            }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleCancelButton() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private boolean isValidInput() {
        return !bugTitle.getText().isEmpty() &&
                !bugDescription.getText().isEmpty() &&
                statusComboBox.getValue() != null &&
                priorityComboBox.getValue() != null &&
                severityComboBox.getValue() != null &&
                !estimatedTime.getText().isEmpty();
    }

    private void showValidationAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText("All fields must be filled.");
        alert.showAndWait();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public void initializeForEdit(Bug bugToEdit) throws SQLException {
        bug = bugToEdit;

        bugTitle.setText(bugToEdit.getBugTitle());
        bugDescription.setText(bugToEdit.getBugDescription());
        statusComboBox.setValue(bugToEdit.getStatus());
        priorityComboBox.setValue(bugToEdit.getPriority());
        severityComboBox.setValue(bugToEdit.getSeverity());
        estimatedTime.setText(bugToEdit.getEstimatedTimeToComplete());

    }


}
