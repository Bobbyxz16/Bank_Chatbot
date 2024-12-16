package com.example.bank_chatbot.Database;

import java.sql.Connection; // Importing the Connection class for establishing database connections
import java.sql.DriverManager; // Importing the DriverManager class for managing database connections
import java.sql.SQLException; // Importing SQLException to handle SQL-related exceptions

public class DatabaseConnection { // Declaration of the DatabaseConnection class

    // Constants for database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_management"; // URL of the database to connect to
    private static final String USER = "bobby"; // Username for database authentication
    private static final String PASS = "Futbol2005@"; // Password for database authentication

    // Method to establish and return a connection to the database
    public static Connection getConnection() throws SQLException {
        // Using DriverManager to get a connection to the specified database
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}