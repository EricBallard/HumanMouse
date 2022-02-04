package manager.gui;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GUI extends Application {

    public Stage stage;

    Scene scene;

    Controller controller;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui.fxml"));
        loader.setController(controller = new Controller(this));
        AnchorPane root = null;

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setHeight(512);
        stage.setWidth(512);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setTitle("v1.0 | HumanMouse-Manager");
        stage.show();
    }


}
