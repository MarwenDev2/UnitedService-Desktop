package tn.test.Controllers.Worker;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import tn.test.entities.DemandeConge;
import tn.test.entities.Status;
import tn.test.entities.TypeConge;
import tn.test.entities.Worker;
import tn.test.services.DemandeCongeService;
import tn.test.tools.SessionManager;

import java.time.LocalDate;

public class PostulerCongeController {

    @FXML
    private ComboBox<TypeConge> typeComboBox;
    @FXML
    private VBox mainContainer;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea reasonField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button submitButton;
    private final DemandeCongeService demandeService = new DemandeCongeService();
    private final Worker currentWorker = SessionManager.getInstance().getCurrentWorker();

    @FXML
    public void initialize() {
        // Populate ComboBox with enum values
        typeComboBox.getItems().addAll(TypeConge.values());

        // Clear error message at start
        errorLabel.setText("");

        // Page fade-in animation
        FadeTransition ft = new FadeTransition(Duration.millis(400), mainContainer);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        // Submit button logic
        submitButton.setOnAction(e -> handleSubmit());
    }

    private void handleSubmit() {
        TypeConge selectedType = typeComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String reason = reasonField.getText().trim();

        // 🧩 Step 1: Basic validation
        if (selectedType == null || start == null || end == null || reason.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (end.isBefore(start)) {
            showError("La date de fin doit être postérieure à la date de début.");
            return;
        }

        if (start.isBefore(LocalDate.now())) {
            showError("La date de début ne peut pas être dans le passé.");
            return;
        }

        // 🚫 NEW: Check if the user already has a pending request
        boolean hasPendingRequest = demandeService.hasPendingRequest(currentWorker.getId());
        if (hasPendingRequest) {
            showError("Vous avez déjà une demande de congé en attente.");
            return;
        }

        // 🧮 Step 2: Calculate days requested
        int requestedDays = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        int remainingDays = currentWorker.getTotalCongeDays() - currentWorker.getUsedCongeDays();

        // 🚫 Step 3: Check balance
        if (requestedDays > remainingDays) {
            showError("Vous avez seulement " + remainingDays + " jour(s) de congé restant(s).");
            return;
        }

        // ✅ Step 4: Create the demande
        DemandeConge demande = new DemandeConge();
        demande.setWorker(currentWorker);
        demande.setType(selectedType);
        demande.setStartDate(start);
        demande.setEndDate(end);
        demande.setReason(reason);
        demande.setStatus(Status.EN_ATTENTE_SECRETAIRE);

        boolean success = demandeService.add(demande);

        if (success) {
            showSuccess("✅ Demande envoyée avec succès !");
            clearFields();
        } else {
            showError("❌ Une erreur est survenue. Veuillez réessayer.");
        }
    }



    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#ff6f61")); // red-pink
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#C6A34F")); // gold
    }

    private void clearFields() {
        typeComboBox.getSelectionModel().clearSelection();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        reasonField.clear();
    }

    public void cancel(ActionEvent actionEvent) {
        clearFields();
        errorLabel.setText("");
    }
}
