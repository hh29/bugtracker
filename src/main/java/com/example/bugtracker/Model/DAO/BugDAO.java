package com.example.bugtracker.Model.DAO;

import com.example.bugtracker.DBConnection.DBConnection;
import com.example.bugtracker.Model.Entity.*;

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
             PreparedStatement statement = connection.prepareStatement(query))  {
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
             PreparedStatement statement = connection.prepareStatement(query))
        {
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
             PreparedStatement statement = connection.prepareStatement(query))  {
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
             PreparedStatement statement = connection.prepareStatement(query))   {

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

    public static int getAllUnresolvedBugs() {
        int numberOfUnresolvedBugs = 0;
        String query = "SELECT COUNT(*) AS unresolved_bugs\n" +
                "FROM bugs\n" +
                "WHERE status <>  'Closed';\n";

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                numberOfUnresolvedBugs = resultSet.getInt("unresolved_bugs");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfUnresolvedBugs;
    }
    public static String getAverageResolutionTime() {
        Duration totalDuration = Duration.ZERO;
        int resolvedBugsCount = 0;

        String query = "SELECT created_date, updated_date FROM bugs WHERE status = 'Resolved'";

        try {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    Timestamp createdTimestamp = resultSet.getTimestamp("created_date");
                    Timestamp updatedTimestamp = resultSet.getTimestamp("updated_date");

                    if (createdTimestamp != null && updatedTimestamp != null) {
                        resolvedBugsCount++;
                        totalDuration = totalDuration.plus(Duration.between(createdTimestamp.toLocalDateTime(), updatedTimestamp.toLocalDateTime()));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (resolvedBugsCount > 0) {
                long totalMinutes = totalDuration.toMinutes();
                long averageMinutes = totalMinutes / resolvedBugsCount;

                long days = averageMinutes / (24 * 60);
                long hours = (averageMinutes % (24 * 60)) / 60;


                if (days > 0) {
                    return String.format("%d days", days);
                } else {
                    return String.format("%d hours", hours);
                }
            } else {
                return "N/A"; // No resolved bugs
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static int getNumberOfActiveProjects() {
        int numberOfActiveProjects = 0;
        String query = "SELECT COUNT(DISTINCT projects.project_id) AS active_projects\n" +
                "FROM projects;";

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                numberOfActiveProjects = resultSet.getInt("active_projects");
            }
        } catch (SQLException e) {
            System.out.println(numberOfActiveProjects);
            e.printStackTrace();
        }
        return numberOfActiveProjects;
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
    public static int getUnassignedBugCountForProject(User projectManager, Project project) {
        int unassignedBugCount = 0;
        String query = "SELECT COUNT(b.bug_id) AS unassigned_bug_count " +
                "FROM projects p " +
                "INNER JOIN project_user pu ON p.project_id = pu.project_id " +
                "LEFT JOIN bugs b ON p.project_id = b.project_id " +
                "LEFT JOIN bug_user bu ON b.bug_id = bu.bug_id " +
                "WHERE pu.user_id = ? AND p.project_id = ? AND bu.user_id IS NULL";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, projectManager.getUserId());
            preparedStatement.setInt(2, project.getProjectId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    unassignedBugCount = resultSet.getInt("unassigned_bug_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unassignedBugCount;
    }
    public static String getAverageResolutionTimeForUserInProject(User projectManager, Project project) {
        Duration totalDuration = Duration.ZERO;
        int resolvedBugsCount = 0;

        String query = "SELECT b.created_date, b.updated_date " +
                "FROM bugs b " +
                "INNER JOIN bug_user bu ON b.bug_id = bu.bug_id " +
                "WHERE bu.user_id = ? AND b.project_id = ? AND b.status = 'Resolved'";

        try {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, projectManager.getUserId());
                preparedStatement.setInt(2, project.getProjectId());
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Timestamp createdTimestamp = resultSet.getTimestamp("created_date");
                    Timestamp updatedTimestamp = resultSet.getTimestamp("updated_date");

                    if (createdTimestamp != null && updatedTimestamp != null) {
                        resolvedBugsCount++;
                        totalDuration = totalDuration.plus(Duration.between(createdTimestamp.toLocalDateTime(), updatedTimestamp.toLocalDateTime()));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (resolvedBugsCount > 0) {
                long totalMinutes = totalDuration.toMinutes();
                long averageMinutes = totalMinutes / resolvedBugsCount;

                long days = averageMinutes / (24 * 60);
                long hours = (averageMinutes % (24 * 60)) / 60;

                if (days > 0) {
                    return String.format("%d days", days);
                } else {
                    return String.format("%d hours", hours);
                }
            } else {
                return "N/A"; // No resolved bugs for the user in the project
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static int getUnresolvedBugsForUserInProject(User user, Project project) {
        int numberOfUnresolvedBugs = 0;
        String query = "SELECT COUNT(*) AS unresolved_bugs\n" +
                "FROM bugs b\n" +
                "INNER JOIN project_user pu ON b.project_id = pu.project_id\n" +
                "WHERE pu.user_id = ? AND b.project_id = ? AND b.status <> 'Closed';\n";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, user.getUserId());
            preparedStatement.setInt(2, project.getProjectId());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                numberOfUnresolvedBugs = resultSet.getInt("unresolved_bugs");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfUnresolvedBugs;
    }
    public static void assignBugToTester(int projectId, int bugId) {
        try (Connection connection = DBConnection.getConnection()) {
            // Find testers (users with the "Tester" role) within the same project
            String query = "SELECT pu.user_id " +
                    "FROM public.project_user pu " +
                    "JOIN public.user_roles ur ON pu.user_id = ur.user_id " +
                    "JOIN public.roles r ON ur.role_id = r.role_id " +
                    "WHERE pu.project_id = ? AND r.role_title = 'Tester'";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, projectId); // Assuming projectId is available

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Integer> testerUserIds = new ArrayList<>();
                    while (resultSet.next()) {
                        testerUserIds.add(resultSet.getInt("user_id"));
                    }

                    // Choose a tester randomly from the list
                    if (!testerUserIds.isEmpty()) {
                        int randomIndex = new Random().nextInt(testerUserIds.size());
                        int selectedTesterUserId = testerUserIds.get(randomIndex);


                        // Update the bug's assigned developer (tester) ID
                        assignBugToUser(bugId,selectedTesterUserId);
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
    public static Integer getAssignedDeveloper(int bugId) {
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

        return null; // Return null if no assigned developer is found
    }
    public static void reassignBugToTester(int projectId, Bug bug) {
        // Check if the bug's status is changing to "Testing"

        if (bug != null && "Testing".equals(bug.getStatus())) {
            // Get a list of testers in the project who have the least tasks
            List<Integer> testersWithLeastTasks = findTestersWithLeastTasks(projectId);

            if (!testersWithLeastTasks.isEmpty()) {
                // Choose a tester randomly from the list
                int selectedTesterUserId = chooseRandomTester(testersWithLeastTasks);


                // Update the bug in the database using your updateBugAssignedDeveloper method
                updateBugAssignedDeveloper(bug.getBugId(), selectedTesterUserId);
            }
        }
    }

    private static List<Integer> findTestersWithLeastTasks(int projectId) {
        List<Integer> testersWithLeastTasks = new ArrayList<>();

        String query = "SELECT u.user_id " +
                "FROM public.users u " +
                "JOIN public.project_user pu ON u.user_id = pu.user_id " +
                "JOIN public.bug_user bu ON u.user_id = bu.user_id " +
                "WHERE pu.project_id = ? AND u.role_id = ? " +
                "GROUP BY u.user_id " +
                "ORDER BY COUNT(bu.bug_id) ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            statement.setInt(2, Roles.Tester.getRoleId()); // Replace with the actual tester role ID

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    testersWithLeastTasks.add(resultSet.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return testersWithLeastTasks;
    }

    private static int chooseRandomTester(List<Integer> testers) {
        Random random = new Random();
        int randomIndex = random.nextInt(testers.size());
        return testers.get(randomIndex);
    }
    public static boolean updateBugAssignedDeveloper(int bugId, int assignedDeveloperId) {
        String query = "UPDATE public.bug_user SET user_id = ? WHERE bug_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, assignedDeveloperId);
            statement.setInt(2, bugId);

            int rowsUpdated = statement.executeUpdate();

            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
    public static int countBugsBySeverity(String severity) throws SQLException {
        int count = 0;

        String query = "SELECT COUNT(*) FROM public.bugs WHERE severity = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, severity);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        }

        return count;
    }






}

