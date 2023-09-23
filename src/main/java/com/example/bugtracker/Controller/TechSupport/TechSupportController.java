package com.example.bugtracker.Controller.TechSupport;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.Entity.Project;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;


public class TechSupportController implements Initializable {

   @FXML
   private TextField searchBar;
    @FXML
    private TableView<Project> projectTable;
    @FXML private TableColumn<Project, Integer> projectIdColumn;
    @FXML private TableColumn<Project, String> projectNameColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, String> priorityColumn;
    @FXML private TableColumn<Project, String> projectManagerColumn;
    @FXML private TableColumn<Project, String> descriptionColumn;
    @FXML private TableColumn<Project, LocalDate> deadlineColumn;

    @FXML
    public Button settingsButton;
    @FXML
    public Button techSupportButton;
    @FXML
    public Button logoutButton;
    @FXML
    private Label nameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label userIdLabel;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ButtonHandler settingsButtonHandler = new ButtonHandler("/com/example/bugtracker/TechSupport/TechSupportSettings.fxml");
        ButtonHandler techSupportButtonHandler = new ButtonHandler("/com/example/bugtracker/TechSupport.fxml");
        ButtonHandler logoutButtonHandler = new ButtonHandler("/com/example/bugtracker/Login/Login.fxml");

        settingsButton.setOnAction(settingsButtonHandler);
        techSupportButton.setOnAction(techSupportButtonHandler);
        logoutButton.setOnAction(logoutButtonHandler);

        usernameLabel.setText(loggedInUser.getUsername());
        userIdLabel.setText(String.valueOf(loggedInUser.getUserId()));
        nameLabel.setText(loggedInUser.getFullName());

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
                            // Navigate to the project details page with the selected project
                            openBugsForProject(project);
                        });
                    }
                }
            };
            return cell;
        });

        fillProjectTable();

    }

    private void openBugsForProject(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/TechSupport/TechSupportDetails.fxml"));
            Parent detailsRoot = loader.load();

            TechSupportDetailsController detailsController = loader.getController();
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


}
