package tn.test.Controllers.User.Admin;


import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import tn.test.entities.Worker;
import tn.test.services.WorkerService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class AddWorkerController {

    @FXML private VBox mainContainer;
    @FXML private TextField nameField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private TextField positionField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField salaryField;
    @FXML private ToggleGroup genderGroup;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextArea addressField;
    @FXML private Spinner<Integer> totalDaysSpinner;
    @FXML private Label errorLabel;
    @FXML private ImageView profileImageView;
    @FXML private Label imagePathLabel;

    private String imagePath = "";
    private final WorkerService workerService = new WorkerService();

    @FXML
    public void initialize() {
        System.out.println("[INIT] Initializing controller...");

        // Debug ToggleGroup status
        if (genderGroup == null) {
            System.out.println("[INIT] Gender ToggleGroup is NULL!");
        } else {
            System.out.println("[INIT] Gender ToggleGroup initialized");
        }

        // Setup department choices
        departmentCombo.getItems().addAll(
                "IT", "RH", "Finance", "Marketing",
                "Maintenance", "Production", "Logistique"
        );

        // Set default values
        birthDatePicker.setValue(LocalDate.now().minusYears(25));
        totalDaysSpinner.getValueFactory().setValue(30);


        // Setup animations
        setupAnimations();
    }

    private void setupAnimations() {
        FadeTransition fade = new FadeTransition(Duration.millis(500), mainContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), mainContainer);
        slide.setFromY(20);
        slide.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.play();
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create target directory if it doesn't exist
                Path targetDir = Paths.get("src/main/resources/Images/Users");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                // Generate filename using timestamp
                String fileName = "profile_" + System.currentTimeMillis() + getFileExtension(selectedFile.getName());

                // Copy file to target directory
                Path targetPath = targetDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Update UI - store only the filename
                imagePath = fileName; // Changed from full path to just filename
                profileImageView.setImage(new Image(targetPath.toUri().toString()));
                imagePathLabel.setText(fileName);

                // Play animation
                playImageUploadAnimation();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur lors du téléchargement de l'image");
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private void playImageUploadAnimation() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), profileImageView);
        scaleIn.setFromX(1);
        scaleIn.setFromY(1);
        scaleIn.setToX(1.1);
        scaleIn.setToY(1.1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), profileImageView);
        scaleOut.setFromX(1.1);
        scaleOut.setFromY(1.1);
        scaleOut.setToX(1);
        scaleOut.setToY(1);

        SequentialTransition sequence = new SequentialTransition(scaleIn, scaleOut);
        sequence.play();
    }

    @FXML
    private void handleAddWorker() {
        System.out.println("[DEBUG] Starting handleAddWorker...");

        // Validate inputs
        System.out.println("[DEBUG] Validating inputs...");
        if (!validateInputs()) {
            System.out.println("[DEBUG] Validation failed!");
            return;
        }
        System.out.println("[DEBUG] Inputs validated successfully");

        try {
            System.out.println("[DEBUG] Creating worker object...");
            Worker worker = createWorkerFromForm();
            System.out.println("[DEBUG] Worker object created: " + worker);

            // Check if CIN already exists
            System.out.println("[DEBUG] Checking for duplicate CIN...");
            Worker existingWorker = workerService.findByCin(worker.getCin());
            if (existingWorker != null) {
                System.out.println("[DEBUG] Duplicate CIN found!");
                showError("Un employé avec ce CIN existe déjà");
                return;
            }
            System.out.println("[DEBUG] No duplicate CIN found");

            System.out.println("[DEBUG] Attempting to add worker to database...");
            boolean success = workerService.add(worker);
            System.out.println("[DEBUG] WorkerService.add returned: " + success);

            if (success) {
                System.out.println("[DEBUG] Worker added successfully!");
                showSuccess("Employé ajouté avec succès!");
                clearForm();
            } else {
                System.out.println("[DEBUG] Failed to add worker!");
                showError("Erreur lors de l'ajout de l'employé");
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Exception occurred:");
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
    private Worker createWorkerFromForm() {
        Worker worker = new Worker();
        worker.setName(nameField.getText().trim());
        worker.setCin(cinField.getText().trim());
        worker.setDepartment(departmentCombo.getValue());
        worker.setPosition(positionField.getText().trim());
        worker.setPhone(phoneField.getText().trim());
        worker.setEmail(emailField.getText().trim());
        worker.setSalary(Float.parseFloat(salaryField.getText().trim()));
        worker.setGender(genderGroup.getSelectedToggle().getUserData().toString());
        worker.setDateOfBirth(birthDatePicker.getValue());
        worker.setAddress(addressField.getText().trim());
        worker.setCreationDate(LocalDate.now());
        worker.setStatus("actif");
        worker.setTotalCongeDays(totalDaysSpinner.getValue());
        worker.setUsedCongeDays(0);
        worker.setProfileImagePath(imagePath);
        return worker;
    }
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder("Veuillez remplir les champs obligatoires:\n");
        boolean isValid = true;

        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("- Nom complet\n");
            nameField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            nameField.setStyle("");
        }

        // CIN validation
        if (cinField.getText().trim().isEmpty()) {
            errorMessage.append("- CIN\n");
            cinField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else if (cinField.getText().trim().length() < 8) {
            errorMessage.append("- CIN doit contenir au moins 8 caractères\n");
            cinField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            cinField.setStyle("");
        }

        // Department validation
        if (departmentCombo.getValue() == null) {
            errorMessage.append("- Département\n");
            departmentCombo.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            departmentCombo.setStyle("");
        }

        // Position validation
        if (positionField.getText().trim().isEmpty()) {
            errorMessage.append("- Poste\n");
            positionField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            positionField.setStyle("");
        }

        // Phone validation
        if (phoneField.getText().trim().isEmpty()) {
            errorMessage.append("- Téléphone\n");
            phoneField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else if (!phoneField.getText().trim().matches("^[0-9]{8,15}$")) {
            errorMessage.append("- Téléphone invalide (8-15 chiffres)\n");
            phoneField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            phoneField.setStyle("");
        }

        // Email validation
        if (emailField.getText().trim().isEmpty()) {
            errorMessage.append("- Email\n");
            emailField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else if (!emailField.getText().trim().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorMessage.append("- Email invalide\n");
            emailField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            emailField.setStyle("");
        }

        // Salary validation
        if (salaryField.getText().trim().isEmpty()) {
            errorMessage.append("- Salaire\n");
            salaryField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            try {
                Float.parseFloat(salaryField.getText().trim());
                salaryField.setStyle("");
            } catch (NumberFormatException e) {
                errorMessage.append("- Salaire doit être un nombre\n");
                salaryField.setStyle("-fx-border-color: #e74c3c");
                isValid = false;
            }
        }

        // Gender validation
        if (genderGroup.getSelectedToggle() == null) {
            System.out.println("[VALIDATION] No gender selected!");  // Debug line
            errorMessage.append("- Genre\n");
            isValid = false;
        } else {
            System.out.println("[VALIDATION] Selected gender: " +
                    genderGroup.getSelectedToggle().getUserData());  // Debug line
        }

        // Birth date validation
        if (birthDatePicker.getValue() == null) {
            errorMessage.append("- Date de naissance\n");
            birthDatePicker.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else if (birthDatePicker.getValue().isAfter(LocalDate.now().minusYears(14))) {
            errorMessage.append("- L'employé doit avoir au moins 14 ans\n");
            birthDatePicker.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            birthDatePicker.setStyle("");
        }

        // Address validation
        if (addressField.getText().trim().isEmpty()) {
            errorMessage.append("- Adresse\n");
            addressField.setStyle("-fx-border-color: #e74c3c");
            isValid = false;
        } else {
            addressField.setStyle("");
        }

        if (!isValid) {
            showError(errorMessage.toString());
            pulseEmptyFields();
        }

        return isValid;
    }

    private void pulseEmptyFields() {
        if (nameField.getText().trim().isEmpty()) {
            animateField(nameField);
        }
        if (cinField.getText().trim().isEmpty() || cinField.getText().trim().length() < 8) {
            animateField(cinField);
        }
        if (departmentCombo.getValue() == null) {
            animateField(departmentCombo);
        }
        if (positionField.getText().trim().isEmpty()) {
            animateField(positionField);
        }
        if (phoneField.getText().trim().isEmpty() || !phoneField.getText().trim().matches("^[0-9]{8,15}$")) {
            animateField(phoneField);
        }

        if (emailField.getText().trim().isEmpty() || !emailField.getText().trim().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            animateField(emailField);
        }
        if (salaryField.getText().trim().isEmpty()) {
            animateField(salaryField);
        }
        if (birthDatePicker.getValue() == null || birthDatePicker.getValue().isAfter(LocalDate.now().minusYears(14))) {
            animateField(birthDatePicker);
        }
        if (addressField.getText().trim().isEmpty()) {
            animateField(addressField);
        }
    }

    private void animateField(Control field) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), field);
        pulse.setFromX(1);
        pulse.setToX(1.02);
        pulse.setFromY(1);
        pulse.setToY(1.02);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c");

        // Shake animation for error
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #2ecc71");

        // Pulse animation for success
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), errorLabel);
        pulse.setFromX(1);
        pulse.setToX(1.05);
        pulse.setFromY(1);
        pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void clearForm() {
        nameField.clear();
        cinField.clear();
        departmentCombo.getSelectionModel().clearSelection();
        positionField.clear();
        phoneField.clear();
        emailField.clear();
        salaryField.clear();
        genderGroup.selectToggle(null);
        birthDatePicker.setValue(LocalDate.now().minusYears(25));
        addressField.clear();
        totalDaysSpinner.getValueFactory().setValue(30);
    }

    @FXML
    private void handleCancel() {
        // Get the current stage and close it
        mainContainer.getScene().getWindow().hide();
    }
}