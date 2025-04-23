package View;

import Controller.GameController;
import Model.BoardGame;
import Model.Player;
import Model.PlayerAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class PlayerPanel extends JPanel {
    private Player player;
    private JPanel playerNamePanel;
    private JPanel playerProfilePanel;
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

        this.playerProfilePanel = new JPanel();
        playerProfilePanel.setLayout(new GridLayout(1, 2));

        //------
        ImageIcon originalIcon = new ImageIcon("roles_images/" + player.getPlayer_role().toImgString() + ".png");
        Image originalImage = originalIcon.getImage();

        Image resizedImage = originalImage.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);
        JLabel player_img = new JLabel(resizedIcon);
        //------

        Color playerColor = player.getPlayerColor().getColor();
        playerNamePanel = new JPanel();
        playerName = new JTextArea(player.getPlayer_name());
        playerName.setEditable(false);


        playerActionsNumText = new JTextArea("");
        playerActionsNumText.setEditable(false);


        playerRole = new JTextArea(player.getPlayer_role().toString());
        playerRole.setEditable(false);

        playerNamePanel.add(playerName);
        playerNamePanel.add(playerRole);
        playerNamePanel.add(playerActionsNumText);

        playerNamePanel.setLayout(new GridLayout(3, 1));
        playerName.setBackground(playerColor);

        playerProfilePanel.add(player_img);
        playerProfilePanel.add(playerNamePanel);




        actionButtonsPanel = new JPanel();
        actionButtonsPanel.setLayout(new GridLayout(3, 3));

        this.add(playerProfilePanel);
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
            case FlyToACard:
                button.addActionListener(e -> {
                    this.gameController.setPilotChooseWhereToFlyTo();
                });
                break;
            case MovePlayer:
                button.addActionListener(e -> {
                    this.gameController.setNavigatorChoosePlayerToMove();
                });
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
    public void makeChoosable(Runnable action){
        this.removeActions();
        this.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 10));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
    }
    public void makeUnchoosable(){
        this.setBorder(null);
        if(this.getMouseListeners().length > 0) {
            this.removeMouseListener(this.getMouseListeners()[0]);
        }
    }
    public Player getPlayer(){
        return player;
    }
}
