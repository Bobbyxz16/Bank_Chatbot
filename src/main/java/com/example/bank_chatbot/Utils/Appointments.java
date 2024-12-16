package com.example.bank_chatbot.Utils; // Package declaration for utility classes

import com.example.bank_chatbot.Database.DatabaseConnection; // Importing DatabaseConnection for database operations
import com.example.bank_chatbot.Database.UserID_Database; // Importing UserID_Database for user ID management
import javafx.scene.control.*; // Importing JavaFX controls
import javafx.scene.layout.GridPane; // Importing GridPane for layout

import java.sql.*; // Importing SQL classes for database operations
import java.time.LocalDate; // Importing LocalDate for date handling
import java.time.LocalDateTime; // Importing LocalDateTime for date and time handling
import java.time.LocalTime; // Importing LocalTime for time handling

public class Appointments { // Declaration of the Appointments class

    private final ListView<GetAppointmentID> appointmentsListView; // ListView to hold appointments
    private AlertMessages alert; // Instance of AlertMessages for showing alerts

    // Constructor to initialize the appointments list view and load user appointments
    public Appointments(ListView<GetAppointmentID> appointmentsListView) {
        this.appointmentsListView = appointmentsListView; // Assign the provided ListView
        this.alert = new AlertMessages(); // Initialize AlertMessages
        loadUserAppointments(); // Load appointments when the object is created
    }

    // Method to load user appointments from the database
    public void loadUserAppointments() {
        String query = "SELECT id, appointment_type, appointment_datetime FROM Appointments WHERE user_id = ?"; // SQL query to get appointments
        try (Connection connection = DatabaseConnection.getConnection(); // Establish database connection
             PreparedStatement statement = connection.prepareStatement(query)) { // Prepare the SQL statement

            statement.setInt(1, UserID_Database.getCurrentUserId()); // Set the user ID in the query
            ResultSet resultSet = statement.executeQuery(); // Execute the query

            appointmentsListView.getItems().clear(); // Clear existing items in the ListView
            while (resultSet.next()) { // Iterate through the result set
                int id = resultSet.getInt("id"); // Get appointment ID
                String type = resultSet.getString("appointment_type"); // Get appointment type
                LocalDateTime dateTime = resultSet.getTimestamp("appointment_datetime").toLocalDateTime(); // Get appointment date and time
                appointmentsListView.getItems().add(new GetAppointmentID(id, type, dateTime)); // Add appointment to the ListView
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
            alert.showAlert("Error loading appointments: " + e.getMessage()); // Show error alert
        }
    }

    // Method to delete an appointment by its ID
    public void deleteAppointment(int appointmentId) {
        String query = "DELETE FROM Appointments WHERE id = ?"; // SQL query to delete an appointment
        try (Connection connection = DatabaseConnection.getConnection(); // Establish database connection
             PreparedStatement statement = connection.prepareStatement(query)) { // Prepare the SQL statement

            statement.setInt(1, appointmentId); // Set the appointment ID in the query
            int rowsAffected = statement.executeUpdate(); // Execute the update
            if (rowsAffected > 0) {
                alert.showAlertConfirmation("Appointment deleted successfully."); // Show confirmation alert
                loadUserAppointments(); // Reload appointments
            } else {
                alert.showAlert("Failed to delete appointment."); // Show error alert if deletion failed
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
            alert.showAlert("Error deleting appointment: " + e.getMessage()); // Show error alert
        }
    }

    // Method to show a dialog for changing the appointment date and time
    public void showCalendarDialog(GetAppointmentID selectedAppointment) {
        Dialog<ButtonType> dialog = new Dialog<>(); // Create a new dialog
        dialog.setTitle("Change Appointment Date and Time"); // Set the dialog title

        // Create a DatePicker for selecting the date
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now()); // Set default date to today

        // Create ComboBox for selecting hours
        ComboBox<Integer> hourComboBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourComboBox.getItems().add(i); // Add hours to the ComboBox
        }
        hourComboBox.setValue(LocalTime.now().getHour()); // Set default hour to current hour

        // Create ComboBox for selecting minutes
        ComboBox<Integer> minuteComboBox = new ComboBox<>();
        for (int i = 0; i < 60; i++) {
            minuteComboBox.getItems().add(i); // Add minutes to the ComboBox
        }
        minuteComboBox.setValue(LocalTime.now().getMinute()); // Set default minute to current minute

        // Create a layout for the dialog
        GridPane grid = new GridPane();
        grid.add(new Label("Date:"), 0, 0); // Add label for date
        grid.add(datePicker, 1, 0); // Add date picker to the grid
        grid.add(new Label("Hour:"), 0, 1); // Add label for hour
        grid.add(hourComboBox, 1, 1); // Add hour combo box to the grid
        grid.add(new Label("Minute:"), 0, 2); // Add label for minute
        grid.add(minuteComboBox, 1, 2); // Add minute combo box to the grid

        dialog.getDialogPane().setContent(grid); // Set the content of the dialog

        // Create a button for confirmation
        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL); // Add confirm and cancel buttons

        // Show the dialog and wait for the result
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == confirmButton) { // If confirm button is pressed
                LocalDate newDate = datePicker.getValue(); // Get the selected date
                int newHour = hourComboBox.getValue(); // Get the selected hour
                int newMinute = minuteComboBox.getValue(); // Get the selected minute
                LocalTime newTime = LocalTime.of(newHour, newMinute); // Create a LocalTime object
                updateAppointmentDateAndTime(selectedAppointment.getId(), newDate, newTime); // Update the appointment with new date and time
            }
        });
    }

    // Method to update the appointment date and time in the database
    private void updateAppointmentDateAndTime(int appointmentId, LocalDate newDate, LocalTime newTime) {
        String query = "UPDATE Appointments SET appointment_datetime = ? WHERE id = ?"; // SQL query to update appointment
        try (Connection connection = DatabaseConnection.getConnection(); // Establish database connection
             PreparedStatement statement = connection.prepareStatement(query)) { // Prepare the SQL statement

            // Combine the new date and new time into a LocalDateTime
            LocalDateTime newDateTime = LocalDateTime.of(newDate, newTime); // Create a LocalDateTime object

            // Set the new date-time and the appointment ID
            statement.setTimestamp(1, Timestamp.valueOf(newDateTime)); // Set the new date-time
            statement.setInt(2, appointmentId); // Set the appointment ID
            int rowsAffected = statement.executeUpdate(); // Execute the update
            if (rowsAffected > 0) {
                alert.showAlertConfirmation("Appointment date and time updated successfully."); // Show confirmation alert
                loadUserAppointments(); // Reload appointments
            } else {
                alert.showAlert("Failed to update appointment date and time."); // Show error alert if update failed
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
            alert.showAlert("Error updating appointment date and time: " + e.getMessage()); // Show error alert
        }
    }
}