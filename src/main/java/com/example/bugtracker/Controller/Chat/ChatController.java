package com.example.bugtracker.Controller.Chat;



import com.example.bugtracker.Controller.Login.LoginController;
import com.example.bugtracker.Model.Entity.*;
import com.example.bugtracker.Model.DAO.CommentDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;

public class ChatController {
    @FXML
    public VBox chatContainer;
    @FXML
    private TextArea messageTextArea;
    public Bug selectedBug;
    private Project selectedProject;
    @FXML
    private ScrollPane chatScrollPane;


    public void setLoggedInUser(User user) {
        loggedInUser = user;
    }


    public void setSelectedBug(Bug bug) {
        selectedBug = bug;
        selectedProject = null;
        loadComments();
    }
    public void setSelectedProject(Project project) {
        selectedProject = project;
        selectedBug = null; // Clear selected bug
        loadComments();
    }

    @FXML
    private void initialize() {
        chatScrollPane.setVvalue(1.0);
        // Set up the Enter key handler for sending messages
        messageTextArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                insertComment();
            }
        });
        // Start a timeline for polling every few seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadComments()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    // Method to load comments based on selectedBug or selectedProject
    private void loadComments() {
        try {
            CommentDAO commentDAO = new CommentDAO();
            List<Comment> comments;

            if (selectedBug != null) {
                comments = commentDAO.getCommentsForBug(selectedBug);
            } else if (selectedProject != null) {
                comments = commentDAO.getCommentsForProject(selectedProject);
            } else {
                // Neither bug nor project is selected, clear the chat
                comments = new ArrayList<>();
            }

            comments.sort(Comparator.comparing(Comment::getCommentDate));

            chatContainer.getChildren().clear(); // Clear existing chat
            for (Comment comment : comments) {
                createCommentBubble(comment);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void createCommentBubble(Comment comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Chat/CommentBubble.fxml"));
            Parent commentBubbleRoot = loader.load();
            CommentBubbleController commentBubbleController = loader.getController();


            // Set the name and text for the CommentBubble
            String name = comment.getUser().getFirstName() + " " +
                    comment.getUser().getLastName() + " -  " + comment.getUser().getRoleName();
            commentBubbleController.setName(name);
            commentBubbleController.setText(comment.getCommentText());

            LocalDateTime timestamp = comment.getCommentDate().toLocalDateTime();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.ENGLISH));
            commentBubbleController.setTimestamp(formattedTimestamp);

            commentBubbleController.setComment(comment);

            // Add the new comment bubble at the end of the chat container
            chatContainer.getChildren().add(commentBubbleRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void insertComment() {
        String commentText = messageTextArea.getText().trim();

        if (!commentText.isEmpty()) {
            Comment newComment = new Comment();
            newComment.setCommentText(commentText);
            newComment.setCommentDate(new Timestamp(System.currentTimeMillis()));
            newComment.setUserId(loggedInUser.getUserId());

            try {
                if (selectedBug != null) {
                    newComment.setBugId(selectedBug.getBugId());
                    CommentDAO.insertBugComment(newComment);
                } else if (selectedProject != null) {
                    newComment.setProjectId(selectedProject.getProjectId());
                    CommentDAO.insertProjectComment(newComment);
                }


                // Update the chat display by creating the new comment bubble
                createCommentBubbleForNewMessages(newComment);

                // Clear the message text area after successfully adding the comment
                messageTextArea.clear();

                // Scroll to the bottom (optional)
                chatScrollPane.setVvalue(1.0);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void createCommentBubbleForNewMessages(Comment comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bugtracker/Chat/CommentBubble.fxml"));
            Parent commentBubbleRoot = loader.load();
            CommentBubbleController commentBubbleController = loader.getController();

            // Set the name and text for the CommentBubble
            String name = loggedInUser.getFullName() + " -  " + loggedInUser.getRole();  // Use the logged-in user's full name and role
            commentBubbleController.setName(name);
            commentBubbleController.setText(comment.getCommentText());

            LocalDateTime timestamp = comment.getCommentDate().toLocalDateTime();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.ENGLISH));
            commentBubbleController.setTimestamp(formattedTimestamp);

            // Add the new comment bubble at the end of the chat container
            chatContainer.getChildren().add(commentBubbleRoot);

            // Scroll to the bottom
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}







