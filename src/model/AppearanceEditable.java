package model;

import java.awt.Color;

public interface AppearanceEditable {
    String getLabel();
    void setLabel(String name);
    Color getFillColor();
    void setFillColor(Color color);
}
