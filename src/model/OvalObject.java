package model;

import java.awt.Graphics2D;

public class OvalObject extends BasicObject {

    private static final double HIT_TOLERANCE = 1.05;

    private static final int[][] PORT_DIRS = {
            { Port.CENTER, Port.START  }, // 上
            { Port.END,    Port.CENTER }, // 右
            { Port.CENTER, Port.END    }, // 下
            { Port.START,  Port.CENTER }, // 左
    };


    public OvalObject(int x, int y, int width, int height) {
        super(x, y, width, height);
        initPorts(PORT_DIRS);
        updatePortPositions();
    }

    @Override
    protected void updatePortPositions() {
        applyPortPositions(PORT_DIRS);
    }


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
