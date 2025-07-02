package tn.test.Controllers.User.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.test.entities.User;
import tn.test.services.UserService;

public class EditUserController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox activeCheckbox;

    @FXML private Label ouvrierLabel;
    @FXML private Label disponibilityLabel;
    @FXML private DatePicker disponibilityField;
    @FXML private Label locationLabel;
    @FXML private TextField locationField;
    @FXML private Label experienceLabel;
    @FXML private TextField experienceField;

    private User userToEdit;
    private final UserService userService = new UserService();

    public void setUserToEdit(User user) {
        this.userToEdit = user;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        // Basic validation (similar to AddUserController)
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter first name", Alert.AlertType.ERROR);
            return false;
        }
        // Add other validation checks as needed...

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}