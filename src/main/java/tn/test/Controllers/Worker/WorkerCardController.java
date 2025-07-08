package tn.test.Controllers.Worker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.test.entities.Worker;

import java.io.InputStream;

public class WorkerCardController {

    @FXML private ImageView avatarImage;
    @FXML private Label nameLabel;
    @FXML private Label positionLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label departmentLabel;
    @FXML private Label hireDateLabel;
    @FXML private Label salaryLabel;
    @FXML private Label statusBadge;

    private Worker worker;

    public void setWorker(Worker worker) {
        this.worker = worker;
        nameLabel.setText(worker.getName());
        positionLabel.setText(worker.getPosition());
        phoneLabel.setText(worker.getPhone());
        emailLabel.setText(worker.getEmail());
        departmentLabel.setText(worker.getDepartment());
        hireDateLabel.setText(worker.getCreationDate().toString());
        salaryLabel.setText(worker.getSalary() + " TND");

        String status = worker.getStatus().equalsIgnoreCase("actif") ? "🟢 ACTIF" : "🔴 INACTIF";
        if (worker.getUsedCongeDays() > 0) {
            status += " | 🟡 EN CONGÉ";
        }
        statusBadge.setText(status);

        avatarImage.setImage(getAvatarImage(worker));
    }

    private Image getAvatarImage(Worker worker) {
        try {
            if (worker.getProfileImagePath() != null && !worker.getProfileImagePath().isEmpty()) {
                String path = "/Images/Users/" + worker.getProfileImagePath();
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) return new Image(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Image(getClass().getResourceAsStream(
                worker.getGender().equalsIgnoreCase("Femme") ?
                        "/Images/Users/default-female.png" :
                        "/Images/Users/default-male.png"));
    }

    @FXML
    private void handleClose() {
        ((Stage) nameLabel.getScene().getWindow()).close();
    }
}
