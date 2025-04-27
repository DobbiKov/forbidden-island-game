package View.SwingView;

import Helper.AddPlayerCallback; // Assuming AddPlayerCallback is in this package

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A simple modal dialog window for adding a new player by entering their name.
 * Uses a callback interface to notify the caller when a name is entered or the dialog is cancelled.
 */
public class AddPlayerDialog extends JDialog {
    private JTextField nameField;
    private final AddPlayerCallback callback;

    /**
     * Creates the Add Player dialog.
     *
     * @param owner    The Frame from which the dialog is displayed.
     * @param callback The callback object to notify upon adding a player or cancelling.
     */
    public AddPlayerDialog(Frame owner, AddPlayerCallback callback) {
        super(owner, "Add New Player", true);
        this.callback = callback;

        setUndecorated(true);
        initComponents();
    }

    /**
     * Initializes the dialog's components (title, text field, buttons) and layout.
     */
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

    /**
     * Helper method to create and style the dialog buttons ("Cancel", "Add").
     *
     * @param text     The text to display on the button.
     * @param listener The ActionListener to attach to the button.
     * @return The configured JButton.
     */
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

    /**
     * Action performed when the "Add" button is clicked.
     * Retrieves the name, validates it's not empty, calls the callback, and closes the dialog.
     * If the name is empty, sets focus back to the name field.
     */
    private void onAdd() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            callback.callAddPlayer(name);
            dispose();
        } else {
            nameField.requestFocus();
        }
    }

    /**
     * Action performed when the "Cancel" button is clicked or the dialog is closed.
     * Simply disposes of the dialog window.
     */
    private void onCancel() {
        dispose();
    }
}
