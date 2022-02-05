package manager.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class GUI extends Application {

    // Maven props
    final Properties properties = new Properties();

    // HumanMouse
    public Stage stage;

    Scene scene;

    Controller controller;

    @Override
    public void start(Stage stage) {
        // Need to cache stage before init controller
        this.stage = stage;

        InputStream icon;
        AnchorPane root;
        String style;

        try {
            // Load Maven props
            properties.load(getClass().getResourceAsStream("/project.properties"));

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui.fxml"));
            loader.setController(controller = new Controller(this));
            root = loader.load();

            // Load CSS
            URL stylePath = getClass().getResource("/style.css");
            if (stylePath == null || (style = stylePath.toExternalForm()) == null)
                throw new IOException("Failed to find style.css");

            // Load Icon
            if ((icon = getClass().getResourceAsStream("/icon.png")) == null)
                throw new IOException("Failed to find icon.png");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        scene = new Scene(root);
        scene.getStylesheets().add(style);

        stage.setScene(scene);
        stage.setHeight(512);
        stage.setWidth(512);

        stage.getIcons().add(new Image(icon));
        stage.setTitle("v" + properties.getProperty("version") + " | HumanMouse-Manager");
        stage.show();
    }


}
