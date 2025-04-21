package View;

import Model.Player;

import javax.swing.*;
import java.awt.*;

public class PlayerPanel extends JPanel {
    private Player player;
    private JPanel playerNamePanel;
    private JTextArea playerName;
    private JTextArea playerRole;

    PlayerPanel() {
        super();
        this.player = null;
    }
    PlayerPanel(Player player) {
        super();
        this.player = player;
        Color playerColor = player.getPlayer_color();
        playerNamePanel = new JPanel();
        playerNamePanel.setBackground(playerColor);
        playerName = new JTextArea("Player" + player.getPlayer_name() + " " + " role: ");
        playerNamePanel.add(playerName);
        playerName.setEditable(false);

        playerRole = new JTextArea(player.getPlayer_role().toString()); //TODO
        this.add(playerNamePanel);
        this.add(playerRole);
    }

    public void update(){
        playerName.setText("Player " + player.getPlayer_name());
    }
}
