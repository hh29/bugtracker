package com.example.bugtracker.Controller.ProjectManager;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Model.Entity.Project;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class ProjectManagerDashboardController implements Initializable {
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
    private Label nameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private BarChart<String, Number> bugChart;
    @FXML
    private PieChart bugSeverityPieChart;
    @FXML
    private PieChart bugPriorityPieChart;
    private ObservableList<PieChart.Data> severityData = FXCollections.observableArrayList();
    private ObservableList<PieChart.Data> bugPriorityData = FXCollections.observableArrayList();


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

        usernameLabel.setText(loggedInUser.getUsername());
        userIdLabel.setText(String.valueOf(loggedInUser.getUserId()));
        nameLabel.setText(loggedInUser.getFullName());

        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("projectDescription"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        fillProjectTable();

        // Initialize and configure the severityPieChart
        bugSeverityPieChart.setTitle("Severity");
        bugSeverityPieChart.setData(severityData);

        // Initialize and configure the bugPriorityPieChart
        bugPriorityPieChart.setTitle("Bug Priority");
        bugPriorityPieChart.setData(bugPriorityData);

        // Add a listener to the projectTable selection
        projectTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {

                // Update PieCharts based on the selected project
                updateSeverityPieChart(newSelection);
                updateBugPriorityPieChart(newSelection);
            } else {
                // No project is selected, you can clear or hide the chart if needed
                bugChart.getData().clear();
                bugSeverityPieChart.getData().clear();
                bugPriorityPieChart.getData().clear();
            }
        });
    }

    private void fillProjectTable() {
        List<Project> projects = ProjectDAO.getProjectsForUser(loggedInUser);

        // Clear the existing items in the table
        projectTable.getItems().clear();

        // Add the fetched projects to the table
        projectTable.getItems().addAll(projects);
    }


    private void updateSeverityPieChart(Project selectedProject) {
        severityData.clear();

        // Retrieve the counts for each severity level
        Map<String, Integer> severityCounts = BugDAO.getSeverityCounts(selectedProject.getProjectId());

        // Populate the PieChart
        for (Map.Entry<String, Integer> entry : severityCounts.entrySet()) {
            severityData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    // Method to update the Bug Priority PieChart
    private void updateBugPriorityPieChart(Project selectedProject) {
        bugPriorityData.clear();

        // Retrieve the counts for each bug priority level
        Map<String, Integer> priorityCounts = BugDAO.getPriorityCounts(selectedProject.getProjectId());

        // Populate the PieChart
        for (Map.Entry<String, Integer> entry : priorityCounts.entrySet()) {
            bugPriorityData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

}
