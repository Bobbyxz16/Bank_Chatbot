package com.example.bank_chatbot.Utils;

import com.example.bank_chatbot.UserExperience.AdminController; // Importing AdminController for admin-related actions
import javafx.application.Platform; // Importing Platform for exiting the application
import javafx.fxml.FXML; // Importing FXML annotations for JavaFX
import javafx.scene.Parent; // Importing Parent class for the scene graph
import javafx.scene.Scene; // Importing Scene class for creating scenes
import javafx.scene.control.*; // Importing JavaFX controls
import javafx.scene.layout.VBox; // Importing VBox for layout
import javafx.stage.Stage; // Importing Stage class for the application window

import java.io.IOException; // Importing IOException for handling I/O errors
import java.util.*; // Importing utility classes for collections

public class AlertMessages { // Declaration of the AlertMessages class

    @FXML
    private VBox chatBox; // VBox for displaying chat messages
    @FXML
    private ListView<String> conversationListView; // ListView for displaying conversations
    private final Map<String, Integer> conversationIdMap = new HashMap<>(); // Map to associate conversation names with IDs
    private int currentConversationId; // Current conversation ID
    @FXML
    private TextField messageInput; // TextField for user message input

    private AdminController admin; // Instance of AdminController for admin-related actions

    public AlertMessages() {
        // Default constructor
    }

    // Constructor to accept the AdminController instance
    public AlertMessages(AdminController admin) {
        this.admin = admin; // Assign the provided AdminController instance
    }

    // Method to show a confirmation alert with a message
    public void showAlertConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Create an information alert
        alert.setContentText(message); // Set the content text
        alert.showAndWait(); // Show the alert and wait for user response
    }

    // Method to show an admin alert with options for logout or new conversation
    public void showAdminAlertConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Create an information alert
        alert.setTitle(title); // Set the title
        alert.setHeaderText(null); // No header text
        alert.setContentText(message); // Set the content text

        // Add buttons for logout or new conversation
        ButtonType logoutButton = new ButtonType("Logout"); // Button for logout
        ButtonType newConversationButton = new ButtonType("New Conversation"); // Button for new conversation
        alert.getButtonTypes().setAll(logoutButton, newConversationButton); // Set the buttons

        // Handle button clicks
        alert.showAndWait().ifPresent(response -> {
            if (response == logoutButton) {
                showAdminLogout(); // Call method to handle admin logout
            } else if (response == newConversationButton) {
                admin.loadNewConversationInterface(); // Call method to load new conversation interface
            }
        });
    }

    // Method to show an error alert with a message
    public void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Create an error alert
        alert.setTitle("Error"); // Set the title
        alert.setHeaderText(null); // No header text
        alert.setContentText(content); // Set the content text
        alert.showAndWait(); // Show the alert and wait for user response
    }

    // Method to show a confirmation alert for logout
    public void showLogoutConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Create a confirmation alert
        alert.setTitle("Logout Confirmation"); // Set the title
        alert.setHeaderText("Are you sure you want to log out?"); // Set the header text

        Optional<ButtonType> result = alert.showAndWait(); // Show the alert and wait for user response
        if (result.isPresent() && result.get() == ButtonType.OK) { // Check if user confirmed logout
            cleanupBeforeLogout(); // Clean up before logout
            Platform.exit(); // Exit the application
        }
    }

    // Method to show a confirmation alert for admin logout
    public void showAdminLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Create a confirmation alert
        alert.setTitle("Logout Confirmation"); // Set the title
        alert.setHeaderText("Are you sure you want to log out?"); // Set the header text

        Optional<ButtonType> result = alert.showAndWait(); // Show the alert and wait for user response
        if (result.isPresent() && result.get() == ButtonType.OK) { // Check if user confirmed logout
            Platform.exit(); // Exit the application
        }
    }

    // Method to clean up data before logout
    public void cleanupBeforeLogout() {
        // Clear all local data
        chatBox.getChildren().clear(); // Clear chat messages
        conversationListView.getItems().clear(); // Clear conversation list
        conversationIdMap.clear(); // Clear conversation ID map
        currentConversationId = 0; // Reset current conversation ID
        messageInput.clear(); // Clear message input field
    }
}