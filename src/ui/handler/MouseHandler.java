package ui.handler;

import java.awt.Graphics2D;
import mode.Mode;

public interface MouseHandler {
    void onPressed(int x, int y);

    void onDragged(int x, int y);

    void onReleased(int x, int y);

    default void onMoved(int x, int y) {}

    default void onActivate(Mode mode) {}

    void drawPreview(Graphics2D g);
}
