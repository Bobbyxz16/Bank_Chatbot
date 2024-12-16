package com.example.bank_chatbot.UserExperience;

import com.example.bank_chatbot.Database.Login_Registration_Database; // Importing the Login_Registration_Database for user registration
import com.example.bank_chatbot.Utils.AlertMessages; // Importing the AlertMessages class for showing alerts
import javafx.event.ActionEvent; // Importing ActionEvent for handling button actions
import javafx.fxml.FXML; // Importing FXML annotations for JavaFX
import javafx.fxml.FXMLLoader; // Importing FXMLLoader for loading FXML files
import javafx.scene.Parent; // Importing Parent class for the scene graph
import javafx.scene.Scene; // Importing Scene class for creating scenes
import javafx.scene.control.PasswordField; // Importing PasswordField class for password input
import javafx.scene.control.TextField; // Importing TextField class for text input
import javafx.scene.image.Image; // Importing Image class for loading images
import javafx.scene.image.ImageView; // Importing ImageView class for displaying images
import javafx.stage.Stage; // Importing Stage class for the application window

import java.io.IOException; // Importing IOException for handling I/O errors

public class RegistrationController { // Declaration of the RegistrationController class

    @FXML
    private TextField nameField; // TextField for user's name input
    @FXML
    private TextField surnameField; // TextField for user's surname input
    @FXML
    private TextField numberField; // TextField for user's phone number input
    @FXML
    private TextField emailField; // TextField for user's email input
    @FXML
    private TextField usernameField; // TextField for user's username input
    @FXML
    private PasswordField passwordField; // PasswordField for user's password input
    @FXML
    private ImageView logoImage; // ImageView for displaying the logo
    @FXML
    private ImageView greenArrowImage; // ImageView for displaying a green arrow

    private AlertMessages alert; // Instance of AlertMessages for showing alerts

    @FXML
    public void initialize() {
        // Initialize AlertMessages and other components
        alert = new AlertMessages(); // Create an instance of AlertMessages
        loadLogoImage(); // Load the logo image
        loadGreenArrowImage(); // Load the green arrow image
    }

    // Method to load the logo image from resources
    private void loadLogoImage() {
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

    // Method to load the green arrow image from resources
    private void loadGreenArrowImage() {
        try {
            String greenArrowImagePath = "/com/example/bank_chatbot/images/green_arrow.png"; // Path to the green arrow image
            Image greenArrow = new Image(getClass().getResourceAsStream(greenArrowImagePath)); // Load the image
            if (greenArrow.isError()) { // Check if there was an error loading the image
                throw new Exception("Error loading green arrow image: " + greenArrow.getException().getMessage());
            }
            greenArrowImage.setImage(greenArrow); // Set the loaded image to the ImageView
        } catch (Exception e) {
            System.err.println("Error loading the green arrow: " + e.getMessage()); // Print error message
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Method to handle user registration action
    @FXML
    private void handleRegistration() {
        if (!isInputValid()) { // Validate user input
            return; // Exit if input is not valid
        }

        // Retrieve user input from text fields
        String name = nameField.getText();
        String surname = surnameField.getText();
        String number = numberField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        Login_Registration_Database service = new Login_Registration_Database(); // Create an instance of the registration service

        try {
            // Try registering the user
            boolean userExists = service.registerUser (name, surname, number, email, username, password);

            if (userExists) {
                alert.showAlert("User  already exists! Please choose a different password or email."); // Alert if user already exists
            } else {
                // Registration successful
                alert.showAlertConfirmation("Registration successful! Please log in."); // Confirmation alert for successful registration

                // Navigate to login page after success
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bank_chatbot/UserInterface/login.fxml")); // Load the login FXML file
                Parent root = loader.load(); // Load the root node for login
                Stage stage = (Stage) nameField.getScene().getWindow(); // Get the current stage
                stage.setScene(new Scene(root)); // Set the new scene for login
                stage.show(); // Show the new scene
            }

        } catch (Exception e) {
            alert.showAlert("Registration could not be completed. Please try again."); // Alert for registration failure
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Method to validate user input
    private boolean isInputValid() {
        if (nameField.getText().isEmpty()) {
            alert.showAlert("Name cannot be empty."); // Alert if name is empty
            return false; // Return false for invalid input
        }
        if (surnameField.getText().isEmpty()) {
            alert.showAlert("Surname cannot be empty."); // Alert if surname is empty
            return false; // Return false for invalid input
        }
        if (numberField.getText().isEmpty()) {
            alert.showAlert("Phone number cannot be empty."); // Alert if phone number is empty
            return false; // Return false for invalid input
        }
        if (emailField.getText().isEmpty() || !emailField.getText().contains("@")) {
            alert.showAlert("Please enter a valid email address."); // Alert for invalid email
            return false; // Return false for invalid input
        }
        if (usernameField.getText().isEmpty()) {
            alert.showAlert("Username cannot be empty."); // Alert if username is empty
            return false; // Return false for invalid input
        }
        if (passwordField.getText().isEmpty()) {
            alert.showAlert("Password cannot be empty."); // Alert if password is empty
            return false; // Return false for invalid input
        }
        return true; // Return true if all inputs are valid
    }

    // Method to handle back action to the login page
    public void handleBackAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bank_chatbot/UserInterface/login.fxml")); // Load the login FXML file
        Parent root = loader.load(); // Load the root node for login

        Stage stage = (Stage) usernameField.getScene().getWindow(); // Get the current stage
        stage.setScene(new Scene(root)); // Set the new scene for login
        stage.show(); // Show the new scene
    }
}