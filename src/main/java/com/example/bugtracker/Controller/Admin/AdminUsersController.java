package com.example.bugtracker.Controller.Admin;


import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Controller.DialogController.AdminUserDialogController;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.DAO.UserDAO;
import com.example.bugtracker.Model.Entity.Project;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;



public class AdminUsersController implements Initializable {
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
    public TableColumn<Project, Integer> projectIDColumn;
    @FXML
    public TableColumn<Project, String> projectNameColumn;
    @FXML
    public TableColumn<Project, String> statusColumn;
   @FXML
   public Label assignedProjectsLabel;
    @FXML
    private TableView<User> usersTable;
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
    private TableView<Project> projectTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, Roles> roleColumn;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    private TextField searchBar;

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


        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        projectIDColumn.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        populateUsersTable();

        // Add a listener to the usersTable selection model
        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Update labels with the selected user's information
                updateUserLabels(newValue);

                // Populate assignedProjectsTable with projects assigned to the selected user
                populateAssignedProjectsTable(newValue);

                updateProjectVisibility(newValue.getRole());
            } else {
                // No user is selected, hide the projectTable and assignedProjectsLabel
                updateProjectVisibility(null);
            }
            }
        );
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }
    private void populateAssignedProjectsTable(User selectedUser) {
        ObservableList<Project> assignedProjectsList = FXCollections.observableArrayList();

        // Use ProjectDAO to get the assigned projects for the selected user
        List<Project> assignedProjects = ProjectDAO.getProjectsForUser(selectedUser);

        // Add the assigned projects to the list
        assignedProjectsList.addAll(assignedProjects);

        // Update the projectTable with the assigned projects
        projectTable.setItems(assignedProjectsList);
    }

    private void updateUserLabels(User selectedUser) {
        userNameLabel.setText(selectedUser.getUsername());
        firstNameLabel.setText(selectedUser.getFirstName());
        surnameLabel.setText(selectedUser.getLastName());

        LocalDate dob = selectedUser.getDob();
        String formattedDob = dob.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        dobLabel.setText(formattedDob);

        emailLabel.setText(selectedUser.getEmail());
        roleLabel.setText(String.valueOf(selectedUser.getRole()));
    }

    private void populateUsersTable() {
        ObservableList<User> userList = FXCollections.observableArrayList();

        List<User> allUsers = UserDAO.getAllUsers(); // Use the UserDAO method to get all users

        userList.addAll(allUsers);

        usersTable.setItems(userList);
    }



    public void handleDeleteButton(ActionEvent actionEvent) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem(); // Assuming tableView is your user table view

        if (selectedUser != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Are you sure you want to delete the selected user?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete all associations with user
                UserDAO.deleteBugsForUser(selectedUser);
                UserDAO.deleteProjectsForUser(selectedUser);
                UserDAO.deleteUser(selectedUser);


                showAlert("User Deleted", "User has been successfully deleted.");
                populateUsersTable();
            }
        } else {
            showAlert("Select User", "Please select a user to delete.");
        }
    }
    public void handleEditButton(ActionEvent actionEvent) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            openEditUserDialog(selectedUser);
        } else {
            showAlert("No User Selected", "Please select a User to edit.");
        }
    }

    private void openEditUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Admin/AdminUserDialog.fxml"));
            Parent root = loader.load();
            AdminUserDialogController dialogController = loader.getController();

            // Retrieve all projects from the database
            List<Project> allProjectsList = ProjectDAO.getAllActiveProjects();

            ObservableList<Project> allProjects = FXCollections.observableArrayList(allProjectsList);

            // Pass the user and projects to the dialog controller's initData method
            dialogController.initData(user, allProjects,this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.setScene(new Scene(root));
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterUsers(String keyword) {
        Predicate<User> userFilter = user -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return true; // Show all users when search field is empty
            }
            String lowerCaseKeyword = keyword.toLowerCase();
            try {
                int userId = Integer.parseInt(keyword);
                return user.getUserId() == userId ||
                        user.getUsername().toLowerCase().contains(lowerCaseKeyword) ||
                        user.getFirstName().toLowerCase().contains(lowerCaseKeyword) ||
                        user.getLastName().toLowerCase().contains(lowerCaseKeyword) ||
                        user.getRole().toString().toLowerCase().contains(lowerCaseKeyword);
            } catch (NumberFormatException e) {
                return user.getUsername().toLowerCase().contains(lowerCaseKeyword) ||
                        user.getFirstName().toLowerCase().contains(lowerCaseKeyword) ||
                        user.getLastName().toLowerCase().contains(lowerCaseKeyword) ||
                        user.getRole().toString().toLowerCase().contains(lowerCaseKeyword);
            }
        };

        List<User> allUsers = UserDAO.getAllUsers(); // Replace with your method to fetch all users
        List<User> filteredUsers = allUsers.stream().filter(userFilter).toList();

        // Clear the existing items in the table
        usersTable.getItems().clear();

        // Add the filtered users to the table
        usersTable.getItems().addAll(filteredUsers);
    }
    public void refreshData() {
        // Clear existing data and repopulate usersTable
        usersTable.getItems().clear();
        projectTable.setVisible(false);
        populateUsersTable();

        userNameLabel.setText("");
        firstNameLabel.setText("");
        surnameLabel.setText("");
        dobLabel.setText("");
        emailLabel.setText("");
        roleLabel.setText("");


        // Refresh the projectTable for the selected user if any
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            populateAssignedProjectsTable(selectedUser);
        }
    }
    private void updateProjectVisibility(Roles role) {
        if (role == Roles.Admin || role == Roles.TechSupport) {
            // Hide projectTable and assignedProjectsLabel
            projectTable.setVisible(false);
            assignedProjectsLabel.setVisible(false);
        } else {
            // Show projectTable and assignedProjectsLabel
            projectTable.setVisible(true);
            assignedProjectsLabel.setVisible(true);
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

