package com.example.bugtracker.Model.DAO;

import com.example.bugtracker.DBConnection.DBConnection;
import com.example.bugtracker.Model.Entity.Project;
import com.example.bugtracker.Model.Entity.Roles;
import com.example.bugtracker.Model.Entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

    public class ProjectDAO {
        public static List<Project> getProjectsForUser(User user) {
            List<Project> projects = new ArrayList<>();
            String query = "SELECT p.project_id, p.project_name, p.project_description, p.status, p.priority, " +
                    "p.start_date, p.end_date, p.project_manager, pm.first_name AS manager_first_name, " +
                    "pm.last_name AS manager_last_name " +
                    "FROM projects p " +
                    "JOIN project_user pu ON p.project_id = pu.project_id " +
                    "JOIN users pm ON p.project_manager = pm.user_id " +
                    "WHERE pu.user_id = ? AND p.status <> 'Completed'";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, user.getUserId());
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Project project = new Project();
                    project.setProjectId(resultSet.getInt("project_id"));
                    project.setProjectName(resultSet.getString("project_name"));
                    project.setProjectDescription(resultSet.getString("project_description"));
                    project.setStatus(resultSet.getString("status"));
                    project.setPriority(resultSet.getString("priority"));
                    project.setStartDate(resultSet.getDate("start_date").toLocalDate());
                    project.setEndDate(resultSet.getDate("end_date").toLocalDate());
                    project.setProjectManagerId(resultSet.getInt("project_manager"));
                    project.setManagerFirstName(resultSet.getString("manager_first_name"));
                    project.setManagerLastName(resultSet.getString("manager_last_name"));

                    projects.add(project);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return projects;
        }

        public static List<Project> getAllProjects() {
            List<Project> projects = new ArrayList<>();
            String query = "SELECT p.project_id, p.project_name, p.project_description, " +
                    "p.status, p.priority, p.end_date, p.project_manager, " +
                    "u.first_name AS manager_first_name, u.last_name AS manager_last_name " +
                    "FROM public.projects p " +
                    "JOIN public.users u ON p.project_manager = u.user_id " +
                    "ORDER BY p.project_id";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Project project = new Project();
                    project.setProjectId(resultSet.getInt("project_id"));
                    project.setProjectName(resultSet.getString("project_name"));
                    project.setProjectDescription(resultSet.getString("project_description"));
                    project.setStatus(resultSet.getString("status"));
                    project.setPriority(resultSet.getString("priority"));
                    project.setEndDate(resultSet.getDate("end_date").toLocalDate());
                    project.setProjectManagerId(resultSet.getInt("project_manager"));
                    project.setManagerFirstName(resultSet.getString("manager_first_name"));
                    project.setManagerLastName(resultSet.getString("manager_last_name"));

                    projects.add(project);
                }


                return projects;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static List<Project> getAllActiveProjects() {
            List<Project> projects = new ArrayList<>();
            String query = "SELECT * from projects where status <> 'Completed' ";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Project project = new Project();
                    project.setProjectId(resultSet.getInt("project_id"));
                    project.setProjectName(resultSet.getString("project_name"));
                    project.setProjectDescription(resultSet.getString("project_description"));
                    project.setStatus(resultSet.getString("status"));
                    project.setPriority(resultSet.getString("priority"));
                    project.setEndDate(resultSet.getDate("end_date").toLocalDate());
                    project.setProjectManagerId(resultSet.getInt("project_manager"));

                    projects.add(project);
                }


                return projects;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static void addProjectToUser(User user, Project project) {
            String query = "INSERT INTO public.project_user (project_id, user_id) VALUES (?, ?)";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, project.getProjectId());
                statement.setInt(2, user.getUserId());
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void removeProjectFromUser(User user, Project project) {
            String query = "DELETE FROM public.project_user WHERE project_id = ? AND user_id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, project.getProjectId());
                statement.setInt(2, user.getUserId());
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static int insertProject(Project project) throws SQLException {
            String query = "INSERT INTO public.projects(" +
                    "project_name, project_description, status, priority, start_date, end_date, project_manager) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING project_id";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, project.getProjectName());
                statement.setString(2, project.getProjectDescription());
                statement.setString(3, project.getStatus());
                statement.setString(4, project.getPriority());
                statement.setDate(5, Date.valueOf(project.getStartDate()));
                statement.setDate(6, Date.valueOf(project.getEndDate()));
                statement.setInt(7, project.getProjectManagerId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve the generated project ID and return it
                    return resultSet.getInt("project_id");
                } else {
                    // Handle the case where no project ID was generated
                    throw new SQLException("Failed to insert project or retrieve generated ID.");
                }
            }
        }


        public static void updateProject(Project project) throws SQLException {
            String query = "UPDATE public.projects " +
                    "SET project_name = ?, project_description = ?, status = ?, " +
                    "priority = ?, end_date = ?, project_manager = ? " +
                    "WHERE project_id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, project.getProjectName());
                statement.setString(2, project.getProjectDescription());
                statement.setString(3, project.getStatus());
                statement.setString(4, project.getPriority());
                statement.setDate(5, Date.valueOf(project.getEndDate()));
                statement.setInt(6, project.getProjectManagerId());
                statement.setInt(7, project.getProjectId());

                statement.executeUpdate();
            }
        }

        public static void deleteProject(int projectId) throws SQLException {
            String query = "DELETE FROM public.projects WHERE project_id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, projectId);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static ObservableList<User> getDevelopersOrTestersForProject(Project project) throws SQLException {
            List<User> users = new ArrayList<>();

            String query = "SELECT u.user_id, u.first_name, u.last_name, r.role_title " +
                    "FROM public.users u " +
                    "JOIN public.user_roles ur ON u.user_id = ur.user_id " +
                    "JOIN public.roles r ON ur.role_id = r.role_id " +
                    "JOIN public.project_user pu ON u.user_id = pu.user_id " +
                    "WHERE r.role_title IN ('Developer', 'Tester') " +
                    "AND pu.project_id = ?";



            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, project.getProjectId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int userId = resultSet.getInt("user_id");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String roleName = resultSet.getString("role_title");


                        User user = new User(userId, firstName, lastName,roleName);
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setUserId(userId);

                        users.add(user);
                    }
                }
            }
            // Convert the List<User> to an ObservableList<User>
            return FXCollections.observableArrayList(users);

        }

        public static void assignUsersToProject(List<User> users, Project project) {
            String query = "INSERT INTO public.project_user (project_id, user_id) VALUES (?, ?)";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (User user : users) {
                    statement.setInt(1, project.getProjectId());
                    statement.setInt(2, user.getUserId());
                    statement.addBatch();
                }

                statement.executeBatch();

            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception properly
            }
        }

        public static void insertProjectManager(Project project, int projectManagerId) throws SQLException {
            String query = "INSERT INTO project_user (project_id, user_id) VALUES (?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, project.getProjectId());
                preparedStatement.setInt(2, projectManagerId);
                preparedStatement.executeUpdate();
            }
        }

        public static void updateProjectManager(Project project, int newProjectManagerId) throws SQLException {
            String query = "UPDATE project_user SET user_id = ? WHERE project_id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, newProjectManagerId);
                preparedStatement.setInt(2, project.getProjectId());
                preparedStatement.executeUpdate();
            }
        }
        public static boolean isUserAssignedToProject(User user, Project project) {
            String query = "SELECT COUNT(*) FROM project_user WHERE project_id = ? AND user_id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, project.getProjectId());
                preparedStatement.setInt(2, user.getUserId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }


        public static User getProjectManagerForProject(Project project) {
            String query = "SELECT u.user_id, u.first_name, u.last_name " +
                    "FROM project_user pu " +
                    "JOIN user_roles ur ON pu.user_id = ur.user_id " +
                    "JOIN roles r ON ur.role_id = r.role_id " +
                    "JOIN users u ON pu.user_id = u.user_id " +
                    "WHERE pu.project_id = ? " +
                    "AND r.role_id = 2";

            User projectManager = null;
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, project.getProjectId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Construct a User object with user_id, first_name, and last_name
                        int userId = resultSet.getInt("user_id");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");

                        projectManager =  new User(userId, firstName, lastName);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return projectManager;
        }

        public static boolean isUserProjectManager(User user) {
            String query = "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role_id = 2";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, user.getUserId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }

        public static void updateProjectManager(Project project, User user) throws SQLException {
            String query = "UPDATE public.projects SET project_manager = ? WHERE project_id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, user.getUserId());
                statement.setInt(2,project.getProjectId() );

                statement.executeUpdate();
            }
        }
        public static List<User> getProjectMembers(int projectId) throws SQLException {
            List<User> members = new ArrayList<>();

            String query = "SELECT DISTINCT " +
                    "    u.user_id, " +
                    "    u.username, " +
                    "    u.first_name, " +
                    "    u.last_name, " +
                    "    r.role_title " +
                    "FROM " +
                    "    public.users u " +
                    "INNER JOIN " +
                    "    public.project_user pu ON u.user_id = pu.user_id " +
                    "INNER JOIN " +
                    "    public.user_roles ur ON u.user_id = ur.user_id " +
                    "LEFT JOIN " +
                    "    public.bug_user bu ON u.user_id = bu.user_id " +
                    "INNER JOIN " +
                    "    public.roles r ON ur.role_id = r.role_id " +
                    "WHERE " +
                    "    pu.project_id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                // Bind the parameter projectId to the prepared statement
                preparedStatement.setInt(1, projectId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("user_id");
                        String username = resultSet.getString("username");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String roleTitle = resultSet.getString("role_title");

                        User member = new User(id, username, firstName, lastName, roleTitle);
                        members.add(member);
                    }
                }
            }

            return members;
        }


        public static Project getProjectById(int projectId) {
            String query = "SELECT project_id, project_name, project_description, status, priority, start_date, end_date, project_manager " +
                    "FROM public.projects WHERE project_id = ?";

            try (PreparedStatement statement = DBConnection.getConnection().prepareStatement(query)) {
                statement.setInt(1, projectId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve project details from the result set
                        int projectIdFromDB = resultSet.getInt("project_id");
                        String projectName = resultSet.getString("project_name");
                        String projectDescription = resultSet.getString("project_description");
                        String status = resultSet.getString("status");
                        String priority = resultSet.getString("priority");
                        LocalDate startDate = resultSet.getDate("start_date").toLocalDate();
                        LocalDate endDate = resultSet.getDate("end_date").toLocalDate();
                        int projectManager = resultSet.getInt("project_manager");

                        // Create a new Project object and set the fields
                        Project project = new Project();
                        project.setProjectId(projectIdFromDB);
                        project.setProjectName(projectName);
                        project.setProjectDescription(projectDescription);
                        project.setStatus(status);
                        project.setPriority(priority);
                        project.setStartDate(startDate);
                        project.setEndDate(endDate);
                        project.setProjectManagerId(projectManager);

                        return project;
                    } else {
                        // No project with the specified ID found
                        return null;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }




    }