package ui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class FileMenu extends JMenu {

    public FileMenu() {
        super("File");
        add(createMenuItem("Exit", 0, 0, e -> System.exit(0)));
    }

    private JMenuItem createMenuItem(String text, int key, int mask, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        if (key != 0)
            item.setAccelerator(KeyStroke.getKeyStroke(key, mask));
        item.addActionListener(listener);
        return item;
    }
}
