package ui;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import model.BasicObject;
import model.CompositeObject;
import model.LinkObject;
import model.UMLObject;

public class EditMenu extends JMenu {

    private final JMenuItem groupItem;
    private final JMenuItem ungroupItem;
    private final JMenuItem labelItem;

    public EditMenu(Canvas canvas) {
        super("Edit");

        groupItem = createMenuItem("Group", KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK,
                e -> canvas.groupSelected());
        ungroupItem = createMenuItem("Ungroup", KeyEvent.VK_G,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                e -> canvas.ungroupSelected());
        labelItem = createMenuItem("Label", 0, 0, e -> canvas.editLabel());

        add(groupItem);
        add(ungroupItem);
        addSeparator();
        add(labelItem);

        addMenuListener(new MenuListener() {
            @Override public void menuSelected(MenuEvent e) { updateState(canvas); }
            @Override public void menuDeselected(MenuEvent e) {}
            @Override public void menuCanceled(MenuEvent e) {}
        });
    }

    private void updateState(Canvas canvas) {
        List<UMLObject> sel = canvas.getSelectedObjects();
        long nonLinkCount = sel.stream().filter(o -> !(o instanceof LinkObject)).count();
        groupItem.setEnabled(nonLinkCount >= 2);
        ungroupItem.setEnabled(sel.size() == 1 && sel.get(0) instanceof CompositeObject);
        labelItem.setEnabled(sel.size() == 1 && sel.get(0) instanceof BasicObject);
    }

    private JMenuItem createMenuItem(String text, int key, int mask, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        if (key != 0)
            item.setAccelerator(KeyStroke.getKeyStroke(key, mask));
        item.addActionListener(listener);
        return item;
    }
}
