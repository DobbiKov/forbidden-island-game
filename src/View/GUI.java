package View;
import Controller.GameController;
import Errors.InvalidNumberOfPlayersException;
import Errors.MaximumNumberOfPlayersReachedException;
import Errors.NoPlayersException;
import Helper.AddPlayerCallback;
import Helper.ChoosablePlayerCallback;
import Model.Player;
import Model.PlayerAction;
import Model.Zone;
import Model.ZoneState;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import javax.swing.*;

public class GUI {
    private static GameController gameController;
    private static int w_width = 800;
    private static int w_height = 600;

    private static Zone[][] zones;
    private static int zone_size;

    private static JPanel[][] zonePanels;

    private static PlayerPanel[] panelPlayers;
    private static int player_panel_size = 0;

    private static JPanel rightPanel;
    private static JPanel leftPanel;
    private static JFrame window;
    private static JPanel boardPanel;
    private static JPanel buttonPanel;
    private static JButton add_player;

    private static JButton start_game;

    private static JPanel getZone(Zone z){
        JPanel button = new FilteredImagePanel(z);
        button.setSize(zone_size, zone_size);

        return button;
    }

    public static void makePlayersChoosable(HashSet<Player> players, ChoosablePlayerCallback callback){
        for(PlayerPanel panel : panelPlayers){
            if(players.contains(panel.getPlayer())){
                panel.makeChoosable(() -> {
                    callback.choose(panel.getPlayer());
                });
            }
        }
    }
    public static void makePlayersUnChoosable(){
        for(PlayerPanel panel : panelPlayers){
            panel.makeUnchoosable();
        }
    }
    public static void removeActionsForPlayerPanel(){
        for(PlayerPanel p : panelPlayers){
            if(p.getPlayer() == gameController.getPlayerForTheTurn()){
                p.removeActions();
            }
        }
    }
    public static void showErrorMess(String title, String message){
        ErrorPopup.CreateErrorPopup(window, title, message);
    }
    public static void addPlayerPanel(Player new_player){
        panelPlayers[player_panel_size++] = new PlayerPanel(gameController, new_player);
        updatePlayerPanels();
        updateZonePanels();
    }

    public static void updateZonePanels() {
        zones = gameController.getZones(); // get the new state
        HashSet<Zone> zoneSet = new HashSet<>();
        if(gameController.isPlayerChoosingSomething()){
            zoneSet = new HashSet<>(gameController.getZonesPossibleForChoosing());
        }
        for (int i = 0; i < zones.length; i++) {
            for (int j = 0; j < zones[i].length; j++) {
                ZoneState state = zones[i][j].getZone_state();
                JPanel panel = zonePanels[i][j];
                if(panel.getMouseListeners().length > 0)
                    panel.removeMouseListener(panel.getMouseListeners()[0]);
                panel.removeAll();

                switch (state) {
                    case Normal:
                        ((FilteredImagePanel)panel).setBlueFilterVisible(false);
                        panel.setBorder(null);
                        break;
                    case Flooded:
                        ((FilteredImagePanel)panel).setBlueFilterVisible(true);
                        panel.setBorder(null);
                        break;
                    case Inaccessible:
                        panel.setBackground(Color.WHITE);
                        panel.setBorder(null);
                        break;
                }
                if(zones[i][j] != null) {
                    for (Player player : zones[i][j].getPlayers_on_zone()) {
                        java.awt.Color color = player.getPlayer_color();
                        JPanel circle = new Circle(color);
                        circle.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                        panel.add(circle);
                    }
                }
                if(zoneSet.contains(zones[i][j])){
                    panel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 5));
                    int finalI = i;
                    int finalJ = j;
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if(gameController.isPlayerChoosingZoneToShoreUp()){
                                gameController.playerShoreUpZone(zones[finalI][finalJ]);
                            }
                            else if(gameController.isPlayerChoosingZoneToMove()) {
                                gameController.movePlayerToTheZone(zones[finalI][finalJ]);
                            }else if(gameController.isPlayerChoosingZoneToFlyTo()){
                                gameController.flyPilotToTheZone(zones[finalI][finalJ]);
                            }else if(gameController.isNavgiatorChoosingAZoneToMovePlayerTo()){
                                gameController.movePlayerToTheZoneByNavigator(zones[finalI][finalJ]);
                            }
                            updatePlayerPanels();
                            updateZonePanels();
                            window.repaint();
                        }
                    });
                }
                panel.validate();
            }
        }
    }
    public static void updatePlayerPanels(){
        for(PlayerPanel panel : panelPlayers){
            if(panel.getPlayer() == null){
                continue;
            }
            panel.update();
            if(gameController.getPlayerForTheTurn() == null) break;
            if(gameController.getPlayerForTheTurn().getPlayer_id() == panel.getPlayer().getPlayer_id()
            ){

                panel.setActions(
                    gameController.getPossibleActionsForPlayer(panel.getPlayer())
                );
            }
        }

        window.remove(rightPanel);
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 1));
        rightPanel.add(panelPlayers[0]);
        rightPanel.add(panelPlayers[1]);
        rightPanel.setVisible(true);
        window.add(rightPanel, BorderLayout.EAST);

        window.remove(leftPanel);
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1));
        leftPanel.add(panelPlayers[2]);
        leftPanel.add(panelPlayers[3]);
        leftPanel.setVisible(true);
        window.add(leftPanel, BorderLayout.WEST);

        window.validate();
    }

    public static void startGameHandleView(){
        JButton fin_de_tour = new JButton("Fin de Tour");
        fin_de_tour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameController.finDeTour();
                updateZonePanels();
            }
        });
        fin_de_tour.setSize(100, 50);
        fin_de_tour.setVisible(true);

        buttonPanel.remove(add_player);
        buttonPanel.remove(start_game);
        buttonPanel.add(fin_de_tour);
        buttonPanel.setBackground(Color.WHITE);
        updatePlayerPanels();
        window.validate();
        window.repaint();
    }

    public static void main(String[] args)
    {
        gameController = new GameController();
        zones = gameController.getZones();

        // init player panels
        rightPanel = new JPanel();
        leftPanel = new JPanel();
        panelPlayers = new PlayerPanel[4];
        for(int i = 0; i < panelPlayers.length; i++){
            panelPlayers[i] = new PlayerPanel();
        }

        boardPanel = new JPanel();
        buttonPanel = new JPanel();
        window = new JFrame();

        zone_size = 50;

        zonePanels = new JPanel[zones.length][zones.length];



        add_player = new JButton("Add Player");

        start_game = new JButton("Start Game");
        start_game.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                   gameController.startGame();
            }
        });


        add_player.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddPlayerPopup panel = new AddPlayerPopup();
                PopupFactory factory = new PopupFactory();
                Popup po = factory.getPopup(window, panel, window.getX() + (window.getSize().width/2), window.getY() + (window.getSize().height/2));
                panel.addMainCallback(new AddPlayerCallback() {
                    @Override
                    public void callHide() {
                        po.hide();
                    }

                    @Override
                    public void callAddPlayer(String name){
                        gameController.addPlayerToTheGame(name);
                    }
                });
                po.show();
            }
        });
        add_player.setSize(100, 50);
        add_player.setVisible(true);
        buttonPanel.add(add_player);
        buttonPanel.add(start_game);

        for(int i = 0; i < zones.length; i++){
            for(int j = 0; j < zones[i].length; j++){
                zonePanels[i][j] = getZone(zones[i][j]);

                // adding button in JFrame
                boardPanel.add(zonePanels[i][j]);
            }
        }
        updateZonePanels();


        boardPanel.setSize(zones.length * zone_size, zones.length * zone_size);
        boardPanel.setLayout(new GridLayout(zones.length, zones.length));
        boardPanel.setVisible(true);
        window.setSize(w_width, w_height);

//        boardPanel.setPreferredSize(new Dimension((int)(w_width * 0.8), (int)(w_height * 0.8)));

        // using no layout managers
//        boardPanel.setLayout();

        //player pane panels
        rightPanel.setLayout(new GridLayout(2, 1));
        rightPanel.add(panelPlayers[0]);
        rightPanel.add(panelPlayers[1]);
        rightPanel.setVisible(true);

        leftPanel.setLayout(new GridLayout(2, 1));
        leftPanel.add(panelPlayers[2]);
        leftPanel.add(panelPlayers[3]);
        leftPanel.setVisible(true);

        //window adders

        window.add(buttonPanel, BorderLayout.NORTH);
        window.add(boardPanel);
        window.add(rightPanel, BorderLayout.EAST);
        window.add(leftPanel, BorderLayout.WEST);

        // making the frame visible
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}
