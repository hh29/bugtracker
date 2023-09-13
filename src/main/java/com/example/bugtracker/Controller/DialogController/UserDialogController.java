package com.example.bugtracker.Controller.DialogController;

import com.example.bugtracker.DBConnection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class UserDialogController {
    @FXML
    public Button okButton;
    @FXML
    public Button cancelButton;
    @FXML
    public TextField userNameLabel;
    @FXML
    public TextField firstNameLabel;
    @FXML
    public TextField surnameLabel;
    @FXML
    public TextField emailLabel;
    @FXML
    public Label roleLabel;
    @FXML
    public DatePicker dob;
    private LocalDate selectedDOB;

    private String updatedUsername;
    private String updatedFirstName;
    private String updatedLastName;
    private String updatedEmail;


    public void initData(String username, String firstName, String lastName, String email, String role, LocalDate dobValue) {
        userNameLabel.setText(username);
        firstNameLabel.setText(firstName);
        surnameLabel.setText(lastName);
        emailLabel.setText(email);
        selectedDOB = dobValue;
        dob.setValue(dobValue);
        roleLabel.setText(role);

    }
    public void handleOkButton(ActionEvent actionEvent) {
        String newUsername = userNameLabel.getText();
        String newFirstName = firstNameLabel.getText();
        String newLastName = surnameLabel.getText();
        String newEmail = emailLabel.getText();
        LocalDate newDOB = dob.getValue();


        // Check if any fields are left unfilled
        if (newUsername.isEmpty() || newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || newDOB == null) {
            // Display an alert if any field is empty
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Information");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields before proceeding.");
            alert.showAndWait();
            return;
        }

        // Check if the new username is already taken
        if (!newUsername.equals(loggedInUser.getUsername()) && isUsernameTaken(newUsername)) {
            // Display an alert for duplicate username
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Username");
            alert.setHeaderText(null);
            alert.setContentText("The username " + newUsername + " is already taken. Please choose a different username.");
            alert.showAndWait();
            return;
        }

        // Update the controller with the new values
        updatedUsername = newUsername;
        updatedFirstName = newFirstName;
        updatedLastName = newLastName;
        updatedEmail = newEmail;
        selectedDOB = newDOB;

        // Close the dialog
        closeDialog();
        // Show a success message
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("User details have been successfully updated.");
        successAlert.showAndWait();
    }

    public boolean isUsernameTaken(String username) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT username FROM public.users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next(); // If result exists, the username is taken

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void handleCancelButton(ActionEvent actionEvent) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    public String getUpdatedUsername() {
        return updatedUsername;
    }

    public String getUpdatedFirstName() {
        return updatedFirstName;
    }

    public String getUpdatedLastName() {
        return updatedLastName;
    }

    public String getUpdatedEmail() {
        return updatedEmail;
    }

    public LocalDate getSelectedDOB() {
        return selectedDOB;
    }
}
