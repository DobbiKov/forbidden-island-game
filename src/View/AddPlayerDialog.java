package View;

import Helper.AddPlayerCallback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class AddPlayerDialog extends JDialog {
    private JTextField nameField;
    private final AddPlayerCallback callback;

    public AddPlayerDialog(Frame owner, AddPlayerCallback callback) {
        super(owner, "Add New Player", true);
        this.callback = callback;

        setUndecorated(true);
        initComponents();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Add Player");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        content.add(title, BorderLayout.NORTH);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setPreferredSize(new Dimension(250, 30));
        content.add(nameField, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        buttons.add(createButton("Cancel", e -> onCancel()));
        buttons.add(createButton("Add", e -> onAdd()));
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.addActionListener(listener);
        return btn;
    }

    private void onAdd() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            callback.callAddPlayer(name);
            dispose();
        } else {
            nameField.requestFocus();
        }
    }

    private void onCancel() {
        dispose();
    }
}
