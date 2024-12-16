package com.example.bank_chatbot.UserExperience;

import javafx.application.Platform; // Importing Platform for running tasks on the JavaFX Application Thread
import javafx.geometry.Insets; // Importing Insets for setting padding
import javafx.geometry.Pos; // Importing Pos for positioning elements
import javafx.scene.control.Label; // Importing Label class for displaying text
import javafx.scene.layout.HBox; // Importing HBox class for horizontal layout
import javafx.scene.layout.VBox; // Importing VBox class for vertical layout

public class ChatMessageDisplay { // Declaration of the ChatMessageDisplay class
    private final VBox chatBox; // VBox to hold chat messages

    // Constructor to initialize the chatBox
    public ChatMessageDisplay(VBox chatBox) {
        this.chatBox = chatBox; // Assign the provided VBox to the instance variable
    }

    // Method to add a user message to the chat
    public void addUserMessage(String message) {
        Platform.runLater(() -> { // Ensure that the UI update happens on the JavaFX Application Thread
            addMessage(message, true); // Call addMessage with isUser  set to true
        });
    }

    // Method to add a bot message to the chat
    public void addBotMessage(String message) {
        Platform.runLater(() -> { // Ensure that the UI update happens on the JavaFX Application Thread
            addMessage(message, false); // Call addMessage with isUser  set to false
        });
    }

    // Method to add a message to the chat, distinguishing between user and bot messages
    public void addMessage(String message, boolean isUser ) {
        if (chatBox == null) { // Check if chatBox is null
            System.err.println("ChatBox is null!"); // Print error message
            return; // Exit the method
        }

        Platform.runLater(() -> { // Ensure that the UI update happens on the JavaFX Application Thread

            HBox messageContainer = new HBox(); // Create a horizontal box for the message
            messageContainer.setPadding(new Insets(5)); // Set padding for the message container

            VBox textContainer = new VBox(); // Create a vertical box for the message text
            textContainer.setSpacing(2); // Set spacing between elements in the vertical box

            // Create a label for the sender's name
            Label nameLabel = new Label(isUser  ? "You -" : "- DBO Bank assistant");
            nameLabel.setStyle(isUser  ? "-fx-text-fill: black; -fx-font-size: 10;"
                    : "-fx-text-fill: green; -fx-font-size: 10;"); // Set text color and font size based on sender
            nameLabel.setAlignment(Pos.TOP_RIGHT); // Align name label to the right

            // Create a label for the message text
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true); // Enable text wrapping
            messageLabel.setMaxWidth(400); // Set maximum width for the message
            messageLabel.setPadding(new Insets(10)); // Set padding for the message label

            // Style the message based on whether it's from the user or the bot
            if (isUser ) {
                messageLabel.setStyle("-fx-background-color: #27c301; -fx-text-fill: white; -fx-background-radius: 15 15 0 15;"); // User message style
                textContainer.setAlignment(Pos.CENTER_RIGHT); // Align text container to the right
                messageContainer.setAlignment(Pos.CENTER_RIGHT); // Align message container to the right
            } else {
                messageLabel.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: black; -fx-background-radius: 15 15 15 0;"); // Bot message style
                textContainer.setAlignment(Pos.CENTER_LEFT); // Align text container to the left
                messageContainer.setAlignment(Pos.CENTER_LEFT); // Align message container to the left
            }

            textContainer.getChildren().addAll(nameLabel, messageLabel); // Add name and message labels to the text container

            messageContainer.getChildren().add(textContainer); // Add text container to the message container

            chatBox.getChildren().add(messageContainer); // Add the message container to the chat box
        });
    }
}