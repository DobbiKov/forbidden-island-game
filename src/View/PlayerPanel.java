package View;

import Controller.GameController;
import Model.BoardGame;
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
    private JTextArea playerActionsNumText;
    JPanel actionButtonsPanel;
    private GameController gameController;

    PlayerPanel() {
        super();
        this.player = null;
    }
    PlayerPanel(GameController gController, Player player) {
        super();
        this.player = player;
        this.gameController = gController;

        Color playerColor = player.getPlayer_color();
        playerNamePanel = new JPanel();
        playerName = new JTextArea("Player: " + player.getPlayer_name() + " " + " role: ");
        playerNamePanel.add(playerName);
        playerName.setEditable(false);

        playerActionsNumText = new JTextArea("");
        playerActionsNumText.setEditable(false);
        playerNamePanel.add(playerActionsNumText);

        playerRole = new JTextArea(player.getPlayer_role().toString());
        playerRole.setEditable(false);
        playerNamePanel.add(playerRole);

        playerNamePanel.setLayout(new GridLayout(2, 1));
        playerName.setBackground(playerColor);

        this.add(playerNamePanel);


        actionButtonsPanel = new JPanel();
        actionButtonsPanel.setLayout(new GridLayout(3, 3));
        this.add(actionButtonsPanel);
        this.setLayout(new GridLayout(2, 1));
    }

    public void setActions(ArrayList<PlayerAction> actions){
        actionButtonsPanel.removeAll();
        for(PlayerAction action : actions){
            JButton button = this.getActionButton(action);
           this.actionButtonsPanel.add(button);
        }
    }
    public JButton getActionButton(PlayerAction action){
        JButton button = new JButton();
        button.setText(action.toString());
        switch(action){
            case Move:
                button.addActionListener(e -> {
                    this.gameController.setPlayerChooseZoneToMoveTo();
                });
                break;
            case Drain:
                button.addActionListener(e -> {
                    this.gameController.setPlayerChooseZoneToShoreUp();
                });
                break;
            default: break;
        }
        return button;
    }

    public void removeActions(){
        this.actionButtonsPanel.removeAll();
    }

    public void update(){
//        playerName.setText("Player: " + player.getPlayer_name());

        if(gameController.getPlayerForTheTurn() == this.player){
            playerActionsNumText.setText("Actions left: " + gameController.getCurrentPlayerActionsNumber());
        }else{
            playerActionsNumText.setText("");
        }
        playerNamePanel.validate();
        playerNamePanel.repaint();
    }
    public Player getPlayer(){
        return player;
    }
}
