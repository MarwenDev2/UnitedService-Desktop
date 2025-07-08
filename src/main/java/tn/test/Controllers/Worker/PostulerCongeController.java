package tn.test.Controllers.Worker;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Duration;
import tn.test.Controllers.Shared;
import tn.test.entities.DemandeConge;
import tn.test.entities.Status;
import tn.test.entities.TypeConge;
import tn.test.entities.Worker;
import tn.test.services.DemandeCongeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private String attachmentPath;
    @FXML private Label attachmentLabel, WorkerLabel;
    @FXML private Button uploadButton;
    @FXML
    private Button submitButton;
    private final DemandeCongeService demandeService = new DemandeCongeService();
    private Worker currentWorker;

    @FXML
    public void initialize() {
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(this::loadWorkerAndDisplayLabel);
            }
        });

        typeComboBox.getItems().addAll(TypeConge.values());

        errorLabel.setText("");
        attachmentLabel.setText("Aucun fichier sélectionné");
        attachmentLabel.setVisible(false);

        FadeTransition ft = new FadeTransition(Duration.millis(400), mainContainer);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        submitButton.setOnAction(e -> handleSubmit());
    }

    private void loadWorkerAndDisplayLabel() {
        currentWorker = Shared.getWorker();

        if (currentWorker == null) {
            promptForCin();  // on demande une seule fois

            // Si après prompt, l'utilisateur n'a rien mis, on n'insiste pas
            currentWorker = Shared.getWorker();
            if (currentWorker == null) {
                WorkerLabel.setText("Aucun employé identifié.");
                WorkerLabel.setVisible(true);
                submitButton.setDisable(true);  // désactive le bouton d’envoi
                return;
            }
        }

        // ✅ Si CIN est bien saisi
        String civilite = currentWorker.getGender().equalsIgnoreCase("HOMME") ? "Mr" : "Mme";
        WorkerLabel.setText("Demande de Congé pour " + civilite + " " + currentWorker.getName());
        WorkerLabel.setVisible(true);
        submitButton.setDisable(false);
    }



    private void promptForCin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Worker/cin_prompt.fxml"));
            VBox content = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(content);
            dialog.setTitle("Identification de l'employé");
            dialog.initOwner(mainContainer.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Joindre un document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.jpg", "*.jpeg", "*.png")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create target directory if it doesn't exist
                Path targetDir = Paths.get("src/main/resources/Attachments");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                // Generate filename using timestamp
                String fileName = "attachment_" + System.currentTimeMillis() + getFileExtension(selectedFile.getName());

                // Copy file to target directory
                Path targetPath = targetDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Store the path
                attachmentPath = fileName;
                attachmentLabel.setText(selectedFile.getName());
                attachmentLabel.setVisible(true);

                // Play animation
                playFileUploadAnimation();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur lors du téléchargement du fichier");
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private void playFileUploadAnimation() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), uploadButton);
        scaleIn.setFromX(1);
        scaleIn.setFromY(1);
        scaleIn.setToX(1.1);
        scaleIn.setToY(1.1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), uploadButton);
        scaleOut.setFromX(1.1);
        scaleOut.setFromY(1.1);
        scaleOut.setToX(1);
        scaleOut.setToY(1);

        SequentialTransition sequence = new SequentialTransition(scaleIn, scaleOut);
        sequence.play();
    }

    private void handleSubmit() {
        currentWorker = Shared.getWorker();
        String civilite = currentWorker.getGender().equalsIgnoreCase("HOMME") ? "Mr" : "Mme";
        WorkerLabel.setText("Demande de Congé pour " + civilite + " " + currentWorker.getName());
        WorkerLabel.setVisible(true);
        if (currentWorker == null) {
            showError("Veuillez vous identifier d'abord.");
            return;
        }
        TypeConge selectedType = typeComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String reason = reasonField.getText().trim();

        // 🧩 Step 1: Basic validation
        if (selectedType == null || start == null || end == null || reason.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if ((selectedType == TypeConge.MALADIE || selectedType == TypeConge.MATERNITE) &&
                (attachmentPath == null || attachmentPath.isEmpty())) {
            showError("Un justificatif est requis pour ce type de congé.");
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
        demande.setDateDemande(LocalDate.now());
        demande.setStatus(Status.EN_ATTENTE_RH);
        demande.setAttachmentPath(attachmentPath);

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
        attachmentPath = null;
        attachmentLabel.setVisible(false);
    }

    public void cancel(ActionEvent actionEvent) {
        Shared.setWorker(null);
        loadWorkerAndDisplayLabel();
    }

}
