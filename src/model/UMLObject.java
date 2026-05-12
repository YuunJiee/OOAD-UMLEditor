package model;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class UMLObject {
    protected boolean selected = false;


    public abstract void draw(Graphics2D g, boolean hovered);

    public abstract boolean contains(int x, int y);

    public abstract Rectangle getBounds();

    public abstract void move(int dx, int dy);

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
