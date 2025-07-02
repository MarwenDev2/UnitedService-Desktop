package tn.test.Controllers;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.test.entities.User;
import tn.test.tools.SessionManager;

import java.io.IOException;

public class HomeController {
    @FXML private HBox adminControls;
    @FXML private BorderPane borderPane;
    @FXML private Button ajouter; // Action Buttons
    @FXML private Label userRoleLabel;

    // 📚 Initialize Method (Runs when the controller is loaded)
    public void initialize() {

        User currentUser = SessionManager.getInstance().getCurrentUser();
        userRoleLabel.setText(currentUser.getRole().toString());

        adminControls.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole().toString()),
                        userRoleLabel.textProperty()
                )
        );
    }

    @FXML
    private void handleProfile() { loadScene("/views/User/Profile/Profile.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateCurrentSession();
        loadScene("/views/User/Authentication/Login.fxml");
    }

    @FXML
    private void loadPosts() { loadScene("/views/Worker/ShowWorkers.fxml"); }


    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root,950,800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ➕ Navigate to Add Publication Page
    @FXML
    private void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Worker/AddPost.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ajouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Publication");
            stage.show();
        } catch (IOException e) {
            showError("Error", "Failed to open the add publication page.");
        }
    }

    // ❌ Helper Method: Display Error Alert
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Helper Method: Display Success Alert
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleOffres() {
        try {
            System.out.println("🔍 Trying to load: /fxml/main_layout.fxml");
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main_layout.fxml"));
            Stage stage = (Stage) borderPane.getScene().getWindow(); // this borderPane is from current scene
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("✅ main_layout.fxml loaded successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to load /fxml/main_layout.fxml");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page des Offres.");
        }
    }


}
