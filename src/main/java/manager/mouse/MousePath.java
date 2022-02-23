package manager.mouse;

import javafx.util.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class MousePath {

    // Transient modifier excludes from gson
    public transient LinkedHashMap<Integer, MousePoint> subRegion;

    public transient LinkedHashMap<Integer, MousePoint> region;

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

    public MousePath(int xs, int ys, int tt, int tp, String ps) {
        this.xSpan = xs;
        this.ySpan = ys;
        this.totalTime = tt;
        this.totalPoints = tp;
        this.points = new LinkedList<>();

        for (String s : ps.split(";")) {
            String[] data = s.split(":");

            int ox = Integer.valueOf(data[0]), x = Integer.valueOf(data[1]);
            int oy = Integer.valueOf(data[2]), y = Integer.valueOf(data[3]);
            int delay = Integer.valueOf(data[4]);

            MousePoint point = new MousePoint(ox, oy, delay);
            point.x = x;
            point.y = y;

            add(point);
        }
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

    void verify(MousePoint end) {
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

        // Calculate total x/y difference from poi to end
        int xDif = end.ox - poi.ox;
        int yDif = end.oy - poi.oy;

        System.out.println("X-Diff: " + xDif + " | Y-Diff: " + yDif);

        // Calculate needed region size
        int regionSize = Math.max(Math.abs(xDif), Math.abs(yDif));
        if (dis > regionSize) regionSize = dis;

        // Ensure region is at least 20% of path and x3 the distance
        regionSize = Math.max(regionSize, totalPoints / 20);
        regionSize = Math.max(regionSize, dis * 2);

        // Find section in path, prior to poi
        Pair<Integer, Integer> regionBounds = findRegion(pindex, regionSize);
        System.out.println("(" + regionSize + ") REGION: " + regionBounds);

        if (regionBounds == null) return;  //TODO
        else this.region = new LinkedHashMap<>();

        // Cache points + calculate average x/y difference in region
        int xAvg = 0, yAvg = 0;

        for (int index = regionBounds.getValue(); index < regionBounds.getKey(); index++) {
            MousePoint curr = points.get(index);
            this.region.put(index, curr.copy());

            MousePoint next = this.points.get(index + 1);
            if (next == null) continue;

            xAvg += Math.abs(next.ox - curr.ox);
            yAvg += Math.abs(next.oy - curr.oy);
        }

        xAvg = xAvg / this.region.size();
        yAvg = yAvg / this.region.size();
        System.out.println("X-Avg: " + xAvg + " | Y-Avg: " + yAvg);

        // TODO - human random
        Random ran = new Random();

        int xTally = 0, yTally = 0;

        for (int ri : this.region.keySet()) {
            int seed = Math.max(Math.max(xAvg, yAvg), 1) + 1;
            int change = ran.ints(0, seed).findFirst().getAsInt();

            if (change != 0) {
                boolean transX = xDif != 0, transY = yDif != 0, negative;
                int xChange = 0, yChange = 0;

                if (transX) {
                    negative = xDif < 0;
                    xChange = negative ? -1 : 1;
                    xDif -= xChange;

                    xTally += xChange;
                }

                if (transY) {
                    negative = yDif < 0;
                    yChange = negative ? -1 : 1;
                    yDif -= yChange;

                    yTally += yChange;
                }

                System.out.println("X-Diff: " + xDif + " | Y-Diff: " + yDif);
            }

            MousePoint mp = this.region.get(ri);
            mp.ox = mp.ox + xTally;
            mp.oy = mp.oy + yTally;
        }

        // Adjust remaining points in path
        this.subRegion = new LinkedHashMap<>();
        for (int i = regionBounds.getKey() + 1; i < totalPoints; i++) {
            MousePoint mp = this.points.get(i).copy();
            mp.ox = mp.ox + xTally;
            mp.oy = mp.oy + yTally;

            this.subRegion.put(i, mp);
        }

        System.out.println("Transformed!!");
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

            // First iteration
            if (last == null) last = next;
            else {
                // Compare x/y diff to last
                int xDif = last.ox - next.ox, yDif = last.oy - next.oy;
                last = next;

                //First compare
                if (lastXdif == -1) {
                    // System.out.println("Starting Region: " + i);
                    regionStartIndex = i;
                    lastXdif = xDif;
                    lastYdif = yDif;
                    continue;
                }

                if (MousePoint.isLinear(lastXdif, xDif, 5)
                        && MousePoint.isLinear(lastYdif, yDif, 5)) {

                    // Found block
                    foundBlocks++;
                    //System.out.println(i + " | Found block: " + foundBlocks);

                    lastXdif = xDif;
                    lastYdif = yDif;

                    // Found region
                    if (foundBlocks * 5 >= dis * 2) return new Pair<>(regionStartIndex, i);
                } else {
                    // Current and last are opposite directions - reset
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

    public String getPointsAsString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < totalPoints; i++) {
            MousePoint point = points.get(i);

            sb.append(point.ox).append(":");
            sb.append(point.x).append(":");

            sb.append(point.oy).append(":");
            sb.append(point.y).append(":");

            sb.append(point.delay);

            if (i < totalPoints - 1)
                sb.append(";");
        }

        return sb.toString();
    }
}
