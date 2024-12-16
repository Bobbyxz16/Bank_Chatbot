package com.example.bank_chatbot.UserExperience;

// Importing necessary classes and packages
import com.example.bank_chatbot.Utils.AlertMessages;
import com.example.bank_chatbot.BankingChat.BankingChatbot;
import com.example.bank_chatbot.Database.Conversation_Database;
import com.example.bank_chatbot.Database.DatabaseConnection;
import com.example.bank_chatbot.Database.UserID_Database;
import com.example.bank_chatbot.Utils.Appointments;
import com.example.bank_chatbot.Utils.FeedbackManager;
import com.example.bank_chatbot.Utils.GetAppointmentID;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Controller class for managing the chatbot user interface and interactions
public class ChatbotController {

    // FXML UI components
    @FXML
    private ScrollPane chatScrollPane; // ScrollPane for chat messages
    @FXML
    private VBox appointmentsPanel; // Panel for displaying appointments
    @FXML
    private VBox chatBox; // VBox to hold chat messages
    @FXML
    private TextField messageInput; // Input field for user messages
    @FXML
    private Button sendButton; // Button to send messages
    @FXML
    private Button saveButton; // Button to save user details
    @FXML
    private StackPane mainContentPane; // Main container for UI components
    @FXML
    private VBox chatView; // Chat view container
    @FXML
    private VBox registrationDetailsView; // View for registration details
    @FXML
    private ImageView newChatImage; // Image for starting a new chat
    @FXML
    private ListView<String> conversationListView; // ListView for past conversations
    @FXML
    private ListView<GetAppointmentID> appointmentsListView; // ListView for appointments
    @FXML
    private VBox feedbackSection; // Section for feedback submission

    private final String BOT_NAME = "DBO Assistant"; // Name of the chatbot

    // Fields for user details
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    private boolean feedbackSubmitted = false; // Flag to track feedback submission status
    private int currentConversationId; // ID for the current conversation

    // Instances of various classes for functionality
    private BankingChatbot chatbot; // Instance of the chatbot
    private AlertMessages alert; // Instance for alert messages
    private FeedbackManager feedback; // Instance for feedback management
    private Conversation_Database conversationManager; // Manager for conversation data
    private ChatMessageDisplay messageDisplay; // Display for chat messages
    private Appointments appointment; // Manager for appointments

    // Constructor for ChatbotController
    public ChatbotController() {
        // Initialize AlertMessages in the constructor
        this.alert = new AlertMessages();
    }

    // Initialize method called after FXML components are loaded
    @FXML
    public void initialize() throws SQLException {
        chatbot = new BankingChatbot(); // Create a new chatbot instance
        loadNewChatImage(); // Load the new chat image

        // Initialize AlertMessages with the required FXML components
        initializeAlertMessages();
        initializeFeedbackManager();

        // Create instances of the classes
        messageDisplay = new ChatMessageDisplay(chatBox);
        conversationManager = new Conversation_Database(conversationListView, chatBox);
        feedback = new FeedbackManager(alert);
        appointment = new Appointments(appointmentsListView);

        // Set up initial view state
        showChatView();

        // Set up chat functionality
        chatBox.heightProperty().addListener((observable, oldValue, newValue) -> chatScrollPane.setVvalue(1.0)); // Auto-scroll chat
        conversationListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Show the selected conversation
                showChatView();
            }
        });
        messageInput.setOnAction(event -> handleSendMessage()); // Send message on Enter key
        sendButton.setOnAction(event -> handleSendMessage()); // Send message on button click

        // Initialize conversation ID for the current session
        conversationManager.initializeConversationId();

        // Load past conversation history
        conversationManager.loadConversationHistory();

        // Show initial menu
        showInitialMenu();
    }

    // Initialize FeedbackManager for managing feedback
    public void initializeFeedbackManager() {
        feedback = new FeedbackManager (alert);

        // Set up feedback section if it exists
        if (feedbackSection != null) {
            feedbackSection.setVisible(false); // Initially hide the feedback section
        }
    }

    // Initialize AlertMessages with necessary components
    public void initializeAlertMessages() {
        // Create a new instance if not already created
        if (alert == null) {
            alert = new AlertMessages();
        }

        // Inject the required FXML components into AlertMessages
        try {
            // Use reflection to set the private fields
            java.lang.reflect.Field chatBoxField = AlertMessages.class.getDeclaredField("chatBox");
            java.lang.reflect.Field conversationListViewField = AlertMessages.class.getDeclaredField("conversationListView");
            java.lang.reflect.Field messageInputField = AlertMessages.class.getDeclaredField("messageInput");

            chatBoxField.setAccessible(true);
            conversationListViewField.setAccessible(true);
            messageInputField.setAccessible(true);

            // Set the fields with the corresponding FXML components
            chatBoxField.set(alert, chatBox);
            conversationListViewField.set(alert, conversationListView);
            messageInputField.set(alert, messageInput);
        } catch (Exception e) {
            System.err.println("Error initializing AlertMessages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load the image for starting a new chat
    public void loadNewChatImage() {
        try {
            String newChatImagePath = "/com/example/bank_chatbot/images/new_chat_image.png"; // Path to the image
            Image newChat = new Image(getClass().getResourceAsStream(newChatImagePath)); // Load the image

            // Check for loading errors
            if (newChat.isError()) {
                throw new Exception("Error loading logo image: " + newChat.getException().getMessage());
            }
            newChatImage.setImage(newChat); // Set the image to the ImageView
            newChatImage.setStyle("-fx-cursor: hand;"); // Change cursor style on hover

            // Set mouse event handlers for image opacity
            newChatImage.setOnMouseEntered(e -> newChatImage.setOpacity(0.8));
            newChatImage.setOnMouseExited(e -> newChatImage.setOpacity(1.0));
        } catch (Exception e) {
            System.err.println("Error loading the logo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Show the registration details view
    @FXML
    public void showRegistrationDetails() {
        // Update view visibility
        updateViewVisibility(false, true, false);
        // Load user details
        loadUserDetails(); // Replace with actual user ID
    }

    // Show the chat view
    @FXML
    public void showChatView() {
        updateViewVisibility(true, false, false);
    }

    // Helper method to manage view visibility
    private void updateViewVisibility(boolean chatVisible, boolean registrationVisible, boolean appointmentsVisible) {
        Platform.runLater(() -> {
            // Set visibility for chat view
            chatView.setVisible(chatVisible);
            chatView.setManaged(chatVisible);

            // Set visibility for registration view
            registrationDetailsView.setVisible(registrationVisible);
            registrationDetailsView.setManaged(registrationVisible);

            // Set visibility for appointments panel
            appointmentsPanel.setVisible(appointmentsVisible);
            appointmentsPanel.setManaged(appointmentsVisible);
        });
    }

    // Show the initial menu with the bot's greeting message
    public void showInitialMenu() throws SQLException {
        // Get and display the initial bot message
        String initialMessage = chatbot.generateBotResponse(null);
        messageDisplay.addBotMessage(initialMessage);
    }

    // Handle the creation of a new chat
    @FXML
    public void handleNewChat() throws SQLException {
        conversationManager.loadConversationHistory(); // Load past conversations
        currentConversationId++;  // Increment for a new conversation
        chatBox.getChildren().clear(); // Clear the chat box
        messageInput.clear(); // Clear the input field
        messageInput.setDisable(false); // Enable input field
        sendButton.setDisable(false); // Enable send button
        chatbot = new BankingChatbot(); // Create a new chatbot instance
        showChatView(); // Show chat view
        showInitialMenu(); // Display initial menu
    }

    // Load and display user details from the database
    private void loadUserDetails() {
        // SQL query to fetch user details
        String query = "SELECT name, surname, phone_number, username, password FROM users WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, UserID_Database.getCurrentUserId()); // Set user ID
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Retrieve user details from the result set
                final String name = resultSet.getString("name");
                final String surname = resultSet.getString("surname");
                final String phone = resultSet.getString("phone_number");
                final String username = resultSet.getString("username");
                final String password = resultSet.getString("password");

                // Update UI on JavaFX Application Thread after ResultSet is closed
                Platform.runLater(() -> {
                    nameField.setText(name);
                    surnameField.setText(surname);
                    phoneField.setText(phone);
                    usernameField.setText(username);
                    passwordField.setText(password);

                    // Make fields editable for updating
                    nameField.setEditable(true);
                    surnameField.setEditable(true);
                    phoneField.setEditable(true);
                    usernameField.setEditable(true);
                    passwordField.setEditable(true);

                    // Show the Save button
                    saveButton.setVisible(true);
                });
            } else {
                Platform.runLater(() -> alert.showAlert("User  details not found.")); // Alert if user not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> alert.showAlert("Error loading user details: " + e.getMessage())); // Handle SQL exceptions
        }
    }

    // Method to save the updated user details back to the database
    @FXML
    private void saveUserDetails() {
        String query = "UPDATE users SET name = ?, surname = ?, phone_number = ?, username = ?, password = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the parameters from text fields
            statement.setString(1, nameField.getText());
            statement.setString(2, surnameField.getText());
            statement.setString(3, phoneField.getText());
            statement.setString(4, usernameField.getText());
            statement.setString(5, passwordField.getText());
            statement.setInt(6, UserID_Database.getCurrentUserId()); // Set user ID

            int rowsUpdated = statement.executeUpdate(); // Execute update
            if (rowsUpdated > 0) {
                alert.showAlertConfirmation("Your details have been updated successfully."); // Confirmation alert
            } else {
                alert.showAlert("Failed to update details. Please try again."); // Failure alert
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alert.showAlert("Error updating user details: " + e.getMessage()); // Handle SQL exceptions
        }
    }

    // Method to handle sending messages
    @FXML
    private void handleSendMessage() {
        // Retrieve the text entered by the user, trimming any whitespace
        String message = messageInput.getText().trim(); // Get user input
        // Log the user's message to the console for debugging
        System.out.println("User message: '" + message + "'");

        // Check if the message is not empty before proceeding
        if (!message.isEmpty()) {
            // Display the user's message in the chat interface
            messageDisplay.addUserMessage(message); // Display user message
            // Save the user's message in the conversation history
            conversationManager.saveConversation(message, true);  // Save user message
            // Clear the input field for the next message
            messageInput.clear(); // Clear input field

            // Create a typing indicator to show that the bot is generating a response
            String typingIndicator = BOT_NAME + " is typing..."; // Typing indicator
            TextFlow typingMessageFlow = createMessageFlow(typingIndicator, BOT_NAME);
            // Add the typing indicator to the chat box
            chatBox.getChildren().add(typingMessageFlow); // Add typing indicator to chat

            // Create a new task to handle the bot's response asynchronously
            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    // Check if the message is null or empty and return an error response if so
                    if (message == null || message.isEmpty()) {
                        return chatbot.getErrorResponse(); // Handle empty message
                    }
                    // Generate and return the bot's response based on the user's message
                    return chatbot.generateBotResponse(message);  // Get bot response
                }

                @Override
                protected void succeeded() {
                    // This method is called when the task completes successfully
                    Platform.runLater(() -> {
                        // Remove the typing indicator from the chat box
                        chatBox.getChildren().remove(typingMessageFlow);  // Remove typing indicator

                        // Retrieve the bot's response from the task
                        String botResponse = getValue();  // Get the response from the task
                        // Log the bot's response to the console for debugging
                        System.out.println("Bot response: '" + botResponse + "'");

                        // Check if the bot's response is valid (not null or empty)
                        if (botResponse != null && !botResponse.isEmpty()) {
                            // Display the bot's response in the chat interface
                            messageDisplay.addBotMessage(botResponse);  // Display the bot response
                            // Save the bot's response in the conversation history
                            conversationManager.saveConversation(botResponse, false);  // Save bot response

                            // Optionally handle the end of the conversation
                            if (botResponse.equals("Thank you very much! If you need help with anything else, feel free to ask. Have a great day!")) {
                                // Disable the input field and send button if the conversation is ending
                                messageInput.setDisable(true); // Disable input
                                sendButton.setDisable(true); // Disable send button
                                // Show a feedback dialog to the user
                                feedback.showFeedbackDialog(); // Show feedback dialog
                            }
                        } else {
                            // Handle the case where the bot did not return a valid response
                            String errorMessage = "Error: Bot did not return a valid response.";
                            // Display the error message in the chat interface
                            messageDisplay.addBotMessage(errorMessage); // Display error message
                            // Save the error message in the conversation history
                            conversationManager.saveConversation(errorMessage, false);  // Save error message
                        }
                    });
                }

                @Override
                protected void failed() {
                    // This method is called if the task fails
                    Platform.runLater(() -> {
                        // Remove the typing indicator from the chat box
                        chatBox.getChildren().remove(typingMessageFlow);  // Remove typing indicator

                        // Display an error message
                        String errorMessage = "Error processing the bot's response.";
                        messageDisplay.addBotMessage(errorMessage); // Display error message
                        // Save the error message in the conversation history
                        conversationManager.saveConversation(errorMessage, false);  // Save error message
                    });
                    // Log the exception message to the console for debugging
                    System.out.println("Error: " + getException().getMessage()); // Log error
                }
            };

            // Start the task in a new thread to avoid blocking the UI thread
            new Thread(task).start();  // Start the task in a new thread to avoid blocking the UI thread
        } else {
            // Log a message if the user entered an empty message
            System.out.println("Empty message entered."); // Log if message is empty
        }
    }

    // Handle user logout
    @FXML
    private void handleLogout() {
        if (!feedbackSubmitted) {
            // Show feedback dialog before logout
            feedback.showFeedbackDialog();
        } else {
            // Proceed with normal logout
            alert.showLogoutConfirmation();
        }
    }

    // Create a message flow for displaying messages
    private TextFlow createMessageFlow(String message, String sender) {
        TextFlow messageFlow = new TextFlow();
        messageFlow.getStyleClass().add(sender.equals(BOT_NAME) ? "bot-message" : "user-message"); // Style based on sender

        // Timestamp
        Text timestamp = new Text(getCurrentTimestamp() + "\n");
        timestamp.getStyleClass().add("timestamp");

        // Sender name
        Text senderText = new Text(sender + ": \n");
        senderText.getStyleClass().add("sender");

        // Message content
        Text messageText = new Text(message + "\n\n");
        messageText.getStyleClass().add("message-content");

        messageFlow.getChildren().addAll(timestamp, senderText, messageText); // Add components to message flow
        return messageFlow;
    }

    // Handle changing an appointment
    @FXML
    private void handleChangeAppointment() {
        GetAppointmentID selectedAppointment = appointmentsListView.getSelectionModel().getSelectedItem(); // Get selected appointment
        if (selectedAppointment != null) {
            appointment.showCalendarDialog(selectedAppointment); // Show calendar dialog for selected appointment
        } else {
            alert.showAlert("Please select an appointment to change."); // Alert if no appointment is selected
        }
    }

    // Handle deleting an appointment
    @FXML
    private void handleDeleteAppointment() {
        GetAppointmentID selectedAppointment = appointmentsListView.getSelectionModel().getSelectedItem(); // Get selected appointment
        if (selectedAppointment != null) {
            // Call deleteAppointment with the ID of the selected appointment
            appointment.deleteAppointment(selectedAppointment.getId()); // Delete the selected appointment
        } else {
            alert.showAlert("Please select an appointment to delete."); // Alert if no appointment is selected
        }
    }

    // Get the current timestamp
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // Format timestamp
    }

    // Show the appointments list
    @FXML
    private void showAppointmentsList() {
        updateViewVisibility(false, false, true); // Show appointments panel
    }
}