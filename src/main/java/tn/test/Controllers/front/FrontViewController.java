package tn.test.Controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.test.entities.Notification;
import tn.test.entities.Worker;
import tn.test.services.NotificationService;
import tn.test.services.WorkerService;
import tn.test.tools.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FrontViewController implements Initializable {

    @FXML private ImageView logoImage, menuAvatar;
    @FXML private Label profileNameLabel, activePageLabel, roleLabel;
    @FXML private StackPane contentPane;
    @FXML private Button accueilButton, btnFavoris, demandeCongeButton, historiqueButton;

    @FXML private MenuButton profileMenu;
    @FXML private Button notificationBtn;
    private final NotificationService notificationService = new NotificationService();
    private VBox notificationPopup;
    private Button currentActiveButton;
    private final WorkerService serviceWorker = new WorkerService();
    private Worker currentWorker = SessionManager.getInstance().getCurrentWorker();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoImage.setImage(loadImage("Images/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        profileNameLabel.setText(currentWorker.getName());
        roleLabel.setText(currentWorker.getPosition());
        setupClip(menuAvatar);
        loadInitialPage();
        notificationBtn.setOnAction(e -> toggleNotifications());
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
    private void toggleNotifications() {
        if (notificationPopup != null && contentPane.getChildren().contains(notificationPopup)) {
            contentPane.getChildren().remove(notificationPopup);
            return;
        }

        List<Notification> notifications = notificationService.findByUserId(currentWorker.getId());
        notificationPopup = new VBox();
        notificationPopup.getStyleClass().add("notification-popup");
        notificationPopup.setSpacing(10);
        notificationPopup.setPadding(new Insets(10));
        notificationPopup.setMaxWidth(320);
        notificationPopup.setLayoutX(contentPane.getWidth() - 350); // Adjust for RTL placement
        notificationPopup.setLayoutY(60);

        Label title = new Label("Notifications");
        title.getStyleClass().add("notification-title");
        notificationPopup.getChildren().add(title);

        if (notifications.isEmpty()) {
            Label empty = new Label("Aucune notification");
            empty.getStyleClass().add("notification-empty");
            notificationPopup.getChildren().add(empty);
        } else {
            for (Notification n : notifications) {
                HBox card = new HBox();
                card.setSpacing(10);
                card.getStyleClass().add("notification-card");
                if (!n.isRead()) card.setStyle("-fx-background-color: #E3F2FD;");
                else card.setStyle("-fx-background-color: #F5F5F5;");

                Label message = new Label(n.getMessage());
                message.getStyleClass().add("notification-message");
                message.setWrapText(true);
                message.setMaxWidth(230);

                Button markRead = new Button("✔");
                markRead.getStyleClass().add("notification-mark-read");
                markRead.setOnAction(e -> {
                    if (!n.isRead()) {
                        notificationService.markAsRead(n.getId());
                        n.setRead(true);

                        // Update style locally
                        card.setStyle("-fx-background-color: #F5F5F5;");
                    }
                });


                card.getChildren().addAll(message, markRead);
                notificationPopup.getChildren().add(card);
            }
        }

        notificationPopup.setOnMouseExited(e -> contentPane.getChildren().remove(notificationPopup));
        contentPane.getChildren().add(notificationPopup);
    }



    // ------------------- NAVIGATION -------------------

    private void loadInitialPage() {
        activePageLabel.setText("Page d'utilisateur");
        loadView("/views/Worker/WorkerHome.fxml");
        setActiveButton(accueilButton);
    }

    @FXML public void goToFavoris() {
        setActiveButton(btnFavoris);
        activePageLabel.setText("Favoris ❤️");
        loadView("/front/produit/FavorisView.fxml");
    }

    @FXML public void goToAccueil() {
        setActiveButton(accueilButton);
        activePageLabel.setText("Tableau de bord");
        loadView("/views/Worker/WorkerHome.fxml");
    }

    @FXML public void goToDemandeConge() {
        setActiveButton(demandeCongeButton);
        activePageLabel.setText("Demander un congé");
        loadView("/views/Worker/PostulerConge.fxml");
    }

    @FXML public void goToHistorique() {
        setActiveButton(historiqueButton);
        activePageLabel.setText("Historique des congés");
        loadView("/views/Worker/HistoriqueConge.fxml");
    }



    @FXML private void openProfile() {
        loadView("/views/User/Profile/Profile.fxml");
    }

    @FXML private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml"));
            Scene scene = new Scene(root,1300, 800);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
