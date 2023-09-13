package com.example.bugtracker.Controller.Admin;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Controller.Chat.ChatController;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Controller.DialogController.ProjectDialogController;
import com.example.bugtracker.Controller.TechSupport.TechSupportDetailsController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class AdminProjectsController implements Initializable {
    @FXML
    public Button projectsButton;
    @FXML
    public Button usersButton;
    @FXML
    public Button dashboardButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button logoutButton;
    @FXML
    public Button addButton;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button chatButton;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Project> projectTable;
    @FXML
    private TableColumn<Project, Integer> projectIdColumn;
    @FXML
    private TableColumn<Project, String> projectNameColumn;
    @FXML
    private TableColumn<Project, String> statusColumn;
    @FXML
    private TableColumn<Project, String> priorityColumn;
    @FXML
    private TableColumn<Project, String> projectManagerColumn;
    @FXML
    private TableColumn<Project, String> descriptionColumn;
    @FXML
    private TableColumn<Project, LocalDate> deadlineColumn;
    @FXML
    public TableColumn<Project, Hyperlink> actionsColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ButtonHandler dashboardButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminDashboard.fxml");
        ButtonHandler usersButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminUsers.fxml");
        ButtonHandler projectsButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminProjects.fxml");
        ButtonHandler settingsButtonHandler = new ButtonHandler("/com/example/bugtracker/AdminSettings.fxml");
        ButtonHandler logoutButtonHandler = new ButtonHandler("/com/example/bugtracker/Login/Login.fxml");


        // Set the common event handler for each button
        dashboardButton.setOnAction(dashboardButtonHandler);
        usersButton.setOnAction(usersButtonHandler);
        projectsButton.setOnAction(projectsButtonHandler);
        settingsButton.setOnAction(settingsButtonHandler);
        logoutButton.setOnAction(logoutButtonHandler);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProjects(newValue);
        });


        projectIdColumn.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        projectManagerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getManagerFirstName() + " " + cellData.getValue().getManagerLastName()));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("projectDescription"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Hyperlink hyperlink = new Hyperlink("View Bugs");

            {
                hyperlink.setOnAction(event -> {
                    Project project = getTableRow().getItem();
                    if (project != null) {
                        openBugsForProject(project);
                    }
                });
            }

            @Override
            protected void updateItem(Hyperlink item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setGraphic(hyperlink);
                } else {
                    setGraphic(null);
                }
            }
        });
        fillProjectTable();
    }
    private void openBugsForProject(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Admin/AdminProjectDetails.fxml"));
            Parent detailsRoot = loader.load();

            AdminProjectDetailsController detailsController = loader.getController();
            detailsController.setSelectedProject(project);

            // Get the stage of the current scene
            Stage currentStage = (Stage) projectTable.getScene().getWindow();

            // Set the new scene to the current stage
            Scene detailsScene = new Scene(detailsRoot);
            currentStage.setScene(detailsScene);
            currentStage.setTitle("Project Details");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillProjectTable() {
        List<Project> projects = ProjectDAO.getAllProjects();

        // Clear the existing items in the table
        projectTable.getItems().clear();

        // Add the fetched projects to the table
        projectTable.getItems().addAll(projects);
    }
    private void filterProjects(String keyword) {
        Predicate<Project> projectFilter = project -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return true; // Show all projects when search field is empty
            }
            String lowerCaseKeyword = keyword.toLowerCase();
            try {
                int projectId = Integer.parseInt(keyword);
                return project.getProjectId() == projectId ||
                        project.getProjectName().toLowerCase().contains(lowerCaseKeyword) ||
                        project.getProjectDescription().toLowerCase().contains(lowerCaseKeyword)||
                        project.getManagerFirstName().toLowerCase().contains(lowerCaseKeyword) ||
                        project.getManagerLastName().toLowerCase().contains(lowerCaseKeyword) ||
                        project.getPriority().toLowerCase().contains(lowerCaseKeyword);
            } catch (NumberFormatException e) {
                return project.getProjectName().toLowerCase().contains(lowerCaseKeyword) ||
                        project.getProjectDescription().toLowerCase().contains(lowerCaseKeyword);
            }
        };

        List<Project> allProjects = ProjectDAO.getAllProjects();
        List<Project> filteredProjects = allProjects.stream().filter(projectFilter).toList();

        // Clear the existing items in the table
        projectTable.getItems().clear();

        // Add the filtered projects to the table
        projectTable.getItems().addAll(filteredProjects);
    }
    private void openChatLobbyForProject(Project project) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Chat.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage chatStage = new Stage();
        chatStage.setTitle("Chat Lobby for Project " + project.getProjectId());
        chatStage.initModality(Modality.WINDOW_MODAL);
        chatStage.initOwner(projectTable.getScene().getWindow());
        Scene scene = new Scene(root);
        chatStage.setScene(scene);

        // Pass the project and logged-in user details to the ChatController
        ChatController chatController = loader.getController();
        chatController.setSelectedProject(project);
        chatController.setLoggedInUser(loggedInUser);

        chatStage.showAndWait();
    }

    public void handleEditButton(ActionEvent actionEvent) throws SQLException {
        Project selectedProject = projectTable.getSelectionModel().getSelectedItem();

        if (selectedProject != null) {
            openProjectDialogForEdit(selectedProject);
        } else {
            showAlert("No Project Selected", "Please select a Project to edit.");
        }
    }

    private void openProjectDialogForEdit(Project project) throws SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/ProjectDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Project");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(projectTable.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        ProjectDialogController controller = loader.getController();
        controller.setProject(project);
        controller.populateUserList(project);
        controller.initializeForEdit(project);

        dialogStage.showAndWait();

        // After editing, refresh the projects table
        fillProjectTable();
    }


    public void handleDeleteButton(ActionEvent actionEvent) {
        Project selectedProject = projectTable.getSelectionModel().getSelectedItem();

        if (selectedProject != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Are you sure you want to delete the selected project?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    projectTable.getItems().remove(selectedProject);
                    ProjectDAO.deleteProject(selectedProject.getProjectId());
                    showAlert("Project Deleted", "Project has been successfully deleted.");
                    fillProjectTable(); // Refresh the projects table
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Deletion Failed", "Failed to delete the project. Please try again.");
                }
            }
        } else {
            showErrorMessage("Please select a project to delete.");
        }
    }

    public void handleAddButton(ActionEvent actionEvent) throws SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/ProjectDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add New Project");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(projectTable.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        // Set up the ProjectDialogController
        ProjectDialogController controller = loader.getController();
        controller.setProject(null); // Pass null to indicate it's a new project

        dialogStage.showAndWait();

        // Check if the user clicked "OK" and get the project details
        if (controller.isOkClicked()) {
            Project newProject = controller.getProject();
            if (newProject != null) {
                try {
                    ProjectDAO.insertProject(newProject);
                    showAlert("New Project Created", "New Project has been successfully created.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Insertion Failed", "Failed to insert the project. Please try again.");
                }
            }
            // Refresh the projects table
            fillProjectTable();
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleChatButton(ActionEvent actionEvent) {
        Project selectedProject = projectTable.getSelectionModel().getSelectedItem(); // Assuming tableView is your project table view

        if (selectedProject != null) {
            openChatLobbyForProject(selectedProject);
        } else {
            showErrorMessage("Please select a project before opening the chat lobby.");
        }
    }
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void refreshData() {
        fillProjectTable();
    }

}
