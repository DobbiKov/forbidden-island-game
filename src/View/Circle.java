package View;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

class Circle extends JPanel {
    private java.awt.Color color;
    public Circle(java.awt.Color color) {
        super();
        this.color = color;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Enable better quality
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the color and draw a circle (oval with equal width and height)
        g2d.setColor(this.color);
        int diameter = 100;
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;
        g2d.fillOval(x, y, diameter, diameter);
    }
}
