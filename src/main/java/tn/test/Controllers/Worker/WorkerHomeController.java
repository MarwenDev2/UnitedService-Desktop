package tn.test.Controllers.Worker;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import tn.test.Controllers.front.FrontViewController;
import tn.test.entities.DemandeConge;
import tn.test.entities.Status;
import tn.test.entities.Worker;
import tn.test.services.DemandeCongeService;
import tn.test.services.WorkerService;
import tn.test.tools.SessionManager;

import java.util.List;

public class WorkerHomeController {

    @FXML private Label welcomeLabel, totalDaysLabel, usedDaysLabel, remainingDaysLabel;
    @FXML private HBox cardContainer; // updated from TableView
    private final DemandeCongeService demandeService = new DemandeCongeService();
    private final WorkerService workerService = new WorkerService();
    private final Worker currentWorker = SessionManager.getInstance().getCurrentWorker();

    public void initialize() {
        loadWorkerInfo();
        loadDemandeCards();
    }

    private void loadWorkerInfo() {
        welcomeLabel.setText("Bienvenue, " + currentWorker.getName() + " !");
        totalDaysLabel.setText(String.valueOf(currentWorker.getTotalCongeDays()));
        usedDaysLabel.setText(String.valueOf(currentWorker.getUsedCongeDays()));
        int remaining = currentWorker.getTotalCongeDays() - currentWorker.getUsedCongeDays();
        remainingDaysLabel.setText(String.valueOf(remaining));
    }

    private void loadDemandeCards() {
        cardContainer.getChildren().clear();
        var worker = workerService.findByCin(currentWorker.getCin());
        List<DemandeConge> demandes = demandeService.findByWorkerId(worker.getId());

        for (DemandeConge demande : demandes) {
            VBox card = new VBox(10);
            card.getStyleClass().add("demande-card");

            // 🗂️ Type
            Label typeLabel = new Label("🗂 " + demande.getType());
            typeLabel.getStyleClass().add("card-title");

            // 📅 Dates
            Label datesLabel = new Label("📅 " + demande.getStartDate() + " → " + demande.getEndDate());

            // 🧾 Reason
            Label reasonLabel = new Label("📝 " + demande.getReason());
            reasonLabel.setWrapText(true);

            // 📊 Status
            Label statusLabel = new Label(getStatusIcon(demande.getStatus()) + " " + formatStatus(demande.getStatus()));
            statusLabel.getStyleClass().add("status-" + demande.getStatus().name().toLowerCase());

            card.getChildren().addAll(typeLabel, datesLabel, reasonLabel, statusLabel);
            cardContainer.getChildren().add(card);
        }
    }

    private String getStatusIcon(Status status) {
        return switch (status) {
            case ACCEPTE -> "✅";
            case REFUSE_ADMIN, REFUSE_RH, REFUSE_SECRETAIRE -> "❌";
            case EN_ATTENTE_ADMIN, EN_ATTENTE_RH, EN_ATTENTE_SECRETAIRE -> "⏳";
            default -> "📌";
        };
    }

    private String formatStatus(Status status) {
        return switch (status) {
            case EN_ATTENTE_SECRETAIRE -> "En attente (Secrétaire)";
            case EN_ATTENTE_RH -> "En attente (RH)";
            case EN_ATTENTE_ADMIN -> "En attente (Admin)";
            case ACCEPTE -> "Approuvée";
            case REFUSE_ADMIN, REFUSE_RH, REFUSE_SECRETAIRE -> "Rejetée";
            default -> status.name();
        };
    }
}
