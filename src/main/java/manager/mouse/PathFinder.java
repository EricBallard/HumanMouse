package manager.mouse;

import javafx.util.Pair;
import manager.gui.Controller;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
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

    final static int xSpanThreshold = 50, ySpanThreshold = 50;

    // TODO - human random
    Random ran = new Random();

    @Nullable // Find path from start -> end
    public MousePath get() {
        System.out.println("Finding pathing..");
        long startTime = Instant.now().toEpochMilli();

        // Get set points
        MousePoint spoint = start.get(), epoint = end.get();

        // Calculate span
        int xSpan = epoint.ox - spoint.ox, ySpan = epoint.oy - spoint.oy;

        System.out.println("Target= xSpan: " + xSpan + " | ySpan: " + ySpan);
        HashMap<MousePath, Pair<Integer, Integer>> potentials = new HashMap<>();

        // Find 3 potential paths near target x/y span
        for (MousePath path : controller.paths.list) {
            int xSpanDif = Math.abs(path.xSpan - xSpan);

            // X-Span meets requirement
            if (xSpanDif <= xSpanThreshold) {
                int ySpanDif = Math.abs(path.ySpan - ySpan);

                // Y-Span meets requirement
                if (ySpanDif <= ySpanThreshold) {
                    potentials.put(path, new Pair<>(xSpanDif, ySpanDif));
                    // if (potentials.size() == 3) break;
                }
            }
        }

        // Sort
        List<MousePath> keys = new ArrayList<>(potentials.keySet());
        LinkedList<MousePath> sorted = new LinkedList<>();

        final int total = potentials.size();
        for (int index = 0; index < total; index++) {
            MousePath mp = keys.get(index);

            if (sorted.isEmpty()) sorted.addFirst(mp);
            else {
                Pair<Integer, Integer> spans = potentials.get(mp);
                int targetIndex = -1, totalSorted = sorted.size();

                // Iterate sorted to compare differences
                for (int i = 0; i < totalSorted; i++) {
                    MousePath sp = sorted.get(i);
                    Pair<Integer, Integer> sspans = potentials.get(sp);

                    if (spans.getKey() < sspans.getKey() && spans.getValue() < sspans.getValue()) {

                        targetIndex = i;
                        break;
                    }
                }

                sorted.add(targetIndex != -1 ? targetIndex : totalSorted, mp);
            }
        }

        // Log time
        long elapsedTime = Instant.now().toEpochMilli() - startTime;
        System.out.println(elapsedTime + "ms | POTENTIAL PATHS: " + total);

        // Return random
        return total == 0 ? null : total == 1 ? sorted.get(0) :
                sorted.get(ran.nextInt((total > 5 ? 5 : total) - 1));
    }

    boolean drawnInfo;
    MousePath path;

    public void execute() {
        controller.toggleCanvas(true);

        // Get human path that resembles our needed x/y span
        if (path == null && (path = this.get()) == null) {
            controller.renderer.drawText("NO SUITABLE PATHS FOUND");
            controller.toggleCanvas(false);
            return;
        }

        // Cache index of reference path
        controller.paths.index = controller.paths.list.indexOf(path);

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
                    controller.toggleCanvas(false);
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