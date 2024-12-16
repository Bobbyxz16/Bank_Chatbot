module com.example.bank_chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.bank_chatbot to javafx.fxml; // Open the main package
    opens com.example.bank_chatbot.UserExperience to javafx.fxml; // Open the UserExperience package for FXML
    exports com.example.bank_chatbot; // Export the main package
}