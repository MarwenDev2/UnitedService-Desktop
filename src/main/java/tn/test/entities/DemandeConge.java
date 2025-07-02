package tn.test.entities;

import java.time.LocalDate;

public class DemandeConge {
    private int id;
    private Worker worker;
    private LocalDate startDate;
    private LocalDate endDate;
    private TypeConge type;
    private String reason;
    private Status status;
    private Decision secretaireDecision;
    private Decision rhDecision;
    private Decision adminDecision;

    public DemandeConge() {
        // Default constructor
    }

    public DemandeConge(int id, Worker worker, LocalDate startDate, LocalDate endDate, TypeConge type, String reason, Status status, Decision secretaireDecision, Decision rhDecision, Decision adminDecision) {
        this.id = id;
        this.worker = worker;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.reason = reason;
        this.status = status;
        this.secretaireDecision = secretaireDecision;
        this.rhDecision = rhDecision;
        this.adminDecision = adminDecision;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public TypeConge getType() {
        return type;
    }

    public void setType(TypeConge type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Decision getSecretaireDecision() {
        return secretaireDecision;
    }

    public void setSecretaireDecision(Decision secretaireDecision) {
        this.secretaireDecision = secretaireDecision;
    }

    public Decision getRhDecision() {
        return rhDecision;
    }

    public void setRhDecision(Decision rhDecision) {
        this.rhDecision = rhDecision;
    }

    public Decision getAdminDecision() {
        return adminDecision;
    }

    public void setAdminDecision(Decision adminDecision) {
        this.adminDecision = adminDecision;
    }
}