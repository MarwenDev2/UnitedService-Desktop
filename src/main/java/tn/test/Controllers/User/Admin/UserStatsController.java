package tn.test.Controllers.User.Admin;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.test.services.UserService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class UserStatsController implements Initializable {

    // Charts
    @FXML private PieChart rolesPieChart;
    @FXML private PieChart statusPieChart;

    // Stats card labels
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label rolesCountLabel;
    @FXML private Label activeUsersNote;
    @FXML private Label rolesNote;
    @FXML private Label rolesChartNote;
    @FXML private Label statusChartNote;

    // Stats cards
    @FXML private VBox totalUsersCard;
    @FXML private VBox activeUsersCard;
    @FXML private VBox rolesCard;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeAnimations();

    }

    private void initializeAnimations() {
        // Add animation classes to elements
        totalUsersCard.getStyleClass().add("zoom-in");
        activeUsersCard.getStyleClass().add("zoom-in");
        rolesCard.getStyleClass().add("zoom-in");
        rolesPieChart.getStyleClass().add("fade-in");
        statusPieChart.getStyleClass().add("fade-in");

        // Create staggered animation
        SequentialTransition seqTransition = new SequentialTransition(
                createCardAnimation(totalUsersCard),
                createCardAnimation(activeUsersCard),
                createCardAnimation(rolesCard),
                createChartAnimation(rolesPieChart),
                createChartAnimation(statusPieChart)
        );

        // Play animation after scene is shown
        Platform.runLater(seqTransition::play);
    }

    private Animation createCardAnimation(VBox card) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);

        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> card.getStyleClass().add("show"));

        return parallel;
    }

    private Animation createChartAnimation(Node chart) {
        if (chart.getStyleClass().contains("fade-in")) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), chart);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setOnFinished(e -> chart.getStyleClass().add("show"));
            return fade;
        } else {
            TranslateTransition translate = new TranslateTransition(Duration.millis(500), chart);
            translate.setFromY(20);
            translate.setToY(0);

            FadeTransition fade = new FadeTransition(Duration.millis(500), chart);
            fade.setFromValue(0);
            fade.setToValue(1);

            ParallelTransition parallel = new ParallelTransition(translate, fade);
            parallel.setOnFinished(e -> chart.getStyleClass().add("show"));
            return parallel;
        }
    }


    @FXML
    private void handleClose() {
        Stage stage = (Stage) rolesPieChart.getScene().getWindow();
        stage.close();
    }
}