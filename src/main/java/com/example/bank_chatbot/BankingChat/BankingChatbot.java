package com.example.bank_chatbot.BankingChat;

// Import necessary classes for database operations and utility functions
import com.example.bank_chatbot.Database.DatabaseConnection;
import com.example.bank_chatbot.Database.UserID_Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankingChatbot {
    // Variables to manage the chatbot's state and conversation flow
    private String currentConversationType = null; // Tracks the current type of conversation
    private String currentState = "INITIAL"; // Tracks the current state of the conversation
    private boolean isFirstMessage = true; // Flag to indicate if it's the user's first message
    private boolean isWaitingForHelpResponse = false; // Flag to indicate if the bot is waiting for help response
    private String appointmentType; // Stores the type of appointment requested by the user
    private String appointmentDateTime; // Stores the date and time for the appointment

    // Map to associate keywords with conversation types
    private Map<String, String> keywordToConversationTypeMap = new HashMap<>();

    // Constructor
    public BankingChatbot() {
        // Initialize the keyword to conversation type mapping
        initializeKeywordToConversationTypeMap();
    }

    // Method to initialize keyword mappings from the database
    private void initializeKeywordToConversationTypeMap() {
        keywordToConversationTypeMap.clear(); // Clear any existing data

        // SQL query to fetch keywords and their corresponding conversation types
        String query = "SELECT k.keyword, c.name " +
                "FROM Keywords k " +
                "INNER JOIN ConversationTypes c ON k.conversation_type_id = c.id";

        // This SQL query retrieves the 'keyword' from the 'Keywords' table and the 'name' from the
        // 'ConversationTypes' table.
        // It uses an INNER JOIN to combine records from both tables based on the relationship defined by
        // 'conversation_type_id' in the 'Keywords' table and 'id' in the 'ConversationTypes' table.
        // The result will include pairs of keywords and their corresponding conversation type names, allowing
        // for a clear association between keywords and the types of conversations they are related to.

        // Establish a database connection and execute the query
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Iterate through the result set and populate the map
            while (rs.next()) {
                String keyword = rs.getString("keyword").trim();
                String conversationType = rs.getString("name").trim();

                // Add the keyword to the map
                keywordToConversationTypeMap.put(keyword, conversationType);
            }

        } catch (SQLException e) {
            // Handle any SQL exceptions that occur during the loading process
            System.err.println("Error loading keywords and conversation types: " + e.getMessage());
        }
    }

    // Method to generate a response based on the user's message
    public String generateBotResponse(String message) {
        System.out.println("User  input: '" + message + "'");  // Log user input

        // Handle the first message from the user
        if (isFirstMessage) {
            isFirstMessage = false; // Set the flag to false after the first message
            return getWelcomeMessage() + "\n" + displayMainMenu();  // First message response
        }

        // Check for empty messages
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Received an empty message.");
            return getErrorResponse(); // Return error response for empty input
        }

        // Normalize the message to lower case and trim whitespace
        message = message.toLowerCase().trim();

        // Handle exit command
        if (message.equals("exit")) {
            return "Thank you very much! If you need help with anything else, feel free to ask. Have a great day!";
        }

        // Check if the bot is waiting for a help response
        if (isWaitingForHelpResponse) {
            return handleHelpResponse(message); // Process help response
        }

        // If there is no current conversation type, find the best match
        if (currentConversationType == null) {
            currentConversationType = findBestMatchingConversationType(message);
            if (currentConversationType == null) {
                return getErrorResponse(); // Return error if no conversation type is found
            }
            // Start the initial conversation flow
            return startInitialConversationFlow();
        }

        // Process user input for the current conversation
        return processUserInputForCurrentConversation(message);
    }

    // Method to find the best matching conversation type based on user input
    private String findBestMatchingConversationType(String message) {
        // Convert the message to lowercase for case-insensitive matching
        String[] words = message.toLowerCase().split("\\s+");
        Map<String, Integer> conversationTypeScores = new HashMap<>();

        // Check each word against keywords in the map
        for (String word : words) {
            for (Map.Entry<String, String> entry : keywordToConversationTypeMap.entrySet()) {
                // Use contains to allow partial matches or exact matches
                if (entry.getKey().contains(word)) {
                    conversationTypeScores.merge(entry.getValue(), 1, Integer::sum); // Increment score for matching conversation type
                }
            }
        }


        // Return the conversation type with the highest score, or null if none found
        return conversationTypeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Method to start the initial conversation flow based on the current conversation type
    private String startInitialConversationFlow() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT response_text, next_state " +
                             "FROM ConversationFlows " +
                             "WHERE conversation_type_id = (SELECT id FROM ConversationTypes WHERE name = ?) " +
                             "AND current_state = 'INITIAL' " +
                             "AND user_input IS NULL")) {

            // This SQL query retrieves the 'response_text' and 'next_state' from the 'ConversationFlows' table.
            // It applies the following filters:
            // 1. The 'conversation_type_id' must match the ID of a specific conversation type, which is determined
            // by a subquery that selects the ID from the 'ConversationTypes' table based on the provided name
            // (indicated by the placeholder '?').
            // 2. The 'current_state' must be equal to 'INITIAL', indicating that the query is looking for flows that
            // start from the initial state of the conversation.
            // 3. The 'user_input' must be NULL, meaning that this part of the flow does not require any user input
            // to proceed.
            // This query is useful for initiating a conversation flow based on a specific conversation type when
            // no user input is present.

            stmt.setString(1, currentConversationType); // Set the conversation type in the query

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String responseText = rs.getString("response_text"); // Get the response text
                    currentState = rs.getString("next_state"); // Update the current state
                    return responseText; // Return the response text
                }
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions that occur during the conversation flow start
            System.err.println("Error starting conversation: " + e.getMessage());
            e.printStackTrace();
        }

        return getErrorResponse(); // Return error response if no valid flow is found
    }

    // Method to process user input for the current conversation state
    private String processUserInputForCurrentConversation(String message) {
        // Normalize user input
        message = message != null ? message.toLowerCase().trim() : "";

        // Define valid inputs for yes/no responses
        Set<String> validInputs = new HashSet<>(Arrays.asList("yes", "no"));

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT next_state, response_text " +
                             "FROM ConversationFlows " +
                             "WHERE conversation_type_id = (SELECT id FROM ConversationTypes WHERE name = ?) " +
                             "AND current_state = ? " +
                             "AND (user_input = ? OR user_input IS NULL)")) {

            // This query retrieves the next state and response text from the 'ConversationFlows' table.
            // It filters the results based on the following criteria:
            // 1. The 'conversation_type_id' must match the ID of a specific conversation type, which is
            // determined by a subquery that selects the ID from the 'ConversationTypes' table based on the provided name.
            // 2. The 'current_state' must match a specified value.
            // 3. The 'user_input' must either match a specified value or be NULL, allowing for flexibility in user responses.
            // This query is useful for determining the flow of a conversation based on the current state and user input.


            stmt.setString(1, currentConversationType); // Set the conversation type
            stmt.setString(2, currentState); // Set the current state
            stmt.setString(3, message); // Set the user input

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nextState = rs.getString("next_state"); // Get the next state
                    String responseText = rs.getString("response_text"); // Get the response text

                    // Handle specific states for appointment scheduling
                    if (currentState.equals("ENTER_DATE_TIME")) {
                        if (isValidDateTime(message)) {
                            appointmentDateTime = message; // Store the valid appointment date/time
                            currentState = "CONFIRM"; // Update state to confirm
                            return "Your appointment is set for " + message + ". Is this correct? (yes/no)";
                        } else {
                            return "Invalid date/time format. Please use YYYY-MM-DD HH:mm format."; // Return error for invalid format
                        }
                    }

                    // Handle appointment type selection
                    if (currentState.equals("CHOOSE_TYPE")) {
                        if (message.equals("1")) {
                            appointmentType = "Bank branch visit"; // Set appointment type
                        } else if (message.equals("2")) {
                            appointmentType = "Phone consultation"; // Set appointment type
                        } else if (message.equals("3")) {
                            appointmentType = "Virtual meeting"; // Set appointment type
                        }
                    }

                    // Handle confirmation of appointment
                    if (currentState.equals("CONFIRM")) {
                        if (message.equals("yes")) {
                            message = appointmentDateTime; // Prepare message for saving appointment
                            saveAppointment(appointmentType, message); // Save the appointment
                        }
                    }

                    // Update current state
                    currentState = nextState;

                    // Check for conversation end conditions
                    if (nextState.equals("EXIT")) {
                        needHelp(); // Ask if the user needs more assistance
                    }

                    return responseText; // Return the response text
                } else {
                    // If no valid state is found, check for valid inputs
                    if (!validInputs .contains(message)) {
                        return "I'm sorry, I didn't understand that. Please respond with 'yes' or 'no'."; // Return error for invalid input
                    } else if (currentState.equals("EXIT")) {
                        // Call needHelp() to ask if the user needs more assistance
                        return needHelp();
                    }
                }
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions that occur during user input processing
            System.err.println("Error processing conversation: " + e.getMessage());
            e.printStackTrace();
        }

        return getErrorResponse(); // Return error response if no valid state is found
    }

    // Method to validate the date and time format
    private boolean isValidDateTime(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setLenient(false); // Set lenient to false to strictly parse the date
        try {
            sdf.parse(dateTime); // Attempt to parse the date
            return true; // Return true if parsing is successful
        } catch (ParseException e) {
            return false; // Return false if parsing fails
        }
    }

    // Method to save the appointment details to the database
    private void saveAppointment(String appointmentType, String appointmentDateTime) {
        int userId = UserID_Database.getCurrentUserId(); // Get the current user ID
        String insertSQL = "INSERT INTO Appointments (user_id, appointment_type, appointment_datetime) VALUES (?, ?, ?)";

        // This query inserts a new record into the 'Appointments' table.
        // It adds values for 'user_id', 'appointment_type', and 'appointment_datetime'.
        // The placeholders (?, ?, ?) will be replaced with actual values when the query is executed,
        // allowing for parameterized queries to prevent SQL injection.

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {

            pstmt.setInt(1, userId); // Set user ID in the query
            pstmt.setString(2, appointmentType); // Set appointment type in the query
            pstmt.setString(3, appointmentDateTime); // Set appointment date/time in the query

            pstmt.executeUpdate(); // Execute the insert operation
        } catch (SQLException e) {
            // Handle any SQL exceptions that occur during appointment saving
            System.err.println("Error saving appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to handle help responses from the user
    private String handleHelpResponse(String message) {
        if (message.equals("yes")) {
            isWaitingForHelpResponse = false; // Reset the help response flag
            currentConversationType = null; // Clear the current conversation type
            currentState = "INITIAL"; // Reset the state to initial
            return "What else can I help you with?\n" + displayMainMenu(); // Prompt for further assistance
        } else if (message.equals("no")) {
            isWaitingForHelpResponse = false; // Reset the help response flag
            return "Thank you very much! If you need help with anything else, feel free to ask. Have a great day!"; // Farewell message
        } else {
            return "Please type 'yes' or 'no'."; // Prompt for valid input
        }
    }

    // Method to get the welcome message for the user
    private String getWelcomeMessage() {
        return "Hello! Welcome to DBO Bank. How can I assist you today?"; // Welcome message
    }

    // Method to display the main menu of options available to the user
    private static String displayMainMenu() {
        StringBuilder menuBuilder = new StringBuilder("\nAvailable options:\n");

        // Hardcoded default questions
        String[] defaultQuestions = {
                "How do I open a new account?",
                "How do I transfer money to another account?",
                "Appointment scheduling",
                "How do I block my card if it's lost or stolen?",
                "How do I apply for a personal loan?",
                "How do I set up two-factor authentication?",
                "How can I maximize my credit card rewards?",
                "How do I report fraud on my account?",
                "How can I check my account balance?",
                "How do I close or terminate my account?",
                "How can I request a credit limit increase?",
                "What do I have to know about foreign exchange?",
                "How can I view or download my account statement?",
                "How do I activate my card?"
        };

        // Add default questions to the menu
        for (String question : defaultQuestions) {
            menuBuilder.append("- ").append(question).append("\n");
        }

        // Fetch admin questions from the database
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT question_text FROM AdminQuestions WHERE is_active = TRUE");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String question = rs.getString("question_text").trim(); // Get the question text
                menuBuilder.append("- ").append(question).append("\n"); // Add admin questions to the menu
            }

        } catch (SQLException e) {
            // Handle any SQL exceptions that occur during fetching admin questions
            System.err.println("Error fetching admin questions: " + e.getMessage());
        }

        // Add exit option to the menu
        menuBuilder.append("Type 'exit' to end the conversation");

        return menuBuilder.toString(); // Return the constructed menu
    }

    // Method to prompt the user if they need help
    public String needHelp() {
        isWaitingForHelpResponse = true; // Set the flag to indicate waiting for help response
        return "Are you sure there is something I can help you with? (yes/no)"; // Prompt for help
    }

    // Method to return an error response when the input is not understood
    public String getErrorResponse() {
        return "I'm sorry, I didn't understand. Could you please choose from one of the available options?\n"; // Error message
    }
}