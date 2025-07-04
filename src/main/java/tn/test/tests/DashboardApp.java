package tn.test.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DashboardApp extends Application {

    @Override
    public void start(Stage stage) {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml"));

            Scene scene = new Scene(root, 1300, 800);
            scene.getStylesheets().add(getClass().getResource("/views/User/Authentication/loginstyle.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            stage.setTitle("United Service");
            Image icon = new Image(getClass().getResourceAsStream("/Images/logo.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
