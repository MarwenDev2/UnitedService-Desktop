package tn.test.Controllers.Worker;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.test.entities.DemandeConge;
import tn.test.entities.Status;
import tn.test.entities.User;
import tn.test.services.DemandeCongeService;
import tn.test.services.UserService;
import tn.test.tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WorkerDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label pendingCount;
    @FXML private Label acceptedCount;
    @FXML private Label refusedCount;
    @FXML private Label userCount;
    @FXML private VBox notificationContainer;

    private final DemandeCongeService demandeService = new DemandeCongeService();
    private final UserService userService = new UserService();
    private final User currentUser = SessionManager.getInstance().getCurrentUser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize labels with 0 to prevent NumberFormatException
        pendingCount.setText("0");
        acceptedCount.setText("0");
        refusedCount.setText("0");
        userCount.setText("0");

        // Welcome message with animation
        welcomeLabel.setText("Bienvenue, " + currentUser.getName() + " (ADMIN)");

        // Load statistics with animations
        loadStatistics();

        // Load notifications with animations
        loadRecentNotifications();
    }

    private void loadStatistics() {
        // Animate the counting up of numbers
        animateCount(pendingCount,
                demandeService.countByStatus(Status.EN_ATTENTE_SECRETAIRE)
                        + demandeService.countByStatus(Status.EN_ATTENTE_RH)
                        + demandeService.countByStatus(Status.EN_ATTENTE_ADMIN));

        animateCount(acceptedCount,
                demandeService.countByStatus(Status.ACCEPTE));

        animateCount(refusedCount,
                demandeService.countByStatus(Status.REFUSE_SECRETAIRE)
                        + demandeService.countByStatus(Status.REFUSE_RH)
                        + demandeService.countByStatus(Status.REFUSE_ADMIN));

        animateCount(userCount, userService.findAll().size());
    }

    private void animateCount(Label label, int targetValue) {
        final int DURATION = 1500; // milliseconds
        final int STEPS = 30;
        final int step = Math.max(1, targetValue / STEPS); // Ensure step is at least 1

        // Initialize with 0 if empty
        if (label.getText().isEmpty()) {
            label.setText("0");
        }

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(DURATION/STEPS), e -> {
            try {
                int current = Integer.parseInt(label.getText());
                if (current < targetValue) {
                    int newValue = Math.min(current + step, targetValue); // Don't exceed target
                    label.setText(String.valueOf(newValue));
                }
            } catch (NumberFormatException ex) {
                label.setText("0"); // Reset to 0 if parsing fails
            }
        }));
        timeline.setCycleCount(STEPS);
        timeline.setOnFinished(e -> label.setText(String.valueOf(targetValue)));
        timeline.play();
    }

    private void loadRecentNotifications() {
        notificationContainer.getChildren().clear();

        List<DemandeConge> recentDemandes = demandeService.findRecentCongeForDashboard(3);

        ParallelTransition parallelTransition = new ParallelTransition();

        for (int i = 0; i < recentDemandes.size(); i++) {
            DemandeConge demande = recentDemandes.get(i);
            VBox notificationItem = createNotificationItem(demande);
            notificationContainer.getChildren().add(notificationItem);

            // Create animation for this item
            FadeTransition fade = new FadeTransition(Duration.millis(500), notificationItem);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(400), notificationItem);
            slide.setFromY(20);
            slide.setToY(0);

            // Stagger animations
            fade.setDelay(Duration.millis(i * 100));
            slide.setDelay(Duration.millis(i * 100));

            parallelTransition.getChildren().addAll(fade, slide);
        }

        parallelTransition.play();
    }

    private VBox createNotificationItem(DemandeConge demande) {
        VBox item = new VBox(8);
        item.getStyleClass().add("notification-item");

        String workerName = demande.getWorker().getName();
        String type = demande.getType().name().toLowerCase();
        String message = buildNotificationMessage(demande, workerName, type);

        Label dateLabel = new Label(demande.getDateDemande() != null ?
                demande.getDateDemande().toString() : "Date inconnue");
        dateLabel.getStyleClass().add("notification-date");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("notification-message");

        // Add status-specific styling
        switch(demande.getStatus()) {
            case EN_ATTENTE_SECRETAIRE, EN_ATTENTE_RH, EN_ATTENTE_ADMIN ->
                    item.getStyleClass().add("notification-pending");
            case ACCEPTE ->
                    item.getStyleClass().add("notification-approved");
            case REFUSE_SECRETAIRE, REFUSE_RH, REFUSE_ADMIN ->
                    item.getStyleClass().add("notification-rejected");
        }

        item.getChildren().addAll(dateLabel, messageLabel);
        return item;
    }

    private String buildNotificationMessage(DemandeConge demande, String workerName, String type) {
        return switch (demande.getStatus()) {
            case EN_ATTENTE_SECRETAIRE -> workerName + " a déposé une nouvelle demande de congé (" + type + ")";
            case EN_ATTENTE_RH -> "Le Secrétaire a approuvé la demande de congé de " + workerName;
            case EN_ATTENTE_ADMIN -> "Le RH a approuvé la demande de congé de " + workerName;
            case ACCEPTE -> "La demande de congé de " + workerName + " a été approuvée par l'Admin";
            case REFUSE_SECRETAIRE -> "La demande de congé de " + workerName + " a été refusée par le Secrétaire";
            case REFUSE_RH -> "La demande de congé de " + workerName + " a été refusée par le RH";
            case REFUSE_ADMIN -> "La demande de congé de " + workerName + " a été refusée par l'Admin";
            default -> "Mise à jour de la demande de congé de " + workerName;
        };
    }

    @FXML
    private void goToStats() {
        try {
            // Load the FXML for the statistics window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Admin/admin_statistics.fxml"));
            Parent root = loader.load();

            // Create a new stage for the statistics window
            Stage statsStage = new Stage();
            statsStage.setTitle("Statistiques des Congés");
            statsStage.setScene(new Scene(root));

            // Configure as modal window
            statsStage.initModality(Modality.APPLICATION_MODAL);
            statsStage.initOwner(welcomeLabel.getScene().getWindow());

            // Optional: Add fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Show the window and play animation
            statsStage.show();
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir les statistiques");
            alert.setContentText("Une erreur est survenue lors du chargement de la fenêtre des statistiques.");
            alert.showAndWait();
        }
    }
}