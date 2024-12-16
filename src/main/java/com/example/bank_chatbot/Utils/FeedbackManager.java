package com.example.bank_chatbot.Utils;

import com.example.bank_chatbot.Database.DatabaseConnection;
import com.example.bank_chatbot.Database.UserID_Database;
import javafx.geometry.Pos; // Importing Pos for alignment
import javafx.scene.control.*; // Importing JavaFX controls
import javafx.scene.layout.HBox; // Importing HBox for horizontal layout
import javafx.scene.layout.VBox; // Importing VBox for vertical layout
import javafx.scene.paint.Color; // Importing Color for color handling
import javafx.scene.shape.SVGPath; // Importing SVGPath for creating star shapes

import java.sql.Connection; // Importing Connection for database connection
import java.sql.PreparedStatement; // Importing PreparedStatement for executing SQL statements
import java.sql.SQLException; // Importing SQLException for handling SQL exceptions

public class FeedbackManager { // Declaration of the FeedbackManager class
    private AlertMessages alert; // Instance of AlertMessages for showing alerts
    private boolean feedbackSubmitted; // Flag to check if feedback has been submitted
    private int currentRating; // Variable to hold the current rating

    // Constructor to initialize the FeedbackManager with an AlertMessages instance
    public FeedbackManager(AlertMessages alert) {
        this.alert = alert; // Assign the provided AlertMessages instance
    }

    // Method to create a star shape using SVGPath
    public SVGPath createStar() {
        SVGPath star = new SVGPath(); // Create a new SVGPath for the star
        star.setContent("M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"); // Set the star shape
        star.setFill(Color.rgb(209, 213, 219)); // Set initial fill color to gray
        star.setScaleX(0.8); // Scale the star in the X direction
        star.setScaleY(0.8); // Scale the star in the Y direction
        return star; // Return the created star
    }

    // Method to highlight stars based on the rating
    public void highlightStars(HBox container, int count) {
        for (int i = 0; i < container.getChildren().size(); i++) {
            SVGPath star = (SVGPath) container.getChildren().get(i); // Get the star from the container
            if (i < count) {
                star.setFill(Color.rgb(250, 204, 21)); // Set fill color to yellow for highlighted stars
            } else {
                star.setFill(Color.rgb(209, 213, 219)); // Set fill color to gray for non-highlighted stars
            }
        }
    }

    // Method to handle star click event
    public void handleStarClick(int rating, HBox starContainer, Label feedbackMessage) {
        currentRating = rating; // Set the current rating
        highlightStars(starContainer, rating); // Highlight the selected stars
        saveFeedback(rating); // Save the feedback

        feedbackMessage.setText("Thank you for your rating of " + rating + " stars!"); // Set feedback message
        feedbackMessage.setVisible(true); // Make the feedback message visible
        feedbackSubmitted = true; // Set feedbackSubmitted to true

        // Disable the stars after the rating
        starContainer.setDisable(true); // Disable the star container
    }

    // Method to save feedback to the database
    public void saveFeedback(int rating) {
        try (Connection conn = DatabaseConnection.getConnection()) { // Establish database connection
            String query = "INSERT INTO feedback (user_id, rating) VALUES (?, ?)"; // SQL query to insert feedback
            PreparedStatement pstmt = conn.prepareStatement(query); // Prepare the SQL statement

            pstmt.setInt(1, UserID_Database.getCurrentUserId()); // Set the user ID
            pstmt.setInt(2, rating); // Set the rating

            pstmt.executeUpdate(); // Execute the update
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
            alert.showAlert("Error saving the rating: " + e.getMessage()); // Show error alert
        }
    }

    // Method to show the feedback dialog
    public void showFeedbackDialog() {
        Dialog<ButtonType> dialog = new Dialog<>(); // Create a new dialog
        dialog.setTitle("Feedback"); // Set the dialog title
        dialog.setHeaderText(" Would you like to rate your experience?"); // Set the dialog header text

        // Create the content of the dialog
        VBox content = new VBox(10); // Create a VBox with spacing
        content.setAlignment(Pos.CENTER); // Center align the content

        // Container for the stars
        HBox starContainer = new HBox(5); // Create an HBox for stars with spacing
        starContainer.setAlignment(Pos.CENTER); // Center align the star container

        // Label for the feedback message
        Label feedbackMessage = new Label(); // Create a label for feedback message
        feedbackMessage.setVisible(false); // Initially hide the feedback message
        feedbackMessage.setStyle("-fx-text-fill: #27c301;"); // Set the text color to green

        // Initialize the stars
        for (int i = 0; i < 5; i++) { // Loop to create 5 stars
            SVGPath star = createStar(); // Create a star
            final int rating = i + 1; // Set the rating value

            star.setOnMouseEntered(e -> highlightStars(starContainer, rating)); // Highlight stars on mouse enter
            star.setOnMouseExited(e -> highlightStars(starContainer, currentRating)); // Reset highlight on mouse exit
            star.setOnMouseClicked(e -> { // Handle star click
                handleStarClick(rating, starContainer, feedbackMessage); // Process the click
            });

            star.setStyle("-fx-cursor: hand;"); // Change cursor to hand on hover
            starContainer.getChildren().add(star); // Add star to the container
        }

        content.getChildren().addAll( // Add components to the content
                new Label("Please rate your experience:"), // Prompt label
                starContainer, // Star container
                feedbackMessage // Feedback message label
        );

        dialog.getDialogPane().setContent(content); // Set the dialog content

        // Add buttons
        ButtonType skipButton = new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE); // Skip button
        ButtonType logoutButton = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE); // Logout button
        dialog.getDialogPane().getButtonTypes().addAll(skipButton, logoutButton); // Add buttons to dialog

        // Handle the result
        dialog.showAndWait().ifPresent(result -> { // Show dialog and wait for response
            if (result == logoutButton) { // If logout button is pressed
                alert.showLogoutConfirmation(); // Show logout confirmation
            }
        });
    }
}