package tn.test.Controllers.User.Profile;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.test.entities.User;
import tn.test.services.UserService;
import tn.test.tools.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;

public class UpdateProfileController {

    @FXML
    private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;
    @FXML private VBox ouvrierFields;
    @FXML private DatePicker availabilityField;
    @FXML private TextField locationField;
    @FXML private TextField experienceField;

    private UserService userService = new UserService();
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    public void initData(User user) {
        this.currentUser = user;




    }
    @FXML
    private void handleCancel() {
        ((Stage) firstNameField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
