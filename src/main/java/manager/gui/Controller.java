package manager.gui;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import manager.files.Database;
import manager.gui.handlers.Buttons;
import manager.gui.handlers.Renderer;
import manager.mouse.PathFinder;
import manager.mouse.Paths;

import javax.annotation.Nullable;

public class Controller implements Initializable {

    /* ~~~~~~~~~~~~ FXML References ~~~~~~~~~~~~ */

    @FXML
    Canvas Canvas;

    @FXML
    ToolBar Tool_Bar;

    @FXML
    Label Auto_Info, Man_Info;

    @FXML
    MenuButton Files_Btn, Demo_Btn;

    @FXML
    ToggleButton Play_Btn, Repeat_Btn;

    @FXML
    TextField Total_Time, Min_Delay, Max_Delay;

    @FXML
    GridPane Tool_Grid, Settings_Grid, Info_Grid;

    @FXML
    RowConstraints Settings_Cell, Controls_Cell, Info_Cell;

    @FXML
    MenuItem Merge_Btn, Load_Btn, Save_Btn, Pack_Btn, Man_Btn, Auto_Btn;

    @FXML
    Button Delete_Btn, Previous_Btn, Next_Btn, Start_Debug, Cancel_Debug;


    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    public GUI gui;

    public Paths paths;

    public Buttons buttons;

    public PathFinder pathFinder;

    public Renderer renderer;

    public Database database;

    public boolean manInfoOpen, autoInfoOpen;

    public Controller(GUI gui) {
        this.gui = gui;
        this.database = new Database();
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

        Pack_Btn.setOnAction(buttons::packPaths);

        Play_Btn.setOnAction(buttons::playPaths);

        Auto_Btn.setOnAction(buttons::autoDebug);

        Man_Btn.setOnAction(buttons::manualDebug);

        Start_Debug.setOnAction(buttons::startDebug);

        Cancel_Debug.setOnAction(this::hideInfo);

        Repeat_Btn.setOnAction(e -> renderer.repeat.set(!renderer.repeat.get()));

        Previous_Btn.setOnAction(e -> buttons.adjustIndex(false, false));

        Next_Btn.setOnAction(e -> buttons.adjustIndex(true, false));

        Delete_Btn.setOnAction(e -> buttons.adjustIndex(paths.index == 0, true));

        /* Init Demo actions */
        Canvas.onMouseClickedProperty().set(buttons.setDemoPoint());

        /* Resize */
        gui.stage.widthProperty().addListener(this::resize);
        gui.stage.heightProperty().addListener(e -> Canvas.setHeight(gui.stage.getHeight() - 40));

        /* Settings */
        UnaryOperator<TextFormatter.Change> textFilter = e -> {
            String text = e.getText();
            return text.matches("[0-9]*") ? e : null;
        };

        // Only allow numeric input (each require unique formatters)
        Total_Time.setTextFormatter(new TextFormatter<>(textFilter));
        Min_Delay.setTextFormatter(new TextFormatter<>(textFilter));
        Max_Delay.setTextFormatter(new TextFormatter<>(textFilter));
    }

    void resize(Observable o) {
        Platform.runLater(() -> {
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
            int size = (int) (width / 100);
            Font font = new Font(Files_Btn.getFont().getFamily(), size < 12 ? 12 : Math.min(size, 16));

            Files_Btn.setFont(font);
            Demo_Btn.setFont(font);
            Play_Btn.setFont(font);
            Repeat_Btn.setFont(font);
            Previous_Btn.setFont(font);

            Next_Btn.setFont(font);
            Delete_Btn.setFont(font);

            // Context Menus
            bwidth = Math.max(45, width / 7.87D - 30);
            String style = "-fx-pref-width: " + bwidth + "px;" +
                    "-fx-font-size: " + (font.getSize() - 1) + ";";

            Merge_Btn.setStyle(style);
            Load_Btn.setStyle(style);
            Save_Btn.setStyle(style);

            bwidth = Math.max(45, width / 9.3D - 30);
            style = "-fx-pref-width: " + bwidth + "px;" +
                    "-fx-font-size: " + (font.getSize() - 1) + ";";

            Auto_Btn.setStyle(style);
            Man_Btn.setStyle(style);
        });
    }

    public void setPaths(Paths paths) {
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
        Pack_Btn.setDisable(disabled);
        Auto_Btn.setDisable(disabled);
        Man_Btn.setDisable(disabled);
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
            toggleCanvas(false);
        }
    }

    public void toggleCanvas(boolean disabled) {
        //TODO -ensure canvas gets renabled
        //Canvas.setDisable(disabled);
    }

    public void disabled(boolean state) {
        this.gui.scene.setCursor(state ? Cursor.WAIT : Cursor.DEFAULT);
        Tool_Bar.setDisable(state);
        toggleCanvas(state);
    }

    public void hideInfo(ActionEvent ignored) {
        manInfoOpen = false;
        autoInfoOpen = false;
        Info_Grid.setVisible(false);
    }

    public void showInfo(boolean autoDebug) {
        configureGrid(autoDebug);
        Info_Grid.setVisible(true);
        Start_Debug.requestFocus();
    }

    void configureGrid(boolean autoDebug) {
        // Label
        Man_Info.setVisible(!autoDebug);
        Auto_Info.setVisible(autoDebug);

        // Resize row constraints (hide/show settings_grid
        Info_Cell.setPercentHeight(autoDebug ? 45 : 60);
        Controls_Cell.setPercentHeight(autoDebug ? 20 : 25);
        Settings_Cell.setPercentHeight(autoDebug ? 30 : 0);
        Settings_Grid.setVisible(autoDebug);

        // Re-size info grid
        double w = gui.stage.getWidth(),
                infoW = w - (w / 3);

        Info_Grid.setMaxHeight(autoDebug ? infoW : infoW - (infoW * .30));
    }

    public void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.initStyle(StageStyle.TRANSPARENT);
        alert.initOwner(gui.stage);
        alert.setResizable(false);
        alert.show();
    }

    @Nullable
    public Pair<Integer, Pair<Integer, Integer>> getDebugSettings() {
        int total, min, max;

        try {
            total = Integer.parseInt(Total_Time.getText());
            min = Integer.parseInt(Min_Delay.getText());
            max = Integer.parseInt(Max_Delay.getText());
        } catch (NumberFormatException ignored) {
            hideInfo(null);
            return null;
        }

        return new Pair<>(total, new Pair<>(min, max));
    }
}
