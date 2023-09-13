package com.example.bugtracker.Model.DAO;

import com.example.bugtracker.DBConnection.DBConnection;
import com.example.bugtracker.Model.Entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private static Connection connection;

    public CommentDAO() {
        connection = DBConnection.getConnection();
    }

    public static void insertProjectComment(Comment comment) throws SQLException {
        String query = "INSERT INTO public.project_comments (project_id, user_id, comment_text, comment_date) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, comment.getProjectId());
            statement.setInt(2, comment.getUserId());
            statement.setString(3, comment.getCommentText());
            statement.setTimestamp(4, comment.getCommentDate());

            statement.executeUpdate();
        }
    }

    public static void insertBugComment(Comment comment) throws SQLException {
        String query = "INSERT INTO public.bug_comments (bug_id, user_id, comment_text, comment_date) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, comment.getBugId());
            statement.setInt(2, comment.getUserId());
            statement.setString(3, comment.getCommentText());
            statement.setTimestamp(4, comment.getCommentDate());

            statement.executeUpdate();
        }
    }

    public List<Comment> getCommentsForProject(Project project) throws SQLException {
        List<Comment> comments = new ArrayList<>();

        String query = "SELECT c.comment_id, c.project_id, c.user_id, c.comment_text, c.comment_date, " +
                "u.first_name, u.last_name, a.attachment_id, a.file_name, a.file_size, r.role_title " +
                "FROM public.project_comments c " +
                "JOIN public.users u ON c.user_id = u.user_id " +
                "LEFT JOIN public.project_comment_attachments a ON c.comment_id = a.comment_id " +
                "LEFT JOIN public.user_roles ur ON c.user_id = ur.user_id " +
                "LEFT JOIN public.roles r ON ur.role_id = r.role_id " +
                "WHERE c.project_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, project.getProjectId());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(resultSet.getInt("comment_id"));
                    comment.setProjectId(resultSet.getInt("project_id"));
                    comment.setUserId(resultSet.getInt("user_id"));
                    comment.setCommentText(resultSet.getString("comment_text"));
                    comment.setCommentDate(Timestamp.valueOf(resultSet.getTimestamp("comment_date").toLocalDateTime()));

                    // Set user's first name, last name, and role title
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String roleTitle = resultSet.getString("role_title");
                    int userId = resultSet.getInt("user_id");

                    User user = new User(userId, firstName, lastName);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRoleName(roleTitle); // Set the user's role title

                    comment.setUser(user);

                    comments.add(comment);
                }
            }
        }

        return comments;
    }
    public List<Comment> getCommentsForBug(Bug bug) throws SQLException {
        List<Comment> comments = new ArrayList<>();

        String query = "SELECT c.comment_id, c.bug_id, c.user_id, c.comment_text, c.comment_date, " +
                "u.first_name, u.last_name, a.attachment_id, a.file_name, a.file_size, r.role_title " +
                "FROM public.bug_comments c " +
                "JOIN public.users u ON c.user_id = u.user_id " +
                "LEFT JOIN public.bug_comment_attachments a ON c.comment_id = a.comment_id " +
                "LEFT JOIN public.user_roles ur ON c.user_id = ur.user_id " +
                "LEFT JOIN public.roles r ON ur.role_id = r.role_id " +
                "WHERE c.bug_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bug.getBugId());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(resultSet.getInt("comment_id"));
                    comment.setBugId(resultSet.getInt("bug_id"));
                    comment.setUserId(resultSet.getInt("user_id"));
                    comment.setCommentText(resultSet.getString("comment_text"));
                    comment.setCommentDate(Timestamp.valueOf(resultSet.getTimestamp("comment_date").toLocalDateTime()));

                    // Set user's first name, last name, and role title
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String roleTitle = resultSet.getString("role_title");
                    int userId = resultSet.getInt("user_id");

                    User user = new User(userId, firstName, lastName);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRoleName(roleTitle); // Set the user's role title

                    comment.setUser(user);


                    comments.add(comment);
                }
            }
        }

        return comments;
    }





}

