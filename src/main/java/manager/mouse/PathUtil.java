package manager.mouse;

import javafx.util.Pair;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PathUtil {

    // GLOBAL BASED TRANSFORM
    public static void globalTransform(MousePath path, MousePoint end, int pindex) {
        // Calculate total x/y difference from poi to end
        int xDif = end.ox - path.poi.ox;
        int yDif = end.oy - path.poi.oy;

        // Adjust remaining points in path
        ThreadLocalRandom r = ThreadLocalRandom.current();
        path.region = new LinkedHashMap<>();
        int xTally = 0, yTally = 0;

        for (int index = 0; index < path.totalPoints; index++) {
            if (index <= pindex) {
                // % progress of completed iteration
                int itrPct = (int) (((double) index / (double) pindex) * 100D);

                // Seed
                int seed = (int) (100 - (Math.max(Math.abs(xDif), Math.abs(yDif)) * r.nextDouble(1.5, 3.0)));

                // Adjust path
                if (itrPct >= seed) {
                    //System.out.println(itrPct + "% | " + seed);
                    int xChange, yChange;

                    if (xDif != 0) {
                        //System.out.println("X: " + xDif);
                        xChange = xDif < 0 ? -1 : 1;
                        xDif -= xChange;

                        xTally += xChange;
                    }

                    if (yDif != 0) {
                       // System.out.println("Y: " + yDif);
                        yChange = yDif < 0 ? -1 : 1;
                        yDif -= yChange;

                        yTally += yChange;
                    }
                }
            }

            // Apply changes to point
            MousePoint mp = path.points.get(index).copy();
            mp.ox = mp.ox + xTally;
            mp.oy = mp.oy + yTally;

            path.region.put(index, mp);
        }
    }

    // REGION BASED TRANSFORM
    public static void regionTransform(MousePath path, MousePoint end, int pindex, int dis) {
        // Calculate total x/y difference from poi to end
        int xDif = end.ox - path.poi.ox;
        int yDif = end.oy - path.poi.oy;

        // Calculate needed region size
        int regionSize = Math.max(Math.abs(xDif), Math.abs(yDif));
        if (dis > regionSize) regionSize = dis;

        // Ensure region is at least 20% of path and x3 the distance
        regionSize = Math.max(regionSize, path.totalPoints / 20);
        regionSize = Math.max(regionSize, dis * 2);

        // Find section in path, prior to poi
        Pair<Integer, Integer> regionBounds = findRegion(path, pindex, regionSize);
        //System.out.println("(" + regionSize + ") REGION: " + regionBounds);

        if (regionBounds == null) return;
        else path.region = new LinkedHashMap<>();

        // Cache points + calculate average x/y difference in region
        int xAvg = 0, yAvg = 0;

        for (int index = regionBounds.getValue(); index < regionBounds.getKey(); index++) {
            MousePoint curr = path.points.get(index);
            path.region.put(index, curr.copy());

            MousePoint next = path.points.get(index + 1);
            if (next == null) continue;

            xAvg += Math.abs(next.ox - curr.ox);
            yAvg += Math.abs(next.oy - curr.oy);
        }

        xAvg = xAvg / path.region.size();
        yAvg = yAvg / path.region.size();

        ThreadLocalRandom r = ThreadLocalRandom.current();
        int xTally = 0, yTally = 0;

        for (int ri : path.region.keySet()) {
            int seed = Math.max(Math.max(xAvg, yAvg), 1) + 1;
            int change = r.nextInt(0, seed);

            if (change != 0) {
                boolean transX = xDif != 0, transY = yDif != 0, negative;
                int xChange, yChange;

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

                //  System.out.println("X-Diff: " + xDif + " | Y-Diff: " + yDif);
            }

            MousePoint mp = path.region.get(ri);
            mp.ox = mp.ox + xTally;
            mp.oy = mp.oy + yTally;
        }

        // Adjust remaining points in path
        path.subRegion = new LinkedHashMap<>();
        for (int i = regionBounds.getKey() + 1; i < path.totalPoints; i++) {
            MousePoint mp = path.points.get(i).copy();
            mp.ox = mp.ox + xTally;
            mp.oy = mp.oy + yTally;

            path.subRegion.put(i, mp);
        }
    }

    @Nullable
    static Pair<Integer, Integer> findRegion(MousePath path, int pindex, int dis) {
        /*
            Find a portion of the path which is relatively
            'smooth' in the sense all points are near and linear
         */
        MousePoint last = null;

        int lastXdif = -1, lastYdif = -1;

        int eindex = pindex - 1, foundBlocks = 0, regionStartIndex = eindex;

        for (int i = eindex; i > 4; i -= 5) {
            // Iterate reverse from poi
            MousePoint next = path.points.get(i);

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
}
