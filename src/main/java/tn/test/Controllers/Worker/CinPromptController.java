package tn.test.Controllers.Worker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.test.Controllers.Shared;
import tn.test.entities.Worker;
import tn.test.services.WorkerService;
import tn.test.tools.SessionManager;

public class CinPromptController {
    @FXML private TextField cinField;
    @FXML private Label errorLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private final WorkerService workerService = new WorkerService();

    @FXML
    public void initialize() {
        confirmButton.setOnAction(e -> validateCin());
        cancelButton.setOnAction(
                e -> ((Stage) cancelButton.getScene().getWindow()).close());
    }

    private void validateCin() {
        String cin = cinField.getText().trim();

        if (cin.isEmpty()) {
            showError("Veuillez entrer un CIN");
            return;
        }

        Worker worker = workerService.findByCin(cin);
        if (worker != null) {
            Shared.setWorker(worker);
            ((Stage) confirmButton.getScene().getWindow()).close();
        } else {
            showError("CIN invalide ou employé introuvable");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
