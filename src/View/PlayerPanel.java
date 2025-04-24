package View;

import Controller.GameController;
import Model.BoardGame;
import Model.Player;
import Model.Card;
import Model.PlayerAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class PlayerPanel extends JPanel {
    private final GameController gc;
    private final Player player;
    private final JPanel buttonBar = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2));
    private final JPanel cardsBar = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2));
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
        add(buttonBar,        BorderLayout.CENTER);
        add(cardsBar,        BorderLayout.SOUTH);

        update();                                    // initial fill
    }

    /* ---------- public API ---------- */

    public void makeChoosable(Runnable onClick) {
        setBorder(new LineBorder(new Color(255,140,0), 3, true));
        addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e){ onClick.run(); }});
    }
    public void makeUnchoosable() {
        setBorder(null);
        for(MouseListener l : getMouseListeners()) removeMouseListener(l);
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
        revalidate();
        repaint();
    }


    /* ---------- private helpers ---------- */

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(6, 0)) {
            @Override protected void paintComponent(Graphics g) {        // pastel bg
                g.setColor(player.getPlayerColor().getColor());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        header.setBorder(new EmptyBorder(6, 8, 6, 8));

        // avatar 70×70
        var avatar = new JLabel(new ImageIcon(
                new ImageIcon("roles_images/" + player.getPlayer_role().toImgString() + ".png")
                        .getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));

        // name + role
        var name   = new JLabel(player.getPlayer_name());
        name.setFont(name.getFont().deriveFont(Font.BOLD, 15f));
        var role   = new JLabel(player.getPlayer_role().toString());
        role.setFont(role.getFont().deriveFont(Font.PLAIN, 13f));

        var labels = Box.createVerticalBox();
        labels.add(name);
        labels.add(role);

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
        }
        return b;
    }

    private Component buildPlayerCard(Card c) {
        int c_size = 80;
        int w = c_size;
        int h = c_size;
        JPanel imagePanel = new JPanel() {
            private final Image image = new ImageIcon("player_cards_images/" + c.getType().getImgString() + ".png")
                    .getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);

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

    private void openDiscardDialog() {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Choose a card to discard",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dlg.setLayout(new FlowLayout());
        for (Card c : player.getHand().getCards()) {
            JButton btn = new JButton("Discard " + c.getType());
            btn.addActionListener(e -> {
                gc.discardCardFromCurrentPlayer(c);
                dlg.dispose();
            });
            dlg.add(btn);
        }
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
