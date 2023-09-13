package com.example.bugtracker.Controller.Chat;

import com.example.bugtracker.Model.Entity.Comment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommentBubbleController {
    @FXML
    private Label nameLabel;
    @FXML private Label textLabel;
    @FXML private Label timestampLabel;
    @FXML private VBox commentVBox;
    private Comment comment;


    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    public void setTimestamp(String timestamp) {
        timestampLabel.setText(timestamp);
    }
    public void setComment(Comment comment) {
        this.comment = comment;
    }


}