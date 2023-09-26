package com.example.bugtracker.Controller.ProjectManager;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Controller.Chat.ChatController;
import com.example.bugtracker.Controller.DialogController.ProjectDialogController;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Model.Entity.Roles;
import com.example.bugtracker.Model.Entity.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class ProjectManagerProjectsController implements Initializable {
    @FXML
    public Button dashboardButton;
    @FXML
    public Button projectsButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button logoutButton;
    @FXML
    public TableView<Project> projectTable;
    @FXML
    public TableColumn<Project, Integer> projectIdColumn;
    @FXML
    public TableColumn<Project, String> projectNameColumn;
    @FXML
    public TableColumn<Project, String> descriptionColumn;
    @FXML
    public TableColumn<Project, String> statusColumn;
    @FXML
    public TableColumn<Project, String> priorityColumn;
    @FXML
    public TableColumn<Project, LocalDate> deadlineColumn;
    @FXML
    public Label daysTillDeadlineLabel;
    @FXML
    public TableView<User> membersTable;
    public TableColumn<User, String> membersColumn;
    public TableColumn<User, Roles> roleColumn;
    public TableColumn<User, Integer> assignedBugsColumn;
    public Label projectCompletedLabel;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button addButton;
    @FXML
    public Button chatButton;
    public TableColumn<User,String> userIdColumn;
    public TableColumn<User,String> usernameColumn;


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

        projectIdColumn.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        projectNameColumn.setCellFactory(col -> {
            TableCell<Project, String> cell = new TableCell<Project, String>() {
                final Hyperlink hyperlink = new Hyperlink();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        // Set the project name as the hyperlink text
                        hyperlink.setText(item);
                        setGraphic(hyperlink);

                        // Define the action to perform when the hyperlink is clicked
                        hyperlink.setOnAction(event -> {
                            Project project = getTableView().getItems().get(getIndex());
                            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            currentStage.close();
                            // Navigate to the project details page with the selected project
                            navigateToProjectDetailsPage(project);
                        });
                    }
                }
            };
            return cell;
        });

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("projectDescription"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        deadlineColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter(dateFormatter, dateFormatter)));


        projectTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // When a project is selected
                // Calculate days till deadline or display "Project Completed"
                String daysTillDeadline = calculateDaysTillDeadline(newValue.getEndDate());

                if (daysTillDeadline.equals("Project Completed")) {
                    daysTillDeadlineLabel.setVisible(false);
                    projectCompletedLabel.setText("Project Completed");
                    projectCompletedLabel.setVisible(true);
                } else {
                    projectCompletedLabel.setVisible(false);
                    daysTillDeadlineLabel.setText(daysTillDeadline);
                    daysTillDeadlineLabel.setVisible(true);
                }

                try {
                    fillMembersTable(newValue);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // If no project is selected, clear the labels
                clearLabel();
            }
        });



        fillProjectTable();


    }



    private void fillProjectTable() {
        List<Project> projects = ProjectDAO.getProjectsForUser(loggedInUser);

        // Clear the existing items in the table
        projectTable.getItems().clear();

        // Add the fetched projects to the table
        projectTable.getItems().addAll(projects);
    }

    private String calculateDaysTillDeadline(LocalDate deadline) {
        LocalDate currentDate = LocalDate.now();
        long days = ChronoUnit.DAYS.between(currentDate, deadline);
        if (days < 0) {
            return "Project Completed";
        } else {
            return String.valueOf(days);
        }
    }

    private void fillMembersTable(Project selectedProject) throws SQLException {
        // Fetch the project members for the selected project
        List<User> members = ProjectDAO.getProjectMembers(selectedProject.getProjectId());

        // Clear the existing items in the table
        membersTable.getItems().clear();

        // Add the fetched members to the table
        membersTable.getItems().addAll(members);

        membersColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String fullName = user.getFirstName() + " " + user.getLastName();
            return new SimpleStringProperty(fullName);
        });
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));



    }
    private void clearLabel() {
        daysTillDeadlineLabel.setText("");
    }
    private void navigateToProjectDetailsPage(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/ProjectManager/ProjectManagerProjectDetails.fxml"));
            Parent root = loader.load();
            ProjectManagerProjectDetailsController controller = loader.getController();
            controller.setProject(project);

            // Create a new scene and show it
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Project Details");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
        Project selectedProject = projectTable.getSelectionModel().getSelectedItem(); // Assuming tableView is your project table view

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
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Deletion Failed", "Failed to delete the project. Please try again.");
                }
            }
        } else {
            showErrorMessage("Please select a project to delete.");
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
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
