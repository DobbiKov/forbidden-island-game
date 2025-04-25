package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InfoDialog extends JDialog {
    public InfoDialog(Frame owner, String title, String message) {
        super(owner, "Info: " + title, true);
        setUndecorated(true);
        initComponents(message);
    }

    private void initComponents(String message) {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(254, 242, 242));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel msgLabel = new JLabel(
                String.format("<html><body style='width:250px; font-family: 'Segoe UI'; font-size: 14px;'>%s</body></html>",
                        escapeHtml(message))
        );
        content.add(msgLabel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        JButton ok = new JButton("OK");
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.setBackground(new Color(52, 152, 219));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        ok.setOpaque(true);
        ok.setContentAreaFilled(true);
        ok.addActionListener(e -> dispose());
        btnPanel.add(ok);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
