package manager.gui;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import manager.gui.handlers.Buttons;
import manager.gui.handlers.Renderer;
import manager.mouse.MousePath;
import manager.mouse.PathFinder;

public class Controller implements Initializable {

    /* ~~~~~~~~~~~~ FXML References ~~~~~~~~~~~~ */

    @FXML
    GridPane Tool_Grid;

    @FXML
    ToolBar Tool_Bar;

    @FXML
    MenuButton Files_Btn;

    @FXML
    MenuItem Merge_Btn, Load_Btn, Save_Btn;

    @FXML
    Button Delete_Btn, Previous_Btn, Next_Btn;

    @FXML
    ToggleButton Demo_Btn, Play_Btn, Repeat_Btn;

    @FXML
    Canvas Canvas;

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    public GUI gui;

    public Buttons buttons;

    public MousePath.Paths paths;

    public PathFinder pathFinder;

    public Renderer renderer;

    public Controller(GUI gui) {
        this.gui = gui;
        this.buttons = new Buttons(this);
        this.pathFinder = new PathFinder(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Init Canvas renderer with valid canvas */
        this.renderer = new Renderer(this, this.Canvas);
        this.gui.stage.setOnCloseRequest(e -> this.renderer.stop());

        /* Init Toolbar actions */
        Load_Btn.setOnAction(buttons::loadPaths);

        Merge_Btn.setOnAction(buttons::mergePaths);

        Save_Btn.setOnAction(buttons::savePaths);

        Play_Btn.setOnAction(buttons::playPaths);

        Demo_Btn.setOnAction(buttons::demoPaths);

        Repeat_Btn.setOnAction(e -> renderer.repeat.set(!renderer.repeat.get()));

        Previous_Btn.setOnAction(e -> buttons.adjustIndex(false, false));

        Next_Btn.setOnAction(e -> buttons.adjustIndex(true, false));

        Delete_Btn.setOnAction(e -> buttons.adjustIndex(paths.index == 0, true));

        /* Init Demo actions */
        Canvas.onMouseClickedProperty().set(buttons.setDemoPoint());

        /* Resize */
        gui.stage.widthProperty().addListener(this::resize);
        gui.stage.heightProperty().addListener(e -> Canvas.setHeight(gui.stage.getHeight() - 40));
    }

    void resize(Observable o) {
        double width = gui.stage.getWidth();

        // Canvas
        Canvas.setWidth(width);

        // Toolbar
        Tool_Bar.setMinWidth(width - 15);
        Tool_Grid.setMinWidth(width - 25);

        // Buttons
        double bwidth = width / 12.8D;
        Previous_Btn.setMinWidth(bwidth);
        Next_Btn.setMinWidth(bwidth);

        bwidth = width / 9.3D;
        Demo_Btn.setMinWidth(bwidth);
        Play_Btn.setMinWidth(bwidth);

        Files_Btn.setMinWidth(width / 7.87D);
        Delete_Btn.setMinWidth(width / 5.68D);
        Repeat_Btn.setMinWidth(width / 8.53D);

        // Fonts
        adjustFont((int) (width / 100));
    }

    void adjustFont(int size) {
        Font font = new Font(Files_Btn.getFont().getFamily(), size < 12 ? 12 : size > 16 ? 16 : size);

        Files_Btn.setFont(font);
        Demo_Btn.setFont(font);
        Play_Btn.setFont(font);
        Repeat_Btn.setFont(font);
        Previous_Btn.setFont(font);

        Next_Btn.setFont(font);
        Delete_Btn.setFont(font);
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

    public void setCanvasCursor(Cursor cursor) {
        Canvas.setCursor(cursor);
    }

    public void disableToggleButton(boolean demo) {
        if (demo) {
            Play_Btn.setSelected(false);
            Play_Btn.setText("RESUME");
        } else {
            Demo_Btn.setSelected(false);
            toggleCanvas(false);
        }
    }

    public void toggleCanvas(boolean disabled) {
        //TODO -ensure canvas gets renabled
        //Canvas.setDisable(disabled);
    }
}
