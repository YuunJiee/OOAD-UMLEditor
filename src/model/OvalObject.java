package model;

import java.awt.Graphics2D;

public class OvalObject extends BasicObject {

    public OvalObject(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    private static final int[][] PORT_DIRS = {
            { 1, 0 }, // 上
            { 2, 1 }, // 右
            { 1, 2 }, // 下
            { 0, 1 } // 左
    };

    @Override
    public Port[] getPorts() {
        Port[] ports = new Port[PORT_DIRS.length];

        for (int i = 0; i < PORT_DIRS.length; i++) {
            int dx = PORT_DIRS[i][0];
            int dy = PORT_DIRS[i][1];

            int px = x + (dx == 0 ? 0 : dx == 1 ? width / 2 : width);
            int py = y + (dy == 0 ? 0 : dy == 1 ? height / 2 : height);

            ports[i] = new Port(px, py, PORT_DIRS[i]);
        }

        return ports;
    }

    private static final double HIT_TOLERANCE = 1.05;

    @Override
    public boolean contains(int px, int py) {
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;

        if (rx == 0 || ry == 0)
            return false;

        double nx = (px - cx) / rx;
        double ny = (py - cy) / ry;

        return nx * nx + ny * ny <= HIT_TOLERANCE;
    }

    @Override
    protected void drawShape(Graphics2D g) {
        g.fillOval(x, y, width, height);
    }

    @Override
    protected void drawShapeBorder(Graphics2D g) {
        g.drawOval(x, y, width, height);
    }

}
