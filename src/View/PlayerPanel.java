package View;

import Controller.GameController;
import Model.BoardGame;
import Model.Player;
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
    private final JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
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
        }
        return b;
    }

    public Player getPlayer() {
        return player;
    }
    public void removeActions(){
        this.actionBadge.removeAll();
    }
}
