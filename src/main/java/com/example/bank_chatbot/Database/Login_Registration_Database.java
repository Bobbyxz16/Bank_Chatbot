package com.example.bank_chatbot.Database;

import com.example.bank_chatbot.Database.DatabaseConnection;

import java.sql.*; // Importing all classes from the java.sql package for database operations

public class Login_Registration_Database {

    // Method to register a new user
    public boolean registerUser (String name, String surname, String phoneNumber, String email, String username, String password) {


        String checkSql = "SELECT COUNT(*) FROM users WHERE password = ? OR email = ?";

        // This SQL query counts the number of records in the 'users' table where either the 'password' or
        // 'email' matches the provided parameters.
        // The query uses the COUNT(*) function, which returns the total number of rows that meet the specified conditions.
        // The placeholders '?' are used for parameterized queries, allowing for safe insertion of user
        // input to prevent SQL injection attacks.
        // This query is typically used to check for the existence of a user with a specific password or
        // email, which is useful for authentication or validation purposes.

        try (Connection connection = DatabaseConnection.getConnection(); // Establish a connection to the database
             PreparedStatement checkStatement = connection.prepareStatement(checkSql)) { // Prepare the SQL statement for checking existing users

            // Set the parameters for the prepared statement
            checkStatement.setString(1, password);
            checkStatement.setString(2, email);

            try (ResultSet resultSet = checkStatement.executeQuery()) { // Execute the query and obtain the result set
                // If a record exists, return true (indicating user already exists)
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return true;
                }
            }


            String sql = "INSERT INTO users (name, surname, phone_number, email, username, password) VALUES (?, ?, ?, ?, ?, ?)";

            // This SQL query is an INSERT statement that adds a new record to the 'users' table.
            // It specifies the columns into which data will be inserted: 'name', 'surname', 'phone_number',
            // 'email', 'username', and 'password'.
            // The placeholders '?' are used for parameterized queries, allowing for safe insertion of user
            // input to prevent SQL injection attacks.
            // This query is used during user registration or when adding a new user to the system.

            try (PreparedStatement statement = connection.prepareStatement(sql)) { // Prepare the insert statement
                // Set the parameters for the prepared statement
                statement.setString(1, name);
                statement.setString(2, surname);
                statement.setString(3, phoneNumber);
                statement.setString(4, email);
                statement.setString(5, username);
                statement.setString(6, password);

                // Execute the update and get the number of rows inserted
                int rowsInserted = statement.executeUpdate();
                // If a row was inserted, print success message and return false (indicating registration was successful)
                if (rowsInserted > 0) {
                    System.out.println("User  registered successfully!");
                    return false;
                }

            } catch (SQLException e) { // Catch any SQL exceptions that occur during the insert process
                System.err.println("Error registering user: " + e.getMessage()); // Print the error message
                e.printStackTrace(); // Print the stack trace for debugging
                throw new RuntimeException("Failed to register user", e); // Throw a runtime exception
            }
        } catch (SQLException e) { // Catch any SQL exceptions that occur during the check process
            System.err.println("Error checking if user exists: " + e.getMessage()); // Print the error message
            e.printStackTrace(); // Print the stack trace for debugging
            return false; // Return false indicating an error occurred
        }

        return false; // Return false if registration failed for any reason
    }

    // Method to validate user login credentials
    public static boolean validateLogin(String username, String password) {

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        // This SQL query retrieves all columns from the 'users' table where the 'username' and 'password'
        // match the provided parameters.
        // The placeholders '?' are used for parameterized queries
        // This query is used for user authentication, where a user provides their username and
        // password to log in.

        try (Connection connection = DatabaseConnection.getConnection(); // Establish a connection to the database
             PreparedStatement statement = connection.prepareStatement(query)) { // Prepare the SQL statement for validation

            // Set the parameters for the prepared statement
            statement.setString(1, username);
            statement.setString(2, password);

            // Execute the query and obtain the result set
            ResultSet resultSet = statement.executeQuery();

            // Return true if a matching record is found (user exists)
            return resultSet.next();

        } catch (SQLException e) { // Catch any SQL exceptions that occur during the validation process
            e.printStackTrace(); // Print the stack trace for debugging
            return false; // Return false indicating an error occurred
        }
    }
}