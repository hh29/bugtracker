package com.example.bugtracker.Controller.Developer;

import com.example.bugtracker.Controller.Chat.ChatController;
import com.example.bugtracker.Controller.DialogController.BugDialogController;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.BugDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.LocalDateStringConverter;

public class DeveloperTicketsController implements Initializable {
    @FXML
    public Button projectsButton;
    @FXML
    public Button ticketsButton;
    @FXML
    public Button dashboardButton;
    @FXML
    public Button settingsButton;
    public TableColumn<Bug,String> severityColumn;
    public TableColumn<Bug,String> timeToCompleteColumn;
    @FXML
    private TableView<Bug> tableView;
    @FXML
    public Button logoutButton;

    @FXML
    private TableColumn<Bug, String> projectColumn;

    @FXML
    private TableColumn<Bug, String> ticketColumn;

    @FXML
    private TableColumn<Bug, String> statusColumn;

    @FXML
    private TableColumn<Bug, LocalDate> dateCreatedColumn;

    @FXML
    private TableColumn<Bug, String> priorityColumn;
    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button sortButton;


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

        // Set up the project hyperlink column
        projectColumn.setCellValueFactory(data -> data.getValue().projectNameProperty());

        ticketColumn.setCellValueFactory(data -> data.getValue().bugTitleProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        // Define the date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Set the cell value factory and cell factory
        dateCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        dateCreatedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter(dateFormatter, dateFormatter)));
        priorityColumn.setCellValueFactory(data -> data.getValue().priorityProperty());
        severityColumn.setCellValueFactory(data->data.getValue().severityProperty());
        timeToCompleteColumn.setCellValueFactory(data->data.getValue().estimatedTimeToCompleteProperty());



        sortComboBox.setPromptText("Sort By");
        sortComboBox.setItems(FXCollections.observableArrayList("Project", "Severity", "Priority"));
        populateTable();

    }

    public void populateTable(){
        // Populate the TableView with bug data associated with the logged-in user
        List<Bug> userBugs = BugDAO.getBugsForUser(loggedInUser);
        tableView.setItems(FXCollections.observableArrayList(userBugs));
        if (!userBugs.isEmpty()) {
            tableView.setItems(FXCollections.observableArrayList(userBugs));
        } else {
            // Handle the case when no bugs are associated with the logged-in user
            tableView.setPlaceholder(new Label("No bugs found for " + loggedInUser.getUsername()));
        }
    }
    public void handleChatButton(ActionEvent actionEvent) {
        Bug bug = tableView.getSelectionModel().getSelectedItem();

        if (bug != null) {
            openChatLobby(bug);
        } else {
            showAlert("No ticket selected","Please select a project before opening the chat lobby.");
        }

    }
    private void openChatLobby(Bug bug) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Chat.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage chatStage = new Stage();
        chatStage.setTitle("Chat Lobby for Bug " + bug.getBugId());
        chatStage.initModality(Modality.WINDOW_MODAL);
        chatStage.initOwner(tableView.getScene().getWindow());
        Scene scene = new Scene(root);
        chatStage.setScene(scene);

        // Pass the bug and logged-in user details to the ChatController
        ChatController ChatController = loader.getController();
        ChatController.setSelectedBug(bug);
        ChatController.setLoggedInUser(loggedInUser);

        chatStage.showAndWait();
    }

    public void handleAddButton(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/BugDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add New Bug");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(tableView.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        // Set up the BugDialogController
        BugDialogController controller = loader.getController();

        controller.setProjectId(tableView.getSelectionModel().getSelectedItem().getProjectId());

        dialogStage.showAndWait();

        // Check if the user clicked "OK" and get the bug details
        if (controller.isOkClicked()) {
            Bug newBug = controller.getBug();
            if (newBug != null) {
                // Insert the new bug into the database using your BugDAO
                BugDAO.insertBug(newBug);

                // Show a success message
                showAlert("Ticket Inserted", "New ticket has been successfully inserted.");
            }
            // Refresh the bugs table

            populateTable();
        }

    }

    public void handleDeleteButton(ActionEvent actionEvent) {
        Bug selectedBug = tableView.getSelectionModel().getSelectedItem();
        if (selectedBug != null) {
            boolean confirmDelete = showConfirmationDialog("Delete Ticket",
                    "Are you sure you want to delete this ticket?");
            if (confirmDelete) {
                boolean deletionSuccessful = BugDAO.deleteBug(selectedBug);
                if (deletionSuccessful) {
                    // Remove the bug from the TableView
                    tableView.getItems().remove(selectedBug);
                    BugDAO.deleteBug(selectedBug);

                    // Show a success message
                    showAlert("Ticket Deleted", "The selected ticket has been deleted successfully.");
                } else {
                    // Show an error message
                    showAlert("Error", "An error occurred while deleting the ticket.");
                }
            }
        } else {
            showAlert("No Ticket Selected", "Please select a Ticket to delete.");
        }
    }
    @FXML
    private void handleSortButton(ActionEvent event) {
        String selectedSortingCriteria = sortComboBox.getSelectionModel().getSelectedItem();

        if (selectedSortingCriteria != null) {
            List<Bug> sortedBugs = new ArrayList<>(tableView.getItems());

            switch (selectedSortingCriteria) {
                case "Project" ->
                    // Sort by Project
                        sortedBugs.sort(Comparator.comparing(Bug::getProjectName));
                case "Severity" ->
                    // Sort by Severity
                        sortedBugs.sort((bug1, bug2) -> {
                            // Define the custom sorting order for severity
                            List<String> severityOrder = Arrays.asList("Critical", "Major", "Minor");
                            int index1 = severityOrder.indexOf(bug1.getSeverity());
                            int index2 = severityOrder.indexOf(bug2.getSeverity());
                            return Integer.compare(index1, index2);
                        });
                case "Priority" ->
                    // Sort by Priority
                        sortedBugs.sort((bug1, bug2) -> {
                            // Define the custom sorting order for priority
                            List<String> priorityOrder = Arrays.asList("High", "Medium", "Low");
                            int index1 = priorityOrder.indexOf(bug1.getPriority());
                            int index2 = priorityOrder.indexOf(bug2.getPriority());
                            return Integer.compare(index1, index2);
                        });
            }

            // Update the TableView with the sorted data
            tableView.setItems(FXCollections.observableArrayList(sortedBugs));
        } else {
            // Handle the case when no sorting criteria is selected
            showAlert("Sorting Criteria Not Selected", "Please select a sorting criteria.");
        }
    }


    public void handleEditButton(ActionEvent actionEvent) {
        Bug selectedBug = tableView.getSelectionModel().getSelectedItem();
        if (selectedBug != null) {
            openBugDialogForEdit(selectedBug);
        } else {
            showAlert("No Ticket Selected", "Please select a Ticket to edit.");
        }
    }
    private void openBugDialogForEdit(Bug bug) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Dialog/BugDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Bug");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(tableView.getScene().getWindow());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        BugDialogController controller = loader.getController();
        controller.initializeForEdit(bug);
        controller.setBug(bug);
        controller.setProjectId(tableView.getSelectionModel().getSelectedItem().getProjectId());
        System.out.println(bug.getProjectId());
        dialogStage.showAndWait();

        // After editing, refresh the bugs table
        populateTable();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
