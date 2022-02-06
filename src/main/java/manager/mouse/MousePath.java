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

        public Paths() {
            this.totalPaths = 0;
            this.list = new ArrayList<>();
        }

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

    public void translate(MousePoint start) {
        MousePath translated = new MousePath();
        translated.add(start);

        MousePoint last = null;
        for (MousePoint p : this.points) {
            int ax = (last == null ? start.ox : last.ox) + p.x,
                    ay = (last == null ? start.oy : last.oy) + p.y;

            MousePoint ap = new MousePoint(ax, ay, p.delay);
            translated.add(ap);
            last = ap;
        }

        translated.calculate();
        this.xSpan = translated.xSpan;
        this.ySpan = translated.ySpan;
        this.points = translated.points;
        this.totalPoints = translated.totalPoints;
        this.totalTime = translated.totalTime;
    }

    public void verify(MousePoint end) {
        // Check if path touches end point
        boolean touches = false;

        MousePoint nearest = null;
        int nearDis = 999;

        for (MousePoint point : this.points) {
            int dis = point.distanceTo(end);

            if (dis == 0) {
                touches = true;
                break;
            } else if (dis < nearDis) {
                nearest = point;
                nearDis = dis;
            }
        }

        System.out.println("Path Touches: " + touches);

        if (!touches) {
            int pindex = this.points.indexOf(nearest),
                    total = this.points.size();

            int percent = (int) (((double) pindex / (double) total) * 100);
            System.out.println(percent + "% - " + pindex + "/" + total + " - Dis:" + nearDis);

            transform(pindex, nearDis, end);
        }
    }

    void transform(int pindex, int dis, MousePoint end) {
        if (dis <= 10) {
            // Wing-it

            // Target - Point of Intersection
            MousePoint poi = this.points.get(pindex);

            // Find injection point in path, prior to poi
            LinkedList<MousePoint> region = findRegion(pindex, dis);

            // Iterate reverse from poi, even distribute remaining difference

        }
    }

    LinkedList<MousePoint> findRegion(int pindex, int dis) {
        /*
            Attempt to find a portion of the path which is relatively
            'smooth' in the sense all points are close and linear
         */
        int eindex = pindex - 1;

        MousePoint last = null;


        for (int i = eindex; i > 4; i -= 5) {
            // Iterate reverse from poi
            MousePoint next = this.points.get(i);

            if (last == null)
                last = next;
            else {
                int xDif = last.ox - next.ox,
                        yDif = last.oy - next.oy;

                // TODO

            }
        }

        return null;
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
