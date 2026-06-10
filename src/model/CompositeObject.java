package model;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.Rectangle;
import java.awt.Color;

public class CompositeObject extends UMLObject implements Groupable {
    private static final int GROUP_PADDING = 4;

    private final List<UMLObject> children;

    public CompositeObject(List<UMLObject> children) {
        this.children = new ArrayList<>(children);
    }

    public List<UMLObject> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public Rectangle getBounds() {
        if (children.isEmpty())
            return new Rectangle(0, 0, 0, 0);

        Rectangle bounds = null;
        for (UMLObject child : children) {
            Rectangle childBounds = child.getBounds();
            if (bounds == null) {
                bounds = new Rectangle(childBounds);
            } else {
                bounds = bounds.union(childBounds);
            }
        }
        return bounds;
    }

    @Override
    public boolean contains(int px, int py) {
        return getBounds().contains(px, py);
    }

    @Override
    public void draw(Graphics2D g, boolean hovered) {
        for (UMLObject child : children) {
            child.draw(g, false);
        }

        if (selected || hovered) {
            Rectangle bound = getBounds();

            Stroke oldStroke = g.getStroke();
            Color oldColor = g.getColor();

            // dashed border
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] { 7f, 4f }, 0));
            g.setColor(new Color(30, 100, 220));

            // draw bounding rect with padding
            g.drawRect(bound.x - GROUP_PADDING, bound.y - GROUP_PADDING,
                    bound.width + GROUP_PADDING * 2, bound.height + GROUP_PADDING * 2);

            g.setStroke(oldStroke);
            g.setColor(oldColor);
        }
    }

    @Override
    public void move(int dx, int dy) {
        children.forEach(c -> c.move(dx, dy));
    }

    @Override
    public void collectDeletingPortOwners(java.util.Set<PortOwner> set) {
        for (UMLObject child : children) {
            child.collectDeletingPortOwners(set);
        }
    }

}
