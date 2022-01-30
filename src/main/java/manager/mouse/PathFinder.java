package manager.mouse;

import manager.gui.Controller;
import manager.gui.handlers.Renderer;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class PathFinder {

    Controller controller;

    public AtomicReference<MousePoint> start, end;

    public PathFinder(Controller controller) {
        this.controller = controller;
        this.start = new AtomicReference(null);
        this.end = new AtomicReference(null);
    }

    public void reset() {
        this.start.set(null);
        this.end.set(null);
    }

    public boolean pointsSet() {
        return this.start.get() != null && this.end.get() != null;
    }

    final static int xSpanThreshold = 25, ySpanThreshold = 25;

    // TODO - human random
    Random ran = new Random();

    @Nullable // Find path from start -> end
    public MousePath get() {
        System.out.println("Finding pathing..");
        long startTime = Instant.now().toEpochMilli();

        // Get set points
        MousePoint spoint = start.get(), epoint = end.get();

        // Calculate span
        int xspan = epoint.ox - spoint.ox, yspan = epoint.oy - spoint.oy;

        System.out.println("Target= xSpan: " + xspan + " | ySpan: " + yspan);
        ArrayList<MousePath> potentials = new ArrayList<>();

        // Find 3 potential paths near target x/y span
        for (MousePath path : controller.paths.list) {
            int xSpanDif = Math.abs(path.xSpan - xspan);

            if (xSpanDif <= xSpanThreshold) {
                // X-Span meets requirement
                int ySpanDif = Math.abs(path.ySpan - yspan);

                if (ySpanDif <= ySpanThreshold) {
                    // Y-Span meets requirement
                    potentials.add(path);

                    if (potentials.size() == 3) break;
                }
            }
        }

        // Log time
        long elapsedTime = startTime - Instant.now().toEpochMilli();
        System.out.println("TIME: " + elapsedTime + "ms");

        int found = potentials.size();
        System.out.println("POTENTIAL PATHS: " + found);

        return found == 0 ? null : found == 1 ? potentials.get(0) : potentials.get(ran.nextInt(found - 1));
    }

    boolean drawnInfo;
    MousePath path;

    public void execute() {
        // Get human path that resembles our needed x/y span
        if (path == null && (path = this.get()) == null) return;

        // Rebuild path for target coordinates
        final MousePoint spoint = start.get(), epoint = end.get();

        MousePath rebuild = new MousePath();
        rebuild.add(spoint);

        MousePoint last = null;
        for (MousePoint p : path.points) {
            int ax = (last == null ? spoint.ox : last.ox) + p.x, ay = (last == null ? spoint.oy : last.oy) + p.y;
            MousePoint ap = new MousePoint(ax, ay, p.delay);
            rebuild.add(ap);
            last = ap;
        }

        rebuild.add(epoint);
        rebuild.calculate();
        path = rebuild;

        new Thread(() -> {
            while (!Thread.interrupted()) {
                // Draw path
                MousePoint next;
                if ((next = path.getNext()) == null) {
                    System.out.println(" PATH is drawn");
                    drawnInfo = false;
                    path = null;
                    break;
                }

                // Draw path info
                if (!drawnInfo) {
                    controller.renderer.drawPathInfo(path);
                    drawnInfo = true;
                }

                // Delay
                if (next.delay > 0) {
                    try {
                        Thread.sleep(next.delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Failed to sleep during path playback due to, " + e.getMessage());
                        break;
                    }
                }

                // Draw points
                controller.renderer.drawCircle(next.ox, next.oy, false);
                path.index++;
            }
        }).start();
    }
}
