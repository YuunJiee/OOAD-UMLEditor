package ui.handler;

import model.PortOwner;
import model.LinkObject;
import model.Port;
import ui.CanvasModel;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;

/**
 * Association / Generalization / Composition 連結模式處理器。
 *
 * 流程：
 * 1. onPressed：用 port-proximity 偵測起點 PortOwner + port
 * 2. onDragged：更新預覽線終點座標
 * 3. onReleased：用 port-proximity 偵測終點，建立 LinkObject 並加入 model 底層
 */
public class LinkHandler implements MouseHandler {
    private static final int HIGHLIGHT_EXTRA = 2;

    public interface LinkFactory {
        LinkObject create(PortOwner from, Port fromPort, PortOwner to, Port toPort);
    }

    private final CanvasModel model;
    private final LinkFactory factory;

    private boolean active = false;
    private PortOwner fromObject;
    private Port fromPort;
    private int currentX, currentY;

    public LinkHandler(CanvasModel model, LinkFactory factory) {
        this.model = model;
        this.factory = factory;
    }

    @Override
    public void onPressed(int x, int y) {
        PortOwner obj = model.getPortOwnerByPort(x, y);
        if (obj == null) {
            active = false;
            return;
        }
        fromObject = obj;
        fromPort = obj.getPortAt(x, y);
        currentX = x;
        currentY = y;
        active = true;
    }

    @Override
    public void onDragged(int x, int y) {
        currentX = x;
        currentY = y;
    }

    @Override
    public void onReleased(int x, int y) {
        if (!active)
            return;
        active = false;

        PortOwner toObj = model.getPortOwnerByPort(x, y);
        if (toObj == null || toObj == fromObject)
            return;
        Port toPort = toObj.getPortAt(x, y);
        if (toPort == null)
            return;

        LinkObject link = factory.create(fromObject, fromPort, toObj, toPort);
        if (link != null)
            model.addAtBottom(link);
    }

    @Override
    public void drawPreview(Graphics2D g) {
        if (!active || fromObject == null)
            return;
        Port p1 = fromPort;

        drawPortHighlight(g, p1, new Color(255, 140, 0));

        PortOwner targetObj = model.getPortOwnerByPort(currentX, currentY);
        if (targetObj != null && targetObj != fromObject) {
            Port p2 = targetObj.getPortAt(currentX, currentY);
            if (p2 != null) drawPortHighlight(g, p2, new Color(0, 200, 80));
        }

        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(p1.getX(), p1.getY(), currentX, currentY);
    }

    private void drawPortHighlight(Graphics2D g, Port p, Color color) {
        int size = Port.SIZE + HIGHLIGHT_EXTRA * 2;
        g.setColor(color);
        g.fillRect(p.getX() - Port.HALF - HIGHLIGHT_EXTRA, p.getY() - Port.HALF - HIGHLIGHT_EXTRA, size, size);
    }

}
