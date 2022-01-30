package manager.gui.handlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
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

public class Buttons {

    boolean shownInfo;

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
        File selected = files.showSaveDialog(controller.gui.stage);

        if (selected != null) Files.save(selected, controller);
    }

    public void openFileLoader(ActionEvent e) {
        FileChooser files = getFileChooser("Load");
        File selected = files.showOpenDialog(controller.gui.stage);

        if (selected != null) Files.load(selected, controller);
    }

    public void playPaths(ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();

        if (btn.isSelected()) {
            // TODO - stop demo  or something?

            // Re/Start/Resume
            controller.togglePathButtons(true);
            controller.disableToggleButton(false);
            controller.renderer.toggleDemo(false);

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

    public void demoPaths(ActionEvent e) {
        boolean selected = ((ToggleButton) e.getSource()).isSelected();

        if (selected) {
            controller.togglePathButtons(true);

            if (controller.renderer.state.get() == Renderer.State.RUNNING) {
                controller.disableToggleButton(true);
                controller.renderer.pause();
            }

            // Start
            showInfo();
            controller.renderer.clear();
            controller.setCanvasCursor(Cursor.CROSSHAIR);
            controller.renderer.toggleDemo(true);
        } else {
            // Stop
            controller.renderer.clear();
            controller.setCanvasCursor(Cursor.DEFAULT);
            controller.renderer.toggleDemo(false);
        }
    }

    void showAlert(Dialog dialog) {
        dialog.initStyle(StageStyle.TRANSPARENT);
        Stage mainStage = controller.gui.stage;

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(mainStage.getScene().getStylesheets().get(0));


        dialog.show();
        shownInfo = true;
    }

    void showInfo() {
        if (shownInfo) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HumanMouse-Manager | DEMO");
        alert.setHeaderText("For your information");
        alert.setContentText("Left-Click to set 1st point,\nRight-Click to set 2nd point.\n\n" + "A path will be built and drawn between these points,clicking either button\n" + "again will start a new path!");

        alert.initOwner(controller.gui.stage);
        showAlert(alert);
    }

    public EventHandler<? super MouseEvent> setDemoPoint() {
        return (EventHandler<MouseEvent>) e -> {
            if (!controller.renderer.demo.get())
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
}