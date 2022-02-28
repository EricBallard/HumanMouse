package manager.gui.handlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import manager.files.Files;
import manager.gui.Controller;
import manager.mouse.MousePoint;

import java.io.File;
import java.util.List;

public class Buttons {

    boolean shownManualInfo;

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

    public void packPaths(ActionEvent e) {
        FileChooser files = Files.getChooser("Save", true);
        File selected = files.showSaveDialog(controller.gui.stage);

        if (selected != null) controller.database.pack(selected, controller);
    }

    public void playPaths(ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();

        if (btn.isSelected()) {
            // TODO - stop demo  or something?

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

    public void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Input.style(alert, controller);
        alert.show();
    }

    public EventHandler<? super MouseEvent> setDemoPoint() {
        return (EventHandler<MouseEvent>) e -> {
            if (!controller.renderer.manualDebug.get())
                return;

            boolean pointA = e.getButton() == MouseButton.PRIMARY;

            boolean reset = pointA ? controller.pathFinder.start.get() != null
                    : controller.pathFinder.end.get() != null;

            int x = (int) e.getX(), y = (int) e.getY();

            // Reset
            if (reset) {
                controller.pathFinder.reset();
                controller.renderer.clear();
            }

            // Set point
            if (pointA)
                controller.pathFinder.start.set(new MousePoint(x, y));
            else
                controller.pathFinder.end.set(new MousePoint(x, y));

            // Draw point
            controller.renderer.drawPoint(pointA, x, y);

            // Find path
            if (controller.pathFinder.pointsSet())
                controller.pathFinder.execute();
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
            if (!shownManualInfo) {
                shownManualInfo = true;
                showInfo("HumanMouse-Manager | DEBUG", "For your information",
                        "Left-Click to set 1st point,\nRight-Click to set 2nd point.\n\n"
                                + "A path will be built and drawn between these points,clicking either button\n"
                                + "again will start a new path!");
            }

            controller.renderer.toggleManualDebug(true);
        } else {
            // Stop
            controller.renderer.toggleManualDebug(false);
        }
    }

    public void autoDebug(ActionEvent e) {
        if (!controller.renderer.autoDebug.get()) {
            controller.togglePathButtons(true);

            if (controller.renderer.state.get() == Renderer.State.RUNNING) {
                controller.disableToggleButton(true);
                controller.renderer.pause();
            }

            controller.renderer.toggleAutoDebug(true);
        } else {
            // Stop
            controller.renderer.toggleAutoDebug(false);
        }
    }
}