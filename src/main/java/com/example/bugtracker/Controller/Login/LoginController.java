package com.example.bugtracker.Controller.Login;

import com.example.bugtracker.Model.Login.LoginModel;
import com.example.bugtracker.Model.Entity.Roles;
import com.example.bugtracker.Model.Entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    LoginModel loginModel = new LoginModel();
    @FXML
    private Button loginButton;

    @FXML
    private TextField userName;
    @FXML
    private PasswordField userPassword;

    @FXML
    private ComboBox<Roles> roleCombo;
    @FXML
    private Hyperlink Signup;

    public static User loggedInUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Roles> roles = FXCollections.observableArrayList(
                Roles.Admin,
                Roles.ProjectManager,
                Roles.Developer,
                Roles.TechSupport,
                Roles.Tester
        );
        roleCombo.setItems(roles);
    }

    public void Login(ActionEvent actionEvent) {
        String username = this.userName.getText();
        String password = this.userPassword.getText();
        Roles selectedRole = this.roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            // Show an error label or display a message to prompt the user to fill in all required fields
            showValidationErrorAlert("Please fill in all fields.");
            userName.clear();
            userPassword.clear();
            return;
        }

        String userType = selectedRole.getDisplayText();
        try {

            if (loginModel.isLogin(username, password, userType)) {
                int loggedInUserId = loginModel.getUserIdFromDatabase(username);
                String fullName = loginModel.getUserFullName(loggedInUserId);
                loggedInUser = new User(loggedInUserId,fullName, username, password,selectedRole);

                Stage stage = (Stage) this.loginButton.getScene().getWindow();
                stage.close();

                switch (userType) {
                    case "Admin" -> AdminLogin();
                    case "Project Manager" -> ProjectManagerLogin();
                    case "Developer", "Tester" -> DeveloperLogin();
                    case "Tech Support" -> TechSupportLogin();
                }
            } else {
                showValidationErrorAlert("Invalid username/password or role type. Please try again.");
                userName.clear();
                userPassword.clear();
            }
        } catch (Exception e) {
            System.out.println("Could not find user credentials.");
            e.printStackTrace();
        }
    }


    private void TechSupportLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/TechSupport.fxml"));
            Parent root = fxmlLoader.load();

            // Create a new stage for the dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Tech Support");
            dashboardStage.setScene(new Scene(root));

            // Show the dashboard stage
            dashboardStage.show();

            // Close the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ProjectManagerLogin() {
        try {
            // Load the Project Manager dashboard FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/ProjectManager/ProjectManagerDashboard.fxml"));
            Parent root = fxmlLoader.load();

            // Create a new stage for the dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Project Manager Dashboard");
            dashboardStage.setScene(new Scene(root));

            // Show the dashboard stage
            dashboardStage.show();

            // Close the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }


    private void AdminLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource
                    ("/com/example/bugtracker/Admin/AdminDashboard.fxml"));
            Parent root = fxmlLoader.load();

            // Create a new stage for the dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Admin");
            dashboardStage.setScene(new Scene(root));

            // Show the dashboard stage
            dashboardStage.show();

            // Close the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void DeveloperLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Developer/DeveloperDashboard.fxml"));
            HBox rootContainer = loader.load();

            Scene dashboardScene = new Scene(rootContainer);
            Stage dashboardStage = new Stage();
            dashboardStage.setScene(dashboardScene);
            dashboardStage.show();

            // Close the login stage
            Stage loginStage = (Stage) this.loginButton.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showValidationErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void Signup(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Signup/Signup.fxml"));
            Parent root = fxmlLoader.load();

            // Create a new stage for the signup
            Stage signupStage = new Stage();
            signupStage.setTitle("Signup");
            signupStage.setScene(new Scene(root));

            // Show the signup stage
            signupStage.show();

            // Close the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}