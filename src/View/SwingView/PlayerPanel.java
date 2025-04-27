package View.SwingView;

import Controller.GameController;
import Helper.ChoosablePlayerCallback;
import Model.*;
import View.SwingView.utils.WrapLayout; // Assuming WrapLayout is in this package

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents the UI panel displaying information and actions for a single player.
 * This includes their avatar, name, role, remaining actions, owned artefacts,
 * hand cards, and available action buttons.
 */
public class PlayerPanel extends JPanel {
    private final GameController gc;
    private final Player player;
    private final Map<PlayerAction, JButton> actionButtons = new EnumMap<>(PlayerAction.class);
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

    /** Placeholder constructor for UI designers or prototyping. */
    public PlayerPanel() {
        this.gc = null;
        this.player = null;
    }

    /**
     * Creates a PlayerPanel associated with a specific player and game controller.
     * Initializes the layout and components, then performs an initial update.
     *
     * @param gc The main game controller.
     * @param p  The Player model object this panel represents.
     */
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

    /**
     * Makes this panel visually distinct (e.g., with a border) and attaches a click listener
     * to the panel itself and its avatar. Used when the player needs to be selected (e.g., by the Navigator).
     *
     * @param onClick The Runnable to execute when this panel or its avatar is clicked.
     */
    public void makeChoosable(Runnable onClick) {
        setBorder(new LineBorder(new Color(255,140,0), 3, true));
        addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e){ onClick.run(); }});

        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    /**
     * Removes the visual distinction and click listeners added by {@link #makeChoosable(Runnable)}.
     */
    public void makeUnchoosable() {
        setBorder(null);
        for(MouseListener l : getMouseListeners()) removeMouseListener(l);
        for(MouseListener l : avatar.getMouseListeners()) { if(l != null) { avatar.removeMouseListener(l); } }
    }

    /**
     * Updates the panel's display based on the current game state.
     * This includes enabling/disabling action buttons, updating the action count badge,
     * refreshing the card display, and showing owned artefacts.
     */
    public void update() {
        ensureButtons();                       // create once

        // enable/disable instead of remove/add
        for (var e : actionButtons.entrySet()) {
            e.getValue().setVisible(
                    gc.getPossiblePlayerActionsForCurrentTurn(player).contains(e.getKey()));
        }
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

    /**
     * Creates the header section of the panel, including the player's avatar,
     * name, role, artefact icons, and action count badge.
     *
     * @return The JComponent representing the header.
     */
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

    /**
     * Ensures that all potential action buttons (one for each PlayerAction enum value)
     * have been created and added to the button bar. Buttons are created lazily on first update.
     */
    private void ensureButtons() {
        for (PlayerAction a : PlayerAction.values()) {
            actionButtons.computeIfAbsent(a, this::createActionButton);
        }
    }

    /**
     * Creates a single JButton for a given PlayerAction, configures its appearance,
     * attaches the appropriate ActionListener to call the GameController, and adds it to the button bar.
     *
     * @param action The PlayerAction this button represents.
     * @return The newly created JButton.
     */
    private JButton createActionButton(PlayerAction action) {
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
        buttonBar.add(b);
        return b;
    }

    /**
     * Creates a visual representation (JPanel with background image) for a player's card.
     * Adds mouse listeners for specific game states (choosing card to give/discard)
     * or for using action cards.
     *
     * @param c The Card model object to display.
     * @return A Component (specifically, a JPanel) representing the card.
     */
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

    /**
     * Returns the Player model object associated with this panel.
     * @return The Player object.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Removes all components from the action badge.
     * Note: This seems unusual, typically you'd hide or update the text. Consider revising if needed.
     */
    public void removeActions(){
        this.actionBadge.removeAll();
    }

}
