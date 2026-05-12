package model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;

public class DiamondStyle implements ArrowStyle {

    @Override
    public void draw(Graphics2D g, Point from, Point to) {
        Shape head = buildHead();
        OpenArrowStyle.drawArrow(g, from.x, from.y, to.x, to.y, head, true);
    }

    private Shape buildHead() {
        int halfLen = 14, halfW = 7;
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(-halfLen, -halfW);
        path.lineTo(-2 * halfLen, 0);
        path.lineTo(-halfLen, halfW);
        path.closePath();
        return path;
    }
}
