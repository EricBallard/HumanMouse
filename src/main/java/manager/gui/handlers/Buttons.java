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

        if (selected != null) Files.load(selected, controller);
    }


    public void playPaths(ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();

        if (btn.isSelected()) {
            controller.togglePathButtons(true);

            // Re/Start/Resume
            if (controller.renderer.state.get() == Renderer.State.FINISHED) controller.rewindPaths();

            controller.renderer.start();
            btn.setText("PAUSE");
        } else {
            // Pause
            controller.renderer.pause();
            btn.setText(controller.renderer.state.get() == Renderer.State.FINISHED ? "RESTART" : "RESUME");

            controller.togglePathButtons(false);
        }
    }

    public void previousPath(ActionEvent e) {
        int index = controller.paths.index;
        if (index == 0) return;

        // Reset current path
        controller.paths.list.get(index).index = 0;

        // Back step index and reset target path
        index--;
        controller.paths.index = index;
        controller.paths.list.get(index).index = 0;

        // Reset renderer draw path in index
        controller.renderer.reset();
        controller.renderer.drawPathInfo(controller.paths.list.get(index));
    }

    public void nextPath(ActionEvent e) {
        int index = controller.paths.index;
        if (index == controller.paths.totalPaths - 1) return;

        // Forward step index and reset target path
        index++;
        controller.paths.index = index;
        controller.paths.list.get(index).index = 0;

        // Reset renderer draw path in index
        controller.renderer.reset();
        controller.renderer.drawPathInfo(controller.paths.list.get(index));
    }

    public void deletePath(ActionEvent e) {
    }
}