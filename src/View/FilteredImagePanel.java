package View;

import Model.Zone;

import javax.swing.*;
import java.awt.*;

class FilteredImagePanel extends JPanel {
    private boolean showBlueFilter = false;
    private Zone zone;

    public void setBlueFilterVisible(boolean visible) {
        this.showBlueFilter = visible;
        repaint();
    }

    public boolean isBlueFilterVisible() {
        return showBlueFilter;
    }

    public FilteredImagePanel(Zone z) {
        super();
        this.zone = z;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.zone.isAccessible()) {
            Image backgroundImage = new ImageIcon("island_card_images/" + this.zone.getZoneCard().toString() + ".png").getImage();
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

            if (showBlueFilter) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 255, 100));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        }
    }
}

