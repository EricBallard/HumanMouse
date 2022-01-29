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

    FileChooser getFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        chooser.setInitialDirectory(Files.directory);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Json Files", "*.json"));
        return chooser;
    }

    public void openFileSaver(ActionEvent e) {
        FileChooser files = getFileChooser("Save");
        File selected = files.showSaveDialog(null);

        if (selected != null) Files.save(selected, controller);
    }
    public void openFileLoader(ActionEvent e) {
        FileChooser files = getFileChooser("Load");
        File selected = files.showOpenDialog(null);

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

    public void adjustIndex(boolean forward, boolean delete) {
        int index = controller.paths.index;
        if (forward ? index == controller.paths.totalPaths - 1 : index == 0) return;

        // Remove path
        if (delete) {
            controller.paths.totalPaths--;
            controller.paths.list.remove(index);
        } else {
            // Reset current path
            controller.paths.list.get(index).index = 0;
        }

        // Step index and reset target path
        index = controller.paths.totalPaths == 1 ? 0 :
                forward && delete ? index : (forward ? index + 1 : index - 1);

        controller.paths.index = index;
        controller.paths.list.get(index).index = 0;

        // Reset renderer draw path in index
        controller.renderer.reset();
        controller.renderer.drawPathInfo(controller.paths.list.get(index));
    }

}