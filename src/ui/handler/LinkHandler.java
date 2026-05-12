package ui.handler;

import mode.Mode;
import model.AssociationLink;
import model.BasicObject;
import model.CompositionLink;
import model.GeneralizationLink;
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
 * 1. onPressed：用 port-proximity 偵測起點 BasicObject + port
 * 2. onDragged：更新預覽線終點座標
 * 3. onReleased：用 port-proximity 偵測終點，建立 LinkObject 並加入 model 底層
 */
public class LinkHandler implements MouseHandler {
    private final CanvasModel model;
    private Mode linkMode = Mode.ASSOCIATION;

    private boolean active = false;
    private BasicObject fromObject;
    private int fromPort;
    private int currentX, currentY;

    public LinkHandler(CanvasModel model) {
        this.model = model;
    }

    public void setLinkMode(Mode mode) {
        this.linkMode = mode;
    }

    @Override
    public void onPressed(int x, int y) {
        BasicObject obj = model.getBasicObjectByPort(x, y);
        if (obj == null) {
            active = false;
            return;
        }
        fromObject = obj;
        fromPort = obj.getPortIndexAt(x, y);
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

        BasicObject toObj = model.getBasicObjectByPort(x, y);
        if (toObj == null || toObj == fromObject)
            return;
        int toPort = toObj.getPortIndexAt(x, y);

        LinkObject link = switch (linkMode) {
            case ASSOCIATION -> new AssociationLink(fromObject, fromPort, toObj, toPort);
            case GENERALIZATION -> new GeneralizationLink(fromObject, fromPort, toObj, toPort);
            case COMPOSITION -> new CompositionLink(fromObject, fromPort, toObj, toPort);
            default -> null;
        };
        if (link != null)
            model.addAtBottom(link);
    }

    @Override
    public void drawPreview(Graphics2D g) {
        if (!active || fromObject == null)
            return;
        Port p1 = fromObject.getPorts()[fromPort];

        drawPortHighlight(g, p1, new Color(255, 140, 0));

        BasicObject targetObj = model.getBasicObjectByPort(currentX, currentY);
        if (targetObj != null && targetObj != fromObject) {
            Port p2 = targetObj.getPorts()[targetObj.getPortIndexAt(currentX, currentY)];
            drawPortHighlight(g, p2, new Color(0, 200, 80));
        }

        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(p1.getX(), p1.getY(), currentX, currentY);
    }

    private void drawPortHighlight(Graphics2D g, Port p, Color color) {
        int size = Port.SIZE + 4;
        g.setColor(color);
        g.fillRect(p.getX() - Port.HALF - 2, p.getY() - Port.HALF - 2, size, size);
    }

}
