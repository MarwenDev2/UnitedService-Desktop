package tn.test.Controllers.User.Admin;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.test.entities.Worker;
import tn.test.services.WorkerService;

import java.io.InputStream;
import java.util.List;

public class WorkersListController {

    @FXML private FlowPane workersContainer;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField searchField;
    @FXML private Label totalWorkersLabel;
    @FXML private Label activeWorkersLabel;
    @FXML private Label onLeaveLabel;

    private final WorkerService workerService = new WorkerService();

    @FXML
    public void initialize() {
        loadDepartments();
        loadWorkers();
        setupAnimations();

        // Set up 3D perspective for flip animation
        workersContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                PerspectiveCamera camera = new PerspectiveCamera();
                camera.setFieldOfView(60);
                newScene.setCamera(camera);
            }
        });
    }

    private void loadDepartments() {
        List<String> departments = workerService.findAll().stream()
                .map(Worker::getDepartment)
                .distinct()
                .toList();

        filterComboBox.getItems().add("Tous les départements");
        filterComboBox.getItems().addAll(departments);
        filterComboBox.getSelectionModel().selectFirst();
    }

    private void loadWorkers() {
        workersContainer.getChildren().clear();
        List<Worker> workers = workerService.findAll();

        updateStats(workers);

        for (Worker worker : workers) {
            VBox card = createWorkerCard(worker);
            workersContainer.getChildren().add(card);
        }
    }

    private VBox createWorkerCard(Worker worker) {
        VBox card = new VBox(10);
        card.getStyleClass().add("worker-card");
        card.setUserData(worker.getId());

        // Header with avatar and name
        HBox header = new HBox(10);
        header.getStyleClass().add("card-header");

        ImageView avatar = new ImageView(getAvatarImage(worker));
        avatar.setFitHeight(80);
        avatar.setFitWidth(80);
        avatar.getStyleClass().add("worker-avatar");

        VBox nameBox = new VBox(5);
        Label nameLabel = new Label(worker.getName());
        nameLabel.getStyleClass().add("worker-name");

        Label positionLabel = new Label(worker.getPosition());
        positionLabel.getStyleClass().add("worker-position");

        nameBox.getChildren().addAll(nameLabel, positionLabel);
        header.getChildren().addAll(avatar, nameBox);

        // Details section
        VBox details = new VBox(8);
        details.getStyleClass().add("card-details");

        details.getChildren().addAll(
                createDetailRow("📞", worker.getPhone()),
                createDetailRow("✉️", worker.getEmail()),
                createDetailRow("🏢", worker.getDepartment()),
                createDetailRow("📅", "Embauche: " + worker.getCreationDate()),
                createDetailRow("💰", "Salaire: " + worker.getSalary() + " TND"),
                createStatusBadge(worker)
        );

        card.getChildren().addAll(header, details);
        return card;
    }

    private HBox createDetailRow(String icon, String text) {
        HBox row = new HBox(5);
        row.getStyleClass().add("detail-row");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("detail-icon");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("detail-label");

        row.getChildren().addAll(iconLabel, textLabel);
        return row;
    }

    private Label createStatusBadge(Worker worker) {
        Label badge = new Label(worker.getStatus().equalsIgnoreCase("actif") ? "ACTIF" : "INACTIF");
        badge.getStyleClass().addAll("status-badge",
                worker.getStatus().equalsIgnoreCase("actif") ? "status-active" : "status-inactive");
        return badge;
    }

    private Image getAvatarImage(Worker worker) {
        try {
            if (worker.getProfileImagePath() != null && !worker.getProfileImagePath().isEmpty()) {
                // Load from resources
                String imagePath = "/Images/Users/" + worker.getProfileImagePath();
                InputStream inputStream = getClass().getResourceAsStream(imagePath);

                if (inputStream != null) {
                    return new Image(inputStream);
                } else {
                    System.err.println("Image not found: " + imagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
        }

        // Default avatar based on gender
        String defaultImage = worker.getGender().equalsIgnoreCase("Femme") ?
                "/Images/Users/default-female.png" : "/Images/Users/default-male.png";
        return new Image(getClass().getResourceAsStream(defaultImage));
    }

    private void updateStats(List<Worker> workers) {
        int total = workers.size();
        int active = (int) workers.stream().filter(w -> w.getStatus().equalsIgnoreCase("actif")).count();
        int onLeave = (int) workers.stream().filter(w -> w.getUsedCongeDays() > 0).count();

        totalWorkersLabel.setText("Total: " + total);
        activeWorkersLabel.setText("Actifs: " + active);
        onLeaveLabel.setText("En congé: " + onLeave);
    }

    private void setupAnimations() {
        SequentialTransition seqTransition = new SequentialTransition();

        for (int i = 0; i < workersContainer.getChildren().size(); i++) {
            Node card = workersContainer.getChildren().get(i);

            FadeTransition fade = new FadeTransition(Duration.millis(300), card);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition translate = new TranslateTransition(Duration.millis(400), card);
            translate.setFromY(20);
            translate.setToY(0);

            ParallelTransition parallel = new ParallelTransition(fade, translate);
            parallel.setDelay(Duration.millis(i * 50));

            seqTransition.getChildren().add(parallel);
        }

        seqTransition.play();
    }

    @FXML
    private void handleFilter() {
        String department = filterComboBox.getValue();
        String searchTerm = searchField.getText().toLowerCase();

        List<Worker> filtered = workerService.findAll().stream()
                .filter(w -> department.equals("Tous les départements") || w.getDepartment().equalsIgnoreCase(department))
                .filter(w -> searchTerm.isEmpty() ||
                        w.getName().toLowerCase().contains(searchTerm) ||
                        w.getPosition().toLowerCase().contains(searchTerm) ||
                        w.getEmail().toLowerCase().contains(searchTerm))
                .toList();

        workersContainer.getChildren().clear();
        filtered.forEach(w -> workersContainer.getChildren().add(createWorkerCard(w)));
        updateStats(filtered);
    }
}