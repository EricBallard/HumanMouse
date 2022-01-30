package manager.gui.handlers;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import manager.gui.Controller;
import manager.mouse.MousePath;
import manager.mouse.MousePoint;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class Renderer {

    public enum State {
        RUNNING, STOPPED, PAUSED, FINISHED
    }

    public AtomicReference<State> state;

    public AtomicBoolean repeat;

    public AtomicBoolean demo;

    boolean drawnInfo;

    MousePath path;

    Thread thread;

    Canvas canvas;

    Controller controller;

    GraphicsContext graphics;

    public Renderer(Controller controller, Canvas canvas) {
        this.state = new AtomicReference<>(State.STOPPED);
        this.repeat = new AtomicBoolean(false);
        this.demo = new AtomicBoolean(false);

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

    public synchronized void start() {
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

    public synchronized void stop() {
        if (this.thread != null)
            this.thread.interrupt();
    }

    public void toggleDemo(boolean enabled) {
        this.demo.set(enabled);

        if (!enabled)
            this.graphics.setFill(Color.YELLOW);
    }

    public void drawTotalPaths(String fileName) {
        clear();

        graphics.setFont(new Font("Arial", 12));
        graphics.fillText(fileName, 10, 40);
        graphics.fillText("Total Paths: " + controller.paths.totalPaths, 10, 60);
    }

    public void drawPathInfo(MousePath path) {
        graphics.fillText("#: " + (controller.paths.index + 1) + "/" + controller.paths.totalPaths, 10, 40);

        graphics.fillText("Time: " + path.totalTime, 10, 60);
        graphics.fillText("Points: " + path.totalPoints, 10, 80);

        graphics.fillText("X-Span: " + path.xSpan, 10, 100);
        graphics.fillText("Y-Span: " + path.ySpan, 10, 120);
    }

    public void drawPoint(boolean a, double x, double y) {
        if (y < 30)
            return;

        graphics.setFill(a ? Color.YELLOW : Color.PURPLE);
        graphics.fillOval(x - 5, y - 5, 10, 10);
    }

    public void clear() {
        this.graphics.clearRect(0, 0, 512, 512);
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
        drawCircle(next.ox, next.oy, true);
        path.index++;
    }

    public void drawCircle(int x, int y, boolean adjust) {
        graphics.fillOval((adjust ? x / 2 : x) - 2, (adjust ? y / 2 : y) - 2, 4, 4);
    }
}
