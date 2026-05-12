package model;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import java.awt.Color;

public class CompositeObject extends UMLObject {
    private final List<UMLObject> children;

    public CompositeObject(List<UMLObject> children) {
        this.children = new ArrayList<>(children);
    }

    public List<UMLObject> getChildren() {
        return children;
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

            // 設置虛線
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] { 7f, 4f }, 0));
            g.setColor(new Color(30, 100, 220));

            // 畫出帶有 padding 的矩形
            g.drawRect(bound.x - 4, bound.y - 4, bound.width + 8, bound.height + 8);

            g.setStroke(oldStroke);
            g.setColor(oldColor);
        }
    }

    @Override
    public void move(int dx, int dy) {
        children.forEach(c -> c.move(dx, dy));
    }

    public List<BasicObject> collectAllBasicObjects() {
        List<BasicObject> result = new ArrayList<>();
        for (UMLObject child : children) {
            if (child instanceof BasicObject bo) {
                result.add(bo);
            } else if (child instanceof CompositeObject co) {
                result.addAll(co.collectAllBasicObjects());
            }
        }
        return result;
    }

}
