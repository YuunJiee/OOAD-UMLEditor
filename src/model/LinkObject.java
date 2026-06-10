package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Rectangle;

public class LinkObject extends UMLObject {
    protected final PortOwner fromObject;
    protected final Port fromPort;
    protected final PortOwner toObject;
    protected final Port toPort;
    private final ArrowStyle arrowStyle;

    public LinkObject(PortOwner from, Port fromPort, PortOwner to, Port toPort,
            ArrowStyle arrowStyle) {
        this.fromObject = from;
        this.fromPort = fromPort;
        this.toObject = to;
        this.toPort = toPort;
        this.arrowStyle = arrowStyle;
        this.zIndex = 0;
    }

    protected Point getFromPoint() {
        return fromPort != null ? fromPort.getPosition() : new Point(fromObject.getX(), fromObject.getY());
    }

    protected Point getToPoint() {
        return toPort != null ? toPort.getPosition() : new Point(toObject.getX(), toObject.getY());
    }

    @Override
    public Rectangle getBounds() {
        Point p1 = getFromPoint();
        Point p2 = getToPoint();
        int w = Math.max(Math.abs(p2.x - p1.x), 1);
        int h = Math.max(Math.abs(p2.y - p1.y), 1);
        return new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), w, h);
    }

    public PortOwner getFromObject() {
        return fromObject;
    }

    public PortOwner getToObject() {
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

    @Override
    public boolean shouldBeDeleted(java.util.Set<PortOwner> deletingOwners) {
        return isSelected() || deletingOwners.contains(fromObject) || deletingOwners.contains(toObject);
    }
}
