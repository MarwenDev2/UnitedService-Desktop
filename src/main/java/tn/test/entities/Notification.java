package tn.test.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // ✅ Required for LocalDateTime
import java.util.ArrayList;
import java.util.List;

public class Notification {

    private int id;
    private Worker recipient;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;

    public Notification() {
        // Default constructor
    }

    public Notification(int id, Worker recipient, String message, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.recipient = recipient;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Worker getRecipient() {
        return recipient;
    }

    public void setRecipient(Worker recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
