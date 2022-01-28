package manager.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import manager.gui.handlers.Renderer;
import manager.mouse.MousePath;

import java.io.IOException;

public class GUI extends Application {

    // Loaded paths
    public MousePath.Paths paths;

    Controller controller;

    Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
        loader.setController(controller = new Controller(this));
        GridPane root = null;

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setHeight(512);
        stage.setWidth(512);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setTitle("v1.0 | HumanMouse-Manager");
        stage.setResizable(false);
        stage.show();
    }

    public void toggleRenderer(boolean paused) {
        if (this.controller != null) {
            this.controller.renderer.state.set(
                    paused ? Renderer.State.PAUSED : Renderer.State.RUNNING);
        }
    }
}
