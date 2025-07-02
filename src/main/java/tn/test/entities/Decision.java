package tn.test.entities;

import java.time.LocalDateTime;

public class Decision {
    private int id;
    private User decisionBy;
    private boolean approved;
    private String comment;
    private LocalDateTime date;

    public Decision() {
        // Default constructor
    }

    public Decision(int id, User decisionBy, boolean approved, String comment, LocalDateTime date) {
        this.id = id;
        this.decisionBy = decisionBy;
        this.approved = approved;
        this.comment = comment;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getDecisionBy() {
        return decisionBy;
    }

    public void setDecisionBy(User decisionBy) {
        this.decisionBy = decisionBy;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}