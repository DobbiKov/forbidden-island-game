package View.SwingView;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class PawnPanel extends JPanel {

    private Color pawnColor;

    public PawnPanel(Color pawnColor) {
        this.pawnColor = pawnColor;
        setPreferredSize(new Dimension(100, 100));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        double scale = Math.min(w, h) / 100.0;
        g2.scale(scale, scale);

        g2.setColor(pawnColor);

        // pawn head
        Shape head = new Ellipse2D.Double(35, 5, 30, 30);
        // slim neck
        Shape neck = new Rectangle2D.Double(42, 35, 16, 8);
        // bulbous body
        Shape body = new RoundRectangle2D.Double(25, 45, 50, 38, 40, 30);
        // flat base
        Shape base = new Rectangle2D.Double(15, 83, 70, 10);

        g2.fill(head);
        g2.fill(neck);
        g2.fill(body);
        g2.fill(base);

        g2.setPaint(Color.BLACK);            // or any “border” colour
        g2.setStroke(new BasicStroke(3f));   // thickness in user-space (scaled)
        g2.draw(head);
        g2.draw(neck);
        g2.draw(body);
        g2.draw(base);
        g2.dispose();
    }
}



