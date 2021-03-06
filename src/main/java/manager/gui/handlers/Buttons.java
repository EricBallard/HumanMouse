package manager.gui.handlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import manager.files.Files;
import manager.gui.Controller;
import manager.mouse.MousePoint;

import java.io.File;
import java.util.List;

public class Buttons {

    Controller controller;

    public Buttons(Controller controller) {
        this.controller = controller;
    }

    public void mergePaths(ActionEvent ignored) {
        FileChooser files = Files.getChooser("Merge", false);
        List<File> selected = files.showOpenMultipleDialog(controller.gui.stage);

        if (selected != null) Files.merge(selected, controller);
    }

    public void loadPaths(ActionEvent ignored) {
        FileChooser files = Files.getChooser("Load", false);
        File selected = files.showOpenDialog(controller.gui.stage);

        if (selected != null) Files.load(selected, controller);
    }

    public void savePaths(ActionEvent ignored) {
        FileChooser files = Files.getChooser("Save", false);
        File selected = files.showSaveDialog(controller.gui.stage);

        if (selected != null) Files.save(selected, controller);
    }

    public void packPaths(ActionEvent ignored) {
        FileChooser files = Files.getChooser("Save", true);
        File selected = files.showSaveDialog(controller.gui.stage);

        if (selected != null) controller.database.pack(selected, controller);
    }

    public void playPaths(ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();

        if (btn.isSelected()) {
            // Re/Start/Resume
            controller.togglePathButtons(true);
            controller.disableToggleButton(false);

            // Stop debug
            controller.renderer.toggleManualDebug(false);
            controller.renderer.toggleAutoDebug(false);

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
        index = controller.paths.totalPaths == 1 ? 0 : forward && delete ? index : (forward ? index + 1 : index - 1);

        controller.paths.index = index;
        controller.paths.list.get(index).index = 0;

        // Reset renderer draw path in index
        controller.renderer.reset();
        controller.renderer.drawPathInfo(controller.paths.list.get(index));
    }


    public EventHandler<? super MouseEvent> setDemoPoint() {
        return (EventHandler<MouseEvent>) e -> {
            if (!controller.renderer.manualDebug.get())
                return;

            boolean pointA = e.getButton() == MouseButton.PRIMARY;

            boolean reset = pointA ? controller.pathFinder.start != null
                    : controller.pathFinder.end != null;

            int x = (int) e.getX(), y = (int) e.getY();

            // Reset
            if (reset) {
                controller.pathFinder.reset();
                controller.renderer.clear();
            }

            // Set point
            controller.pathFinder.setPoint(pointA, new MousePoint(x, y));

            // Draw point
            controller.renderer.drawPoint(pointA, x, y);

            // Find path
            if (controller.pathFinder.pointsSet())
                controller.pathFinder.execute(false);
        };
    }

    public void manualDebug(ActionEvent ignored) {
        if (!controller.renderer.manualDebug.get()) {
            controller.togglePathButtons(true);

            if (controller.renderer.state.get() == Renderer.State.RUNNING) {
                controller.disableToggleButton(true);
                controller.renderer.pause();
            }

            // Start
            controller.manInfoOpen = true;
            controller.showInfo(false);
        } else {
            // Stop
            controller.renderer.toggleManualDebug(false);
        }
    }

    public void autoDebug(ActionEvent ignored) {
        if (!controller.renderer.autoDebug.get()) {
            controller.togglePathButtons(true);

            if (controller.renderer.state.get() == Renderer.State.RUNNING) {
                controller.disableToggleButton(true);
                controller.renderer.pause();
            }


            controller.autoInfoOpen = true;
            controller.showInfo(true);
        } else {
            // Stop
            controller.renderer.toggleAutoDebug(false);
        }
    }

    public void startDebug(ActionEvent ignored) {
        if (controller.manInfoOpen) controller.renderer.toggleManualDebug(true);
        else controller.renderer.toggleAutoDebug(true);

        controller.hideInfo(null);
    }
}