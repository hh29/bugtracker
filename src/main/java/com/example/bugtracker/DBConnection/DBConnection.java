package com.example.bugtracker.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        final String USERNAME = "";         // CHANGE DETAILS TO SUIT DBMS
        final String PASSWORD = "";
        final String URL = "jdbc:postgresql://localhost:5432/";

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
