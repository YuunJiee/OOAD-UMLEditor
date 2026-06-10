package model;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Label {
    private String name;

    public Label(String name) {
        this.name = name;
    }

    public void draw(Graphics2D g, int x, int y, int width, int height) {
        if (name == null || name.isEmpty())
            return;
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(name);
        int tx = x + (width - tw) / 2;
        int ty = y + (height + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Color.BLACK);
        g.drawString(name, tx, ty);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
