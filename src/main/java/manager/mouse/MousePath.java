package manager.mouse;

import java.util.ArrayList;
import java.util.LinkedList;

public class MousePath {

    public static class Paths {
        public transient int index = -1;

        public int totalPaths;
        public ArrayList<MousePath> list;

        public MousePath getNext() {
            if (index >= totalPaths - 1) return null;

            index++;
            return list.get(index);
        }
    }

    // Transient modifier excludes from gson
    public transient int index;

    public int xSpan, ySpan;

    public long totalTime;

    public int totalPoints;

    public LinkedList<MousePoint> points;

    public MousePoint getNext() {
        if (index >= totalPoints - 1) return null;

        index++;
        return points.get(index);
    }
}
