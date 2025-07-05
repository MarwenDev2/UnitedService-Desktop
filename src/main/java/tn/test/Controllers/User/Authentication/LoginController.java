package tn.test.Controllers.User.Authentication;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.test.entities.User;
import tn.test.entities.Worker;
import tn.test.services.UserService;
import tn.test.services.WorkerService;
import tn.test.tools.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML private StackPane centerPane;
    @FXML private VBox loginContainer;
    @FXML private VBox workerContainer;
    @FXML private Hyperlink workerLink;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField cinField;
    @FXML private Button loginButton;
    @FXML private Button cinLoginButton;
    @FXML private Label errorLabel;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label cinError;
    @FXML private Hyperlink userBackLink;

    private final UserService userService = new UserService();
    private final WorkerService workerService = new WorkerService();
    private boolean isFlipping = false;

    @FXML
    public void initialize() {
        // Initialize UI state
        errorLabel.setVisible(false);
        cinError.setVisible(false);
        loginContainer.setOpacity(0);
        loginContainer.setTranslateY(30);
        workerContainer.setVisible(false);
        workerContainer.setManaged(false);

        // Set up animations
        setupInitialAnimation();

        // Set up event handlers
        loginButton.setOnAction(event -> handleLogin());
        cinLoginButton.setOnAction(event -> handleCinLogin());
        workerLink.setOnAction(event -> flipToWorker());
        userBackLink.setOnAction(event -> flipToUser());

        // Set up 3D perspective for flip animation
        centerPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                PerspectiveCamera camera = new PerspectiveCamera();
                camera.setFieldOfView(60);
                newScene.setCamera(camera);
            }
        });
    }

    private void setupInitialAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(700), loginContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(700), loginContainer);
        translate.setFromY(30);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }

    private void handleLogin() {
        emailError.setVisible(false);
        passwordError.setVisible(false);
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

        User user = userService.login(email, password);
        if (user != null) {
            try {
                proceedWithLoginUser(user);
            } catch (IOException e) {
                showError("Erreur lors du chargement de la page suivante.");
                e.printStackTrace();
            }
        } else {
            showError("Email ou mot de passe incorrect.");
        }
    }

    private void handleCinLogin() {
        cinError.setVisible(false);
        String cin = cinField.getText().trim();

        if (cin.isEmpty()) {
            cinError.setText("Veuillez entrer votre CIN");
            cinError.setVisible(true);
            return;
        }

        Worker worker = workerService.findByCin(cin);
        if (worker != null) {
            try {
                SessionManager.getInstance().createSession(worker);
                proceedWithLoginWorker(worker);
            } catch (IOException e) {
                showError("Erreur lors du chargement de la page.");
                e.printStackTrace();
            }
        } else {
            cinError.setText("CIN non reconnu");
            cinError.setVisible(true);
        }
    }

    private void flipToWorker() {
        if (isFlipping) return;
        isFlipping = true;

        RotateTransition rotateOut = new RotateTransition(Duration.millis(500), centerPane);
        rotateOut.setAxis(Rotate.Y_AXIS);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setInterpolator(Interpolator.EASE_OUT);

        rotateOut.setOnFinished(event -> {
            loginContainer.setVisible(false);
            loginContainer.setManaged(false);
            workerContainer.setVisible(true);
            workerContainer.setManaged(true);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(500), centerPane);
            rotateIn.setAxis(Rotate.Y_AXIS);
            rotateIn.setFromAngle(-90);
            rotateIn.setToAngle(0);
            rotateIn.setInterpolator(Interpolator.EASE_IN);
            rotateIn.setOnFinished(e -> isFlipping = false);
            rotateIn.play();
        });

        rotateOut.play();
    }

    private void flipToUser() {
        if (isFlipping) return;
        isFlipping = true;

        RotateTransition rotateOut = new RotateTransition(Duration.millis(500), centerPane);
        rotateOut.setAxis(Rotate.Y_AXIS);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(-90);
        rotateOut.setInterpolator(Interpolator.EASE_OUT);

        rotateOut.setOnFinished(event -> {
            workerContainer.setVisible(false);
            workerContainer.setManaged(false);
            loginContainer.setVisible(true);
            loginContainer.setManaged(true);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(500), centerPane);
            rotateIn.setAxis(Rotate.Y_AXIS);
            rotateIn.setFromAngle(90);
            rotateIn.setToAngle(0);
            rotateIn.setInterpolator(Interpolator.EASE_IN);
            rotateIn.setOnFinished(e -> isFlipping = false);
            rotateIn.play();
        });

        rotateOut.play();
    }

    private void proceedWithLoginUser(User user) throws IOException {
        SessionManager.getInstance().createSession(user);
        loadMainView("/fxml/main_layout.fxml");
    }

    private void proceedWithLoginWorker(Worker worker) throws IOException {
        SessionManager.getInstance().createSession(worker);
        loadMainView("/front/frontview.fxml");
    }

    private void loadMainView(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().addAll(
                getClass().getResource("/styles/main.css").toExternalForm(),
                getClass().getResource("/styles/dashboard.css").toExternalForm(),
                getClass().getResource("/styles/popup.css").toExternalForm(),
                getClass().getResource("/styles/sidebar.css").toExternalForm()
        );

        Stage stage = (Stage) centerPane.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Shake animation for error
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}