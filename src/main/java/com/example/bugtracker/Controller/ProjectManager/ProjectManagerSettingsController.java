package com.example.bugtracker.Controller.ProjectManager;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Controller.DialogController.UserDialogController;
import com.example.bugtracker.Controller.Login.LoginController;
import com.example.bugtracker.DBConnection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ProjectManagerSettingsController implements Initializable {
    @FXML
    public Button dashboardButton;
    @FXML
    public Button projectsButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button logoutButton;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label surnameLabel;
    @FXML
    private Label dobLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;
    @FXML
    public Button submitButton;
    @FXML
    public Button updateButton;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmNewPasswordField;
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

        loadUserDetails();
    }
    private void loadUserDetails() {

        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT u.username, u.first_name, u.last_name, u.dob, u.email, r.role_title " +
                    "FROM public.users u " +
                    "JOIN public.user_roles ur ON u.user_id = ur.user_id " +
                    "JOIN public.roles r ON ur.role_id = r.role_id " +
                    "WHERE u.user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, LoginController.loggedInUser.getUserId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        userNameLabel.setText(resultSet.getString("username"));
                        firstNameLabel.setText(resultSet.getString("first_name"));
                        surnameLabel.setText(resultSet.getString("last_name"));

                        LocalDate dob = resultSet.getDate("dob").toLocalDate();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        dobLabel.setText(dob.format(formatter));

                        emailLabel.setText(resultSet.getString("email"));
                        roleLabel.setText(resultSet.getString("role_title"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleUpdate(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/UserDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Get the controller for the dialog
        UserDialogController dialogController = loader.getController();

        // Pass the user details to the dialog controller
        dialogController.initData(
                userNameLabel.getText(),
                firstNameLabel.getText(),
                surnameLabel.getText(),
                emailLabel.getText(),
                roleLabel.getText(),
                LocalDate.parse(dobLabel.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Update User Details");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(updateButton.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        dialogStage.showAndWait();

        String newUsername = dialogController.getUpdatedUsername();
        String newFirstName = dialogController.getUpdatedFirstName();
        String newLastName = dialogController.getUpdatedLastName();
        String newEmail = dialogController.getUpdatedEmail();
        LocalDate newDOB = dialogController.getSelectedDOB();

        if (newUsername != null && newFirstName != null && newLastName != null && newEmail != null && newDOB != null) {
            userNameLabel.setText(newUsername);
            firstNameLabel.setText(newFirstName);
            surnameLabel.setText(newLastName);
            emailLabel.setText(newEmail);
            dobLabel.setText(newDOB.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            updateUserInfoInDatabase(newUsername, newFirstName, newLastName, newEmail, newDOB);
        }

    }
    public void onSubmitPassword(ActionEvent actionEvent) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmNewPassword = confirmNewPasswordField.getText();

        if (!newPassword.equals(confirmNewPassword)) {
            showAlert(Alert.AlertType.WARNING, "Password Mismatch", "The new passwords you entered do not match.");
            return;
        }

        boolean passwordChangeSuccessful = performPasswordChange(currentPassword, newPassword);

        if (passwordChangeSuccessful) {
            showAlert(Alert.AlertType.INFORMATION, "Password Changed", "Your password has been successfully changed.");

            // Clear the fields after successful password change
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Password Change Failed", "Failed to change your password because your current password is incorrect. Please try again.");
        }
    }

    private boolean performPasswordChange(String currentPassword, String newPassword) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "UPDATE public.users " +
                    "SET password = ? " +
                    "WHERE user_id = ? AND password = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newPassword);
                statement.setInt(2, LoginController.loggedInUser.getUserId());
                statement.setString(3, currentPassword);

                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void updateUserInfoInDatabase(String newUsername, String newFirstName, String newLastName, String newEmail, LocalDate newDOB) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "UPDATE users " +
                    "SET username = ?, first_name = ?, last_name = ?, email = ?, dob = ? " +
                    "WHERE user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newUsername);
                statement.setString(2, newFirstName);
                statement.setString(3, newLastName);
                statement.setString(4, newEmail);
                statement.setDate(5, Date.valueOf(newDOB)); // Convert LocalDate to SQL Date
                statement.setInt(6, LoginController.loggedInUser.getUserId());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Update Successful", "User details have been updated.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update user details.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
