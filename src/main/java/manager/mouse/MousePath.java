package manager.mouse;

import javafx.util.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class MousePath {

    // Transient modifier excludes from gson
    public transient MousePoint poi;

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
            int ax = (last == null ? start.ox : last.ox) + p.x, ay = (last == null ? start.oy : last.oy) + p.y;

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
            int pindex = this.points.indexOf(nearest), total = this.points.size();

            int percent = (int) (((double) pindex / (double) total) * 100);
            System.out.println(percent + "% - " + pindex + "/" + total + " - Dis:" + nearDis);

            transform(pindex, nearDis, end);
        }
    }

    void transform(int pindex, int dis, MousePoint end) {
        // Target - Point of Intersection
        MousePoint poi = this.points.get(pindex);

        // Highlight poi
        this.poi = poi;

        // Find injection point in path, prior to poi
        Pair<Integer, Integer> region = findRegion(pindex, dis);

        // Iterate reverse from poi, even distribute remaining difference
        System.out.println("REGION? : " + region);

    }

    boolean isLinear(int lastDif, int currDif, int tolerance) {
        return lastDif < 0 ?
                // Current & last = moving left
                (currDif <= lastDif || (lastDif - currDif) <= tolerance) :
                // Current & last = moving right
                (currDif >= lastDif || (lastDif - currDif) <= tolerance);
    }

    @Nullable
    Pair<Integer, Integer> findRegion(int pindex, int dis) {
        /*
            Find a portion of the path which is relatively
            'smooth' in the sense all points are near and linear
         */
        MousePoint last = null;

        int lastXdif = -1, lastYdif = -1;

        int eindex = pindex - 1, foundBlocks = 0, regionStartIndex = eindex;

        for (int i = eindex; i > 4; i -= 5) {
            // Iterate reverse from poi
            MousePoint next = this.points.get(i);

            if (last == null) last = next;
            else {
                // Compare x/y diff to last
                int xDif = last.ox - next.ox, yDif = last.oy - next.oy;
                last = next;

                //First compare
                if (lastXdif == -1) {
                    lastXdif = xDif;
                    lastYdif = yDif;
                    continue;
                }

                if (isLinear(lastXdif, xDif, 4) && isLinear(lastYdif, yDif, 4)) {
                    foundBlocks++;

                    if (foundBlocks * 5 >= dis) {
                        // Found region
                        System.out.println("FOUND REGION: " + regionStartIndex);
                        return new Pair<>(regionStartIndex, i);
                    }

                    // Found block
                    System.out.println(i + " | Found block: " + foundBlocks);
                    lastXdif = xDif;
                    lastYdif = yDif;
                } else {
                    // Current and last are opposite directions
                    System.out.println(regionStartIndex + " | Reset region.. X doesnt match, Found: " + foundBlocks);
                    System.out.println("LAST: " + lastXdif + ", " + lastYdif);
                    System.out.println("CURR: " + xDif + ", " + yDif);

                    regionStartIndex = i - 1;
                    foundBlocks = 0;
                    lastXdif = -1;
                }
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
            MousePoint p = this.points.get(i), last = this.points.get(i - 1);

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
        MousePoint first = this.points.getFirst(), last = this.points.getLast();

        return new AbstractMap.SimpleEntry<>(last.ox - first.ox, last.oy - first.oy);
    }
}
