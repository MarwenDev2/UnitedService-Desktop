package tn.test.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class DashboardController {

    @FXML
    public void initialize() {
    }

    @FXML
    private void customizeCards() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personnalisation");
        alert.setHeaderText("Fonctionnalité à implémenter");
        alert.setContentText("Ajout dynamique de cartes à implémenter ici.");
        alert.showAndWait();
    }
}