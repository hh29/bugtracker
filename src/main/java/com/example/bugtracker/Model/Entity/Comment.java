package com.example.bugtracker.Model.Entity;

import java.sql.Timestamp;

public class Comment {
    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    private int Id;
   private int commentId;

    public int getBugId() {
        return bugId;
    }

    public void setBugId(int bugId) {
        this.bugId = bugId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    private int bugId;
    private int projectId;
    private User user;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;
    private String commentText;
    private Timestamp commentDate;



    // Constructors, getters, setters, and other methods

    public Comment(int Id, int userId, String commentText, Timestamp commentDate) {
        this.Id = Id;
        this.userId = userId;
        this.commentText = commentText;
        this.commentDate = commentDate;
    }


    public Comment() {
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }




    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Timestamp getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Timestamp commentDate) {
        this.commentDate = commentDate;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

