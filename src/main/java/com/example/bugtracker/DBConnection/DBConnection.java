package com.example.bugtracker.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        final String USERNAME = "postgres";
        final String PASSWORD = "hh291298";
        final String URL = "jdbc:postgresql://localhost:5432/bug_tracker";

        try {
            //Connect to the database
            Class.forName("org.postgresql.Driver");

            return DriverManager.getConnection(URL, USERNAME, PASSWORD);


        } catch (SQLException e) {
            System.out.println("Error connecting to PostgresSQL server");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
