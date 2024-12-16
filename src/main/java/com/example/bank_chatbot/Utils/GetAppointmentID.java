package com.example.bank_chatbot.Utils;

import java.time.LocalDateTime; // Importing LocalDateTime for date and time handling

public class GetAppointmentID { // Declaration of the GetAppointmentID class
    private int id; // Appointment ID
    private String type; // Appointment type
    private LocalDateTime dateTime; // Appointment date and time

    // Constructor to initialize the appointment details
    public GetAppointmentID(int id, String type, LocalDateTime dateTime) {
        this.id = id; // Assign the appointment ID
        this.type = type; // Assign the appointment type
        this.dateTime = dateTime; // Assign the appointment date and time
    }

    // Getter method for the appointment ID
    public int getId() {
        return id; // Return the appointment ID
    }

    // Override toString method to customize the string representation of the appointment
    @Override
    public String toString() {
        return type + " - " + dateTime.toString(); // Return a formatted string with type and date-time
    }
}