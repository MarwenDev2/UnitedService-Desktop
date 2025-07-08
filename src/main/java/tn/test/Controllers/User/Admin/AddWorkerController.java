package tn.test.Controllers.User.Admin;


import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import tn.test.Controllers.Shared;
import tn.test.entities.Worker;
import tn.test.services.WorkerService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class AddWorkerController {

    @FXML private Button cancelButton;
    @FXML private Label title;
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
    private Worker workerToEdit = null;
    private boolean isEditMode = Shared.isEditMode();
    private String imagePath = "";
    private final WorkerService workerService = new WorkerService();
    @FXML private Button actionButton;
    private boolean formModified = false;

    @FXML
    public void initialize() {
        setupFormListeners();

        // Update button text based on mode
        actionButton.textProperty().bind(
                Bindings.when(Bindings.createBooleanBinding(() -> isEditMode))
                        .then("Modifier")
                        .otherwise("Ajouter")
        );

        // Disable button in edit mode until changes are made
        if (isEditMode) {
            actionButton.setDisable(true);
            title.setText("Modifier Employé");
            cancelButton.setVisible(true);
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

    private void setupFormListeners() {
        // Add listeners to all fields
        nameField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        cinField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        departmentCombo.valueProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        positionField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        salaryField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        genderGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        birthDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        addressField.textProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
        totalDaysSpinner.valueProperty().addListener((obs, oldVal, newVal) -> checkFormModified());
    }

    private void checkFormModified() {
        if (isEditMode) {
            Worker currentFormData = createWorkerFromForm();
            formModified = !currentFormData.equals(workerToEdit);
            actionButton.setDisable(!formModified);
        }
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
    public void setWorkerForEdit(Worker worker) {
        this.workerToEdit = worker;
        populateForm(worker);
    }

    private void populateForm(Worker worker) {
        nameField.setText(worker.getName());
        cinField.setText(worker.getCin());
        departmentCombo.setValue(worker.getDepartment());
        positionField.setText(worker.getPosition());
        phoneField.setText(worker.getPhone());
        emailField.setText(worker.getEmail());
        salaryField.setText(String.valueOf(worker.getSalary()));

        // Set gender radio button
        if (worker.getGender().equalsIgnoreCase("Homme")) {
            genderGroup.selectToggle(genderGroup.getToggles().get(0));
        } else {
            genderGroup.selectToggle(genderGroup.getToggles().get(1));
        }

        birthDatePicker.setValue(worker.getDateOfBirth());
        addressField.setText(worker.getAddress());
        totalDaysSpinner.getValueFactory().setValue(worker.getTotalCongeDays());

        // Load profile image if exists
        if (worker.getProfileImagePath() != null && !worker.getProfileImagePath().isEmpty()) {
            try {
                String imagePath = "/Images/Users/" + worker.getProfileImagePath();
                InputStream inputStream = getClass().getResourceAsStream(imagePath);
                if (inputStream != null) {
                    profileImageView.setImage(new Image(inputStream));
                    imagePathLabel.setText(worker.getProfileImagePath());
                    this.imagePath = worker.getProfileImagePath();
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddWorker() {
        if (!validateInputs()) {
            return;
        }
        try {
            Worker worker = createWorkerFromForm();

            if (isEditMode) {
                worker.setId(workerToEdit.getId());
                worker.setUsedCongeDays(workerToEdit.getUsedCongeDays());
                workerService.update(worker);
                showSuccess("Employé modifié avec succès!");
            } else {
                if (workerService.findByCin(worker.getCin()) != null) {
                    showError("Un employé avec ce CIN existe déjà");
                    return;
                }
                boolean success = workerService.add(worker);

                if (success) {
                    showSuccess("Employé ajouté avec succès!");
                    clearForm();
                } else {
                    showError("Erreur lors de l'ajout de l'employé");
                }
            }

            clearForm();
        } catch (Exception e) {
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
        String salaryText = salaryField.getText().trim();
        worker.setSalary(salaryText.isEmpty() ? 0f : Float.parseFloat(salaryText));
        if (genderGroup.getSelectedToggle() != null) {
            worker.setGender(genderGroup.getSelectedToggle().getUserData().toString());
        } else {
            worker.setGender(""); // ou null selon ta logique
        }
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