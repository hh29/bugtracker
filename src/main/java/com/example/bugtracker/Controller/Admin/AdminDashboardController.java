package com.example.bugtracker.Controller.Admin;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.BugDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


import java.net.URL;


import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
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
    private Label activeProjectsLabel;
    @FXML
    private Label unresolvedBugsLabel;
    @FXML
    private LineChart<String, Number> bugsTimeChart;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ButtonHandler dashboardButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminDashboard.fxml");
        ButtonHandler usersButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminUsers.fxml");
        ButtonHandler projectsButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminProjects.fxml");
        ButtonHandler settingsButtonHandler = new ButtonHandler("/com/example/bugtracker/Admin/AdminSettings.fxml");
        ButtonHandler logoutButtonHandler = new ButtonHandler("/com/example/bugtracker/Login/Login.fxml");


        // Set the common event handler for each button
        dashboardButton.setOnAction(dashboardButtonHandler);
        usersButton.setOnAction(usersButtonHandler);
        projectsButton.setOnAction(projectsButtonHandler);
        settingsButton.setOnAction(settingsButtonHandler);
        logoutButton.setOnAction(logoutButtonHandler);


        configureBugCountChart();

    }
    private void configureBugCountChart() {
        bugsTimeChart.setTitle("Bug Count Over Time");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Bug Count");
        yAxis.setLabel("Bug Count");
        yAxis.setTickUnit(1);

        bugsTimeChart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> bugCounts = BugDAO.getBugCountsByMonth();

        for (Map.Entry<String, Integer> entry : bugCounts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        bugsTimeChart.getData().add(series);
    }

}