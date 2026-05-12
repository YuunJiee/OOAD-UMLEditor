package model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class OpenArrowStyle implements ArrowStyle {

    @Override
    public void draw(Graphics2D g, Point from, Point to) {
        Shape head = buildHead();
        drawArrow(g, from.x, from.y, to.x, to.y, head, false);
    }

    private Shape buildHead() {
        double angle = Math.PI / 7;
        int size = 16;
        Path2D path = new Path2D.Double();
        path.moveTo(-size * Math.cos(angle), -size * Math.sin(angle));
        path.lineTo(0, 0);
        path.lineTo(-size * Math.cos(angle), size * Math.sin(angle));
        return path;
    }

    static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2,
            Shape head, boolean fillWhite) {
        g.drawLine(x1, y1, x2, y2);
        AffineTransform old = g.getTransform();
        g.translate(x2, y2);
        g.rotate(Math.atan2(y2 - y1, x2 - x1));
        if (fillWhite) {
            var color = g.getColor();
            g.setColor(java.awt.Color.WHITE);
            g.fill(head);
            g.setColor(color);
        }
        g.draw(head);
        g.setTransform(old);
    }
}
