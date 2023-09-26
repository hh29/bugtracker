package com.example.bugtracker.Controller.Developer;

import com.example.bugtracker.Model.DAO.UserDAO;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.BugDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.example.bugtracker.DBConnection.DBConnection.getConnection;
import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;


public class DeveloperDashboardController implements Initializable {
    @FXML
    public Button projectsButton;
    @FXML
    public Button ticketsButton;
    @FXML
    public Button dashboardButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button logoutButton;

    @FXML
    private Label activeProjectsLabel;

    @FXML
    private Label unresolvedBugsLabel;

    @FXML
    private Label projectsCompletedLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private PieChart bugPriorityPieChart;
    @FXML
    private PieChart unresolvedBugsPieChart;
    @FXML
    private PieChart severityPieChart;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ButtonHandler dashboardButtonHandler = new ButtonHandler("/com/example/bugtracker/Developer/DeveloperDashboard.fxml");
        ButtonHandler ticketsButtonHandler = new ButtonHandler("/com/example/bugtracker/Developer/DeveloperTickets.fxml");
        ButtonHandler projectsButtonHandler = new ButtonHandler("/com/example/bugtracker/Developer/DeveloperProjects.fxml");
        ButtonHandler settingsButtonHandler = new ButtonHandler("/com/example/bugtracker/Developer/DeveloperSettings.fxml");
        ButtonHandler logoutButtonHandler = new ButtonHandler("/com/example/bugtracker/Login/Login.fxml");


        // Set the common event handler for each button
        dashboardButton.setOnAction(dashboardButtonHandler);
        ticketsButton.setOnAction(ticketsButtonHandler);
        projectsButton.setOnAction(projectsButtonHandler);
        settingsButton.setOnAction(settingsButtonHandler);
        logoutButton.setOnAction(logoutButtonHandler);




        updateDashboardData();
        updateBugPriorityPieChart();
        updateBugStatusPieChart();
        updateSeverityPieChart();
    }



    private void updateDashboardData() {
        if (loggedInUser != null) {
            roleLabel.setText(String.valueOf(loggedInUser.getRole()));

            userIdLabel.setText(String.valueOf(loggedInUser.getUserId()));
            usernameLabel.setText(loggedInUser.getUsername());
            nameLabel.setText(loggedInUser.getFullName());

            int numberOfActiveProjects = getNumberOfActiveProjects();
            int numberOfUnresolvedBugs = getNumberOfUnresolvedBugs();
            int numberOfProjectsCompleted = getNumberOfProjectsCompleted();

            // Set default values if necessary
            String activeProjectsText = String.valueOf(numberOfActiveProjects);
            String unresolvedBugsText = String.valueOf(numberOfUnresolvedBugs);
            String projectsCompletedText = String.valueOf(numberOfProjectsCompleted);

            // Update labels
            activeProjectsLabel.setText(activeProjectsText);
            unresolvedBugsLabel.setText(unresolvedBugsText);
            projectsCompletedLabel.setText(projectsCompletedText);
        } else {
            // Set default values or display a message when loggedInUser is null
            activeProjectsLabel.setText("0");
            unresolvedBugsLabel.setText("0");
            projectsCompletedLabel.setText("0");
        }
    }

    private int getNumberOfActiveProjects() {
        return UserDAO.getNumberOfActiveProjects();
    }

    private int getNumberOfUnresolvedBugs() {
       return UserDAO.getNumberOfUnresolvedBugs();
    }

    private int getNumberOfProjectsCompleted() {
        int numberOfProjectsCompleted = 0;
        String query = "SELECT COUNT(DISTINCT projects.project_id) AS num_projects_completed " +
                "FROM projects " +
                "JOIN project_user ON projects.project_id = project_user.project_id " +
                "WHERE project_user.user_id = ? AND projects.status = 'Completed'";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, loggedInUser.getUserId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                numberOfProjectsCompleted = resultSet.getInt("num_projects_completed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfProjectsCompleted;
    }


    public DeveloperDashboardController() {
    }

    private void updateBugPriorityPieChart() {
        if (loggedInUser == null) {
            // If the user is not logged in, clear the pie chart and return
            bugPriorityPieChart.setData(FXCollections.emptyObservableList());
            return;
        }

        // Fetch bug data from the database

        List<Bug> bugs = BugDAO.getBugsForUser(loggedInUser);

        // Calculate the number of bugs for each priority
        Map<String, Integer> priorityCountMap = new HashMap<>();
        for (Bug bug : bugs) {
            String priority = bug.getPriority();
            priorityCountMap.put(priority, priorityCountMap.getOrDefault(priority, 0) + 1);
        }

        // Create a list of PieChart.Data objects for the pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : priorityCountMap.entrySet()) {
            String priority = entry.getKey();
            int count = entry.getValue();
            pieChartData.add(new PieChart.Data(priority, count));
        }

        // Update the pie chart data
        bugPriorityPieChart.setData(pieChartData);
    }

    private void updateBugStatusPieChart() {
        if (loggedInUser == null) {
            // If the user is not logged in, clear the pie chart and return
            unresolvedBugsPieChart.setData(FXCollections.emptyObservableList());
            return;
        }

        // Fetch bug data from the database
        List<Bug> bugs = BugDAO.getBugsForUser(loggedInUser);

        // Calculate the number of bugs for each status
        Map<String, Integer> StatusCountMap = new HashMap<>();
        for (Bug bug : bugs) {
            String status = bug.getStatus();
            StatusCountMap.put(status, StatusCountMap.getOrDefault(status, 0) + 1);
        }

        // Create a list of PieChart.Data objects for the pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : StatusCountMap.entrySet()) {
            String status = entry.getKey();
            int count = entry.getValue();
            pieChartData.add(new PieChart.Data(status, count));
        }

        // Update the pie chart data
        unresolvedBugsPieChart.setData(pieChartData);
    }

    private void updateSeverityPieChart() {
        if (loggedInUser == null) {
            // If the user is not logged in, clear the pie chart and return
            severityPieChart.setData(FXCollections.emptyObservableList());
            return;
        }

        // Fetch bug data from the database
        List<Bug> bugs = BugDAO.getBugsForUser(loggedInUser);

        // Calculate the number of bugs for each status
        Map<String, Integer> SeverityCountMap = new HashMap<>();
        for (Bug bug : bugs) {
            String severity = bug.getSeverity();
            SeverityCountMap.put(severity, SeverityCountMap.getOrDefault(severity, 0) + 1);
        }

        // Create a list of PieChart.Data objects for the pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : SeverityCountMap.entrySet()) {
            String severity = entry.getKey();
            int count = entry.getValue();


            PieChart.Data data = new PieChart.Data(severity, count);
            pieChartData.add(data);

            // Update the pie chart data
            severityPieChart.setData(pieChartData);
        }
    }
}
