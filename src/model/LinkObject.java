package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Rectangle;

public abstract class LinkObject extends UMLObject {
    protected final BasicObject fromObject;
    protected final int fromPort;
    protected final BasicObject toObject;
    protected final int toPort;
    private final ArrowStyle arrowStyle;

    public LinkObject(BasicObject from, int fromPort, BasicObject to, int toPort,
            ArrowStyle arrowStyle) {
        this.fromObject = from;
        this.fromPort = fromPort;
        this.toObject = to;
        this.toPort = toPort;
        this.arrowStyle = arrowStyle;
    }

    private Point getPoint(BasicObject obj, int portIndex) {
        if (obj == null)
            return new Point(0, 0);

        Port[] ports = obj.getPorts();
        if (portIndex >= 0 && portIndex < ports.length) {
            return ports[portIndex].getPosition();
        }

        return new Point(obj.getX(), obj.getY());
    }

    protected Point getFromPoint() {
        return getPoint(fromObject, fromPort);
    }

    protected Point getToPoint() {
        return getPoint(toObject, toPort);
    }

    @Override
    public Rectangle getBounds() {
        Point p1 = getFromPoint();
        Point p2 = getToPoint();
        int w = Math.max(Math.abs(p2.x - p1.x), 1);
        int h = Math.max(Math.abs(p2.y - p1.y), 1);
        return new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), w, h);
    }

    @Override
    public boolean contains(int px, int py) {
        return false;
    }

    @Override
    public void move(int dx, int dy) {
        // links follow their connected BasicObjects; no position to update
    }

    public BasicObject getFromObject() {
        return fromObject;
    }

    public BasicObject getToObject() {
        return toObject;
    }

    @Override
    public final void draw(Graphics2D g, boolean hovered) {
        Point p1 = getFromPoint();
        Point p2 = getToPoint();

        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

        g.setColor(hovered ? Color.BLUE : Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));

        drawLinkShape(g, p1, p2);

        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    protected void drawLinkShape(Graphics2D g, Point p1, Point p2) {
        arrowStyle.draw(g, p1, p2);
    }
}
