package tn.test.Controllers.Worker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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
    private Worker currentWorker = SessionManager.getInstance().getCurrentWorker();


    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(TypeConge.values());
        errorLabel.setText("");
        submitButton.setOnAction(e -> handleSubmit());
    }

    private void handleSubmit() {
        TypeConge selectedType = typeComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String reason = reasonField.getText().trim();

        if (selectedType == null || start == null || end == null || reason.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            errorLabel.setTextFill(Color.web("#ff6f61"));
            return;
        }

        if (end.isBefore(start)) {
            errorLabel.setText("La date de fin doit être postérieure à la date de début.");
            errorLabel.setTextFill(Color.web("#ff6f61"));
            return;
        }

        DemandeConge demande = new DemandeConge();
        demande.setWorker(currentWorker);
        demande.setType(selectedType);
        demande.setStartDate(start);
        demande.setEndDate(end);
        demande.setReason(reason);
        demande.setStatus(Status.EN_ATTENTE_SECRETAIRE);

        boolean success = demandeService.add(demande);
        if (success) {
            errorLabel.setText("Demande envoyée avec succès !");
            errorLabel.setTextFill(Color.web("#c6a34f"));
            clearFields();
        } else {
            errorLabel.setText("Une erreur est survenue. Veuillez réessayer.");
            errorLabel.setTextFill(Color.web("#ff6f61"));
        }
    }

    private void clearFields() {

    }

    public void cancel(ActionEvent actionEvent) {
    }
}
