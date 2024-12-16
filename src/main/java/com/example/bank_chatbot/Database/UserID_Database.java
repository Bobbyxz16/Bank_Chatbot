package com.example.bank_chatbot.Database;



import java.sql.*;

public class UserID_Database { // Declaration of the UserID_Database class

    private static int currentUserId; // Static variable to store the current user's ID

    // Method to set the current user ID
    public static void setCurrentUserId(int userId) {
        currentUserId = userId; // Assign the provided user ID to the static variable
    }

    // Method to get the current user ID
    public static int getCurrentUserId() {
        return currentUserId; // Return the current user ID
    }

    // Method to validate login and retrieve the user ID
    public static int getId(String username, String password) {
        int userId = -1; // Default value if user not found (using -1 to indicate no valid user ID)

        String query = "SELECT id FROM users WHERE username = ? AND password = ?";

        // This SQL query retrieves the 'id' from the 'users' table where the 'username' and 'password'
        // match the provided parameters.

        try (Connection connection = DatabaseConnection.getConnection(); // Establish a connection to the database
             PreparedStatement statement = connection.prepareStatement(query)) { // Prepare the SQL statement for querying user ID

            // Set the parameters for the prepared statement
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery(); // Execute the query and obtain the result set

            // If a record is found, retrieve the user ID
            if (resultSet.next()) {
                userId = resultSet.getInt("id"); // Get the user ID from the result set
            }
        } catch (SQLException e) { // Catch any SQL exceptions that occur during the database operations
            e.printStackTrace(); // Print the stack trace for debugging
        }

        return userId; // Return the user ID or -1 if not found
    }
}