package manager.gui.handlers;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
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
        dialog.setResizable(false);
    }

    @Nullable
    public static Input getDebugSettings(Controller controller) {
        // Init dialog, config
         Dialog<Input> dialog = new Dialog<>();

        // Define title/header
 //       dialog.setTitle("HumanMouse-Manager | AUTO-DEBUG");
//        dialog.setHeaderText("Enter the total time for test, and the min/max" +
//                "amount of time between every path. Points for each" +
//                "path are set at random, if a path is not found the" +
//                "test will stop with related information displayed.");

        // Create input nodes
        Label timeLbl = new Label("TOTAL: "),
                minLbl = new Label("MIN: "),
                maxLbl = new Label("MAX: ");

        Color c = Color.valueOf("#252525");
        timeLbl.setTextFill(c);
        minLbl.setTextFill(c);
        maxLbl.setTextFill(c);

        TextField time = new TextField();
        TextField min = new TextField();
        TextField max = new TextField();

        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        for (int i = 0; i < 4; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(20);
            rc.setMinHeight(20);

            grid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHalignment(HPos.RIGHT);
            cc.setPercentWidth(i <= 1 ? 10 : 30);

            grid.getColumnConstraints().add(cc);
        }

        grid.setMaxWidth(50);
        grid.add(timeLbl, 0, 0);
        grid.add(time, 2, 0);

        grid.add(minLbl, 0, 2);
        grid.add(min, 2, 2);

        grid.add(maxLbl, 0, 4);
        grid.add(max, 2, 4);

        // Define content
        dialog.getDialogPane().setContent(grid);


        dialog.getDialogPane().setStyle("-fx-background-color: green;");

        // Add confirm/cancel button
        ButtonType confirm = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);

        // dialog.getDialogPane().getButtonTypes().add(confirm);

        style(dialog, controller);




        // Handle action
        dialog.setResultConverter(b -> b != confirm ? null : valueOf(time.getText(), min.getText(), max.getText()));

        // Wait for user - Process result, or lack of
        return dialog.showAndWait().stream().findFirst().orElse(null);
    }
}
