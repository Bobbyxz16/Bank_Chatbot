package com.example.bank_chatbot.UserExperience; // Package declaration for organizing user experience-related classes

import com.example.bank_chatbot.Database.Login_Registration_Database; // Importing the Login_Registration_Database for user login and registration
import com.example.bank_chatbot.Database.UserID_Database; // Importing the UserID_Database for user ID management
import com.example.bank_chatbot.Utils.AlertMessages; // Importing the AlertMessages class for showing alerts
import javafx.fxml.FXML; // Importing FXML annotations for JavaFX
import javafx.fxml.FXMLLoader; // Importing FXMLLoader for loading FXML files
import javafx.scene.Parent; // Importing Parent class for the scene graph
import javafx.scene.Scene; // Importing Scene class for creating scenes
import javafx.scene.control.PasswordField; // Importing PasswordField class for password input
import javafx.scene.control.TextField; // Importing TextField class for username input
import javafx.scene.image.Image; // Importing Image class for loading images
import javafx.scene.image.ImageView; // Importing ImageView class for displaying images
import javafx.stage.Stage; // Importing Stage class for the application window

import java.io.IOException; // Importing IOException for handling I/O errors

public class LoginController { // Declaration of the LoginController class

    @FXML
    private TextField usernameField; // TextField for username input
    @FXML
    private PasswordField passwordField; // PasswordField for password input
    @FXML
    private ImageView logoImage; // ImageView for displaying the logo

    private AlertMessages alert; // Instance of AlertMessages for showing alerts

    @FXML
    public void initialize() {
        // Initialize AlertMessages and other components
        alert = new AlertMessages(); // Create an instance of AlertMessages
        loadLogoImage(); // Load the logo image
    }

    // Method to load the logo image from resources
    public void loadLogoImage() {
        try {
            String logoImagePath = "/com/example/bank_chatbot/images/Bank_Logo.png"; // Path to the logo image
            Image logo = new Image(getClass().getResourceAsStream(logoImagePath)); // Load the image
            if (logo.isError()) { // Check if there was an error loading the image
                throw new Exception("Error loading logo image: " + logo.getException().getMessage());
            }
            logoImage.setImage(logo); // Set the loaded image to the ImageView
        } catch (Exception e) {
            System.err.println("Error loading the logo: " + e.getMessage()); // Print error message
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Method to handle user sign-up action
    public void handleSignUp() {
        // Check if the username field is properly initialized
        if (usernameField == null || usernameField.getScene() == null) {
            System.err.println("Username field is not properly initialized or scene is not loaded.");
            return; // Exit the method if the check fails
        }
        try {
            // Load the registration FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bank_chatbot/UserInterface/Registration.fxml"));
            Parent registrationRoot = fxmlLoader.load(); // Load the root node for registration
            Scene registrationScene = new Scene(registrationRoot); // Create a new scene for registration
            Stage currentStage = (Stage) usernameField.getScene().getWindow(); // Get the current stage
            currentStage.setScene(registrationScene); // Set the new scene
            currentStage.setTitle("User  Registration"); // Set the title of the stage
        } catch (IOException e) {
            e.printStackTrace(); // Print stack trace if an error occurs
        }
    }

    // Method to handle user login action
    @FXML
    public void handleLogIn() {
        String username = usernameField.getText(); // Get the username from the input field
        String password = passwordField.getText(); // Get the password from the input field

        // Check for hardcoded admin credentials
        if (username.equals("bobby") && password.equals("admin")) {
            try {
                // Load the admin interface
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bank_chatbot/UserInterface/admin.fxml"));
                Parent root = loader.load(); // Load the admin interface root

                Stage stage = (Stage) usernameField.getScene().getWindow(); // Get the current stage
                stage.setScene(new Scene(root)); // Set the new scene for admin interface
                stage.show(); // Show the new scene
            } catch (IOException e) {
                e.printStackTrace(); // Print stack trace if an error occurs
                alert.showAlert("Unable to load the chatbot interface."); // Show alert for loading error
            }
            alert.showAlertConfirmation("Welcome Admin Manager\n\nAdmins have the possibility to add conversations to the chatbot,\nplease FOLLOW THE INSTRUCTIONS"); // Show confirmation alert for admin
            return; // Exit the method after handling admin login
        }

        int userId = UserID_Database.getId(username, password); // Retrieve user ID based on username and password
        if (userId <= 0) { // Check if user ID is valid
            alert.showAlert("Login Failed: Invalid username or password."); // Show alert for invalid credentials
            return; // Exit the method
        }

        UserID_Database.setCurrentUserId(userId); // Set the current user ID

        // Validate login credentials
        if (Login_Registration_Database.validateLogin(username, password)) {
            try {
                // Load the chatbot interface
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bank_chatbot/UserInterface/chatbot.fxml"));
                Parent root = loader.load(); // Load the chatbot interface root

                Stage stage = (Stage) usernameField.getScene().getWindow(); // Get the current stage
                stage.setScene(new Scene(root)); // Set the new scene for chatbot interface
                stage.show(); // Show the new scene
            } catch (IOException e) {
                e.printStackTrace(); // Print stack trace if an error occurs
                alert.showAlert("Unable to load the chatbot interface."); // Show alert for loading error
            }
        } else {
            alert.showAlert("Login Failed..."); // Show alert for failed login
        }
    }
}