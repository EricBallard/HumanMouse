package manager.mouse;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class MousePath {

    public static class Paths {
        public transient int index = 0;

        public int totalPaths;
        public ArrayList<MousePath> list;

        public MousePath getNext() {
            if (index >= totalPaths) return null;
            return list.get(index);
        }
    }

    // Transient modifier excludes from gson
    public transient int index;

    public int xSpan, ySpan;

    public long totalTime;

    public int totalPoints;

    public LinkedList<MousePoint> points;

    public MousePath() {
        this.points = new LinkedList<>();
        this.totalPoints = 0;
        this.totalTime = 0;
        this.xSpan = 0;
        this.ySpan = 0;
        this.index = 0;
    }

    public MousePoint getNext() {
        if (index >= totalPoints) return null;
        return points.get(index);
    }

    public void add(MousePoint mp) {
        points.addLast(mp);
        totalPoints++;
    }

    public void calculate() {
        // Register total time and span of path
        this.totalTime = getTotalTime();

        // Global span for path
        Map.Entry<Integer, Integer> span = getSpan();
        this.xSpan = span.getKey();
        this.ySpan = span.getValue();

        // Calculate span for each point
        for (int i = 1; i < this.totalPoints; i++) {
            MousePoint p = this.points.get(i),
                    last = this.points.get(i - 1);

            p.x = p.ox - last.ox;
            p.y = p.oy - last.oy;
        }
    }

    long getTotalTime() {
        long totalTime = 0L;

        for (MousePoint point : points)
            totalTime += point.delay;

        return totalTime;
    }

    // Returns difference between low/high for x/y
    Map.Entry<Integer, Integer> getSpan() {
        MousePoint first = this.points.getFirst(),
                last = this.points.getLast();

        return new AbstractMap.SimpleEntry<>(last.ox - first.ox, last.oy - first.oy);
    }
}
