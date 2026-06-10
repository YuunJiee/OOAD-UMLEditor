package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

public abstract class ArrowStyle {

    public final void draw(Graphics2D g, Point from, Point to) {
        g.drawLine(from.x, from.y, to.x, to.y);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.translate(to.x, to.y);
            g2.rotate(Math.atan2(to.y - from.y, to.x - from.x));
            Shape head = getArrowHead();
            if (shouldFillWhite()) {
                Color oldColor = g2.getColor();
                g2.setColor(Color.WHITE);
                g2.fill(head);
                g2.setColor(oldColor);
            }
            g2.draw(head);
        } finally {
            g2.dispose();
        }
    }

    protected abstract Shape getArrowHead();

    protected abstract boolean shouldFillWhite();
}
