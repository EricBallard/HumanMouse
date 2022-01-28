package manager.gui.handlers;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import manager.gui.Controller;
import manager.mouse.MousePath;
import manager.mouse.MousePoint;


public class Renderer {

    public enum State {
        RUNNING, STOPPED, PAUSED
    }

    public State state;

    Thread thread;

    Canvas canvas;

    Controller controller;

    GraphicsContext graphics;

    public Renderer(Controller controller, Canvas canvas) {
        this.controller = controller;
        this.canvas = canvas;

        this.graphics = canvas.getGraphicsContext2D();
        this.graphics.setStroke(Color.GREEN);
        this.graphics.setFill(Color.GREEN);
        this.graphics.setLineWidth(2);

        this.thread = initThread();
        this.state = State.STOPPED;
    }

    Thread initThread() {
        return new Thread(() -> {
            this.state = State.RUNNING;
            this.graphics.clearRect(0, 0, 512, 512);

            while (!Thread.interrupted()) {
                if (state == State.RUNNING)
                    draw();
            }

            this.state = State.STOPPED;
        });
    }

    public synchronized void start() {
        if (this.thread != null) {
            if (this.state == State.STOPPED)
                this.thread.start();

            this.state = State.RUNNING;
        }
    }

    public void pause() {
        if (this.thread != null)
            this.state = State.PAUSED;
    }

    public synchronized void stop() {
        if (this.thread != null)
            this.thread.interrupt();
    }

    public void drawTotalPaths(String fileName) {
        this.graphics.setFill(Color.YELLOW);

        graphics.setFont(new Font("Arial", 12));
        graphics.fillText(fileName, 10, 40);
        graphics.fillText("Total Paths: " + controller.paths.totalPaths, 10, 60);
    }

    private void drawPathInfo(MousePath path) {
        this.graphics.setFill(Color.YELLOW);

        graphics.fillText("#: " + (controller.paths.index + 1) + "/" + controller.paths.totalPaths, 10, 40);

        graphics.fillText("Time: " + path.totalTime, 10, 60);
        graphics.fillText("Points: " + path.totalPoints, 10, 80);

        graphics.fillText("X-Span: " + path.xSpan, 10, 100);
        graphics.fillText("Y-Span: " + path.ySpan, 10, 120);
    }

    /* ~~~~~~~~~~~~~~~~~~ Draw ~~~~~~~~~~~~~~~~~ */

    MousePath path;

    boolean drawnInfo;

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

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
            } catch (InterruptedException e) {
            } finally {
                if (state == State.PAUSED)
                    return;
            }

            // Ignore clear on  last path
            if (controller.paths.index + 1 != controller.paths.totalPaths)
                graphics.clearRect(0, 0, 512, 512);
            drawnInfo = false;
            path = null;
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
        graphics.fillOval(next.ox / 2 - 2, next.oy / 2 - 2, 4, 4);
    }
}
