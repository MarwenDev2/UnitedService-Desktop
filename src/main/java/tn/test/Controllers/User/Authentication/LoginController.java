package tn.test.Controllers.User.Authentication;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import tn.test.entities.User;
import tn.test.services.UserService;
import tn.test.tools.EmailService;
import tn.test.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private VBox loginCard;
    @FXML private Hyperlink signup;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    private final UserService userService = new UserService();
    private EmailService emailService;
    private static final String REDIRECT_URI = "http://localhost:8000/login/google/callback";

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Animations
        setupAnimations();

        // Login button action
        loginButton.setOnAction(event -> handleLogin());

        // Signup link action
        signup.setOnMouseClicked(event -> loadScene("/views/User/Authentication/SignUp.fxml"));
    }
    private void setupAnimations() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), loginCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), loginCard);
        slide.setFromY(30);
        slide.setToY(0);

        fade.play();
        slide.play();
    }
    private void clearErrors() {
        emailError.setVisible(false);
        passwordError.setVisible(false);
    }

    private void handleLogin() {
        clearErrors();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            emailError.setText("Veuillez entrer votre email");
            emailError.setVisible(true);
            return;
        }

        if (password.isEmpty()) {
            passwordError.setText("Veuillez entrer votre mot de passe");
            passwordError.setVisible(true);
            return;
        }
    }
    private void proceedWithLogin(User user) throws IOException {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.createSession(user);

        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole().toString())) {
            loadScene("/fxml/main_layout.fxml");
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/frontview.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/main.css").toExternalForm(),
                    getClass().getResource("/styles/dashboard.css").toExternalForm(),
                    getClass().getResource("/styles/offre.css").toExternalForm(),
                    getClass().getResource("/styles/popup.css").toExternalForm(),
                    getClass().getResource("/styles/sidebar.css").toExternalForm(),
                    getClass().getResource("/styles/job-cards.css").toExternalForm(),
                    getClass().getResource("/styles/offre_card.css").toExternalForm(),
                    getClass().getResource("/styles/candidatures.css").toExternalForm(),
                    getClass().getResource("/styles/add_offre_dialog.css").toExternalForm(),
                    getClass().getResource("/styles/edit_offre_dialog.css").toExternalForm()
            );

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorLabel);
            shake.setFromX(0);
            shake.setByX(10);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 950, 800));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }
}