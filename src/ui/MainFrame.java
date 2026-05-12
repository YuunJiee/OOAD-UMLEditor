package ui;

import java.awt.*;

import javax.swing.*;

import mode.Mode;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("OOAD UML Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ToolPanel toolPanel = new ToolPanel();
        Canvas canvas = new Canvas();

        JLabel statusBar = new JLabel();
        statusBar.setFont(new Font("Arial", Font.PLAIN, 12));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setOpaque(true);

        java.util.function.Consumer<Mode> syncUI = mode -> {
            toolPanel.setActiveMode(mode);
            statusBar.setText(" Mode: " + mode.statusText);
        };

        toolPanel.setOnModeChange(mode -> {
            canvas.setMode(mode);
            syncUI.accept(mode);
        });
        canvas.setOnModeChange(syncUI);

        syncUI.accept(Mode.SELECT); // 初始化高亮與狀態列

        setJMenuBar(createMenuBar(canvas));

        setLayout(new BorderLayout());
        add(toolPanel, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screen.width * 0.75), (int) (screen.height * 0.80));
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

    }

    private JMenuBar createMenuBar(Canvas canvas) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new FileMenu());
        menuBar.add(new EditMenu(canvas));
        return menuBar;
    }

}
