package model;

import java.awt.Shape;
import java.awt.geom.Path2D;

public class HollowTriangleStyle extends ArrowStyle {
    private final Shape head;

    public HollowTriangleStyle() {
        double angle = Math.PI / 6;
        int size = 20;
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(-size * Math.cos(angle), -size * Math.sin(angle));
        path.lineTo(-size * Math.cos(angle), size * Math.sin(angle));
        path.closePath();
        head = path;
    }

    @Override
    protected Shape getArrowHead() {
        return head;
    }

    @Override
    protected boolean shouldFillWhite() {
        return true;
    }
}
