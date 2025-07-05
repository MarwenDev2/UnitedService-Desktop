package tn.test.Controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.test.entities.User;
import tn.test.tools.SessionManager;

public class MainController {

    @FXML private BorderPane rootLayout;
    @FXML private VBox sidebar;
    @FXML private AnchorPane mainContent;
    @FXML private ImageView profileImage;
    @FXML private Label navTitle;
    @FXML private MenuButton profileMenu;
    @FXML private Button btnDashboard, btnUsers, btnBlog, btnLogout, toggleSidebarBtn;
    @FXML private VBox usersSubmenu;
    @FXML private Button btnListWorkers, btnAddWorker;

    private User currentUser = SessionManager.getInstance().getCurrentUser();
    private boolean sidebarVisible = true;
    private boolean usersSubmenuVisible = false;
    private Button activeButton = null;

    public void initialize() {
        loadInitialPage();
        loadProfileImage();
        sidebar.setTranslateX(0);
        profileMenu.setText("👤 "+currentUser.getName());

        // Set arrow icon for users button
        ImageView arrowIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/arrow-right.png")));
        arrowIcon.setFitHeight(12);
        arrowIcon.setFitWidth(12);
        btnUsers.setGraphic(arrowIcon);
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
    private void goToProfile() {
        setNavTitle(" Profile");
        loadPage("/views/User/Profile/Profile.fxml");
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