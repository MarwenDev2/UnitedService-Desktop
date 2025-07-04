package tn.test.Controllers.Worker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.test.entities.DemandeConge;
import tn.test.entities.User;
import tn.test.services.DemandeCongeService;

import java.net.URL;
import java.util.*;

public class WorkerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label approvedCountLabel;
    @FXML private Label refusedCountLabel;

    @FXML private TableView<DemandeConge> recentLeavesTable;

    private final DemandeCongeService congeService = new DemandeCongeService();
    private User currentUser;

    // Called automatically when the view is loaded
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
    }

    private void setupTable() {
        TableColumn<DemandeConge, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));

        TableColumn<DemandeConge, String> motifCol = new TableColumn<>("Motif");
        motifCol.setCellValueFactory(new PropertyValueFactory<>("motif"));

        TableColumn<DemandeConge, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        recentLeavesTable.getColumns().setAll(dateCol, motifCol, statusCol);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Vous êtes connecté en tant que " + user.getName());
        loadDashboardData();
    }

    private void loadDashboardData() {
        int pending = congeService.countByStatus(currentUser.getId(), "EN_ATTENTE");
        int approved = congeService.countByStatus(currentUser.getId(), "ACCEPTE");
        int refused = congeService.countByStatus(currentUser.getId(), "REFUSE");

        pendingCountLabel.setText(String.valueOf(pending));
        approvedCountLabel.setText(String.valueOf(approved));
        refusedCountLabel.setText(String.valueOf(refused));

        List<DemandeConge> recentConges = congeService.getRecentCongeByUser(currentUser.getId(), 5);
        recentLeavesTable.getItems().setAll(recentConges);
    }

    // Optional: show alert if needed
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
