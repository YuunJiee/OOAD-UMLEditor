package model;

import java.awt.Graphics2D;
import java.awt.Point;

public interface ArrowStyle {
    void draw(Graphics2D g, Point from, Point to);
}
