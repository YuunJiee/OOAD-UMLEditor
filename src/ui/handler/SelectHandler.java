package ui.handler;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.function.Consumer;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Cursor;

import model.*;
import ui.CanvasModel;

/**
 * SELECT 模式處理器。
 *
 * 內部子狀態機：
 * NONE → 初始 / 動作完成後
 * MOVING → 拖曳移動一個或多個選取物件
 * RESIZING → 拖曳 port 縮放物件
 * RUBBER_BAND → 在空白區域拖曳框選
 */

public class SelectHandler implements MouseHandler {
    private enum State {
        NONE, MOVING, RESIZING, RUBBER_BAND
    }

    private final CanvasModel model;
    private final Runnable repaint;
    private final Consumer<Cursor> setCursor;

    private State state = State.NONE;
    private UMLObject hoveredObject = null;

    private int pressX, pressY, currentX, currentY;
    private int lastDragX, lastDragY;

    private PortOwner resizingObject;
    private Port resizingPort;
    private int resizeOrigX, resizeOrigY, resizeOrigW, resizeOrigH;

    private static final int DRAG_THRESHOLD = 5;

    public SelectHandler(CanvasModel model, Runnable repaint, Consumer<Cursor> setCursor) {
        this.model = model;
        this.repaint = repaint;
        this.setCursor = setCursor;
    }

    public UMLObject getHoveredObject() {
        return hoveredObject;
    }

    public void clearHover() {
        hoveredObject = null;
    }

    @Override
    public void onPressed(int x, int y) {
        pressX = x;
        pressY = y;
        currentX = x;
        currentY = y;

        for (int i = model.getAll().size() - 1; i >= 0; i--) {
            UMLObject obj = model.getAll().get(i);
            if (obj instanceof PortOwner po && (po.isSelected() || po == hoveredObject)) {
                Port port = po.getPortAt(x, y);
                if (port != null) {
                    startResize(po, port);
                    return;
                }
            }
        }

        UMLObject topObj = model.getTopmostSelectableAt(x, y);
        if (topObj != null) {
            if (!topObj.isSelected()) {
                model.deselectAll();
                topObj.setSelected(true);
                model.bringToFront(topObj);
            }
            startMove();
        } else {
            model.deselectAll();
            state = State.RUBBER_BAND;
        }
    }

    @Override
    public void onDragged(int x, int y) {
        currentX = x;
        currentY = y;
        switch (state) {
            case MOVING -> {
                int dx = x - lastDragX;
                int dy = y - lastDragY;
                model.getSelectedObjects().forEach(obj -> obj.move(dx, dy));
                lastDragX = x;
                lastDragY = y;
            }
            case RESIZING -> applyResize(x, y);
            default -> {
            }
        }
    }

    @Override
    public void onReleased(int x, int y) {
        currentX = x;
        currentY = y;
        if (state == State.RUBBER_BAND) {
            finishRubberBand(x, y);
        } else if (state == State.RESIZING) {
            applyResize(x, y);
        }
        state = State.NONE;
    }

    @Override
    public void onMoved(int x, int y) {
        UMLObject prev = hoveredObject;
        hoveredObject = null;
        boolean onPort = false;

        var all = model.getAll();
        for (int i = all.size() - 1; i >= 0; i--) {
            UMLObject obj = all.get(i);
            if (!(obj instanceof Groupable))
                continue;

            if (hoveredObject == null && obj.contains(x, y)) {
                hoveredObject = obj;
            }

            if (!onPort && obj instanceof PortOwner po &&
                    (po.isSelected() || obj == hoveredObject)) {
                if (po.getPortAt(x, y) != null)
                    onPort = true;
            }
        }

        if (hoveredObject != prev)
            repaint.run();

        if (onPort)
            setCursor.accept(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        else if (hoveredObject != null)
            setCursor.accept(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        else
            setCursor.accept(Cursor.getDefaultCursor());
    }

    @Override
    public void drawPreview(Graphics2D g) {
        if (state != State.RUBBER_BAND)
            return;
        int dx = Math.abs(currentX - pressX);
        int dy = Math.abs(currentY - pressY);
        if (dx < DRAG_THRESHOLD && dy < DRAG_THRESHOLD)
            return;

        int x = Math.min(pressX, currentX);
        int y = Math.min(pressY, currentY);
        int w = Math.abs(currentX - pressX);
        int h = Math.abs(currentY - pressY);

        g.setColor(new Color(0, 100, 220, 35));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0, 80, 200));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10, new float[] { 4f, 4f }, 0));
        g.drawRect(x, y, w, h);
        g.setStroke(new BasicStroke(1));

    }

    private void startResize(PortOwner obj, Port port) {
        state = State.RESIZING;
        resizingObject = obj;
        resizingPort = port;
        resizeOrigX = obj.getX();
        resizeOrigY = obj.getY();
        resizeOrigW = obj.getWidth();
        resizeOrigH = obj.getHeight();
    }

    private void startMove() {
        state = State.MOVING;
        lastDragX = pressX;
        lastDragY = pressY;
    }

    private void finishRubberBand(int mx, int my) {
        int dx = Math.abs(mx - pressX), dy = Math.abs(my - pressY);
        if (dx < DRAG_THRESHOLD && dy < DRAG_THRESHOLD)
            return;

        Rectangle selRect = new Rectangle(
                Math.min(pressX, mx), Math.min(pressY, my),
                Math.abs(mx - pressX), Math.abs(my - pressY));

        for (UMLObject obj : model.getAll()) {
            if (!(obj instanceof Groupable))
                continue;
            obj.setSelected(selRect.contains(obj.getBounds()));
        }
    }

    private void applyResize(int mx, int my) {
        if (resizingObject == null)
            return;
        int[] dir = resizingPort.getDirection();

        int x1 = resizeOrigX, y1 = resizeOrigY;
        int x2 = resizeOrigX + resizeOrigW, y2 = resizeOrigY + resizeOrigH;

        if (dir[0] == 0)
            x1 = mx;
        else if (dir[0] == 2)
            x2 = mx;
        if (dir[1] == 0)
            y1 = my;
        else if (dir[1] == 2)
            y2 = my;

        resizingObject.resize(Math.min(x1, x2), Math.min(y1, y2),
                Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
}
