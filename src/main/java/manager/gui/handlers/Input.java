package manager.gui.handlers;

import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import manager.gui.Controller;

import javax.annotation.Nullable;

public class Input {

    int time, min, max;

    public Input(int time, int min, int max) {
        this.time = time;
        this.min = min;
        this.max = max;
    }

    @Nullable
    static Input valueOf(String time, String min, String max) {
        try {
            int iTime = Integer.parseInt(time),
                    iMin = Integer.parseInt(min),
                    iMax = Integer.parseInt(max);
            return new Input(iTime, iMin, iMax);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public static void style(Dialog dialog, Controller controller) {
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(controller.gui.stage);
    }

    @Nullable
    public static Input getDebugSettings(Controller controller) {
        // Init dialog, config
        Dialog<Input> dialog = new Dialog<>();
        dialog.setResizable(false);
        style(dialog, controller);

        // Define title/header
        dialog.setTitle("HumanMouse-Manager | AUTO-DEBUG");
        dialog.setHeaderText("Enter the total time for test, and the min/max amount of time between\n" +
                             "every path. Points for each path are set at random, if a path is not\n" +
                             "found the test will stop with related information displayed. ");

        // Create input nodes
        Label timeLbl = new Label("TOTAL: ");
        Label minLbl = new Label("MIN: ");
        Label maxLbl = new Label("MAX: ");

        timeLbl.setTextFill(Color.WHITE);
        minLbl.setTextFill(Color.WHITE);
        maxLbl.setTextFill(Color.WHITE);

        TextField time = new TextField();
        TextField min = new TextField();
        TextField max = new TextField();

        GridPane grid = new GridPane();
        grid.add(timeLbl, 1, 1);
        grid.add(time, 2, 1);

        grid.add(minLbl, 1, 2);
        grid.add(min, 2, 2);

        grid.add(maxLbl, 1, 3);
        grid.add(max, 2, 3);

        // Define content
        dialog.getDialogPane().setContent(grid);

        // Add confirm/cancel button
        ButtonType confirm = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(confirm);

        // Handle action
        dialog.setResultConverter(b -> b != confirm ? null : valueOf(time.getText(), min.getText(), max.getText()));

        // Wait for user - Process result, or lack of
        return dialog.showAndWait().stream().findFirst().orElse(null);
    }
}
