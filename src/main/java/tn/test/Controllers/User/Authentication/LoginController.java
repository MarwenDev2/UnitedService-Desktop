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
    @FXML private Hyperlink worker;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField cinField;
    @FXML private Button loginButton;
    @FXML private Button cinLoginButton;
    @FXML private Label errorLabel;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label cinError;
    @FXML private Hyperlink userBack;


    private final UserService userService = new UserService();
    private WorkerService workerService = new WorkerService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        cinError.setVisible(false);
        loginContainer.setOpacity(0);
        loginContainer.setTranslateY(30);
        workerContainer.setVisible(false);
        workerContainer.setManaged(false);
        setupAnimations();

        loginButton.setOnAction(event -> handleLogin());
        cinLoginButton.setOnAction(event -> handleCinLogin());
        worker.setOnMouseClicked(event -> flipToWorker());
        userBack.setOnMouseClicked(event -> flipToUser());

    }

    private void setupAnimations() {
        FadeTransition ft = new FadeTransition(Duration.seconds(0.7), loginContainer);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.7), loginContainer);
        tt.setFromY(30);
        tt.setToY(0);

        new ParallelTransition(ft, tt).play();
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
        if (user == null) {
            showError("Email ou mot de passe incorrect.");
            return;
        }
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

        if (worker == null) {
            cinError.setText("CIN non reconnu");
            cinError.setVisible(true);
            return;
        }

        if (worker != null) {
            try {
                SessionManager.getInstance().createSession(worker);
                proceedWithLoginWorker(worker);
            } catch (IOException e) {
                showError("Erreur lors du chargement de la page.");
                e.printStackTrace();
            }
        } else {
            showError("CIN incorrect ou non reconnu.");
        }
    }


    private void flipToWorker() {
        PerspectiveCamera camera = new PerspectiveCamera();
        centerPane.getScene().setCamera(camera);

        RotateTransition rotateOut = new RotateTransition(Duration.millis(600), centerPane);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setAxis(Rotate.Y_AXIS);

        rotateOut.setOnFinished(event -> {
            loginContainer.setVisible(false);
            loginContainer.setManaged(false);
            workerContainer.setVisible(true);
            workerContainer.setManaged(true);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(600), centerPane);
            rotateIn.setFromAngle(-90);
            rotateIn.setToAngle(0);
            rotateIn.setAxis(Rotate.Y_AXIS);
            rotateIn.play();
        });

        rotateOut.play();
    }

    private void flipToUser() {
        PerspectiveCamera camera = new PerspectiveCamera();
        centerPane.getScene().setCamera(camera);

        RotateTransition rotateOut = new RotateTransition(Duration.millis(600), centerPane);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(-90);
        rotateOut.setAxis(Rotate.Y_AXIS);

        rotateOut.setOnFinished(event -> {
            workerContainer.setVisible(false);
            workerContainer.setManaged(false);
            loginContainer.setVisible(true);
            loginContainer.setManaged(true);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(600), centerPane);
            rotateIn.setFromAngle(90);
            rotateIn.setToAngle(0);
            rotateIn.setAxis(Rotate.Y_AXIS);
            rotateIn.play();
        });

        rotateOut.play();
    }


    private void proceedWithLoginUser(User user) throws IOException {
        SessionManager.getInstance().createSession(user);
        String fxmlPath = "/fxml/main_layout.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().addAll(
                getClass().getResource("/styles/main.css").toExternalForm(),
                getClass().getResource("/styles/dashboard.css").toExternalForm(),
                getClass().getResource("/styles/popup.css").toExternalForm(),
                getClass().getResource("/styles/sidebar.css").toExternalForm()
        );

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void proceedWithLoginWorker(Worker worker) throws IOException {
        SessionManager.getInstance().createSession(worker);
        String fxmlPath = "/front/frontview.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(
                getClass().getResource("/styles/main.css").toExternalForm(),
                getClass().getResource("/styles/dashboard.css").toExternalForm(),
                getClass().getResource("/styles/popup.css").toExternalForm(),
                getClass().getResource("/styles/sidebar.css").toExternalForm()
        );

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}