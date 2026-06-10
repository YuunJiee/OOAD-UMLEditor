package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;

public abstract class BasicObject extends UMLObject implements PortOwner, Groupable, AppearanceEditable {
    protected int x, y, width, height;
    private Color fillColor = new Color(180, 180, 180);
    private Label label = new Label("");

    public static final int MIN_SIZE = 20;

    protected Port[] ports;

    public BasicObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(Math.abs(width), MIN_SIZE);
        this.height = Math.max(Math.abs(height), MIN_SIZE);
    }

    protected abstract void updatePortPositions();

    protected void initPorts(int[][] dirs) {
        ports = new Port[dirs.length];
        for (int i = 0; i < dirs.length; i++)
            ports[i] = new Port(0, 0, dirs[i]);
    }

    protected void applyPortPositions(int[][] dirs) {
        for (int i = 0; i < dirs.length; i++) {
            int px = x + switch (dirs[i][0]) {
                case Port.START  -> 0;
                case Port.CENTER -> width / 2;
                default          -> width;
            };
            int py = y + switch (dirs[i][1]) {
                case Port.START  -> 0;
                case Port.CENTER -> height / 2;
                default          -> height;
            };
            ports[i].setPosition(px, py);
        }
    }

    public Port[] getPorts() {
        return ports;
    }

    public Port getPortAt(int mx, int my) {
        for (Port p : ports) {
            if (p.contains(mx, my)) return p;
        }
        return null;
    }

    protected void drawPorts(Graphics2D g) {
        for (Port p : ports)
            p.draw(g);
    }

    @Override
    public final void draw(Graphics2D g, boolean hovered) {
        g.setColor(fillColor);
        drawShape(g);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        drawShapeBorder(g);
        label.draw(g, x, y, width, height);
        if (selected || hovered)
            drawPorts(g);
    }

    protected abstract void drawShape(Graphics2D g);

    protected abstract void drawShapeBorder(Graphics2D g);

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    @Override
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
        for (Port p : ports) p.shift(dx, dy);
    }

    public void resize(int newX, int newY, int newW, int newH) {
        this.x = newX;
        this.y = newY;
        this.width = Math.max(newW, MIN_SIZE);
        this.height = Math.max(newH, MIN_SIZE);
        updatePortPositions();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getLabel() {
        return label.getName();
    }

    public void setLabel(String name) {
        label.setName(name);
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    @Override
    public void collectDeletingPortOwners(java.util.Set<PortOwner> set) {
        set.add(this);
    }
}
