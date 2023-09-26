package com.example.bugtracker.Controller.Admin;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.BugDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


import java.net.URL;


import java.util.Map;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

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
    private LineChart<String, Number> bugsTimeChart;
    @FXML
    private Label nameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label userIdLabel;




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

        usernameLabel.setText(loggedInUser.getUsername());
        userIdLabel.setText(String.valueOf(loggedInUser.getUserId()));
        nameLabel.setText(loggedInUser.getFullName());


        configureBugCountChart();

    }
    @FXML
    private void configureBugCountChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> bugCounts = BugDAO.getBugCountsByMonth();

        for (Map.Entry<String, Integer> entry : bugCounts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Set the y-axis for the LineChart
        bugsTimeChart.getYAxis().setLabel("Bug Count");
        bugsTimeChart.getXAxis().setLabel("Month");


        bugsTimeChart.getData().add(series);
    }


}