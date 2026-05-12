package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.*;

import mode.Mode;
import model.BasicObject;
import model.UMLObject;

import ui.handler.*;

public class Canvas extends JPanel {

    private final CanvasModel model = new CanvasModel();

    private Mode currentMode = Mode.SELECT;
    private Mode previousMode = Mode.SELECT;
    private Consumer<Mode> onModeChange;

    private final SelectHandler selectHandler;
    private final LinkHandler linkHandler;
    private final CreateHandler createHandler;

    public Canvas() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160)));
        setFocusable(true);

        selectHandler = new SelectHandler(model, this::repaint, this::setCursor);
        linkHandler = new LinkHandler(model);
        createHandler = new CreateHandler(model, () -> notifyModeChange(previousMode));

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
                activeHandler().onDragged(e.getX(), e.getY());
                repaint();
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
        if (mode.isLinkMode()) {
            linkHandler.setLinkMode(mode);
        } else if (mode == Mode.RECT || mode == Mode.OVAL) {
            createHandler.setShapeMode(mode);
        }
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
        List<BasicObject> basics = model.getSelectedObjects().stream()
                .filter(o -> o instanceof BasicObject)
                .map(o -> (BasicObject) o)
                .collect(Collectors.toList());

        if (basics.size() != 1)
            return;
        new LabelDialog(SwingUtilities.getWindowAncestor(this), basics.get(0)).setVisible(true);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isLinkMode = currentMode.isLinkMode();
        UMLObject hovered = selectHandler.getHoveredObject();

        for (UMLObject obj : model.getLinks()) {
            obj.draw(g2, false);
        }

        for (UMLObject obj : model.getNonLinks()) {
            boolean showPorts = (obj == hovered) || (isLinkMode && obj instanceof BasicObject);
            obj.draw(g2, showPorts);
        }

        activeHandler().drawPreview(g2);
        g2.dispose();
    }

    private MouseHandler activeHandler() {
        return switch (currentMode) {
            case SELECT -> selectHandler;
            case ASSOCIATION, GENERALIZATION, COMPOSITION -> linkHandler;
            case RECT, OVAL -> createHandler;
        };
    }

    private void notifyModeChange(Mode mode) {
        currentMode = mode;
        setCursor(Cursor.getDefaultCursor());
        if (onModeChange != null)
            onModeChange.accept(mode);
    }

}
