package tn.test.Controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.test.entities.Notification;
import tn.test.entities.Role;
import tn.test.entities.User;
import tn.test.services.NotificationService;
import tn.test.tools.SessionManager;

public class MainController {

    @FXML private Button demandeCongeButton;
    @FXML private VBox usersSubmenuVBOX;
    @FXML private BorderPane rootLayout;
    @FXML private VBox sidebar;
    @FXML private AnchorPane mainContent;
    @FXML private ImageView profileImage;
    @FXML private Label navTitle, notificationBadge;
    @FXML private MenuButton profileMenu;
    @FXML private Button btnDashboard, btnUsers, btnBlog, toggleSidebarBtn;
    @FXML private VBox usersSubmenu;
    @FXML private Button btnListWorkers, btnAddWorker;
    @FXML private Button notificationBtn;
    private VBox notificationPopup;
    private final User currentUser = SessionManager.getInstance().getCurrentUser();
    private boolean sidebarVisible = true;
    private boolean usersSubmenuVisible = false;
    private Button activeButton = null;
    private final NotificationService notificationService = new NotificationService();
    private Timeline notificationRefreshTimeline;
    public void initialize() {
        loadInitialPage();
        loadProfileImage();
        sidebar.setTranslateX(0);
        profileMenu.setText("👤 "+currentUser.getName());
        if(currentUser.getRole().equals(Role.SECRETAIRE)){
            usersSubmenuVBOX.setVisible(false);
            demandeCongeButton.setVisible(true);
        } else if (currentUser.getRole().equals(Role.RH)) {
            btnAddWorker.setVisible(true);
        }
        setupNotificationButton();
        updateNotificationBadge();
        setupNotificationRefresh();

        // Set arrow icon for users button
        ImageView arrowIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/arrow-right.png")));
        arrowIcon.setFitHeight(12);
        arrowIcon.setFitWidth(12);
        btnUsers.setGraphic(arrowIcon);
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
        int unreadCount = notificationService.countUnreadForDashboard();

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
        if (notificationPopup != null && mainContent.getChildren().contains(notificationPopup)) {
            mainContent.getChildren().remove(notificationPopup);
            return;
        }

        // Get notifications from service
        List<Notification> notifications = notificationService.findAllForDashboard();


        // Create notification popup
        notificationPopup = new VBox();
        notificationPopup.getStyleClass().add("notification-popup");
        notificationPopup.setSpacing(10);
        notificationPopup.setPadding(new Insets(10));
        notificationPopup.setMaxWidth(320);
        notificationPopup.setLayoutX(mainContent.getWidth() - 350);
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
        notificationPopup.setOnMouseExited(e -> mainContent.getChildren().remove(notificationPopup));
        mainContent.getChildren().add(notificationPopup);
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

    @FXML
    private void toggleUsersSubmenu() {
        usersSubmenuVisible = !usersSubmenuVisible;
        usersSubmenu.setManaged(usersSubmenuVisible);
        usersSubmenu.setVisible(usersSubmenuVisible);

        // Rotate arrow icon
        RotateTransition rotate = new RotateTransition(Duration.millis(200), btnUsers.getGraphic());
        rotate.setByAngle(usersSubmenuVisible ? 90 : -90);
        rotate.play();

        // Animate submenu
        if (usersSubmenuVisible) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), usersSubmenu);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }

    @FXML
    private void goToListWorkers() {
        setNavTitle("👥 Liste des Employés");
        loadPage("/views/User/Admin/WorkersList.fxml");
        setActive(btnListWorkers);
    }

    @FXML
    private void goToAddWorker() {
        setNavTitle("➕ Ajouter Employé");
        loadPage("/views/User/Admin/AddWorker.fxml");
        setActive(btnAddWorker);
    }

    private void loadInitialPage() {
        setNavTitle("🏠 Dashboard");
        loadPage("/views/Worker/WorkerDashboard.fxml");
        setActive(btnDashboard);
    }
    @FXML
    private void goToDashboard() {
        setNavTitle("🏠 Dashboard");
        loadPage("/views/Worker/WorkerDashboard.fxml");
        setActive(btnDashboard);
    }

    @FXML
    private void goToDemandes() {
        setNavTitle("📋 Demandes Congés");
        loadPage("/views/User/Admin/AdminConge.fxml");
        setActive(btnBlog);
    }

    @FXML
    private void goToDemandeConge() {
        setNavTitle("📋 Postuler demande un congé");
        loadPage("/views/Worker/PostulerConge.fxml");
        setActive(demandeCongeButton);
    }

    private void loadProfileImage() {
        Image img = new Image(getClass().getResource("/images/icons8_james_bond_32px.png").toExternalForm());
        profileImage.setImage(img);
    }

    private void loadPage(String path) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(path));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), mainContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                mainContent.getChildren().setAll(newPage);
                AnchorPane.setTopAnchor(newPage, 0.0);
                AnchorPane.setBottomAnchor(newPage, 0.0);
                AnchorPane.setLeftAnchor(newPage, 0.0);
                AnchorPane.setRightAnchor(newPage, 0.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void toggleSidebar() {
        double targetWidth = sidebarVisible ? 0 : 250;
        double targetOpacity = sidebarVisible ? 0 : 1;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebar.prefWidthProperty(), sidebar.getWidth()),
                        new KeyValue(sidebar.opacityProperty(), sidebar.getOpacity())
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebar.opacityProperty(), targetOpacity)
                )
        );

        if (sidebarVisible) {
            timeline.setOnFinished(e -> rootLayout.setLeft(null));
        } else {
            rootLayout.setLeft(sidebar);
        }

        timeline.play();
        sidebarVisible = !sidebarVisible;
    }

    private void setActive(Button newActive) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        activeButton = newActive;
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void setNavTitle(String title) {
        navTitle.setText(title);
    }


    @FXML
    private void goToLogout() {
        try {
            Parent landingRoot = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml"));
            Scene landingScene = new Scene(landingRoot);
            landingScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(landingScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}