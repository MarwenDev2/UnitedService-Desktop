package tn.test.Controllers.front;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
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
    @FXML private Label profileNameLabel, activePageLabel, roleLabel, notificationBadge;
    @FXML private StackPane contentPane;
    @FXML private Button accueilButton, demandeCongeButton, historiqueButton;
    @FXML private MenuButton profileMenu;
    @FXML private Button notificationBtn;

    private VBox notificationPopup;
    private Button currentActiveButton;
    private final NotificationService notificationService = new NotificationService();
    private final WorkerService serviceWorker = new WorkerService();
    private Worker currentWorker = SessionManager.getInstance().getCurrentWorker();
    private Timeline notificationRefreshTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize UI components
        logoImage.setImage(loadImage("Images/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        profileNameLabel.setText(currentWorker.getName());
        roleLabel.setText(currentWorker.getPosition());
        setupClip(menuAvatar);

        // Setup notification system
        setupNotificationButton();
        updateNotificationBadge();
        setupNotificationRefresh();

        // Load initial page
        loadInitialPage();
    }

    private void setupNotificationButton() {
        // Add tooltip
        Tooltip tooltip = new Tooltip("Notifications");
        tooltip.setStyle("-fx-font-size: 12px;");
        notificationBtn.setTooltip(tooltip);

        // Set action for notification button
        notificationBtn.setOnAction(e -> toggleNotifications());

        // Update badge when mouse enters (optional)
        notificationBtn.setOnMouseEntered(e -> updateNotificationBadge());
    }

    private void updateNotificationBadge() {
        int unreadCount = notificationService.countUnreadNotifications(currentWorker.getId());
        if (unreadCount > 0) {
            notificationBadge.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
            notificationBadge.setVisible(true);

            // Add pulse animation when there are new notifications
            ScaleTransition pulse = new ScaleTransition(Duration.millis(300), notificationBadge);
            pulse.setFromX(1);
            pulse.setToX(1.3);
            pulse.setFromY(1);
            pulse.setToY(1.3);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            pulse.play();
        } else {
            notificationBadge.setVisible(false);
        }
    }

    private void setupNotificationRefresh() {
        // Refresh notifications every 30 seconds
        notificationRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> updateNotificationBadge())
        );
        notificationRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationRefreshTimeline.play();
    }

    private void toggleNotifications() {
        // Hide if already showing
        if (notificationPopup != null && contentPane.getChildren().contains(notificationPopup)) {
            contentPane.getChildren().remove(notificationPopup);
            return;
        }

        // Get notifications from service
        List<Notification> notifications = notificationService.findByUserId(currentWorker.getId());

        // Create notification popup
        notificationPopup = new VBox();
        notificationPopup.getStyleClass().add("notification-popup");
        notificationPopup.setSpacing(10);
        notificationPopup.setPadding(new Insets(10));
        notificationPopup.setMaxWidth(320);
        notificationPopup.setLayoutX(contentPane.getWidth() - 350);
        notificationPopup.setLayoutY(60);

        // Add title
        Label title = new Label("Notifications");
        title.getStyleClass().add("notification-title");
        notificationPopup.getChildren().add(title);

        // Add notifications or empty message
        if (notifications.isEmpty()) {
            Label empty = new Label("Aucune notification");
            empty.getStyleClass().add("notification-empty");
            notificationPopup.getChildren().add(empty);
        } else {
            for (Notification n : notifications) {
                HBox card = createNotificationCard(n);
                notificationPopup.getChildren().add(card);
            }
        }

        // Close when mouse exits
        notificationPopup.setOnMouseExited(e -> contentPane.getChildren().remove(notificationPopup));
        contentPane.getChildren().add(notificationPopup);
    }

    private HBox createNotificationCard(Notification notification) {
        HBox card = new HBox();
        card.setSpacing(10);
        card.getStyleClass().add("notification-card");

        // Highlight unread notifications
        if (!notification.isRead()) {
            card.setStyle("-fx-background-color: #E3F2FD;");
        } else {
            card.setStyle("-fx-background-color: #F5F5F5;");
        }

        // Notification message
        Label message = new Label(notification.getMessage());
        message.getStyleClass().add("notification-message");
        message.setWrapText(true);
        message.setMaxWidth(230);

        // Mark as read button
        Button markRead = new Button("✔");
        markRead.getStyleClass().add("notification-mark-read");
        markRead.setOnAction(e -> handleMarkAsRead(notification, card));

        card.getChildren().addAll(message, markRead);
        return card;
    }

    private void handleMarkAsRead(Notification notification, HBox card) {
        if (!notification.isRead()) {
            // Mark as read in database
            notificationService.markAsRead(notification.getId());
            notification.setRead(true);

            // Update UI
            card.setStyle("-fx-background-color: #F5F5F5;");

            // Add visual feedback
            FadeTransition fade = new FadeTransition(Duration.millis(300), card);
            fade.setFromValue(1.0);
            fade.setToValue(0.7);
            fade.setCycleCount(2);
            fade.setAutoReverse(true);
            fade.play();

            // Update badge count
            updateNotificationBadge();
        }
    }

    // ------------------- NAVIGATION METHODS -------------------
    public void loadView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
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

    private void loadInitialPage() {
        activePageLabel.setText("Page d'utilisateur");
        loadView("/views/Worker/WorkerHome.fxml");
        setActiveButton(accueilButton);
    }

    @FXML
    public void goToAccueil() {
        setActiveButton(accueilButton);
        activePageLabel.setText("Tableau de bord");
        loadView("/views/Worker/WorkerHome.fxml");
    }

    @FXML
    public void goToDemandeConge() {
        setActiveButton(demandeCongeButton);
        activePageLabel.setText("Demander un congé");
        loadView("/views/Worker/PostulerConge.fxml");
    }

    @FXML
    public void goToHistorique() {
        setActiveButton(historiqueButton);
        activePageLabel.setText("Historique des congés");
        loadView("/views/Worker/HistoriqueConge.fxml");
    }

    @FXML
    private void openProfile() {
        loadView("/views/User/Profile/Profile.fxml");
    }

    @FXML
    private void logout() {
        // Stop the notification refresh timeline
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml"));
            Scene scene = new Scene(root, 1300, 800);
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