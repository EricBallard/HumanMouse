package manager.gui.handlers;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;
import manager.gui.Controller;
import manager.mouse.MousePath;
import manager.mouse.MousePoint;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class Renderer {

    public enum State {
        RUNNING, STOPPED, PAUSED, FINISHED
    }

    public AtomicReference<State> state;

    public AtomicBoolean repeat;

    public AtomicBoolean manualDebug, autoDebug;

    boolean drawnInfo;

    MousePath path;

    Thread thread;

    Canvas canvas;

    Controller controller;

    GraphicsContext graphics;

    public Renderer(Controller controller, Canvas canvas) {
        this.state = new AtomicReference<>(State.STOPPED);

        this.repeat = new AtomicBoolean(false);
        this.autoDebug = new AtomicBoolean(false);
        this.manualDebug = new AtomicBoolean(false);

        this.controller = controller;
        this.canvas = canvas;

        this.graphics = canvas.getGraphicsContext2D();
        this.graphics.setFill(Color.YELLOW);

        this.thread = initThread();
    }

    Thread initThread() {
        return new Thread(() -> {
            this.state.set(State.RUNNING);
            this.clear();

            while (!Thread.interrupted()) {
                if (state.get() == State.RUNNING)
                    draw();
            }

            this.state.set(State.STOPPED);
        });
    }

    public void start() {
        if (this.thread != null) {
            if (this.state.get() == State.STOPPED)
                this.thread.start();

            this.state.set(State.RUNNING);
        }
    }

    public void pause() {
        if (this.thread != null && this.state.get() != State.FINISHED)
            this.state.set(State.PAUSED);
    }

    public void stop() {
        if (this.thread != null)
            this.thread.interrupt();
    }

    public void toggleManualDebug(boolean enabled) {
        manualDebug.set(enabled);

        if (!enabled) {
            graphics.setFill(Color.YELLOW);
            controller.paths.index = 0;
        }

        controller.renderer.clear();
        controller.setCanvasCursor(enabled ? Cursor.CROSSHAIR : Cursor.DEFAULT);
    }

    MousePoint getRandom(Bounds bounds) {
        // Generate random point
        ThreadLocalRandom ran = ThreadLocalRandom.current();

        int ranX = (int) ran.nextDouble(bounds.getMinX(), bounds.getMaxX()),
                ranY = (int) ran.nextDouble(bounds.getMinY(), bounds.getMaxY());

        return new MousePoint(ranX, ranY);
    }

    public void toggleAutoDebug(boolean enabled) {
        autoDebug.set(enabled);
        controller.renderer.clear();

        if (enabled) {
            Pair<Integer, Pair<Integer, Integer>> settings = controller.getDebugSettings();

            if (settings != null && settings.getKey() > 0) {
                // START - Auto
                Pair<Integer, Integer> interval = settings.getValue();

                // Auto-Debug Thread
                new Thread(() -> {
                    final long start = Instant.now().toEpochMilli();
                    final Bounds bounds = controller.getSize();

                    final int runTime = settings.getKey(), minDelay = interval.getKey(), maxDelay = interval.getValue();
                    int foundPaths = 0;

                    System.out.println("DEBUG |  " + settings.getKey() + ", " + "[" + minDelay + ", " + maxDelay + "]");

                    while (!Thread.interrupted()
                            && controller.gui.stage.isShowing()) {

                        // Success - Log info
                        int runMinutes = (int) (((Instant.now().toEpochMilli() - start) / 1000) / 60);
                        String msg = "Used " + foundPaths + " paths\nTesting for " + runMinutes + " minutes";

                        if (runMinutes >= runTime) {
                            drawText("Auto-Debug | Completed", 80);
                            break;
                        }

                        System.out.println(msg);
                        drawText(msg, 30);

                        // Generate 2 random points
                        MousePoint spoint = getRandom(bounds), epoint = getRandom(bounds);

                        // Paint them on canvas
                        drawPoint(true, spoint.ox, spoint.oy);
                        drawPoint(false, epoint.ox, epoint.oy);

                        // Generate path between points
                        controller.pathFinder.setPoint(true, spoint);
                        controller.pathFinder.setPoint(false, epoint);

                        if (!controller.pathFinder.execute(true))
                            // Failed to find path - stop debugging
                            break;

                        foundPaths++;

                        // Random delay interval
                        try {
                            Thread.sleep((long)
                                    ThreadLocalRandom.current().nextDouble(minDelay, maxDelay));
                        } catch (InterruptedException ignored) {
                            System.out.println("DEBUG | Failed to delay between paths, stopping...");
                            break;
                        }

                        clear();
                    }

                    // Stop
                    System.out.println("DEBUG | STOPPED!!!");
                }).start();
                return;
            } else {
                // Bad/No input - disable
                drawText("PLEASE ENTER SETTINGS", 20);
                autoDebug.set(false);
            }
        }

        graphics.setFill(Color.YELLOW);
        controller.paths.index = 0;
    }

    public void drawText(String text, int y) {
        graphics.setFill(Color.DEEPPINK);
        graphics.fillText(text, 10, y);
    }

    public void drawTotalPaths(String fileName) {
        clear();
        graphics.setFont(new Font("Arial", 12));

        graphics.fillText(fileName, 10, 60);
        graphics.fillText("Total Paths: " + controller.paths.totalPaths, 10, 80);
    }

    public void drawPathInfo(MousePath path) {
        graphics.setFill(Color.YELLOW);
        graphics.fillText("#: " + (controller.paths.index + 1) + "/" + controller.paths.totalPaths, 10, 100);

        graphics.fillText("Time: " + path.totalTime, 10, 120);
        graphics.fillText("Points: " + path.totalPoints, 10, 140);

        graphics.fillText("X-Span: " + path.xSpan, 10, 160);
        graphics.fillText("Y-Span: " + path.ySpan, 10, 180);
    }

    public void highlightPoint(MousePoint p, Color c) {
        graphics.setFill(c);
        graphics.fillOval(p.ox - 2, p.oy - 2, 4, 4);
    }

    public void drawPoint(MousePoint p, Color c) {
        graphics.setFill(c);
        graphics.fillOval(p.ox - 1, p.oy - 1, 2, 2);
    }

    public void drawPoint(MousePoint p) {
        graphics.fillOval(p.ox - 1, p.oy - 1, 2, 2);
    }

    public void drawPoint(boolean a, double x, double y) {
        if (y < 30)
            return;

        graphics.setFill(a ? Color.YELLOW : Color.PURPLE);
        graphics.fillOval(x - 5, y - 5, 10, 10);
    }

    public void clear() {
        graphics.clearRect(0, 0,
                controller.gui.stage.getWidth(), controller.gui.stage.getHeight());
    }

    public void reset() {
        drawnInfo = true;
        repeated = false;
        path = null;

        clear();
    }

    boolean repeated = false;

    public void draw() {
        MousePoint next;

        // Get next path
        if (path == null
                && (path = controller.paths.getNext()) == null) {
            return;
            // Get next point in path
        } else if ((next = path.getNext()) == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            // Honor pause
            if (state.get() == State.PAUSED)
                return;
                // Honor repeat
            else if (repeat.get()) {
                if (!repeated) {
                    repeated = true;
                    drawnInfo = false;
                    path.index = 0;
                    clear();
                    return;
                }
            }

            // Ignore clear on  last path
            if (controller.paths.index >= controller.paths.totalPaths - 1) {
                state.set(State.FINISHED);
                controller.resetPlayButtton();
            } else
                this.clear();

            drawnInfo = false;
            repeated = false;
            path = null;

            controller.paths.index++;
            return;
        }

        // Draw path info
        if (!drawnInfo) {
            drawPathInfo(path);
            drawnInfo = true;
        }

        // Delay
        if (next.delay > 0) {
            try {
                Thread.sleep(next.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Failed to sleep during path playback due to, " + e.getMessage());
            }
        }

        // Draw points
        drawPoint(next);
        path.index++;
    }
}
