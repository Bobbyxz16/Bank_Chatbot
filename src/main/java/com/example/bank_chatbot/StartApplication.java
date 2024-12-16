package com.example.bank_chatbot;

import javafx.application.Application; // Importing the Application class from JavaFX
import javafx.fxml.FXMLLoader; // Importing FXMLLoader for loading FXML files
import javafx.scene.Scene; // Importing Scene class for creating a scene
import javafx.stage.Stage; // Importing Stage class for the main application window
import java.io.IOException; // Importing IOException for handling input/output exceptions


public class StartApplication extends Application {

    // Override the start method to set up the primary stage (window) of the application
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file for the user interface (login screen)
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("/com/example/bank_chatbot/UserInterface/login.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        // Set the title of the application window
        stage.setTitle("DBO Bank!");

        // Set the scene for the primary stage
        stage.setScene(scene);

        // Prevent the window from being resizable
        stage.setResizable(false);

        // Display the stage (window) to the user
        stage.show();
    }

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(); // Call the launch method to start the application
    }
}