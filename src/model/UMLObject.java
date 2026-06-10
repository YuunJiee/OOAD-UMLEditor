package model;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Set;

public abstract class UMLObject {
    protected boolean selected = false;
    protected int zIndex = 50;

    public abstract void draw(Graphics2D g, boolean hovered);

    public boolean contains(int x, int y) { return false; }

    public abstract Rectangle getBounds();

    public void move(int dx, int dy) {}

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public int getZIndex() { return zIndex; }
    public void setZIndex(int zIndex) { this.zIndex = Math.min(Math.max(zIndex, 0), 99); }

    public void collectDeletingPortOwners(Set<PortOwner> set) {}

    public boolean shouldBeDeleted(Set<PortOwner> deletingOwners) {
        return selected;
    }
}
