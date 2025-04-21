package View;

import Model.Player;
import Model.PlayerAction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PlayerPanel extends JPanel {
    private Player player;
    private JPanel playerNamePanel;
    private JTextArea playerName;
    private JTextArea playerRole;
    JPanel actionButtonsPanel;

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

        playerRole = new JTextArea(player.getPlayer_role().toString());
        this.add(playerNamePanel);
        this.add(playerRole);

        actionButtonsPanel = new JPanel();
        actionButtonsPanel.setLayout(new GridLayout(3, 3));
        this.add(actionButtonsPanel);
    }

    public void setActions(ArrayList<PlayerAction> actions){
        for(PlayerAction action : actions){
            JButton button = new JButton();
            button.setText(action.toString());
           this.actionButtonsPanel.add(button);
        }
    }
    public void removeActions(){
        this.actionButtonsPanel.removeAll();
    }

    public void update(){
        playerName.setText("Player " + player.getPlayer_name());
    }
    public Player getPlayer(){
        return player;
    }
}
