package ui.handler;

import java.awt.Graphics2D;

public interface MouseHandler {
    void onPressed(int x, int y);

    void onDragged(int x, int y);

    void onReleased(int x, int y);

    default void onMoved(int x, int y) {
    }

    void drawPreview(Graphics2D g);
}
