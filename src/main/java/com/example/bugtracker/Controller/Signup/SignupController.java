package com.example.bugtracker.Controller.Signup;

import com.example.bugtracker.Main.Login;
import com.example.bugtracker.Model.Signup.SignupModel;
import com.example.bugtracker.Model.Entity.SignupRoles;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SignupController implements Initializable {
    @FXML
    public Button signupButton;
    @FXML
    public PasswordField confirmPassword;
    @FXML
    private Button backButton;
    @FXML
    private TextField userName;
    @FXML
    private PasswordField userPassword;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField emailAddress;
    @FXML
    private DatePicker dob;
    @FXML
    private ComboBox<SignupRoles> roleCombo;



    public void initialize(URL location, ResourceBundle resources) {
        this.roleCombo.setItems(FXCollections.observableArrayList(SignupRoles.values()));
    }
    public void signUp(ActionEvent actionEvent) throws IOException, SQLException {
        String username = userName.getText();
        String password = userPassword.getText();
        String confirmPword = confirmPassword.getText();
        String firstName = this.firstName.getText();
        String lastName = this.lastName.getText();
        String emailAddress = this.emailAddress.getText();
        LocalDate dobValue = this.dob.getValue();
        Date dob = dobValue != null ? Date.valueOf(dobValue) : null;
        SignupRoles selectedRole = roleCombo.getValue();

        if (isEmptyField(username, password, confirmPword, firstName, lastName, dob, selectedRole)) {
            showAlert("Empty Fields", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPword)) {
            showAlert("Password Mismatch", "The password fields do not match. Please try again.");
            userPassword.clear();
            confirmPassword.clear();
            return;

        }

        if (SignupModel.isUsernameExists(username)) {
            showAlert("Username Exists", "This username already exists. Please choose a different one.");
           userName.clear();
           return;
        }

        try {
            int userId = SignupModel.insertUser(username, password, firstName, lastName, emailAddress, dob);
            SignupModel.insertUserRoles(userId, selectedRole);

            showSuccessAlert();
            Stage stage = (Stage) this.signupButton.getScene().getWindow();
            stage.close();

            new Login().start(new Stage());

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean isEmptyField(Object... fields) {
        for (Object field : fields) {
            String fieldString = field.toString();
            if (fieldString.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void handleBackButton(javafx.event.ActionEvent actionEvent) {
        Stage stage = (Stage) this.backButton.getScene().getWindow();
        stage.close();
        try {
            new Login().start(new Stage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Sign-up successful! You can now log in using your credentials.");
        alert.showAndWait();
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



}

