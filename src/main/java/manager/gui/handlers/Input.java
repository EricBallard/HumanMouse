package manager.gui.handlers;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.util.Optional;

public class Input {

    int time, min, max;

    public Input(int time, int min, int max) {
        this.time = time;
        this.min = min;
        this.max = max;
    }

    public void getDebugSettings() {
        Dialog<Input> dialog = new Dialog<>();
        dialog.setResizable(false);
        dialog.setTitle("HumanMouse-Manager | AUTO-DEBUG");

        dialog.setHeaderText("This is a custom dialog. Enter info and \n" +
                "press Okay (or click title bar 'X' for cancel).");

        Label label1 = new Label("Name: ");
        Label label2 = new Label("Phone: ");
        TextField text1 = new TextField();
        TextField text2 = new TextField();

        GridPane grid = new GridPane();
        grid.add(label1, 1, 1);
        grid.add(text1, 2, 1);
        grid.add(label2, 1, 2);
        grid.add(text2, 2, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                //new Input(text1.getText(), text2.getText())
            }

            return null;
        });

        Optional<Input> result = dialog.showAndWait();

        if (result.isPresent()) {
            System.out.println("Result: " + result.get());
        }
    }
}
