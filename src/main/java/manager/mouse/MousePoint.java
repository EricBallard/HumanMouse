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
}