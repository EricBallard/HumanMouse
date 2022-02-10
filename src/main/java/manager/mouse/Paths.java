package manager.mouse;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Paths {

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

    public static LinkedList<MousePath> sort(HashMap<MousePath, Pair<Integer, Integer>> potentials) {
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
        return sorted;
    }

}
