package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;

public abstract class BasicObject extends UMLObject {
    protected int x, y, width, height;
    private Label label = new Label("", new Color(180, 180, 180));

    public static final int MIN_SIZE = 20;

    public BasicObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(Math.abs(width), MIN_SIZE);
        this.height = Math.max(Math.abs(height), MIN_SIZE);
    }

    public abstract Port[] getPorts();

    public int getPortIndexAt(int mx, int my) {
        Port[] ports = getPorts();
        for (int i = 0; i < ports.length; i++) {
            if (ports[i].contains(mx, my))
                return i;
        }
        return -1;
    }

    protected void drawPorts(Graphics2D g) {
        for (Port p : getPorts())
            p.draw(g);
    }

    @Override
    public final void draw(Graphics2D g, boolean hovered) {
        g.setColor(label.getColor());
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
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void resize(int newX, int newY, int newW, int newH) {
        this.x = newX;
        this.y = newY;
        this.width = Math.max(newW, MIN_SIZE);
        this.height = Math.max(newH, MIN_SIZE);
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
        return label.getColor();
    }

    public void setFillColor(Color color) {
        label.setColor(color);
    }

}
