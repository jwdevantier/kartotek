package it.defmacro.kartotek.jartotek;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class KartotekApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(KartotekApp.class.getResource("KartotekApp.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        KartotekController ctrl = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> {
            ctrl.quit();
        });
        scene.getStylesheets().add("style.css");
        stage.setTitle("Kartotek");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}