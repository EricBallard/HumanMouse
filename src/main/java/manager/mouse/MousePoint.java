package manager.mouse;

public class MousePoint {

    public int ox, x;

    public int oy, y;

    public long delay;

    public MousePoint(int x, int y) {
        this.delay = 0;
        this.x = 0;
        this.y = 0;
        this.ox = x;
        this.oy = y;
    }

    public MousePoint(int x, int y, long delay) {
        this.x = 0;
        this.y = 0;
        this.ox = x;
        this.oy = y;
        this.delay = delay;
    }

    public int distanceTo(MousePoint p) {
        return (int) Math.sqrt(Math.pow(p.oy - this.oy, 2) + Math.pow(p.ox - this.ox, 2));
    }

    public static boolean isLinear(int lastDif, int currDif, int tolerance) {
        return lastDif < 0 ?
                // Current & last = moving left
                (currDif <= lastDif || (lastDif - currDif) <= tolerance) :
                // Current & last = moving right
                (currDif >= lastDif || (lastDif - currDif) <= tolerance);
    }

    public MousePoint copy() {
        MousePoint mp = new MousePoint(this.ox, this.oy);
        mp.x = this.x;
        mp.y = this.y;
        mp.delay = this.delay;
        return mp;
    }
}