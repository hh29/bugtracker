package com.example.bugtracker.Model.Login;

import com.example.bugtracker.DBConnection.DBConnection;

import java.sql.*;

public class LoginModel {
    Connection connection;
    public LoginModel(){
        this.connection = DBConnection.getConnection();
    }
    public boolean isLogin(String username, String password,String userType) throws  Exception{
        PreparedStatement statement =null;
        ResultSet rs =null;
        String query = null;

        switch (userType) {
            case "Admin" -> query = "SELECT username, password\n" +
                    "FROM users \n" +
                    "JOIN user_roles ON users.user_id = user_roles.user_id\n" +
                    "WHERE username = ? AND password = ? AND role_id = '1';";
            case "Project Manager" -> query = "SELECT username, password\n" +
                    "FROM users \n" +
                    "JOIN user_roles ON users.user_id = user_roles.user_id\n" +
                    "WHERE username = ? AND password = ? AND role_id = '2';";
            case "Developer" -> query = "SELECT username, password\n" +
                    "FROM users \n" +
                    "JOIN user_roles ON users.user_id = user_roles.user_id\n" +
                    "WHERE username = ? AND password = ? AND role_id = '3';";
            case "Tech Support" -> query = "SELECT username, password\n" +
                    "FROM users \n" +
                    "JOIN user_roles ON users.user_id = user_roles.user_id\n" +
                    "WHERE username = ? AND password = ? AND role_id = '4';" ;
            case "Tester" -> query = "SELECT username, password\n" +
                    "FROM users \n" +
                    "JOIN user_roles ON users.user_id = user_roles.user_id\n" +
                    "WHERE username = ? AND password = ? AND role_id = '5';";


        }
        try{
            statement = this.connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,password);
            rs = statement.executeQuery();
            return rs.next();
        }catch (SQLException e){
            return false;
        }finally {
            // Close the ResultSet and PreparedStatement if they are not null
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    public int getUserIdFromDatabase(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        int userId = -1; // Default value indicating no matching user found

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userId = resultSet.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userId;
    }

    public String getUserFullName(int userId) {
            String fullName = null;

            String query = "SELECT first_name, last_name FROM users WHERE user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    fullName = firstName + " " + lastName;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return fullName;
        }


    }
