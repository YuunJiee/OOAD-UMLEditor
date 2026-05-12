package ui;

import mode.Mode;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ToolPanel extends JPanel{
    private static final Color ACTIVE_BG = Color.BLACK;
    private static final Color ACTIVE_FG = Color.WHITE;
    private static final Color NORMAL_BG = new Color(210, 210, 210);
    private static final Color NORMAL_FG = Color.BLACK;

    private static final Mode[] MODES = Mode.values();
    private final JButton[] buttons = new JButton[MODES.length];

    private Consumer<Mode> onModeChange;

    private JButton createModeButton(Mode mode) {
        JButton btn = new JButton(mode.label);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setFocusable(false);
        btn.setBackground(NORMAL_BG);
        btn.setForeground(NORMAL_FG);
        btn.setOpaque(true);
        
        btn.addActionListener(e -> {
            if (onModeChange != null) onModeChange.accept(mode);
        });
        
        return btn;
    }

    public ToolPanel() {
        setLayout(new GridLayout(MODES.length, 1, 4, 4));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(new Color(225, 225, 225));

        for (int i = 0; i < MODES.length; i++) {
            buttons[i] = createModeButton(MODES[i]);
            add(buttons[i]);
        }
    }

    public void setOnModeChange(Consumer<Mode> listener) {
        this.onModeChange = listener;
    }

    public void setActiveMode(Mode mode) {
        for (int i = 0; i < MODES.length; i++) {
            if (MODES[i] == mode) {
                buttons[i].setBackground(ACTIVE_BG);
                buttons[i].setForeground(ACTIVE_FG);
            } else {
                buttons[i].setBackground(NORMAL_BG);
                buttons[i].setForeground(NORMAL_FG);
            }
        }
    }
}
