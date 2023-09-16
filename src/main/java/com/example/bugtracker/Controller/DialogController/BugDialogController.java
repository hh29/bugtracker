package com.example.bugtracker.Controller.DialogController;

import com.example.bugtracker.Model.DAO.UserDAO;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Controller.Login.LoginController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.example.bugtracker.Model.DAO.BugDAO.*;

public class BugDialogController implements Initializable {
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
    boolean okClicked = false;
    private int projectId;
    private Bug bug;

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    public void setBug(Bug bug) {
        this.bug = bug;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize and populate the ComboBoxes with values
        statusComboBox.getItems().addAll("New", "In Progress", "Testing","Reopened", "Closed");
        priorityComboBox.getItems().addAll("Low", "Medium", "High");
        severityComboBox.getItems().addAll("Minor", "Major", "Critical");
    }

    @FXML
    private void handleOkButton(ActionEvent actionEvent) {

        if (isValidInput()) {
            boolean hasChanges = true;

            if (bug != null) {
                // Editing an existing bug
                String originalTitle = bug.getBugTitle();
                String originalDescription = bug.getBugDescription();
                String originalStatus = bug.getStatus();
                String originalPriority = bug.getPriority();
                String originalSeverity = bug.getSeverity();
                String originalEstimatedTime = bug.getEstimatedTimeToComplete();

                if (originalTitle.equals(bugTitle.getText()) &&
                        originalDescription.equals(bugDescription.getText()) &&
                        originalStatus.equals(statusComboBox.getValue()) &&
                        originalPriority.equals(priorityComboBox.getValue()) &&
                        originalSeverity.equals(severityComboBox.getValue()) &&
                        originalEstimatedTime.equals(estimatedTime.getText())) {
                    // No changes are made, retain original Updated Date
                    hasChanges = false;
                }
            }

            if (bug == null || hasChanges) {
                if (bug == null) {
                    // Adding a new bug
                    Bug newBug = new Bug();
                    newBug.setBugTitle(bugTitle.getText());
                    newBug.setBugDescription(bugDescription.getText());
                    newBug.setStatus(statusComboBox.getValue());
                    newBug.setPriority(priorityComboBox.getValue());
                    newBug.setSeverity(severityComboBox.getValue());
                    newBug.setEstimatedTimeToComplete(estimatedTime.getText());

                    newBug.setReporterId(LoginController.loggedInUser.getUserId());
                    newBug.setProjectId(projectId);

                    // Set the created and updated dates to the current date and time
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    newBug.setCreatedDate(currentDateTime.toLocalDate());
                    newBug.setUpdatedDate(currentDateTime.toLocalDate());

                    // Insert the new bug into the database using your BugDAO
                    int bugId = BugDAO.insertBug(newBug);
                    newBug.setBugId(bugId);

                    if ("Testing".equals(newBug.getStatus()) && showMoveToTestingConfirmation()) {
                        // If the user confirms moving to testing, assign the bug to testers
                        assignBugToTester(projectId, newBug.getBugId());
                    }

                    showAlert("Ticket Inserted", "New ticket has been successfully inserted.");
                } else {

                    String oldStatus = bug.getStatus();

                    // Editing an existing bug
                    bug.setBugTitle(bugTitle.getText());
                    bug.setBugDescription(bugDescription.getText());
                    bug.setStatus(statusComboBox.getValue());
                    bug.setPriority(priorityComboBox.getValue());
                    bug.setSeverity(severityComboBox.getValue());
                    bug.setEstimatedTimeToComplete(estimatedTime.getText());


                    // Set the updated date to the current date and time
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    bug.setUpdatedDate(currentDateTime.toLocalDate());

                    String newStatus = bug.getStatus();  // Get the new status

                    // Debugging: Print the old and new status
                    System.out.println("Old Status: " + oldStatus);
                    System.out.println("New Status: " + newStatus);

                    // Check if the status has changed to "Testing"
                    if (!oldStatus.equals(newStatus) && "Testing".equals(newStatus)) {

                        System.out.println("Status changed to 'Testing', checking for assigned tester...");

                        if (getAssignedUser(bug.getBugId()) == null) {
                            // No assigned tester, assign the bug to a tester with the least tasks
                            boolean confirmed = showMoveToTestingConfirmation();
                            if (confirmed) {
                                System.out.println("Handling No Assigned Tester Case...");

                                handleNoAssignedTesterCase(bug);
                            } else {
                                // User clicked "No," do not update the status
                                return;
                            }
                        } else {
                            // Assigned tester exists, only show the confirmation when changing from another status to "Testing"
                            System.out.println("Handling Assigned Tester Case...");

                            boolean confirmed = showMoveToTestingConfirmation();
                            if (confirmed) {
                                handleAssignedTesterCase(bug);
                            } else {
                                // User clicked "No," do not update the status
                                return;
                            }
                        }
                    }

                    // Update the bug in the database using BugDAO
                    boolean updateSuccessful = BugDAO.updateBug(bug);
                        if (updateSuccessful) {
                            showAlert("Ticket Updated", "The ticket has been successfully updated.");
                        } else {
                            showAlert("Update Failed", "Failed to update the ticket. Please try again.");
                        }
                    }


                    okClicked = true;
                    closeDialog();
                } else{
                    // No changes made, just close the dialog
                    closeDialog();
                }
            } else {
                showValidationAlert();
            }
        }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showMoveToTestingConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Move to Testing Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to move this bug to 'Testing'?");

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
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
    public Bug getBug() {
        return bug;
    }
    public void initializeForEdit(Bug bugToEdit) {
        bug = bugToEdit;

        bugTitle.setText(bugToEdit.getBugTitle());
        bugDescription.setText(bugToEdit.getBugDescription());
        statusComboBox.setValue(bugToEdit.getStatus());
        priorityComboBox.setValue(bugToEdit.getPriority());
        severityComboBox.setValue(bugToEdit.getSeverity());
        estimatedTime.setText(bugToEdit.getEstimatedTimeToComplete());

    }
    private void showNoTestersAssignedAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Testers Assigned");
        alert.setHeaderText(null);
        alert.setContentText("There are no testers assigned to this project. Please assign testers to the project before moving to testing.");
        alert.showAndWait();
    }
    private void handleNoAssignedTesterCase(Bug bug) {
        int selectedTesterUserId = findTesterWithLeastTasks(bug.getProjectId());

        if (selectedTesterUserId != -1) {
            assignBugToUser(bug.getBugId(), selectedTesterUserId);
        } else {
            showNoTestersAssignedAlert();
        }
    }

    private void handleAssignedTesterCase(Bug bug) {
          int selectedTesterUserId = findTesterWithLeastTasks(bug.getProjectId());
            System.out.println(selectedTesterUserId);


        if (selectedTesterUserId != -1) {
            updateBugAssignedUser(bug.getBugId(), selectedTesterUserId);
        } else {
            showNoTestersAssignedAlert();
        }
    }


}
