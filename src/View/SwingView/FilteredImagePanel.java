package View.SwingView;

import Controller.GameController;
import Model.Artefact;
import Model.ArtefactZone;
import Model.Zone;
import Model.ZoneType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Objects;

class FilteredImagePanel extends JPanel {
    private boolean showBlueFilter = false;
    private Zone zone;
    private Image overlayImage;
    private int TILE  = 150;
    private boolean selectable;
    private GameController gameController;
    public boolean showBlueOverlay;
    public boolean isInaccessible;

    public void setBlueFilterVisible(boolean visible) {
        this.showBlueFilter = visible;
        repaint();
    }

    public boolean isBlueFilterVisible() {
        return showBlueFilter;
    }

    public void setInaccessible() {
        this.isInaccessible = true;
        this.setBackground(GUI.backgroundColor);
        repaint();
    }
    public boolean isInaccessible() {
        return isInaccessible;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(TILE, TILE); // your exact size
    }
    @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
    @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

    public FilteredImagePanel(Zone z, int zone_size, GameController gameController) {
        super();
        this.selectable = false;
        this.gameController = gameController;
        this.isInaccessible = false;
        this.TILE = zone_size;
        Dimension fixedSize = new Dimension(this.TILE, this.TILE);
        setPreferredSize(fixedSize);
        setMinimumSize(fixedSize);
        setMaximumSize(fixedSize);
        setSize(fixedSize);
//        this.setLayout(null);
        this.zone = z;
        if(z != null){
            if(z.getZone_type() == ZoneType.ArtefactAssociated){
                Artefact art = ((ArtefactZone)z).getArtefact();
                overlayImage = ResourceMapper.getArtefactIcon(art, -1, -1).getImage();
            }else if(z.getZone_type() == ZoneType.Helicopter){

                overlayImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/artefacts_images/helicopter_no_background.png"))).getImage();
            }
        }
        this.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!FilteredImagePanel.this.isSelectable()) return;        // fast exit
                dispatchZoneClick(FilteredImagePanel.this.zone);                       // switch-statement below
            }
        });

    }
    public void setSelectable() {
        this.selectable = true;
    }
    public void setUnSelectable() {
        this.selectable = false;
    }
    public boolean isSelectable(){
        return this.selectable;
    }
    private void dispatchZoneClick(Zone z) {
        if (gameController.isPlayerChoosingZoneToRunFromInaccesbleZone())
            gameController.chooseZoneToRunFromInaccessible(z);
        else if (gameController.isPlayerChoosingZoneToShoreUp())
            gameController.playerShoreUpZone(z);
        else if (gameController.isPlayerChoosingZoneToMove())
            gameController.movePlayerToTheZone(z);
        else if (gameController.isPlayerChoosingZoneToFlyTo())
            gameController.flyPilotToTheZone(z);
        else if (gameController.isNavgiatorChoosingAZoneToMovePlayerTo())
            gameController.movePlayerToTheZoneByNavigator(z);
        else if (gameController.isPlayerChoosingZoneToFlyWithCard())
            gameController.flyPlayerToZoneWithCard(z);
        else if (gameController.isPlayerChoosingZoneToShoreUpWithCard())
            gameController.shoreUpZoneWithCard(z);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.zone.isAccessible()) {
            Image backgroundImage = ResourceMapper.getZoneCardImage(this.zone.getZoneCard()).getImage();
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

            // draw the overlay in the bottom‐right corner
            if (overlayImage != null) {
//                int imgW = overlayImage.getWidth(null);
//                int imgH = overlayImage.getHeight(null);
//
//                int x = getWidth()  - 10;
//                int y = getHeight() - 10;
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

