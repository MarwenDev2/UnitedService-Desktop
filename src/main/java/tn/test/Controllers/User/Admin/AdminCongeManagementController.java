package tn.test.Controllers.User.Admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.test.entities.*;
import tn.test.services.DecisionService;
import tn.test.services.DemandeCongeService;
import tn.test.services.NotificationService;
import tn.test.tools.SessionManager;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AdminCongeManagementController implements Initializable {

    @FXML private Label dashboardTitle;
    @FXML private Label welcomeLabel;

    @FXML private Label pendingLabel;
    @FXML private Label acceptedLabel;
    @FXML private Label refusedLabel;

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> stageFilterCombo;
    @FXML private TableView<DemandeConge> table;
    @FXML private TableColumn<DemandeConge, String> workerNameCol;
    @FXML private TableColumn<DemandeConge, String> dateCol;
    @FXML private TableColumn<DemandeConge, String> motifCol;
    @FXML private TableColumn<DemandeConge, String> statusCol;
    @FXML private TableColumn<DemandeConge, Void> actionsCol;
    private final DecisionService decisionService = new DecisionService();
    private final NotificationService notificationService = new NotificationService();
    private final DemandeCongeService service = new DemandeCongeService();
    private final User currentUser = SessionManager.getInstance().getCurrentUser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadTableData();
        loadStats();

        welcomeLabel.setText("Bienvenue, " + currentUser.getName());
    }

    private void setupTable() {
        workerNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWorker().getName()));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        motifCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✔");
            private final Button refuseBtn = new Button("✖");
            private final HBox actionBox = new HBox(approveBtn, refuseBtn);

            {
                actionBox.setSpacing(10);
                approveBtn.getStyleClass().add("btn-approve");
                refuseBtn.getStyleClass().add("btn-refuse");

                approveBtn.setOnAction(e -> {
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    handleApproval(demande, true);
                });

                refuseBtn.setOnAction(e -> {
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    handleApproval(demande, false);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                DemandeConge demande = getTableView().getItems().get(getIndex());
                boolean showButtons = false;

                // Display buttons only if the demande is at the correct stage for the user's role
                switch (currentUser.getRole()) {
                    case SECRETAIRE -> showButtons = demande.getStatus() == Status.EN_ATTENTE_SECRETAIRE;
                    case RH -> showButtons = demande.getStatus() == Status.EN_ATTENTE_RH;
                    case ADMIN -> showButtons = demande.getStatus() == Status.EN_ATTENTE_ADMIN;
                }

                setGraphic(showButtons ? actionBox : null);
            }

        });
    }


    private void setupFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "TOUS",
                "EN_ATTENTE_SECRETAIRE",
                "EN_ATTENTE_RH",
                "EN_ATTENTE_ADMIN",
                "ACCEPTE",
                "REFUSE_SECRETAIRE",
                "REFUSE_RH",
                "REFUSE_ADMIN"
        ));

        stageFilterCombo.setItems(FXCollections.observableArrayList("TOUS", "SECRETAIRE", "RH", "ADMIN"));

        statusFilterCombo.setValue("TOUS");
        stageFilterCombo.setValue("TOUS");

        statusFilterCombo.setOnAction(e -> loadTableData());
        stageFilterCombo.setOnAction(e -> loadTableData());
    }

    private void loadTableData() {
        List<DemandeConge> data = service.findAll();
        ObservableList<DemandeConge> filtered = FXCollections.observableArrayList();

        for (DemandeConge d : data) {
            boolean statusMatch = statusFilterCombo.getValue().equals("TOUS")
                    || d.getStatus().name().equals(statusFilterCombo.getValue());
            boolean stageMatch = stageFilterCombo.getValue().equals("TOUS")
                    || d.getCurrentStage().equalsIgnoreCase(stageFilterCombo.getValue());

            if (statusMatch && stageMatch) filtered.add(d);
        }

        table.setItems(filtered);
    }

    private void loadStats() {
        int total = service.countAll();
        int enAttente = service.countByStatus(Status.EN_ATTENTE_SECRETAIRE)
                + service.countByStatus(Status.EN_ATTENTE_RH)
                + service.countByStatus(Status.EN_ATTENTE_ADMIN);

        int accepte = service.countByStatus(Status.ACCEPTE);

        int refuse = service.countByStatus(Status.REFUSE_SECRETAIRE)
                + service.countByStatus(Status.REFUSE_RH)
                + service.countByStatus(Status.REFUSE_ADMIN);


        pendingLabel.setText("En attente: " + enAttente);
        acceptedLabel.setText("Acceptées: " + accepte);
        refusedLabel.setText("Refusées: " + refuse);
    }

    private void handleApproval(DemandeConge demande, boolean isApproved) {
        Role role = currentUser.getRole();

        boolean validStep = switch (role) {
            case SECRETAIRE -> demande.getStatus() == Status.EN_ATTENTE_SECRETAIRE;
            case RH -> demande.getStatus() == Status.EN_ATTENTE_RH;
            case ADMIN -> demande.getStatus() == Status.EN_ATTENTE_ADMIN;
            default -> false;
        };

        if (!validStep) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vous ne pouvez pas valider cette demande à ce stade.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String comment = promptForComment(isApproved);
        if (comment == null) return; // user cancelled

        // Create and save the decision
        Decision decision = new Decision();
        decision.setApproved(isApproved);
        decision.setComment(comment);
        decision.setDecisionBy(currentUser);
        decision.setDate(LocalDateTime.now());
        decisionService.add(decision);

        // Send a notification to the demandeur
        String notifMsg;
        if (!isApproved) {
            notifMsg = "Votre demande a été refusée par " + role.name() + " (" + currentUser.getName() + ")";
        } else {
            if (role == Role.ADMIN) {
                notifMsg = "🎉 Votre demande de congé a été définitivement acceptée.";
            } else {
                Role nextRole = switch (role) {
                    case SECRETAIRE -> Role.RH;
                    case RH -> Role.ADMIN;
                    default -> null;
                };

                notifMsg = "Votre demande a été validée par " + role.name() + " (" + currentUser.getName() + ")";
                if (nextRole != null) {
                    notifMsg += ". En attente de la décision de " + nextRole.name();
                }
            }
        }


        Notification notification = new Notification();
        notification.setRecipient(demande.getWorker());
        notification.setMessage(notifMsg);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notificationService.add(notification);

        // Update demande status and associate decision
        switch (role) {
            case SECRETAIRE -> service.updateSecretaireStatus(demande.getId(), isApproved);
            case RH -> service.updateRHStatus(demande.getId(), isApproved);
            case ADMIN -> service.finalApprove(demande.getId(), isApproved);
        }

        loadTableData();
        loadStats();
    }


    private String promptForComment(boolean isApproved) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter un commentaire");
        dialog.setHeaderText(isApproved ? "Valider la demande" : "Refuser la demande");
        dialog.setContentText("Commentaire :");

        return dialog.showAndWait().orElse(null);
    }


}
