package com.example.bugtracker.Controller.Developer;

import com.example.bugtracker.Controller.ButtonHandler.ButtonHandler;
import com.example.bugtracker.Model.DAO.BugDAO;
import com.example.bugtracker.Model.DAO.ProjectDAO;
import com.example.bugtracker.Model.Entity.Bug;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Model.Entity.Roles;
import com.example.bugtracker.Model.Entity.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class DeveloperProjectsController implements Initializable {
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
    public TableColumn<Project,String> descriptionColumn;
    public TableColumn<Project,String> priorityColumn;
    public TableView<User> membersTable;
    public TableColumn<User,String> membersColumn;
    public TableColumn<User, Roles> roleColumn;
    public Label daysTillDeadlineLabel;
    public TableColumn<User,String> userIdColumn;
    public TableColumn<User,String> usernameColumn;
    public Label projectCompletedLabel;
    @FXML
    private TableView<Project> tableView;

    @FXML
    private TableColumn<Project, String> projectColumn;
    @FXML
    private TableColumn<Project, String> statusColumn;
    @FXML
    private TableColumn<Project, String> projectManagerColumn;
    @FXML
    private TableColumn<Project, String> deadlineColumn;
    @FXML
    private TableColumn<Project, Hyperlink> actionsColumn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        projectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProjectName()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        projectManagerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getManagerFirstName() + " " + cellData.getValue().getManagerLastName()));
        deadlineColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getEndDate();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(formatter.format(date));
            } else {
                return new SimpleStringProperty("");
            }
        });
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProjectDescription()));
        priorityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPriority()));
        actionsColumn.setCellFactory(createHyperlinkCellFactory());

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // When a project is selected

                // Calculate days till deadline or display "Project Completed"
                String daysTillDeadline = calculateDaysTillDeadline(newValue.getEndDate());
                if (daysTillDeadline.equals("Project Completed")) {
                    daysTillDeadlineLabel.setVisible(false);
                    projectCompletedLabel.setText("Project Completed");
                    projectCompletedLabel.setVisible(true);
                } else {
                    projectCompletedLabel.setVisible(false);
                    daysTillDeadlineLabel.setText(daysTillDeadline);
                    daysTillDeadlineLabel.setVisible(true);
                }

                try {
                    fillMembersTable(newValue);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // If no project is selected, clear the labels
                clearLabel();
            }
        });

        populateTable();

    }

    private void clearLabel() {
        daysTillDeadlineLabel.setText("");
    }

    private Callback<TableColumn<Project, Hyperlink>, TableCell<Project, Hyperlink>> createHyperlinkCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Project, Hyperlink> call(TableColumn<Project, Hyperlink> param) {
                return new TableCell<>() {
                    private final Hyperlink detailsLink = new Hyperlink("View Tickets");

                    {
                        detailsLink.setOnAction(this::handleDetailsLink);
                    }

                    @FXML
                    private void handleDetailsLink(javafx.event.ActionEvent actionEvent) {
                        Project project = getTableRow().getItem();
                        if (project != null) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Developer/DeveloperProjectDetails.fxml"));
                                Parent detailsRoot = loader.load();

                                DeveloperProjectDetailsController detailsController = loader.getController();
                                detailsController.setSelectedProject(project.getProjectId());

                                // Get the stage of the current scene
                                Stage currentStage = (Stage) tableView.getScene().getWindow();

                                // Set the new scene to the current stage
                                Scene detailsScene = new Scene(detailsRoot);
                                currentStage.setScene(detailsScene);
                                currentStage.setTitle("Project Details");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    protected void updateItem(Hyperlink item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setGraphic(detailsLink);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        };
    }


    public void populateTable() {
        // Populate the TableView with bug data associated with the logged-in user
        List<Project> userProjects = ProjectDAO.getProjectsForUser(loggedInUser);
        tableView.setItems(FXCollections.observableArrayList(userProjects));
        if (!userProjects.isEmpty()) {
            tableView.setItems(FXCollections.observableArrayList(userProjects));
        } else {
            // Handle the case when no bugs are associated with the logged-in user
            tableView.setPlaceholder(new Label("No bugs found for " + loggedInUser.getUsername()));
        }
    }


    private String calculateDaysTillDeadline(LocalDate deadline) {
        LocalDate currentDate = LocalDate.now();
        long days = ChronoUnit.DAYS.between(currentDate, deadline);
        if (days < 0) {
            return "Project Completed";
        } else {
            return String.valueOf(days);
        }
    }
    private void fillMembersTable(Project selectedProject) throws SQLException {
        // Fetch the project members for the selected project
        List<User> members = ProjectDAO.getProjectMembers(selectedProject.getProjectId());

        // Clear the existing items in the table
        membersTable.getItems().clear();

        // Add the fetched members to the table
        membersTable.getItems().addAll(members);

        membersColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String fullName = user.getFirstName() + " " + user.getLastName();
            return new SimpleStringProperty(fullName);
        });
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));



    }
}

