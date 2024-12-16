package com.example.bank_chatbot.UserExperience;

import com.example.bank_chatbot.Database.DatabaseConnection;
import com.example.bank_chatbot.Utils.AlertMessages; // Importing the AlertMessages class for showing alerts
import javafx.fxml.FXML; // Importing FXML annotations for JavaFX
import javafx.fxml.FXMLLoader; // Importing FXMLLoader for loading FXML files
import javafx.scene.Parent; // Importing Parent class for the scene graph
import javafx.scene.Scene; // Importing Scene class for creating scenes
import javafx.scene.control.TextArea; // Importing TextArea class for multi-line text input
import javafx.scene.control.TextField; // Importing TextField class for single-line text input
import javafx.stage.Stage; // Importing Stage class for the application window

import java.io.IOException; // Importing IOException for handling I/O errors
import java.sql.*; // Importing all classes from the java.sql package for database operations

public class AdminController { // Declaration of the AdminController class

    @FXML
    private TextField conversationTypeField; // TextField for conversation type name
    @FXML
    private TextField keywordsField; // TextField for trigger words (keywords)
    @FXML
    private TextField userQuestionField; // TextField for the user question
    @FXML
    private TextArea flowField; // TextArea for conversation steps

    private AlertMessages alert; // Instance of AlertMessages for showing alerts

    @FXML
    public void initialize() {
        alert = new AlertMessages(this); // Initialize AlertMessages with the current AdminController instance
    } // Initialize method to set up the controller

    @FXML
    private void handleAdd() { // Method to handle adding conversation data
        // Retrieve text from input fields
        String conversationType = conversationTypeField.getText();
        String keywords = keywordsField.getText();
        String userQuestion = userQuestionField.getText();
        String flow = flowField.getText();
        handleAddAdminQuestion(); // Call method to add admin question

        // Validate input fields
        if (conversationType.isEmpty() || keywords.isEmpty() || userQuestion.isEmpty() || flow.isEmpty()) {
            alert.showAlert("Please fill in all fields."); // Show alert if any field is empty
            return; // Exit the method
        }

        // Check each line in flow for exactly 3 commas
        String[] flows = flow.split("\n"); // Split flow into lines
        for (String flowEntry : flows) {
            if (flowEntry.chars().filter(ch -> ch == ',').count() != 3) { // Count commas in each line
                alert.showAlert("Each line in flow must contain exactly three commas."); // Show alert if not
                return; // Exit the method
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            // SQL query to insert conversation type
            String insertTypeSQL = "INSERT INTO ConversationTypes (name) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertTypeSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, conversationType); // Set conversation type name
                pstmt.executeUpdate(); // Execute the insert

                // Get the last inserted ID for conversation type
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) { // If a key is generated
                        int conversationTypeId = generatedKeys.getInt(1); // Get the generated ID

                        // Insert keywords into the database
                        for (String keyword : keywords.split(",")) { // Split keywords by comma
                            String insertKeywordSQL = "INSERT INTO Keywords (keyword, conversation_type_id) VALUES (?, ?)";
                            try (PreparedStatement keywordStmt = conn.prepareStatement(insertKeywordSQL)) {
                                keywordStmt.setString(1, keyword.trim()); // Set keyword
                                keywordStmt.setInt(2, conversationTypeId); // Set conversation type ID
                                keywordStmt.executeUpdate(); // Execute the insert
                            }
                        }

                        // Insert conversation flows into the database
                        for (int i = 0; i < flows.length; i++) {
                            String[] parts = flows[i].split(","); // Split flow entry into parts
                            if (parts.length >= 4) { // Ensure there are at least 4 parts
                                String currentState = parts[0].trim(); // Get current state
                                String userInput = (i == 0) ? null : parts[1].trim(); // Set user_input to NULL for the first entry
                                String responseText = parts[2].trim(); // Get response text
                                String nextState = parts[3].trim(); // Get next state

                                // SQL query to insert conversation flows
                                String insertFlowSQL = "INSERT INTO ConversationFlows (conversation_type_id, current_state, user_input, response_text, next_state) VALUES (?, ?, ?, ?, ?)";
                                try (PreparedStatement flowStmt = conn.prepareStatement(insertFlowSQL)) {
                                    flowStmt.setInt(1, conversationTypeId); // Set conversation type ID
                                    flowStmt.setString(2, currentState); // Set current state
                                    flowStmt.setObject(3, userInput); // Use setObject to set NULL for user input
                                    flowStmt.setString(4, responseText); // Set response text
                                    flowStmt.setString(5, nextState); // Set next state
                                    flowStmt.executeUpdate(); // Execute the insert
                                }
                            }
                        }

                        alert.showAdminAlertConfirmation("Success", "Conversation type, keywords, and flows added successfully."); // Show success alert
                    } else {
                        throw new SQLException("Creating conversation type failed, no ID obtained."); // Throw exception if no ID is obtained
                    }
                }
            } catch (SQLException e) {
                alert.showAlert("Error adding conversation data: " + e.getMessage()); // Show error alert
            }
        } catch (SQLException e) {
            alert.showAlert("Error adding conversation data: " + e.getMessage()); // Show error alert
        }
    }

    // Method to handle adding admin questions
    @FXML
    private void handleAddAdminQuestion() {
        String questionText = userQuestionField.getText().trim(); // Get the question text

        if (questionText.isEmpty()) { // Check if the question text is empty
            alert.showAlert("Please enter a question."); // Show alert if empty
            return; // Exit the method
        }

        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            String insertQuestionSQL = "INSERT INTO AdminQuestions (question_text, is_active) VALUES (?, ?)"; // SQL query to insert admin question
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuestionSQL)) {
                pstmt.setString(1, questionText); // Set the question text
                pstmt.setBoolean(2, true); // Mark the question as active by default
                pstmt.executeUpdate(); // Execute the insert
            }
        } catch (SQLException e) {
            e.getMessage(); // Handle SQL exception
        }
    }

    // Method to load a new conversation interface
    public void loadNewConversationInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bank_chatbot/UserInterface/admin.fxml")); // Load the FXML file
            Parent root = loader.load(); // Load the root node

            // Get the current stage and change the scene
            Stage stage = (Stage) conversationTypeField.getScene().getWindow(); // Get the current stage
            stage.setScene(new Scene(root)); // Set the new scene
            stage.show(); // Show the new scene

            // Clear input fields
            conversationTypeField.clear();
            keywordsField.clear();
            userQuestionField.clear();
            flowField.clear();
        } catch (IOException e) {
            e.printStackTrace(); // Print stack trace for debugging
            alert.showAlert("Unable to load the chatbot interface."); // Provide user feedback
        }
    }
}