package tn.test.Controllers.Worker;

import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.test.Controllers.Shared;
import tn.test.entities.DemandeConge;
import tn.test.entities.Status;
import tn.test.entities.TypeConge;
import tn.test.entities.Worker;
import tn.test.services.DemandeCongeService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

import java.net.URL;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class LeaveHistoryController implements Initializable {
    @FXML
    private Label workerNameLabel;
    @FXML
    private Label workerPositionLabel;
    @FXML
    private ImageView workerAvatar;
    @FXML
    private ComboBox<Integer> yearFilter;
    @FXML
    private ComboBox<TypeConge> typeFilter;
    @FXML
    private ComboBox<Status> statusFilter;
    @FXML
    private TableView<DemandeConge> leaveTable;
    @FXML
    private Label totalLeaveLabel;
    @FXML
    private Label approvedLabel;
    @FXML
    private Label pendingLabel;
    @FXML
    private Label rejectedLabel;

    private final Worker worker = Shared.getWorker();
    private final DemandeCongeService demandeCongeService = new DemandeCongeService();
    private final ObservableList<DemandeConge> leaveRequests = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeData();
    }

    private void initializeData() {
        // Set worker info
        System.out.println(worker.getCin());
        workerNameLabel.setText(worker.getName());
        workerPositionLabel.setText(worker.getPosition() + " - " + worker.getDepartment());

        try {
            if (worker.getProfileImagePath() != null && !worker.getProfileImagePath().isEmpty()) {
                String imagePath = "/Images/Users/" + worker.getProfileImagePath();
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                workerAvatar.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
        }

        // Initialize filters
        initializeFilters();

        // Load initial data
        loadLeaveRequests(null, null, null);

        // Set up table columns
        setupTableColumns();

        // Set up event handlers
        yearFilter.setOnAction(e -> applyFilters());
        typeFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void initializeFilters() {
        // Year filter
        int currentYear = Year.now().getValue();
        yearFilter.getItems().add(null); // For "All years"
        for (int year = currentYear; year >= currentYear - 5; year--) {
            yearFilter.getItems().add(year);
        }
        yearFilter.getSelectionModel().selectFirst();

        // Type filter
        typeFilter.getItems().add(null); // All types
        typeFilter.getItems().addAll(TypeConge.values());
        typeFilter.getSelectionModel().selectFirst();

        // Status filter - default to ACCEPTE
        statusFilter.getItems().add(null); // All statuses
        statusFilter.getItems().addAll(Status.values());
        statusFilter.getSelectionModel().select(Status.ACCEPTE);
    }

    private void loadLeaveRequests(Integer year, TypeConge type, Status status) {
        // Get all requests for this worker
        List<DemandeConge> requests = demandeCongeService.findByWorkerId(worker.getId());

        // Filter for past requests only (end date before today)
        requests = requests.stream()
                .filter(req -> req.getEndDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        // Apply additional filters
        if (year != null) {
            requests = requests.stream()
                    .filter(req -> req.getStartDate().getYear() == year ||
                            req.getEndDate().getYear() == year)
                    .collect(Collectors.toList());
        }

        if (type != null) {
            requests = requests.stream()
                    .filter(req -> req.getType() == type)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            requests = requests.stream()
                    .filter(req -> req.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            // Default to showing only approved requests if no status filter
            requests = requests.stream()
                    .filter(req -> req.getStatus() == Status.ACCEPTE)
                    .collect(Collectors.toList());
        }

        leaveRequests.setAll(requests);
        leaveTable.setItems(leaveRequests);

        // Update statistics
        updateStatistics(requests);
    }

    private void updateStatistics(List<DemandeConge> requests) {
        int totalDays = requests.stream()
                .mapToInt(req -> (int) req.getStartDate().datesUntil(req.getEndDate().plusDays(1)).count())
                .sum();

        long approvedCount = requests.stream()
                .filter(req -> req.getStatus() == Status.ACCEPTE)
                .count();

        long pendingCount = requests.stream()
                .filter(req -> req.getStatus().name().contains("EN_ATTENTE"))
                .count();

        long rejectedCount = requests.stream()
                .filter(req -> req.getStatus().name().contains("REFUSE"))
                .count();

        totalLeaveLabel.setText("Total Jours: " + totalDays);
        approvedLabel.setText("Approuvés: " + approvedCount);
        pendingLabel.setText("En Attente: " + pendingCount);
        rejectedLabel.setText("Refusés: " + rejectedCount);
    }

    private void setupTableColumns() {
        // Clear existing columns
        leaveTable.getColumns().clear();

        // Type Column
        TableColumn<DemandeConge, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType().toString();
            return new SimpleStringProperty(type);
        });
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    switch (demande.getType()) {
                        case ANNUEL -> setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                        case MALADIE -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case MATERNITE -> setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold;");
                        case SANS_SOLDE -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Date Range Column
        TableColumn<DemandeConge, String> dateRangeCol = new TableColumn<>("Période");
        dateRangeCol.setCellValueFactory(cellData -> {
            String dateRange = cellData.getValue().getStartDate() + " - " + cellData.getValue().getEndDate();
            return new SimpleStringProperty(dateRange);
        });
        dateRangeCol.setPrefWidth(180);

        // Duration Column
        TableColumn<DemandeConge, String> durationCol = new TableColumn<>("Durée");
        durationCol.setCellValueFactory(cellData -> {
            DemandeConge demande = cellData.getValue();
            long days = demande.getStartDate().datesUntil(demande.getEndDate().plusDays(1)).count();
            String duration = days + " jour" + (days > 1 ? "s" : "");
            return new SimpleStringProperty(duration);
        });

        // Status Column
        TableColumn<DemandeConge, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus().toString();
            return new SimpleStringProperty(status);
        });
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    DemandeConge demande = getTableView().getItems().get(getIndex());
                    switch (demande.getStatus()) {
                        case ACCEPTE -> setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                        case EN_ATTENTE_RH, EN_ATTENTE_ADMIN ->
                                setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        case REFUSE_RH, REFUSE_ADMIN -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Reason Column
        TableColumn<DemandeConge, String> reasonCol = new TableColumn<>("Motif");
        reasonCol.setCellValueFactory(cellData -> {
            String reason = cellData.getValue().getReason();
            return new SimpleStringProperty(reason);
        });
        reasonCol.setPrefWidth(200);

        // Add columns to table
        leaveTable.getColumns().addAll(typeCol, dateRangeCol, durationCol, statusCol, reasonCol);
    }

    private void applyFilters() {
        // Animation for smoother transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), leaveTable);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0.5);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), leaveTable);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1);

        fadeOut.setOnFinished(e -> {
            loadLeaveRequests(
                    yearFilter.getValue(),
                    typeFilter.getValue(),
                    statusFilter.getValue()
            );
            fadeIn.play();
        });

        fadeOut.play();
    }

    @FXML
    private void handleExportPDF() {
        try {
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("historique_conges_" + worker.getName() + ".pdf");

            File file = fileChooser.showSaveDialog(workerNameLabel.getScene().getWindow());
            if (file != null) {
                // Create PDF document
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
                Paragraph title = new Paragraph("Historique des Congés - " + worker.getName(), titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Add worker info
                Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Paragraph info = new Paragraph();
                info.add(new Chunk("Poste: ", infoFont));
                info.add(new Chunk(worker.getPosition(), infoFont));
                info.add(Chunk.NEWLINE);
                info.add(new Chunk("Département: ", infoFont));
                info.add(new Chunk(worker.getDepartment(), infoFont));
                info.add(Chunk.NEWLINE);
                info.add(new Chunk("Filtres appliqués: ", infoFont));
                info.add(new Chunk(getCurrentFilters(), infoFont));
                info.setSpacingAfter(20);
                document.add(info);

                // Create table
                PdfPTable table = new PdfPTable(5); // 5 columns
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);

                // Add table headers
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
                addTableHeader(table, "Type", headerFont, BaseColor.DARK_GRAY);
                addTableHeader(table, "Période", headerFont, BaseColor.DARK_GRAY);
                addTableHeader(table, "Durée", headerFont, BaseColor.DARK_GRAY);
                addTableHeader(table, "Statut", headerFont, BaseColor.DARK_GRAY);
                addTableHeader(table, "Motif", headerFont, BaseColor.DARK_GRAY);

                // Add table rows
                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                for (DemandeConge demande : leaveTable.getItems()) {
                    // Type
                    addTableCell(table, demande.getType().toString(), cellFont, getTypeColor(demande.getType()));

                    // Date range
                    String dateRange = demande.getStartDate().format(dateFormatter) + " - " +
                            demande.getEndDate().format(dateFormatter);
                    addTableCell(table, dateRange, cellFont, BaseColor.BLACK);

                    // Duration
                    long days = demande.getStartDate().datesUntil(demande.getEndDate().plusDays(1)).count();
                    String duration = days + " jour" + (days > 1 ? "s" : "");
                    addTableCell(table, duration, cellFont, BaseColor.BLACK);

                    // Status
                    addTableCell(table, demande.getStatus().toString(), cellFont, getStatusColor(demande.getStatus()));

                    // Reason
                    addTableCell(table, demande.getReason(), cellFont, BaseColor.BLACK);
                }

                document.add(table);

                // Add statistics
                Paragraph stats = new Paragraph();
                stats.add(new Chunk("Statistiques: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                stats.add(new Chunk("Total jours: " + totalLeaveLabel.getText().replace("Total Jours: ", "") + " | ", infoFont));
                stats.add(new Chunk("Approuvés: " + approvedLabel.getText().replace("Approuvés: ", "") + " | ", infoFont));
                stats.add(new Chunk("En attente: " + pendingLabel.getText().replace("En Attente: ", "") + " | ", infoFont));
                stats.add(new Chunk("Refusés: " + rejectedLabel.getText().replace("Refusés: ", ""), infoFont));
                stats.setSpacingBefore(20);
                document.add(stats);

                // Add date of export
                Paragraph date = new Paragraph();
                date.add(new Chunk("Exporté le: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
                date.setAlignment(Element.ALIGN_RIGHT);
                document.add(date);

                document.close();

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("L'historique des congés a été exporté avec succès!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'export");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'export: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBorderColor(color);
        table.addCell(cell);
    }

    private BaseColor getTypeColor(TypeConge type) {
        return switch (type) {
            case ANNUEL -> new BaseColor(52, 152, 219); // Blue
            case MALADIE -> new BaseColor(231, 76, 60);  // Red
            case MATERNITE -> new BaseColor(155, 89, 182); // Purple
            case SANS_SOLDE -> new BaseColor(243, 156, 18); // Orange
            default -> BaseColor.DARK_GRAY;
        };
    }

    private BaseColor getStatusColor(Status status) {
        return switch (status) {
            case ACCEPTE -> new BaseColor(46, 204, 113); // Green
            case EN_ATTENTE_RH, EN_ATTENTE_ADMIN -> new BaseColor(243, 156, 18); // Orange
            case REFUSE_RH, REFUSE_ADMIN -> new BaseColor(231, 76, 60); // Red
            default -> BaseColor.DARK_GRAY;
        };
    }

    private String getCurrentFilters() {
        StringBuilder filters = new StringBuilder();

        if (yearFilter.getValue() != null) {
            filters.append("Année: ").append(yearFilter.getValue()).append(", ");
        }

        if (typeFilter.getValue() != null) {
            filters.append("Type: ").append(typeFilter.getValue()).append(", ");
        }

        if (statusFilter.getValue() != null) {
            filters.append("Statut: ").append(statusFilter.getValue()).append(", ");
        }

        if (filters.length() == 0) {
            return "Aucun filtre";
        } else {
            return filters.substring(0, filters.length() - 2);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) workerNameLabel.getScene().getWindow();

        // Add fade-out animation before closing
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), workerNameLabel.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> stage.close());
        fadeOut.play();
    }
}