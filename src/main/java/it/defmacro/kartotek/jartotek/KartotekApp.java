package it.defmacro.kartotek.jartotek;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class KartotekApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(KartotekApp.class.getResource("KartotekApp.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        KartotekController ctrl = fxmlLoader.getController();
        KeyCombination kbNewNote = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        KeyCombination kbTabClose = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
        KeyCombination kbSearch = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.SHORTCUT_DOWN);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (kbNewNote.match(keyEvent)) {
                    ctrl.onNewNote();
                } else if (kbTabClose.match(keyEvent)) {
                    ctrl.onTabClose();
                } else if (kbSearch.match(keyEvent)) {
                    ctrl.onSearch();
                }
            }
        });
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