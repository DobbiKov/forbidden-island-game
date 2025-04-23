package View;

import Model.Artefact;
import Model.ArtefactZone;
import Model.Zone;
import Model.ZoneType;

import javax.swing.*;
import java.awt.*;

class FilteredImagePanel extends JPanel {
    private boolean showBlueFilter = false;
    private Zone zone;
    private Image overlayImage;

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
        if(z != null){
            if(z.getZone_type() == ZoneType.ArtefactAssociated){
                Artefact art = ((ArtefactZone)z).getArtefact();
                overlayImage = new ImageIcon("artefacts_images/" + art.toImgString() + ".png").getImage();
            }else if(z.getZone_type() == ZoneType.Helicopter){

                overlayImage = new ImageIcon("artefacts_images/" + "helicopter_no_background" + ".png").getImage();
            }
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.zone.isAccessible()) {
            Image backgroundImage = new ImageIcon("island_card_images/" + this.zone.getZoneCard().toString() + ".png").getImage();
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

            // draw the overlay in the bottom‐right corner
            if (overlayImage != null) {
                System.out.println("trying to draw ");
                int imgW = overlayImage.getWidth(null);
                int imgH = overlayImage.getHeight(null);

                int x = getWidth()  - 10;
                int y = getHeight() - 10;
                g.drawImage(overlayImage,
                        getWidth() - 50, getHeight() - 50,
                        50, 50,
                        this);
            }

            if (showBlueFilter) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 255, 100));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        }
    }
}

