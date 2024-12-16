package com.example.bank_chatbot.Database;

import com.example.bank_chatbot.Utils.AlertMessages;
import com.example.bank_chatbot.UserExperience.ChatMessageDisplay;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Conversation_Database {
    private final ListView<String> conversationListView; // ListView to display conversations
    private final VBox chatBox; // VBox to display chat messages
    private final ChatMessageDisplay messageDisplay; // Helper class to manage message display
    private int currentConversationId; // ID of the current conversation
    private final Map<String, Integer> conversationIdMap = new HashMap<>(); // Map to associate conversation names with IDs
    private AlertMessages alert; // Alert message utility

    // Constructor to initialize the Conversation_Database with UI components
    public Conversation_Database(ListView<String> conversationListView, VBox chatBox) {
        this.conversationListView = conversationListView; // Initialize conversation ListView
        this.chatBox = chatBox; // Initialize chat VBox
        this.messageDisplay = new ChatMessageDisplay(chatBox); // Create message display instance
        this.alert = new AlertMessages(); // Create alert message instance
    }

    // Method to initialize the current conversation ID
    public void initializeConversationId() {
        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            int currentUserId = UserID_Database.getCurrentUserId(); // Get the current user ID
            String query = "SELECT COALESCE(MAX(conversation_id), 0) + 1 AS next_id FROM conversations WHERE user_id = ?";

            // This query retrieves the next available conversation ID for a specific user.
            // It uses COALESCE to return the maximum 'conversation_id' from the 'conversations' table
            // for the given 'user_id'.
            // If no conversation IDs exist, it returns 0, and then adds 1 to generate the next conversation ID.
            // The conversation ID is used as a unique identifier for a specific conversation
            // within the chat application. It make it more easy to handle the chat history

            PreparedStatement pstmt = conn.prepareStatement(query); // Prepare the SQL statement
            pstmt.setInt(1, currentUserId); // Set the user ID parameter

            ResultSet resultSet = pstmt.executeQuery(); // Execute the query
            if (resultSet.next()) {
                currentConversationId = resultSet.getInt("next_id"); // Get the next conversation ID
            } else {
                currentConversationId = 1; // Default to 1 if no conversations exist
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for SQL exceptions
        }
    }

    // Method to save a conversation message
    public void saveConversation(String message, boolean isUser ) {
        if (message == null || message.trim().isEmpty()) { // Check for empty message
            System.out.println("Empty message, not saving to conversation."); // Log message
            return; // Exit if message is empty
        }

        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            int currentUserId = UserID_Database.getCurrentUserId(); // Get the current user ID

            String query = "INSERT INTO conversations " + //
                    "(user_id, conversation_id, message, sender, timestamp) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

            // // This query inserts a new record into the 'conversations' table.
            //// It adds values for 'user_id', 'conversation_id', 'message', and 'sender'.
            //// The 'timestamp' is set to the current timestamp using CURRENT_TIMESTAMP,
            // automatically recording the time of the message insertion.

            PreparedStatement pstmt = conn.prepareStatement(query); // Prepare the SQL statement
            pstmt.setInt(1, currentUserId); // Set the user ID parameter
            pstmt.setInt(2, currentConversationId); // Set the conversation ID parameter
            pstmt.setString(3, message.trim()); // Set the message parameter
            pstmt.setString(4, isUser  ? "user" : "bot"); // Set the sender parameter

            pstmt.executeUpdate(); // Execute the insert operation
        } catch (SQLException e) {
            alert.showAlert("Error saving conversation: " + e.getMessage()); // Show alert on error
        }
    }

    // Method to load conversation history
    public void loadConversationHistory() {
        // Clear existing items
        conversationListView.getItems().clear(); // Clear the ListView
        conversationIdMap.clear(); // Clear the conversation ID map

        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            int currentUserId = UserID_Database.getCurrentUserId(); // Get the current user ID

            String query = "SELECT DISTINCT c.conversation_id, " +
                    "(SELECT message FROM conversations c2 " +
                    "WHERE c2.conversation_id = c.conversation_id " +
                    "AND c2.user_id = ? " +
                    "ORDER BY c2.timestamp ASC LIMIT 1) as first_message " +
                    "FROM conversations c " +
                    "WHERE c.user_id = ? " +
                    "ORDER BY c.conversation_id DESC";

            // This query is designed to retrieve a list of unique conversation IDs along with the first message
            // for each conversation associated with a specific user. By using a subquery to fetch the first message,
            // the query ensures that the displayed information is relevant and concise. The use of DISTINCT prevents
            // duplicate conversation IDs, while filtering by user ID ensures that only the relevant conversations are
            // considered. The results are ordered by conversation ID in descending order, allowing users to see their
            // most recent conversations first

            PreparedStatement pstmt = conn.prepareStatement(query); // Prepare the SQL statement
            pstmt.setInt(1, currentUserId); // Set the user ID parameter
            pstmt.setInt(2, currentUserId); // Set the user ID parameter again

            ResultSet resultSet = pstmt.executeQuery(); // Execute the query
            while (resultSet.next()) { // Iterate through the results
                int conversationId = resultSet.getInt("conversation_id"); // Get the conversation ID
                String firstMessage = resultSet.getString("first_message"); // Get the first message
                String conversationName = "Conversation " + conversationId + // Create a conversation name
                        (firstMessage != null ? " - " + firstMessage.substring(0, Math.min(30, firstMessage.length())) + "..." : ""); // Append first message snippet

                conversationListView.getItems().add(conversationName); // Add conversation name to ListView
                conversationIdMap.put(conversationName, conversationId); // Map conversation name to ID
            }

            // Add selection listener
            conversationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> { // Listen for selection changes
                if (newVal != null) { // Check if a new value is selected
                    int selectedConversationId = conversationIdMap.get(newVal); // Get the selected conversation ID
                    loadConversationMessages(selectedConversationId); // Load messages for the selected conversation
                }
            });
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for SQL exceptions
            alert.showAlert("Error loading conversation history: " + e.getMessage()); // Show alert on error
        }
    }

    // Method to load messages for a specific conversation
    public void loadConversationMessages(int conversationId) {
        chatBox.getChildren().clear(); // Clear the chat box
        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            int currentUserId = UserID_Database.getCurrentUserId(); // Get the current user ID

            String query = "SELECT message, sender, timestamp " +
                    "FROM conversations " +
                    "WHERE conversation_id = ? AND user_id = ? " +
                    "ORDER BY timestamp ASC";

            // This query retrieves messages from a specific conversation for a particular user.
            // It selects the 'message', 'sender', and 'timestamp' columns from the 'conversations' table.
            // The 'WHERE' clause filters the results to include only those messages that belong to the
            // specified conversation (identified by 'conversation_id') and user (identified by 'user_id').
            // The results are ordered by 'timestamp' in ascending order, ensuring that the earliest messages
            // appear first.

            PreparedStatement pstmt = conn.prepareStatement(query); // Prepare the SQL statement
            pstmt.setInt(1, conversationId); // Set the conversation ID parameter
            pstmt.setInt(2, currentUserId); // Set the user ID parameter

            ResultSet resultSet = pstmt.executeQuery(); // Execute the query
            while (resultSet.next()) { // Iterate through the results
                String message = resultSet.getString("message"); // Get the message
                boolean isUser  = "user".equals(resultSet.getString("sender")); // Determine if the sender is the user
                messageDisplay.addMessage(message, isUser ); // Display the message
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for SQL exceptions
            alert.showAlert("Error loading conversation messages: " + e.getMessage()); // Show alert on error
        }
    }
}