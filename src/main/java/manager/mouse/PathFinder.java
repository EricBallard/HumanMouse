package manager.mouse;

import manager.gui.Controller;

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

    // Find path from start -> end
    public void execute() {
        System.out.println("Finding pathing..");
    }

}
