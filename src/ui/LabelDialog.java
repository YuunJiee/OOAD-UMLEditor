package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import model.AppearanceEditable;

public class LabelDialog extends JDialog {
    private final JTextField nameField;
    private Color selectedColor;

    public LabelDialog(Window owner, AppearanceEditable target) {
        super(owner, "Customize Label Style", ModalityType.APPLICATION_MODAL);
        selectedColor = target.getFillColor();

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        form.add(new JLabel("Name:"));
        nameField = new JTextField(target.getLabel(), 12);
        nameField.selectAll();
        form.add(nameField);

        form.add(new JLabel("Color:"));
        JButton colorBtn = new JButton(" ");
        colorBtn.setBackground(selectedColor);
        colorBtn.setOpaque(true);
        colorBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose Color", selectedColor);
            if (chosen != null) {
                selectedColor = chosen;
                colorBtn.setBackground(chosen);
            }
        });
        form.add(colorBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton okBtn = new JButton("OK");

        cancelBtn.addActionListener(e -> dispose());

        okBtn.addActionListener(e -> {
            target.setLabel(nameField.getText());
            target.setFillColor(selectedColor);
            dispose();
        });

        // 按下 Enter 直接觸發 OK
        this.getRootPane().setDefaultButton(okBtn);

        // 按下 Esc 鍵關閉視窗
        this.getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }


}
