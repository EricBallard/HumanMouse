package manager.mouse;
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

            int ox = Integer.parseInt(data[0]), x = Integer.parseInt(data[1]);
            int oy = Integer.parseInt(data[2]), y = Integer.parseInt(data[3]);
            int delay = Integer.parseInt(data[4]);

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
        // Re-map all points in path from reference coords to target
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

        if (!touches) {
            int pindex = this.points.indexOf(nearest), total = this.points.size();

            // Highlight poi
            this.poi = this.points.get(pindex);

            int percent = (int) (((double) pindex / (double) total) * 100);
            System.out.println(percent + "% - " + pindex + "/" + total + " - Dis:" + nearDis);

            // 2 options to transform path, ensuring it touches end point
            PathUtil.globalTransform(this, end, pindex);
            //PathUtil.regionTransform(this, end, pindex, nearDis);
        }
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
