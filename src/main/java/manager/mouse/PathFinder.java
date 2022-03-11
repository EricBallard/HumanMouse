package manager.mouse;

import javafx.scene.paint.Color;
import javafx.util.Pair;
import manager.gui.Controller;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class PathFinder {

    final static int xSpanThreshold = 50, ySpanThreshold = 50;

    ArrayList<Integer> usedPaths;

    // TODO - human-metric based random
    ThreadLocalRandom ran = ThreadLocalRandom.current();

    Controller controller;

    boolean drawnInfo;

    MousePath path;

    public AtomicReference<MousePoint> start, end;

    public PathFinder(Controller controller) {
        this.controller = controller;
        this.usedPaths = new ArrayList<>();
    }

    public void reset() {
        this.start = null;
        this.end = null;
    }

    public boolean pointsSet() {
        return this.start != null && this.end != null;
    }

    public void setPoint(boolean start, MousePoint point) {
        if (start) {
            if (this.start == null)
                this.start = new AtomicReference<>(point);
            else
                this.start.set(point);
        } else {
            if (this.end == null)
                this.end = new AtomicReference<>(point);
            else
                this.end.set(point);
        }
    }

    public boolean execute(boolean wait) {
        // Get human path that resembles our needed x/y span
        if (path == null && (path = this.get()) == null) {
            controller.renderer.drawText("NO SUITABLE PATHS FOUND", 80);

            int xspan = start.get().ox - end.get().ox,
                    yspan = start.get().oy - end.get().oy;

            controller.renderer.drawText("X-Span: " + xspan + " | Y-Span: " + yspan, 100);
            return false;
        }

        // Cache index of reference path (for drawing path info)
        controller.paths.index = controller.paths.list.indexOf(path);

        // Mark path as recently used
        usedPaths.add(path.hashCode());

        // Translate reference path to target coords
        path.translate(start.get());

        // Verify/adjust path to ensure it touches end point
        path.verify(end.get());

        // Draw path
        draw(wait);
        return true;
    }

    @Nullable
        // Find path from start -> end
    MousePath get() {
        long startTime = Instant.now().toEpochMilli();

        // Get set points
        MousePoint spoint = start.get(), epoint = end.get();

        // Calculate span
        int xSpan = epoint.ox - spoint.ox, ySpan = epoint.oy - spoint.oy;

        System.out.println("Target= xSpan: " + xSpan + " | ySpan: " + ySpan);
        HashMap<MousePath, Pair<Integer, Integer>> potentials = new HashMap<>();

        // Find 5 potential paths near target x/y span
        for (MousePath path : controller.paths.list) {
            int xSpanDif = Math.abs(path.xSpan - xSpan);

            // X-Span meets requirement
            if (xSpanDif <= xSpanThreshold) {
                int ySpanDif = Math.abs(path.ySpan - ySpan);

                // Y-Span meets requirement
                if (ySpanDif <= ySpanThreshold) {
                    // Ignore if path has been recently used
                    if (usedPaths.contains(path.hashCode()))
                        continue;

                    // Add as potential path
                    potentials.put(path, new Pair<>(xSpanDif, ySpanDif));
                    if (potentials.size() == 5) break;
                }
            }
        }

        // Sort
        LinkedList<MousePath> sorted = Paths.sort(potentials);
        int total = sorted.size();

        // Log time
        long elapsedTime = Instant.now().toEpochMilli() - startTime;
        System.out.println(elapsedTime + "ms | POTENTIAL PATHS: " + total);

        // Return random
        return total == 0 ? null : total == 1 ? sorted.get(0) :
                sorted.get(ran.nextInt((Math.min(total, 5)) - 1));
    }

    void draw(boolean wait) {
        Runnable draw = () -> {
            while (!Thread.interrupted()) {
                // Draw path
                MousePoint next;
                if ((next = path.getNext()) == null) {

                    // Draw highlights..
                    if (path.poi != null)
                        controller.renderer.highlightPoint(path.poi, Color.DEEPPINK);

                    // Re-draw transformed region
                    if (path.region != null) {
                        path.region.forEach((i, p) -> {
                            controller.renderer.drawPoint(p, Color.PURPLE);
                            try {
                                Thread.sleep(p.delay);
                            } catch (InterruptedException ignored) {
                            }
                        });

                        // Re-draw translated subregion
                        if (path.subRegion != null) {
                            path.subRegion.forEach((i, p) -> {
                                controller.renderer.drawPoint(p, Color.YELLOW);
                                try {
                                    Thread.sleep(p.delay);
                                } catch (InterruptedException ignored) {
                                }
                            });
                        }
                    }

                    // Done
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
                int index = path.points.indexOf(next);

                if (path.region != null) {
                    Color c = path.region.containsKey(index) ? Color.BLUE : path.subRegion != null &&
                            path.subRegion.containsKey(index) ? Color.GREEN : Color.YELLOW;

                    controller.renderer.drawPoint(next, c);
                } else {
                    controller.renderer.drawPoint(next, Color.YELLOW);
                }

                path.index++;
            }
        };

        if (wait)
            draw.run();
        else
            new Thread(draw).start();
    }
}
