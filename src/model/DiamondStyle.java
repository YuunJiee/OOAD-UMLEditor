package model;

import java.awt.Shape;
import java.awt.geom.Path2D;

public class DiamondStyle extends ArrowStyle {
    private final Shape head;

    public DiamondStyle() {
        int halfLen = 14, halfW = 7;
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(-halfLen, -halfW);
        path.lineTo(-2 * halfLen, 0);
        path.lineTo(-halfLen, halfW);
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
