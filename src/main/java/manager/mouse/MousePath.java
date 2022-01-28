package manager.mouse;

import java.util.ArrayList;
import java.util.LinkedList;

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
    public transient int index = 0;

    public int xSpan, ySpan;

    public long totalTime;

    public int totalPoints;

    public LinkedList<MousePoint> points;

    public MousePoint getNext() {
        if (index >= totalPoints) return null;
        return points.get(index);
    }
}
