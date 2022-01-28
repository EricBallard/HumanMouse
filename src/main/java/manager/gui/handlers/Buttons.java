package manager.gui.handlers;

import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import javafx.stage.FileChooser;
import manager.files.Files;
import manager.gui.Controller;

import java.io.File;

public class Buttons {

    Controller controller;

    public Buttons(Controller controller) {
        this.controller = controller;
    }

    public void openFileChooser(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(Files.directory);
        chooser.setTitle("Select your mouse_paths.json");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Json Files", "*.json"));

        File selected = chooser.showOpenDialog(null);

        if (selected != null)
            Files.load(selected, controller);
    }


    public void playPaths(ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();

        if (btn.isSelected()) {
            // Start/Resume
            controller.renderer.start();
        } else {
            // Pause
            controller.renderer.pause();
        }
    }
}