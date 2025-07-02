package tn.test.Controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.test.entities.User;
import tn.test.services.UserService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontViewController implements Initializable {

    @FXML private ImageView logoImage, menuAvatar;
    @FXML private Label profileNameLabel, activePageLabel, roleLabel;
    @FXML private StackPane contentPane;
    @FXML private Button accueilButton,blogButton, btnFavoris;

    @FXML private MenuButton profileMenu;

    private Button currentActiveButton;
    private final UserService serviceUser = new UserService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoImage.setImage(loadImage("photos/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        setupClip(menuAvatar);
    }

    public void setCurrentUser(User user) {
        this.currentUser = serviceUser.findById(user.getId());
        if (this.currentUser == null) this.currentUser = user;
        if (this.currentUser.getRole() == null) this.currentUser.setRole(user.getRole());

        profileNameLabel.setText(currentUser.getName());
        roleLabel.setText("Connecté en tant que: " + currentUser.getRole());
    }

    private void loadView(String fxmlPath) {
        try {
            String fullPath = fxmlPath.startsWith("/") ? fxmlPath : "/front/" + fxmlPath;
            URL resource = getClass().getResource(fullPath);
            if (resource == null) {
                System.err.println("❌ Fichier FXML non trouvé : " + fullPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button newActiveButton) {
        if (newActiveButton != null) {
            if (currentActiveButton != null)
                currentActiveButton.getStyleClass().remove("active-button");
            if (!newActiveButton.getStyleClass().contains("active-button"))
                newActiveButton.getStyleClass().add("active-button");
            currentActiveButton = newActiveButton;
        }
    }

    private void setupClip(ImageView imageView) {
        Circle clip = new Circle();
        clip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
        imageView.setClip(clip);
    }

    private Image loadImage(String path) {
        File file = new File(path);
        return file.exists() ? new Image(file.toURI().toString()) : null;
    }



    // ------------------- NAVIGATION -------------------

    @FXML private void goToAccueil() {
        setActiveButton(accueilButton);
        activePageLabel.setText("Accueil");
        loadView("AccueilView.fxml");
    }

    @FXML public void goToFavoris() {
        setActiveButton(btnFavoris);
        activePageLabel.setText("Favoris ❤️");
        loadView("/front/produit/FavorisView.fxml");
    }

    @FXML private void goToBlog() {
        setActiveButton(blogButton);
        activePageLabel.setText("Blog");
        loadView1("/Views/Blog/ShowWorkers.fxml");
    }

    private void loadView1(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ Fichier FXML non trouvé : " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void openProfile() {
        loadView1("/views/User/Profile/Profile.fxml");
    }

    @FXML private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
