// AdminStatisticsController.java
package tn.test.Controllers.User.Admin;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.test.entities.Status;
import tn.test.entities.TypeConge;
import tn.test.services.DemandeCongeService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminStatisticsController implements Initializable {

    @FXML private PieChart statusPieChart;
    @FXML private PieChart typePieChart;
    @FXML private BarChart<String, Number> monthlyBarChart;
    @FXML private Label totalDemandesLabel;
    @FXML private VBox rootContainer;

    private final DemandeCongeService demandeService = new DemandeCongeService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        animateChart();
        loadStatusStats();
        loadTypeStats();
        loadMonthlyStats();
    }

    private void loadStatusStats() {
        int total = demandeService.countAll();
        int accepted = demandeService.countByStatus(Status.ACCEPTE);
        int refused = demandeService.countByStatus(Status.REFUSE_SECRETAIRE)
                + demandeService.countByStatus(Status.REFUSE_RH)
                + demandeService.countByStatus(Status.REFUSE_ADMIN);
        int pending = demandeService.countByStatus(Status.EN_ATTENTE_SECRETAIRE)
                + demandeService.countByStatus(Status.EN_ATTENTE_RH)
                + demandeService.countByStatus(Status.EN_ATTENTE_ADMIN);

        totalDemandesLabel.setText("Total des demandes : " + total);

        PieChart.Data acceptedData = new PieChart.Data("Acceptées", accepted);
        PieChart.Data refusedData = new PieChart.Data("Refusées", refused);
        PieChart.Data pendingData = new PieChart.Data("En attente", pending);

        statusPieChart.getData().setAll(acceptedData, refusedData, pendingData);
    }

    private void loadTypeStats() {
        PieChart.Data annuel = new PieChart.Data("Annuel", demandeService.countByType(TypeConge.ANNUEL));
        PieChart.Data maladie = new PieChart.Data("Maladie", demandeService.countByType(TypeConge.MALADIE));
        PieChart.Data maternite = new PieChart.Data("Maternité", demandeService.countByType(TypeConge.MATERNITE));
        PieChart.Data sansSolde = new PieChart.Data("Sans solde", demandeService.countByType(TypeConge.SANS_SOLDE));
        PieChart.Data autre = new PieChart.Data("Autre", demandeService.countByType(TypeConge.AUTRE));

        typePieChart.getData().setAll(annuel, maladie, maternite, sansSolde, autre);
    }

    private void loadMonthlyStats() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Demandes par mois");

        for (int i = 1; i <= 12; i++) {
            int count = demandeService.countByMonth(i);
            series.getData().add(new XYChart.Data<>(getMonthName(i), count));
        }

        monthlyBarChart.getData().clear();
        monthlyBarChart.getData().add(series);
    }

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Fév";
            case 3 -> "Mars";
            case 4 -> "Avr";
            case 5 -> "Mai";
            case 6 -> "Juin";
            case 7 -> "Juil";
            case 8 -> "Août";
            case 9 -> "Sept";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Déc";
            default -> "";
        };
    }

    private void animateChart() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), rootContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void backToDashboard() {
        try {
            Stage stage = (Stage) rootContainer.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/WorkerDashboard.fxml"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
