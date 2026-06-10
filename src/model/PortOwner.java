package model;

public interface PortOwner {
    Port getPortAt(int x, int y);
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    void resize(int newX, int newY, int newW, int newH);
    boolean isSelected();
}
