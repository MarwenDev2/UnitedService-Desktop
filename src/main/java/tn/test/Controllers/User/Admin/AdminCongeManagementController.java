package tn.test.Controllers.User.Admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.test.Controllers.Worker.WorkerCardController;
import tn.test.entities.*;
import tn.test.services.DecisionService;
import tn.test.services.DemandeCongeService;
import tn.test.services.NotificationService;
import tn.test.tools.SessionManager;

import java.io.IOException;
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
    @FXML private TableColumn<DemandeConge, String> durationCol;
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

    private String formatStatus(Status status) {
        return switch (status) {
            case EN_ATTENTE_RH -> "En attente RH";
            case EN_ATTENTE_ADMIN -> "En attente Directeur";
            case REFUSE_RH -> "Refusé par RH";
            case REFUSE_ADMIN -> "Refusé par Directeur";
            case ACCEPTE -> "Acceptée";
            default -> status.name(); // fallback (in case of unknown or deprecated values)
        };
    }

    private String displayStatus(String value) {
        return switch (value) {
            case "EN_ATTENTE_RH" -> "En attente RH";
            case "EN_ATTENTE_ADMIN" -> "En attente Directeur";
            case "ACCEPTE" -> "Acceptée";
            case "REFUSE_RH" -> "Refusé RH";
            case "REFUSE_ADMIN" -> "Refusé Directeur";
            case "TOUS" -> "Tous";
            default -> value;
        };
    }

    private String displayStage(String value) {
        return switch (value.toUpperCase()) {
            case "ADMIN" -> "Directeur";
            case "RH" -> "RH";
            case "TOUS" -> "Tous";
            default -> value;
        };
    }
    private <T> void centerColumn(TableColumn<T, String> column) {
        column.setStyle("-fx-alignment: CENTER;");
    }


    private void setupTable() {
        workerNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWorker().getName()));
        dateCol.setCellValueFactory(cellData -> {
            DemandeConge d = cellData.getValue();
            String periode = d.getStartDate() + " - " + d.getEndDate();
            return new SimpleStringProperty(periode);
        });

        durationCol.setCellValueFactory(cellData -> {
            DemandeConge d = cellData.getValue();
            long days = java.time.temporal.ChronoUnit.DAYS.between(d.getStartDate(), d.getEndDate()) + 1;
            return new SimpleStringProperty(days + " jours");
        });
        motifCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatStatus(cellData.getValue().getStatus())));


        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✔");
            private final Button refuseBtn = new Button("✖");
            private final Button viewBtn = new Button("👁");
            private final HBox actionBox = new HBox(viewBtn, approveBtn, refuseBtn);

            {
                actionBox.setSpacing(10);
                actionBox.getStyleClass().add("action-box");

                approveBtn.getStyleClass().add("btn-approve");
                refuseBtn.getStyleClass().add("btn-refuse");
                viewBtn.getStyleClass().add("btn-view");

                approveBtn.setOnAction(e -> {
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    handleApproval(demande, true);
                });

                refuseBtn.setOnAction(e -> {
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    handleApproval(demande, false);
                });

                viewBtn.setOnAction(e -> {
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    showWorkerDetails(demande.getWorker());
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
                boolean showButtons = switch (currentUser.getRole()) {
                    case RH -> demande.getStatus() == Status.EN_ATTENTE_RH;
                    case ADMIN -> demande.getStatus() == Status.EN_ATTENTE_ADMIN;
                    default -> false;
                };

                setGraphic(showButtons ? actionBox : new HBox(viewBtn)); // Always show view
            }
        });

        centerColumn(workerNameCol);
        centerColumn(dateCol);
        centerColumn(durationCol);
        centerColumn(motifCol);
        centerColumn(statusCol);

    }

    private void showWorkerDetails(Worker worker) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Worker/WorkerCard.fxml"));
            Parent root = loader.load();

            WorkerCardController controller = loader.getController();
            controller.setWorker(worker);

            Stage popup = new Stage();
            Scene scene = new Scene(root, 600, 400);

            popup.setTitle("Détails du salarié");
            popup.setScene(scene);
            popup.setResizable(false);
            popup.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void setupFilters() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "TOUS", "EN_ATTENTE_RH", "EN_ATTENTE_ADMIN", "ACCEPTE", "REFUSE_RH", "REFUSE_ADMIN"
        );
        statusFilterCombo.setItems(statusOptions);

        statusFilterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayStatus(item));
            }
        });
        statusFilterCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayStatus(item));
            }
        });

        ObservableList<String> stageOptions = FXCollections.observableArrayList("TOUS", "RH", "ADMIN");
        stageFilterCombo.setItems(stageOptions);

        stageFilterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayStage(item));
            }
        });
        stageFilterCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayStage(item));
            }
        });

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
        int enAttente = service.countByStatus(Status.EN_ATTENTE_RH)
                + service.countByStatus(Status.EN_ATTENTE_ADMIN);
        int accepte = service.countByStatus(Status.ACCEPTE);
        int refuse = service.countByStatus(Status.REFUSE_RH)
                + service.countByStatus(Status.REFUSE_ADMIN);

        pendingLabel.setText("En attente: " + enAttente);
        acceptedLabel.setText("Acceptées: " + accepte);
        refusedLabel.setText("Refusées: " + refuse);
    }


    private void handleApproval(DemandeConge demande, boolean isApproved) {
        Role role = currentUser.getRole();

        boolean validStep = switch (role) {
            case RH -> demande.getStatus() == Status.EN_ATTENTE_RH;
            case ADMIN -> demande.getStatus() == Status.EN_ATTENTE_ADMIN;
            default -> false;
        };

        if (!validStep) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vous ne pouvez pas valider cette demande à ce stade.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // --- Determine if this is the final step ---
        boolean isFinalStep = (!isApproved) || (role == Role.ADMIN);
        String comment = null;

        if (isFinalStep) {
            comment = promptForComment(isApproved);
            if (comment == null) return; // user cancelled
        }

        // --- Create decision only for final step (refused or final approval) ---
        if (isFinalStep) {
            Decision decision = new Decision();
            decision.setApproved(isApproved);
            decision.setComment(comment);
            decision.setDecisionBy(currentUser);
            decision.setDate(LocalDateTime.now());
            decisionService.add(decision);

            if (role == Role.ADMIN) {
                service.finalApprove(demande.getId(), isApproved);
            } else if (role == Role.RH) {
                service.updateRHStatus(demande.getId(), false);
            }
        } else {
            if (role == Role.RH) {
                service.updateRHStatus(demande.getId(), true);
            }
        }

        // --- Notification message logic ---
        String notifMsg;
        String fullName = demande.getWorker().getName();
        String dateRange = "du " + demande.getStartDate() + " au " + demande.getEndDate();

        if (!isApproved) {
            notifMsg = "❌ La demande de congé " + dateRange + " de " + fullName +
                    " a été refusée par " + displayRole(role) + " (" + currentUser.getName() + ")";
        } else {
            if (role == Role.ADMIN) {
                notifMsg = "✅ La demande de congé " + dateRange + " de " + fullName + " a été approuvée définitivement.";
            } else {
                notifMsg = "✔ La demande de congé " + dateRange + " de " + fullName +
                        " a été validée par " + displayRole(role) + " (" + currentUser.getName() + ")" +
                        ". En attente de validation du Directeur.";
            }
        }


        Notification notification = new Notification();
        notification.setRecipient(demande.getWorker());
        notification.setMessage(notifMsg);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notificationService.add(notification);

        loadTableData();
        loadStats();
    }

    private String displayRole(Role role) {
        return switch (role) {
            case ADMIN -> "Directeur";
            case RH -> "RH";
            default -> role.name();
        };
    }

    private String promptForComment(boolean isApproved) {
        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Commentaire");
        choiceAlert.setHeaderText(isApproved ? "Souhaitez-vous ajouter un commentaire à l'approbation ?" :
                "Souhaitez-vous ajouter un commentaire au refus ?");
        choiceAlert.setContentText("Choisissez une option :");

        ButtonType ouiBtn = new ButtonType("Oui");
        ButtonType nonBtn = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

        choiceAlert.getButtonTypes().setAll(ouiBtn, nonBtn);

        var result = choiceAlert.showAndWait();
        if (result.isEmpty() || result.get() == nonBtn) return null;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter un commentaire");
        dialog.setHeaderText("Saisissez votre commentaire ci-dessous :");
        dialog.setContentText("Commentaire :");

        return dialog.showAndWait().orElse("");
    }



}
