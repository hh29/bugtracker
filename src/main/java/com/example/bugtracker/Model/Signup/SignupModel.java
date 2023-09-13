package com.example.bugtracker.Model.Signup;


import com.example.bugtracker.DBConnection.DBConnection;
import com.example.bugtracker.Model.Entity.SignupRoles;

import java.sql.*;

public class SignupModel {

    public static int insertUser(String username, String password, String firstName, String lastName,
                                 String emailAddress, Date dob) {
        String query = "INSERT INTO users (username, password, first_name, last_name, email, dob) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = DBConnection.getConnection().prepareStatement(query, new String[]{"user_id"})) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, firstName);
            preparedStatement.setString(4, lastName);
            preparedStatement.setString(5, emailAddress);
            preparedStatement.setDate(6,
                    dob);
            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return 0; //Insert fail
    }

    public static void insertUserRoles(int userId, SignupRoles selectedRole) throws SQLException {
        String query = "INSERT INTO user_roles (role_id, user_id) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = DBConnection.getConnection().prepareStatement(query)) {
            preparedStatement.setInt(1, selectedRole.getRoleId());
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        }
    }
    public static boolean isUsernameExists(String username) {
        String query = "SELECT user_id FROM public.users WHERE username = ?";
        boolean exists = false;

        try(PreparedStatement preparedStatement = DBConnection.getConnection().prepareStatement(query)){
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                exists = resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return exists;
    }
}
