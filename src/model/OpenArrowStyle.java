package model;

import java.awt.Shape;
import java.awt.geom.Path2D;

public class OpenArrowStyle extends ArrowStyle {
    private final Shape head;

    public OpenArrowStyle() {
        double angle = Math.PI / 7;
        int size = 16;
        Path2D path = new Path2D.Double();
        path.moveTo(-size * Math.cos(angle), -size * Math.sin(angle));
        path.lineTo(0, 0);
        path.lineTo(-size * Math.cos(angle), size * Math.sin(angle));
        head = path;
    }

    @Override
    protected Shape getArrowHead() {
        return head;
    }

    @Override
    protected boolean shouldFillWhite() {
        return false;
    }
}
