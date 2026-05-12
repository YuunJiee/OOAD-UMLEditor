package model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;

public class HollowTriangleStyle implements ArrowStyle {

    @Override
    public void draw(Graphics2D g, Point from, Point to) {
        Shape head = buildHead();
        OpenArrowStyle.drawArrow(g, from.x, from.y, to.x, to.y, head, true);
    }

    private Shape buildHead() {
        double angle = Math.PI / 6;
        int size = 20;
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(-size * Math.cos(angle), -size * Math.sin(angle));
        path.lineTo(-size * Math.cos(angle), size * Math.sin(angle));
        path.closePath();
        return path;
    }
}
