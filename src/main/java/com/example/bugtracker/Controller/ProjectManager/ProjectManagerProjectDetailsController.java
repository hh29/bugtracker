package com.example.bugtracker.Controller.ProjectManager;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Controller.Chat.ChatController;
import com.example.bugtracker.Controller.DialogController.AssignBugDialogController;
import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Model.Entity.Project;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class ProjectManagerProjectDetailsController implements Initializable {
    private Project project;
    @FXML
    public Button dashboardButton;
    @FXML
    public Button projectsButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button logoutButton;
    @FXML
    private TableColumn<Bug, Integer> bugIDColumn;

    @FXML
    private TableColumn<Bug, String> bugTitleColumn;
    @FXML
    private TableColumn<Bug, String>  descriptionColumn;

    @FXML
    private TableColumn<Bug, String> statusColumn;

    @FXML
    private TableColumn<Bug, String> severityColumn;
    @FXML private TableColumn<Bug,String> assignedToColumn;
    @FXML
    private TableView<Bug> bugTable;
    @FXML
    private TableColumn<Bug, LocalDate> updatedDateColumn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ButtonHandler dashboardButtonHandler = new ButtonHandler("/com/example/bugtracker/ProjectManager/ProjectManagerDashboard.fxml");
        ButtonHandler projectsButtonHandler = new ButtonHandler("/com/example/bugtracker/ProjectManager/ProjectManagerProjects.fxml");
        ButtonHandler settingsButtonHandler = new ButtonHandler("/com/example/bugtracker/ProjectManager/ProjectManagerSettings.fxml");
        ButtonHandler logoutButtonHandler = new ButtonHandler("/com/example/bugtracker/Login/Login.fxml");


        // Set the common event handler for each button
        dashboardButton.setOnAction(dashboardButtonHandler);
        projectsButton.setOnAction(projectsButtonHandler);
        settingsButton.setOnAction(settingsButtonHandler);
        logoutButton.setOnAction(logoutButtonHandler);

        bugIDColumn.setCellValueFactory(data -> data.getValue().bugIdProperty().asObject());
        bugTitleColumn.setCellValueFactory(data -> data.getValue().bugTitleProperty());
        descriptionColumn.setCellValueFactory(data -> data.getValue().bugDescriptionProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        assignedToColumn.setCellValueFactory(data -> {
            Bug bug = data.getValue();
            String assigneeFirstName = bug.getAssigneeFirstName();
            String assigneeLastName = bug.getAssigneeLastName();
            if (assigneeFirstName != null && assigneeLastName != null) {
                return new SimpleStringProperty(assigneeFirstName + " " + assigneeLastName);
            } else {
                return new SimpleStringProperty("Unassigned");
            }
        });

        updatedDateColumn.setCellValueFactory(date ->date.getValue().updatedDateProperty());
        severityColumn.setCellValueFactory(data -> data.getValue().severityProperty());



    }

    public void setProject(Project project) {
        this.project = project;
        populateBugsTableForSelectedProject();
    }



    public void handleChatButton(ActionEvent actionEvent) {
        Bug bug = bugTable.getSelectionModel().getSelectedItem(); // Assuming tableView is your project table view

        if (bug != null) {
            openChatLobby(bug);
        } else {
            showAlert("No ticket selected","Please select a project before opening the chat lobby.");
        }

    }

    private void populateBugsTableForSelectedProject() {
        if (project != null) {
            List<Bug> projectUserBugs = BugDAO.getBugsForSelectedProject(project);

            if (!projectUserBugs.isEmpty()) {
                bugTable.setItems(FXCollections.observableArrayList(projectUserBugs));
            } else {
                // Handle the case when no bugs are associated with the project
                bugTable.setPlaceholder(new Label("No tickets found for " + project.getProjectName()));
            }
        }
    }
    private void openChatLobby(Bug bug) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Chat.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage chatStage = new Stage();
        chatStage.setTitle("Chat Lobby for Bug " + bug.getBugId());
        chatStage.initModality(Modality.WINDOW_MODAL);
        chatStage.initOwner(bugTable.getScene().getWindow());
        Scene scene = new Scene(root);
        chatStage.setScene(scene);

        // Pass the bug and logged-in user details to the ChatController
        ChatController ChatController = loader.getController();
        ChatController.setSelectedBug(bug);
        ChatController.setLoggedInUser(loggedInUser);

        chatStage.showAndWait();
    }
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Bug selectedBug = bugTable.getSelectionModel().getSelectedItem();
        if (selectedBug != null) {
            boolean confirmDelete = showConfirmationDialog("Delete Bug",
                    "Are you sure you want to delete this bug?");
            if (confirmDelete) {
                // Delete the bug from your data source (e.g., database or list)
                // For example, if you're using a list:
                bugTable.getItems().remove(selectedBug);
                BugDAO.deleteBug(selectedBug);
            }
        } else {
            showAlert("No Bug Selected", "Please select a bug to delete.");
        }
    }
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleEditButton(ActionEvent actionEvent) throws SQLException {
        Bug selectedBug = bugTable.getSelectionModel().getSelectedItem();
        if (selectedBug != null) {
            openBugDialogForEdit(selectedBug);
        } else {
            showAlert("No Ticket Selected", "Please select a Ticket to edit.");
        }
    }

    private void openBugDialogForEdit(Bug bug) throws SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/AssignBugDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Bug");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(bugTable.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        AssignBugDialogController controller = loader.getController();
        controller.setBug(bug);
        controller.setProject(project);
        controller.initializeForEdit(bug);
        controller.populateComboBox(project);


        dialogStage.showAndWait();

        // After editing, refresh the bugs table
        populateBugsTableForSelectedProject();
    }

    public void handleAddButton(ActionEvent actionEvent) throws SQLException {
        // Load the BugDialogController FXML and create a new stage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/AssignBugDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add New Bug");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(bugTable.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        // Set up the BugDialogController
        AssignBugDialogController controller = loader.getController();

        controller.setProject(project);

        dialogStage.showAndWait();

        // Check if the user clicked "OK" and get the bug details
        if (controller.isOkClicked()) {
            Bug newBug = controller.getBug();
            if (newBug != null) {
                // Insert the new bug into the database using your BugDAO
                BugDAO.insertBug(newBug);


                // Show a success message
                showAlert("Ticket Inserted", "New ticket has been successfully inserted.");
                // Refresh the bugs table
                populateBugsTableForSelectedProject();
            }

        }
    }

    public void backButton(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/ProjectManager/ProjectManagerProjects.fxml"));
            Parent projectsRoot = loader.load();

            // Get the stage of the current scene
            Stage currentStage = (Stage) bugTable.getScene().getWindow();

            // Set the new scene to the current stage (reuse the existing stage)
            currentStage.getScene().setRoot(projectsRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
