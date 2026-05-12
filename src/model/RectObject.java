package model;

import java.awt.Graphics2D;

public class RectObject extends BasicObject {

    public RectObject(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    // {dx, dy}：0=左/上, 1=中, 2=右/下
    private static final int[][] PORT_DIRS = {
            { 0, 0 }, // 0: 左上
            { 1, 0 }, // 1: 上中
            { 2, 0 }, // 2: 右上
            { 2, 1 }, // 3: 右中
            { 2, 2 }, // 4: 右下
            { 1, 2 }, // 5: 下中
            { 0, 2 }, // 6: 左下
            { 0, 1 }, // 7: 左中
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

    @Override
    protected void drawShape(Graphics2D g) {
        g.fillRect(x, y, width, height);
    }

    @Override
    protected void drawShapeBorder(Graphics2D g) {
        g.drawRect(x, y, width, height);
    }
}
