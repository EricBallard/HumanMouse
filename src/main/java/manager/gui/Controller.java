package manager.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.canvas.Canvas;
import manager.gui.handlers.Buttons;
import manager.gui.handlers.Renderer;
import manager.mouse.MousePath;

public class Controller implements Initializable {

    /* ~~~~~~~~~~~~ FXML References ~~~~~~~~~~~~ */

    @FXML
    Button Save_Btn, Load_Btn, Delete_Btn, Previous_Btn, Next_Btn;

    @FXML
    ToggleButton Demo_Btn, Play_Btn, Repeat_Btn;

    @FXML
    Canvas Canvas;

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    GUI gui;

    Buttons buttons;

    public MousePath.Paths paths;

    public Renderer renderer;

    public Controller(GUI gui) {
        this.gui = gui;
        this.buttons = new Buttons(this);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Init Canvas renderer with valid canvas */
        this.renderer = new Renderer(this, this.Canvas);
        this.gui.stage.setOnCloseRequest(e -> this.renderer.stop());

        /* Init Toolbar actions */
        Save_Btn.setOnAction(buttons::openFileSaver);

        Load_Btn.setOnAction(buttons::openFileLoader);

        Play_Btn.setOnAction(buttons::playPaths);

        Repeat_Btn.setOnAction(e -> renderer.repeat.set(!renderer.repeat.get()));

        Previous_Btn.setOnAction(e -> buttons.adjustIndex(false, false));

        Next_Btn.setOnAction(e -> buttons.adjustIndex(true, false));

        Delete_Btn.setOnAction(e -> buttons.adjustIndex(paths.index == 0, true));
    }

    public void setPaths(MousePath.Paths paths) {
        this.paths = paths;
    }

    public void rewindPaths() {
        this.paths.index = 0;
        this.paths.list.forEach(path -> path.index = 0);
        this.renderer.state.set(Renderer.State.PAUSED);
        this.renderer.clear();
    }

    public void resetPlayButtton() {
        Platform.runLater(() -> {
            Play_Btn.setSelected(false);
            Play_Btn.setText("RESTART");
        });
    }

    public void toggleToolbarButtons(boolean disabled, String fileName) {
        Save_Btn.setDisable(disabled);
        Demo_Btn.setDisable(disabled);
        Play_Btn.setDisable(disabled);
        Repeat_Btn.setDisable(disabled);

        if (!disabled)
            Play_Btn.requestFocus();

        renderer.drawTotalPaths(fileName);
    }

    public void togglePathButtons(boolean disabled) {
        Previous_Btn.setDisable(disabled);
        Next_Btn.setDisable(disabled);
        Delete_Btn.setDisable(disabled);
    }
}
