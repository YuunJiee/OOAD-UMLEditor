package ui.handler;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

import mode.Mode;
import model.BasicObject;
import model.OvalObject;
import model.RectObject;
import ui.CanvasModel;

/**
 * RECT / OVAL 建立模式處理器。
 *
 * 流程：
 * 1. onPressed：記錄起點座標
 * 2. onDragged：更新當前座標（讓 drawPreview 顯示虛線輪廓）
 * 3. onReleased：計算邊界框，建立新物件，呼叫 onCreated 回呼（Canvas 用來切回前一模式）
 */
public class CreateHandler implements MouseHandler {
    private final CanvasModel model;
    private final Runnable onCreated;
    private Mode shapeMode = Mode.RECT;

    private boolean drawing = false;
    private int pressX, pressY, currentX, currentY;

    public CreateHandler(CanvasModel model, Runnable onCreated) {
        this.model = model;
        this.onCreated = onCreated;
    }

    public void setShapeMode(Mode mode) {
        this.shapeMode = mode;
    }

    @Override
    public void onPressed(int x, int y) {
        pressX = x;
        pressY = y;
        currentX = x;
        currentY = y;
        drawing = true;
    }

    @Override
    public void onDragged(int x, int y) {
        currentX = x;
        currentY = y;
    }

    @Override
    public void onReleased(int x, int y) {
        if (!drawing)
            return;
        drawing = false;

        int bx = Math.min(pressX, x);
        int by = Math.min(pressY, y);
        int bw = Math.max(Math.abs(x - pressX), BasicObject.MIN_SIZE);
        int bh = Math.max(Math.abs(y - pressY), BasicObject.MIN_SIZE);

        BasicObject obj = (shapeMode == Mode.RECT)
                ? new RectObject(bx, by, bw, bh)
                : new OvalObject(bx, by, bw, bh);
        model.add(obj);
        onCreated.run(); // 通知 Canvas 建立完成，切回前一模式
    }

    @Override
    public void drawPreview(Graphics2D g) {
        if (!drawing)
            return;

        int x = Math.min(pressX, currentX);
        int y = Math.min(pressY, currentY);
        int w = Math.max(Math.abs(currentX - pressX), 1);
        int h = Math.max(Math.abs(currentY - pressY), 1);

        g.setColor(new Color(80, 80, 80, 160));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10, new float[] { 6f, 4f }, 0));
        if (shapeMode == Mode.RECT) {
            g.drawRect(x, y, w, h);
        } else {
            g.drawOval(x, y, w, h);
        }
        g.setStroke(new BasicStroke(1));

    }
}
