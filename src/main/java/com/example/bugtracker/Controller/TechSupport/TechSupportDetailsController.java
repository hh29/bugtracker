package com.example.bugtracker.Controller.TechSupport;

import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Controller.DialogController.BugDialogController;
import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Controller.Chat.ChatController;
import com.example.bugtracker.Model.DAO.BugDAO;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class TechSupportDetailsController implements Initializable {
    
    @FXML
    private Button backButton;
   @FXML
   private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button chatButton;
    Project selectedProject;
    @FXML
    private TableView<Bug> tableView;
    @FXML
    private Button techSupportButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;
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
    private TableColumn<Bug, Hyperlink> commentsColumn;
    @FXML
    private TableColumn<Bug, LocalDate> updatedDateColumn;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ButtonHandler TechSupportButtonHandler = new ButtonHandler("/com/example/bugtracker/TechSupport.fxml");
        ButtonHandler settingsButtonHandler = new ButtonHandler("/com/example/bugtracker/TechSupport/TechSupportSettings.fxml");
        ButtonHandler logoutButtonHandler = new ButtonHandler("/com/example/bugtracker/Login/Login.fxml");


        techSupportButton.setOnAction(TechSupportButtonHandler);
        settingsButton.setOnAction(settingsButtonHandler);
        logoutButton.setOnAction(logoutButtonHandler);

        // Initialize the TableView columns to map to the Bug class properties
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


        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        updatedDateColumn.setCellValueFactory(date -> date.getValue().updatedDateProperty());
        updatedDateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter(dateFormatter, dateFormatter)));

        severityColumn.setCellValueFactory(data -> data.getValue().severityProperty());

    }

    public void setSelectedProject(Project project) {
        selectedProject = project;
        populateBugsTableForSelectedProject();
    }

    private void populateBugsTableForSelectedProject() {
        if (selectedProject != null && loggedInUser != null) {
            List<Bug> projectUserBugs = BugDAO.getBugsForSelectedProject(selectedProject);

            if (!projectUserBugs.isEmpty()) {
                tableView.setItems(FXCollections.observableArrayList(projectUserBugs));
            } else {
                // Handle the case when no bugs are associated with the project
                tableView.setPlaceholder(new Label("No tickets found for " + selectedProject.getProjectName()));
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
        chatStage.initOwner(tableView.getScene().getWindow());
        Scene scene = new Scene(root);
        chatStage.setScene(scene);

        // Pass the bug and logged-in user details to the ChatController
        ChatController ChatController = loader.getController();
        ChatController.setSelectedBug(bug);
        ChatController.setLoggedInUser(loggedInUser);

        chatStage.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleEditButton(ActionEvent actionEvent) {
        Bug selectedBug = tableView.getSelectionModel().getSelectedItem();
        if (selectedBug != null) {
            openBugDialogForEdit(selectedBug);
        } else {
            showAlert("No Ticket Selected", "Please select a Ticket to edit.");
        }
    }

    private void openBugDialogForEdit(Bug bug) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/BugDialog.fxml"));
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
        dialogStage.initOwner(tableView.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        BugDialogController controller = loader.getController();
        controller.initializeForEdit(bug);

        dialogStage.showAndWait();

        // After editing, refresh the bugs table
        populateBugsTableForSelectedProject();
    }

    public void handleAddButton(ActionEvent actionEvent) {
        // Load the BugDialogController FXML and create a new stage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/BugDialog.fxml"));
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
        dialogStage.initOwner(tableView.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        // Set up the BugDialogController
        BugDialogController controller = loader.getController();

        controller.setProjectId(selectedProject.getProjectId());

        dialogStage.showAndWait();

        // Check if the user clicked "OK" and get the bug details
        if (controller.isOkClicked()) {
            Bug newBug = controller.getBug();
            if (newBug != null) {
                // Insert the new bug into the database using your BugDAO
                BugDAO.insertBug(newBug);

                // Show a success message
                showAlert("Ticket Inserted", "New ticket has been successfully inserted.");
                dialogStage.close();
            }
        }
        // Refresh the bugs table
        populateBugsTableForSelectedProject();
    }

    public void backButton(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/TechSupport.fxml"));
            Parent projectsRoot = loader.load();

            // Get the stage of the current scene
            Stage currentStage = (Stage) tableView.getScene().getWindow();

            // Set the new scene to the current stage
            Scene projectsScene = new Scene(projectsRoot);
            currentStage.setScene(projectsScene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleChatButton(ActionEvent actionEvent) {
        Bug bug = tableView.getSelectionModel().getSelectedItem(); // Assuming tableView is your project table view

        if (bug != null) {
            openChatLobby(bug);
        } else {
            showAlert("No ticket selected","Please select a project before opening the chat lobby.");
        }

    }
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Bug selectedBug = tableView.getSelectionModel().getSelectedItem();
        if (selectedBug != null) {
            boolean confirmDelete = showConfirmationDialog("Delete Bug",
                    "Are you sure you want to delete this bug?");
            if (confirmDelete) {
                // Delete the bug from your data source (e.g., database or list)
                // For example, if you're using a list:
                tableView.getItems().remove(selectedBug);
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
}
