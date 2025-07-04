package tn.test.Controllers.Worker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import tn.test.entities.DemandeConge;
import tn.test.entities.Worker;
import tn.test.services.DemandeCongeService;
import tn.test.services.WorkerService;
import tn.test.tools.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class HistoriqueCongeController implements Initializable {

    @FXML
    private HBox cardContainer;

    private final DemandeCongeService demandeService = new DemandeCongeService();
    private final WorkerService workerService = new WorkerService();
    private Worker currentWorker = SessionManager.getInstance().getCurrentWorker();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadHistory();
    }

    private void loadHistory() {
        cardContainer.getChildren().clear();
        var demandes = demandeService.findByWorkerId(currentWorker.getId());
        for (var d : demandes) {
            VBox card = createCard(d);
            cardContainer.getChildren().add(card);
        }
    }

    private VBox createCard(DemandeConge d) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        Label title = new Label(d.getType().name());
        title.getStyleClass().add("card-title");

        Label range = new Label(String.format("Du %s au %s",
                d.getStartDate(), d.getEndDate()));
        range.getStyleClass().add("card-label");

        Label reason = new Label(d.getReason());
        reason.getStyleClass().add("card-label");

        Label status = new Label(d.getStatus().name());
        status.getStyleClass().addAll("status-label", "status-" + d.getStatus().name());

        card.getChildren().addAll(title, range, reason, status);
        return card;
    }

}
