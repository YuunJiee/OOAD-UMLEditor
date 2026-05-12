package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class Port {
    public static final int SIZE = 8;
    public static final int HALF = SIZE / 2;
    public static final int HIT_RADIUS = HALF + 5;

    private final int x, y;
    private final int[] direction;

    public Port(int x, int y, int[] direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public boolean contains(int mx, int my) {
        return Math.abs(x - mx) <= HIT_RADIUS && Math.abs(y - my) <= HIT_RADIUS;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(x - HALF, y - HALF, SIZE, SIZE);
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int[] getDirection() { return direction; }
}
