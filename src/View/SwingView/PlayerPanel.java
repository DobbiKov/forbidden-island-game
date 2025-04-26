package View.SwingView;

import Controller.GameController;
import Model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PlayerPanel extends JPanel {
    private final GameController gc;
    private final Player player;
    private final JPanel buttonBar = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2));
    private final JPanel cardsBar = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2)) {
        @Override
        public Dimension getMinimumSize() {
            return new Dimension(200, 1);
        }
    };
    private final JPanel artefactBar = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 2, 0));
    private JLabel avatar;

    private final JLabel actionBadge = new JLabel();

    public PlayerPanel() {
        this.gc = null;
        this.player = null;
    }
    public PlayerPanel(GameController gc, Player p) {
        this.gc = gc;
        this.player = p;

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);                            // let header paint its own bg

        add(createHeader(), BorderLayout.NORTH);
        add(new JScrollPane(buttonBar),        BorderLayout.CENTER);
        add(new JScrollPane(cardsBar),         BorderLayout.SOUTH);
        artefactBar.setBackground(ResourceMapper.getAwtColor(player.getPlayerColor()));

        update();                                    // initial fill
    }
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(200, 1);
    }
    @Override
    public Dimension getPreferredSize() {
        // let the normal layout compute a size first
        Dimension pref = super.getPreferredSize();
        // enforce a floor of 200 px
        int w = Math.max(pref.width, 200);
        return new Dimension(w, pref.height);
    }

    /* ---------- public API ---------- */

    public void makeChoosable(Runnable onClick) {
        setBorder(new LineBorder(new Color(255,140,0), 3, true));
        addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e){ onClick.run(); }});

        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }
    public void makeUnchoosable() {
        setBorder(null);
        for(MouseListener l : getMouseListeners()) removeMouseListener(l);
        for(MouseListener l : avatar.getMouseListeners()) avatar.removeMouseListener(l);
    }

    public void update() {
        // badge
        if(!gc.isPlayerChoosingZoneToFlyWithCard() && !gc.isNavgiatorChoosingAPlayerToMove()){
            this.makeUnchoosable();
        }
        if (gc.getPlayerForTheTurn() == player) {
            actionBadge.setText(" " + gc.getCurrentPlayerActionsNumber() + " ");
            actionBadge.setVisible(true);
        } else {
            actionBadge.setVisible(false);
        }

        // action buttons
        buttonBar.removeAll();
        for (PlayerAction a : gc.getPossiblePlayerActionsForCurrentTurn(player)) {
            buttonBar.add(buildActionButton(a));
        }
        buttonBar.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));

        //cards
        cardsBar.removeAll();
        for (Card c : gc.getCurrentPlayerCards(player)) {
            cardsBar.add(buildPlayerCard(c));
        }
        cardsBar.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        //artefacts

        artefactBar.removeAll();
        for (Artefact a : player.getArtefacts()) {
            JLabel icon = new JLabel(new ImageIcon( ResourceMapper.getArtefactIcon(a, 24, 24) .getImage()));
            artefactBar.add(icon);
        }
        revalidate();
        repaint();
    }


    /* ---------- private helpers ---------- */

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(6, 0)) {
            @Override protected void paintComponent(Graphics g) {        // pastel bg
                g.setColor(ResourceMapper.getAwtColor(player.getPlayerColor()));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        header.setBorder(new EmptyBorder(6, 8, 6, 8));

        // avatar 70×70
        avatar = new JLabel(new ImageIcon(ResourceMapper.getRoleImage(player.getPlayer_role(), 70, 70).getImage()));

        // name + role
        var name   = new JLabel(player.getPlayer_name());
        name.setFont(name.getFont().deriveFont(Font.BOLD, 15f));
        var role   = new JLabel(player.getPlayer_role().toString());
        role.setFont(role.getFont().deriveFont(Font.PLAIN, 13f));

        var labels = Box.createVerticalBox();
        labels.add(name);
        labels.add(role);
        labels.add(artefactBar);

        // badge
        actionBadge.setOpaque(true);
        actionBadge.setBackground(new Color(30,30,30,200));
        actionBadge.setForeground(Color.WHITE);
        actionBadge.setFont(actionBadge.getFont().deriveFont(Font.BOLD));
        actionBadge.setBorder(new EmptyBorder(2,6,2,6));
        actionBadge.setVisible(false);    // hidden until first update()

        header.add(avatar, BorderLayout.WEST);
        header.add(labels, BorderLayout.CENTER);
        header.add(actionBadge, BorderLayout.EAST);

        return header;
    }

    private JButton buildActionButton(PlayerAction action) {
        JButton b = new JButton(action.toString());
        b.setFocusable(false);
        switch (action) {
            case Move          : b.addActionListener(e -> gc.setPlayerChooseZoneToMoveTo()); break;
            case Drain         : b.addActionListener(e -> gc.setPlayerChooseZoneToShoreUp()); break;
            case FlyToACard    : b.addActionListener(e -> gc.setPilotChooseWhereToFlyTo()); break;
            case MovePlayer    : b.addActionListener(e -> gc.setNavigatorChoosePlayerToMove()); break;
            case DiscardCard   : b.addActionListener(e -> gc.setPlayerDiscardCard()); break;
            case GiveTreasureCard: b.addActionListener(e -> gc.setPlayerGiveTreasureCards()); break;
            case TakeArtefact  : b.addActionListener(e -> gc.takeArtefact()); break;
            case RunFromInaccessibleZone: b.addActionListener(e -> gc.setPlayerChooseZoneToRunFromInaccessbileZone(getPlayer()));
        }
        return b;
    }

    private Component buildPlayerCard(Card c) {
        int c_size = 80;
        int w = c_size;
        int h = c_size;
        JPanel imagePanel = new JPanel() {
            private final Image image = ResourceMapper.getCardImage(c.getType(), w, h).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }
        };
        imagePanel.setPreferredSize(new Dimension(w, h));
        if(gc.isThisPlayerChoosingCardToGive(this.getPlayer())){
            imagePanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 5));
            imagePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    gc.playerChooseCardToGive(getPlayer(), c);
                }
            });
        }else if(gc.isThisPlayerChoosingCardToDiscard(getPlayer())){

            imagePanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 5));
            imagePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    gc.playerDiscardCard(getPlayer(), c);
                }
            });
        }
        else if(c.isAction()){
            imagePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    gc.playerUseActionCard(player, c);
                }
            });
        }
        return imagePanel;
    }

    public Player getPlayer() {
        return player;
    }
    public void removeActions(){
        this.actionBadge.removeAll();
    }

}
