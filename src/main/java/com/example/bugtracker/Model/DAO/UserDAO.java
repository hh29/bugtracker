package com.example.bugtracker.Model.DAO;

import com.example.bugtracker.Controller.Login.LoginController;
import com.example.bugtracker.DBConnection.DBConnection;
import com.example.bugtracker.Model.Entity.Roles;
import com.example.bugtracker.Model.Entity.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.bugtracker.Controller.Login.LoginController.loggedInUser;
import static com.example.bugtracker.DBConnection.DBConnection.getConnection;

public class UserDAO {
    public static List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();

        String query = "SELECT u.user_id, u.username, u.first_name, u.last_name, u.dob, u.email, r.role_title " +
                "FROM public.users u " +
                "JOIN public.user_roles ur ON u.user_id = ur.user_id " +
                "JOIN public.roles r ON ur.role_id = r.role_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                LocalDate dob = resultSet.getDate("dob").toLocalDate();
                String email = resultSet.getString("email");
                String roleTitle = resultSet.getString("role_title");
                Roles role = Roles.getRoleEnum(roleTitle);

                User user = new User(userId, username, firstName, lastName, dob, email, role);
                userList.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception properly
        }

        return userList;
    }


    public static void updateUser(User user, String newUsername, String newFirstName, String newLastName, String newEmail, LocalDate newDOB) {
        String query = "UPDATE users SET username=?, first_name=?, last_name=?, email=?, dob=? WHERE user_id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newUsername);
            statement.setString(2, newFirstName);
            statement.setString(3, newLastName);
            statement.setString(4, newEmail);
            statement.setDate(5, Date.valueOf(newDOB));
            statement.setInt(6, user.getUserId());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(User user) {
        String deleteUserQuery = "DELETE FROM public.users WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteUserQuery)) {

            statement.setInt(1, user.getUserId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUserRole(User user, Roles newRole) {
        String query = "UPDATE user_roles SET role_id=? WHERE user_id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newRole.getRoleId());
            statement.setInt(2, user.getUserId());

            statement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<User> getUsersWithRole(String roleName) {
        List<User> users = new ArrayList<>();

        String query = "SELECT u.user_id, u.first_name, u.last_name " +
                "FROM public.users u " +
                "JOIN public.user_roles ur ON u.user_id = ur.user_id " +
                "JOIN public.roles r ON ur.role_id = r.role_id " +
                "WHERE r.role_title = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, roleName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                users.add(new User(userId, firstName, lastName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static boolean isUsernameTaken(String username) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT username FROM public.users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next(); // If result exists, the username is taken

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<User> getUsersWithoutExcludedRoles() {
        List<User> users = new ArrayList<>();

        String query = "SELECT u.user_id, u.first_name, u.last_name, r.role_title " +
                "FROM public.users u " +
                "JOIN public.user_roles ur ON u.user_id = ur.user_id " +
                "JOIN public.roles r ON ur.role_id = r.role_id " +
                "WHERE r.role_title NOT IN (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the roles to exclude as parameters
            statement.setString(1, "Project Manager");
            statement.setString(2, "Admin");
            statement.setString(3, "Tech Support");

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String roleName = resultSet.getString("role_title");

                users.add(new User(userId,firstName, lastName, roleName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    public static List<User> getAllDevelopersAndTesters() throws SQLException {
        List<User> users = new ArrayList<>();
        // Write SQL query to fetch all users with role IDs 3 (developer) and 5 (tester) and include role title
        String query = "SELECT u.*, r.role_title FROM users u " +
                "INNER JOIN user_roles ur ON u.user_id = ur.user_id " +
                "INNER JOIN roles r ON ur.role_id = r.role_id " +
                "WHERE ur.role_id IN (3, 5)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String roleTitle = resultSet.getString("role_title"); // Get the role title
                // Add more user properties as needed
                User user = new User(userId, username, firstName, lastName, roleTitle);
                users.add(user);
            }
        }
        return users;
    }


    public static int getNumberOfActiveProjects() {
        int numberOfActiveProjects = 0;
        String query = "SELECT COUNT(DISTINCT project_user.project_id) AS active_projects " +
                "FROM projects " +
                "JOIN project_user ON projects.project_id = project_user.project_id " +
                "WHERE project_user.user_id = ? AND projects.status = 'In Progress'";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, loggedInUser.getUserId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                numberOfActiveProjects = resultSet.getInt("active_projects");
            }
        } catch (SQLException e) {
            System.out.println(numberOfActiveProjects);
            e.printStackTrace();
        }
        return numberOfActiveProjects; // Sample data; replace with actual database query
    }
    public static int getNumberOfUnresolvedBugs() {
        int numberOfUnresolvedBugs = 0;
        String query = "SELECT COUNT(DISTINCT bug_user.bug_id) AS unresolved_bugs " +
                "FROM bugs " +
                "JOIN bug_user ON bugs.bug_id = bug_user.bug_id " +
                "WHERE bug_user.user_id = ? AND bugs.status = 'In Progress'";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, loggedInUser.getUserId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                numberOfUnresolvedBugs = resultSet.getInt("unresolved_bugs");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfUnresolvedBugs;
    }
    public static void deleteBugsForUser(User user) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "DELETE FROM bug_user WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, user.getUserId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void deleteProjectsForUser(User user) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "DELETE FROM project_user WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, user.getUserId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





}

