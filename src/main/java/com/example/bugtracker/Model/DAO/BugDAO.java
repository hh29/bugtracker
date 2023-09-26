package com.example.bugtracker.Model.DAO;

import com.example.bugtracker.DBConnection.DBConnection;
import com.example.bugtracker.Model.Entity.*;
import javafx.scene.control.Alert;

import java.sql.*;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static com.example.bugtracker.DBConnection.DBConnection.getConnection;

public class BugDAO {

    public static List<Bug> getBugsForUser(User user) {
        List<Bug> bugs = new ArrayList<>();
        String query = "SELECT b.*, u.first_name, u.last_name, p.project_name, p.project_id " +
                "FROM bugs b " +
                "JOIN bug_user bu ON b.bug_id = bu.bug_id " +
                "JOIN projects p ON b.project_id = p.project_id " +
                "JOIN users u ON b.reporter_id = u.user_id " +
                "WHERE bu.user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user.getUserId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int bugId = resultSet.getInt("bug_id");
                int reporterId = resultSet.getInt("reporter_id");
                String bugTitle = resultSet.getString("bug_title");
                String bugDescription = resultSet.getString("bug_description");
                String status = resultSet.getString("status");
                String priority = resultSet.getString("priority");
                String estimatedTime = resultSet.getString("estimated_time_to_complete");
                LocalDate createdDate = resultSet.getDate("created_date").toLocalDate();
                LocalDate updatedDate = resultSet.getDate("updated_date").toLocalDate();
                String severity = resultSet.getString("severity");
                String projectName = resultSet.getString("project_name");
                int projectId = resultSet.getInt("project_id");


                // Fetch reporter details
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                Reporter reporter = new Reporter();
                reporter.setReporterId(reporterId);
                reporter.setFirstName(firstName);
                reporter.setLastName(lastName);


                // Set bug object
                Bug bug = new Bug();
                bug.setProjectName(projectName);
                bug.setBugId(bugId);
                bug.setBugTitle(bugTitle);
                bug.setBugDescription(bugDescription);
                bug.setStatus(status);
                bug.setPriority(priority);
                bug.setEstimatedTimeToComplete(estimatedTime);
                bug.setCreatedDate(createdDate);
                bug.setUpdatedDate(updatedDate);
                bug.setReporter(reporter);
                bug.setSeverity(severity);
                bug.setProjectId(projectId);

                bugs.add(bug);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bugs;
    }

    public static List<Bug> getBugsForSelectedProject(Project selectedProject) {
        List<Bug> bugs = new ArrayList<>();
        String query = "SELECT DISTINCT ON (b.bug_id) b.bug_id, b.*, u.first_name AS reporter_first_name, u.last_name AS reporter_last_name, " +
                "au.first_name AS assignee_first_name, au.last_name AS assignee_last_name, p.project_name " +
                "FROM bugs b " +
                "JOIN users u ON b.reporter_id = u.user_id " +
                "JOIN projects p ON b.project_id = p.project_id " +
                "LEFT JOIN bug_user bu ON b.bug_id = bu.bug_id " +
                "LEFT JOIN users au ON bu.user_id = au.user_id " +
                "WHERE b.project_id = ? " +
                "ORDER BY b.bug_id";


        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, selectedProject.getProjectId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int bugId = resultSet.getInt("bug_id");
                int reporterId = resultSet.getInt("reporter_id");
                String bugTitle = resultSet.getString("bug_title");
                String bugDescription = resultSet.getString("bug_description");
                String status = resultSet.getString("status");
                String priority = resultSet.getString("priority");
                String estimatedTime = resultSet.getString("estimated_time_to_complete");
                LocalDate createdDate = resultSet.getDate("created_date").toLocalDate();
                LocalDate updatedDate = resultSet.getDate("updated_date").toLocalDate();
                String severity = resultSet.getString("severity");
                String projectName = resultSet.getString("project_name");


                // Fetch reporter details
                String reporterFirstName = resultSet.getString("reporter_first_name");
                String reporterLastName = resultSet.getString("reporter_last_name");
                Reporter reporter = new Reporter();

                reporter.setReporterId(reporterId);
                reporter.setFirstName(reporterFirstName);
                reporter.setLastName(reporterLastName);

                String assigneeFirstName = resultSet.getString("assignee_first_name");
                String assigneeLastName = resultSet.getString("assignee_last_name");


                // Set bug object
                Bug bug = new Bug();
                bug.setAssigneeFirstName(assigneeFirstName);
                bug.setAssigneeLastName(assigneeLastName);
                bug.setProjectName(projectName);
                bug.setBugId(bugId);
                bug.setBugTitle(bugTitle);
                bug.setBugDescription(bugDescription);
                bug.setStatus(status);
                bug.setPriority(priority);
                bug.setEstimatedTimeToComplete(estimatedTime);
                bug.setCreatedDate(createdDate);
                bug.setUpdatedDate(updatedDate);
                bug.setReporter(reporter);
                bug.setSeverity(severity);

                bugs.add(bug);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bugs;
    }

    public static int insertBug(Bug bug) {
        String query = "INSERT INTO bugs (project_id, reporter_id, bug_title, bug_description, status, priority, estimated_time_to_complete, severity, created_date, updated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedBugId = -1; // Initialize to -1

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // Set the parameters for the insert query using bug object properties
            statement.setInt(1, bug.getProjectId());
            statement.setInt(2, bug.getReporterId());
            statement.setString(3, bug.getBugTitle());
            statement.setString(4, bug.getBugDescription());
            statement.setString(5, bug.getStatus());
            statement.setString(6, bug.getPriority());
            statement.setString(7, bug.getEstimatedTimeToComplete());
            statement.setString(8, bug.getSeverity());
            statement.setDate(9, Date.valueOf(bug.getCreatedDate()));
            statement.setDate(10, Date.valueOf(bug.getUpdatedDate()));

            // Execute the insert statement
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the generated keys
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedBugId = generatedKeys.getInt(1); // Update with generated ID
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedBugId;
    }

    public static boolean deleteBug(Bug bug) {
        String query = "DELETE FROM bugs WHERE bug_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bug.getBugId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateBug(Bug bug) {
        String query = "UPDATE bugs " +
                "SET bug_title = ?, bug_description = ?, status = ?, priority = ?, " +
                "severity = ?, estimated_time_to_complete = ?, updated_date = NOW() " +
                "WHERE bug_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, bug.getBugTitle());
            statement.setString(2, bug.getBugDescription());
            statement.setString(3, bug.getStatus());
            statement.setString(4, bug.getPriority());
            statement.setString(5, bug.getSeverity());
            statement.setString(6, bug.getEstimatedTimeToComplete());
            statement.setInt(7, bug.getBugId());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




    public static Map<String, Integer> getBugCountsByMonth() {
        String query = "SELECT TO_CHAR(created_date, 'YYYY-MM') AS month, COUNT(*) AS bug_count " +
                "FROM bugs " +
                "GROUP BY month " +
                "ORDER BY month";

        Map<String, Integer> bugCounts = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String month = resultSet.getString("month");
                int bugCount = resultSet.getInt("bug_count");
                bugCounts.put(month, bugCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bugCounts;
    }

    public static User getAssignedUserForBug(Bug bug) throws SQLException {
        User assignedUser = null;
        String query = "SELECT u.* FROM public.users u " +
                "JOIN public.bug_user bu ON u.user_id = bu.user_id " +
                "WHERE bu.bug_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bug.getBugId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int userId = resultSet.getInt("user_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");


                    assignedUser = new User(userId, firstName, lastName);
                }
            }
        }
        return assignedUser;
    }

    public static void assignBugToUser(Bug bug, User user) {
        String query = "INSERT INTO bug_user (bug_id, user_id) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bug.getBugId());
            statement.setInt(2, user.getUserId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeBugFromUser(Bug bug, User user) throws SQLException {
        String query = "DELETE FROM public.bug_user WHERE bug_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bug.getBugId());
            statement.setInt(2, user.getUserId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // You can handle the exception as needed
        }
    }

    public static void assignBugToTester(int projectId, int bugId) {
        try (Connection connection = DBConnection.getConnection()) {
            // Find testers within the same project
            String query = "SELECT pu.user_id " +
                    "FROM public.project_user pu " +
                    "JOIN public.user_roles ur ON pu.user_id = ur.user_id " +
                    "JOIN public.roles r ON ur.role_id = r.role_id " +
                    "WHERE pu.project_id = ? AND r.role_title = 'Tester'";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, projectId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Integer> testerUserIds = new ArrayList<>();
                    while (resultSet.next()) {
                        testerUserIds.add(resultSet.getInt("user_id"));
                    }

                    // Find the tester with the least tasks
                    int selectedTesterUserId = findTesterWithLeastTasks(projectId);

                    if (selectedTesterUserId != -1) {
                        // Update the bug's assigned developer (tester) ID
                        assignBugToUser(bugId, selectedTesterUserId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void assignBugToUser(int bugId, int userId) {
        String query = "INSERT INTO public.bug_user (bug_id, user_id) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bugId);
            statement.setInt(2, userId);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                // Association created successfully
            } else {
                // Association creation failed
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Integer getAssignedUser(int bugId) {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT user_id FROM public.bug_user WHERE bug_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, bugId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Return the user_id if an assigned developer is found
                        return resultSet.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Return null if no assigned user is found
    }

    public static int findTesterWithLeastTasks(int projectId) throws SQLException {
        int testerId = -1; // Initialize the result to -1

        try (Connection connection = DBConnection.getConnection()) {
            // Find testers within the same project and their task counts
            String query = "SELECT pu.user_id, COUNT(bu.bug_id) AS bug_count " +
                    "FROM public.project_user pu " +
                    "JOIN public.user_roles ur ON pu.user_id = ur.user_id " +
                    "JOIN public.roles r ON ur.role_id = r.role_id " +
                    "LEFT JOIN public.bug_user bu ON pu.user_id = bu.user_id " +
                    "WHERE pu.project_id = ? AND r.role_title = 'Tester' " +
                    "GROUP BY pu.user_id " +
                    "ORDER BY bug_count ASC";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, projectId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    int minBugCount = Integer.MAX_VALUE;
                    List<Integer> testersWithMinTasks = new ArrayList<>();

                    while (resultSet.next()) {
                        int userId = resultSet.getInt("user_id");
                        int bugCount = resultSet.getInt("bug_count");

                        if (bugCount < minBugCount) {
                            minBugCount = bugCount;
                            testersWithMinTasks.clear();
                            testersWithMinTasks.add(userId);
                            System.out.println("User ID: " + userId + ", Bug Count: " + bugCount); // Print user_id and bug_count

                        } else if (bugCount == minBugCount) {
                            testersWithMinTasks.add(userId);
                        }
                    }

                    if (!testersWithMinTasks.isEmpty()) {
                        // Choose a tester randomly from those with the minimum tasks
                        int randomIndex = new Random().nextInt(testersWithMinTasks.size());
                        System.out.println(testersWithMinTasks);
                        testerId = testersWithMinTasks.get(randomIndex); // Update the result
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return testerId; // Return the result, which is -1 if no testers are found with minimum tasks
    }





    public static Map<String, Integer> getPriorityCounts(int projectId) {
        Map<String, Integer> priorityCounts = new HashMap<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT priority, COUNT(*) AS priority_count FROM public.bugs WHERE project_id = ? GROUP BY priority;")) {

            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String priority = resultSet.getString("priority");
                int count = resultSet.getInt("priority_count");
                priorityCounts.put(priority, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return priorityCounts;
    }

    public static Map<String, Integer> getSeverityCounts(int projectId) {
        Map<String, Integer> severityCounts = new HashMap<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT severity, COUNT(*) AS severity_count FROM public.bugs WHERE project_id = ? GROUP BY severity;")) {

            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String severity = resultSet.getString("severity");
                int count = resultSet.getInt("severity_count");
                severityCounts.put(severity, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return severityCounts;
    }





    public static void removeBugsFromUserInProject(User user, Project project) {
        String query = "DELETE FROM public.bug_user WHERE user_id = ? AND bug_id IN " +
                "(SELECT bug_id FROM public.bugs WHERE project_id = ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {


            // Set user and project IDs as parameters
            statement.setInt(1, user.getUserId());
            statement.setInt(2, project.getProjectId());

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static void removeBugsFromUser(User user) {
        String query = "DELETE FROM public.bug_user WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set user ID as a parameter
            statement.setInt(1, user.getUserId());

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void updateBugAssignedUser(int bugId, int selectedTesterUserId) {
        // Define the SQL query for updating the bug_user table
        String updateQuery = "UPDATE bug_user SET user_id = ? WHERE bug_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, selectedTesterUserId); // Set the new user_id
            preparedStatement.setInt(2, bugId); // Set the bug_id for the update

            // Execute the update query
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}

