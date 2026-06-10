package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;

import mode.Mode;
import model.BasicObject;
import model.AppearanceEditable;
import model.UMLObject;
import model.RectObject;
import model.OvalObject;
import model.LinkObject;
import model.OpenArrowStyle;
import model.HollowTriangleStyle;
import model.DiamondStyle;

import ui.handler.*;

public class Canvas extends JPanel {

    private final CanvasModel model = new CanvasModel();

    private Mode currentMode = Mode.SELECT;
    private Mode previousMode = Mode.SELECT;
    private Consumer<Mode> onModeChange;

    private final SelectHandler selectHandler;
    private final Map<Mode, MouseHandler> handlers = new EnumMap<>(Mode.class);

    public Canvas() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160)));
        setFocusable(true);

        selectHandler = new SelectHandler(model, this::repaint, this::setCursor);

        handlers.put(Mode.SELECT, selectHandler);
        handlers.put(Mode.ASSOCIATION, new LinkHandler(model, (f, fp, t, tp) -> new LinkObject(f, fp, t, tp, new OpenArrowStyle())));
        handlers.put(Mode.GENERALIZATION, new LinkHandler(model, (f, fp, t, tp) -> new LinkObject(f, fp, t, tp, new HollowTriangleStyle())));
        handlers.put(Mode.COMPOSITION, new LinkHandler(model, (f, fp, t, tp) -> new LinkObject(f, fp, t, tp, new DiamondStyle())));
        
        CreateHandler.ShapeFactory rectFactory = new CreateHandler.ShapeFactory() {
            public BasicObject create(int x, int y, int w, int h) { return new RectObject(x, y, w, h); }
            public void drawPreview(Graphics2D g, int x, int y, int w, int h) { g.drawRect(x, y, w, h); }
        };
        CreateHandler.ShapeFactory ovalFactory = new CreateHandler.ShapeFactory() {
            public BasicObject create(int x, int y, int w, int h) { return new OvalObject(x, y, w, h); }
            public void drawPreview(Graphics2D g, int x, int y, int w, int h) { g.drawOval(x, y, w, h); }
        };

        handlers.put(Mode.RECT, new CreateHandler(model, () -> notifyModeChange(previousMode), rectFactory));
        handlers.put(Mode.OVAL, new CreateHandler(model, () -> notifyModeChange(previousMode), ovalFactory));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    activeHandler().onPressed(e.getX(), e.getY());
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    activeHandler().onDragged(e.getX(), e.getY());
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    activeHandler().onReleased(e.getX(), e.getY());
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                activeHandler().onMoved(e.getX(), e.getY());
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
                repaint();
            }
        });
    }

    public void setMode(Mode mode) {
        if ((mode == Mode.RECT || mode == Mode.OVAL) &&
                currentMode != Mode.RECT && currentMode != Mode.OVAL) {
            previousMode = currentMode;
        }
        currentMode = mode;
        handlers.get(mode).onActivate(mode);
        selectHandler.clearHover();
        setCursor(Cursor.getPredefinedCursor(mode == Mode.SELECT ? Cursor.DEFAULT_CURSOR : Cursor.CROSSHAIR_CURSOR));
        repaint();
    }

    public void setOnModeChange(Consumer<Mode> listener) {
        this.onModeChange = listener;
    }

    public List<UMLObject> getSelectedObjects() {
        return model.getSelectedObjects();
    }

    public void deleteSelected() {
        model.deleteSelected();
        repaint();
    }

    public void groupSelected() {
        model.groupSelected();
        repaint();
    }

    public void ungroupSelected() {
        model.ungroupSelected();
        repaint();
    }

    public void editLabel() {
        List<UMLObject> sel = model.getSelectedObjects();
        if (sel.size() != 1 || !(sel.get(0) instanceof AppearanceEditable le))
            return;
        new LabelDialog(SwingUtilities.getWindowAncestor(this), le).setVisible(true);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UMLObject hovered = selectHandler.getHoveredObject();

        model.getAll().stream()
                .sorted(java.util.Comparator.comparingInt(UMLObject::getZIndex))
                .forEach(obj -> obj.draw(g2, currentMode.shouldShowPortsFor(obj, hovered)));

        activeHandler().drawPreview(g2);
        g2.dispose();
    }

    private MouseHandler activeHandler() {
        return handlers.get(currentMode);
    }

    private void notifyModeChange(Mode mode) {
        currentMode = mode;
        setCursor(Cursor.getDefaultCursor());
        if (onModeChange != null)
            onModeChange.accept(mode);
    }

}
