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
    private static int artefactSize = zone_size;

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
        ErrorDialog dlg = new ErrorDialog(window, title, message);
        dlg.setVisible(true);
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
                        java.awt.Color color = player.getPlayerColor().getColor();
                        JPanel circle = new PawnPanel(color);
                        circle.setPreferredSize(new Dimension(50, 100));
//                        circle.setBorder(BorderFactory.createLineBorder(Color.BLACK));
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

                panel.update();
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
        artefactSize = (int)(zone_size*(1.5));

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
                AddPlayerDialog dlg = new AddPlayerDialog(window, new AddPlayerCallback() {
                    @Override public void callAddPlayer(String name) {
                        gameController.addPlayerToTheGame(name);
                    }
                    @Override public void callHide() {
                        // nothing special—dialog will dispose itself
                    }
                });
                dlg.setVisible(true);
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
        window.add(rightPanel, BorderLayout.EAST);
        window.add(leftPanel, BorderLayout.WEST);

        // top row
        JPanel topCorners = new JPanel();
        topCorners.setOpaque(false);
        topCorners.setLayout(new BoxLayout(topCorners, BoxLayout.X_AXIS));
        topCorners.setPreferredSize(new Dimension(0, artefactSize));

        JLabel topLeft  = new JLabel(scaledIcon("artefacts_images/earth_stone.png", artefactSize, artefactSize));
        JLabel topRight = new JLabel(scaledIcon("artefacts_images/statue_of_wind.png", artefactSize, artefactSize));

        topCorners.add(topLeft);
        topCorners.add(Box.createHorizontalGlue());
        topCorners.add(topRight);

// bottom row
        JPanel bottomCorners = new JPanel();
        bottomCorners.setOpaque(false);
        bottomCorners.setLayout(new BoxLayout(bottomCorners, BoxLayout.X_AXIS));
        bottomCorners.setPreferredSize(new Dimension(0, artefactSize));

        JLabel botLeft  = new JLabel(scaledIcon("artefacts_images/crystal_of_fire.png", artefactSize, artefactSize));
        JLabel botRight = new JLabel(scaledIcon("artefacts_images/oceans_chalice.png", artefactSize, artefactSize));

        bottomCorners.add(botLeft);
        bottomCorners.add(Box.createHorizontalGlue());
        bottomCorners.add(botRight);

        //board wrapper
        //-----------
        JPanel boardPanelWrapper = new JPanel(new BorderLayout());
        boardPanelWrapper.add(Box.createHorizontalStrut(artefactSize), BorderLayout.WEST);
        boardPanelWrapper.add(boardPanel,  BorderLayout.CENTER);
        boardPanelWrapper.add(Box.createHorizontalStrut(artefactSize), BorderLayout.EAST);
        //-----------

        JPanel gameArea = new JPanel(new BorderLayout());
        gameArea.add(topCorners, BorderLayout.NORTH);
        gameArea.add(boardPanelWrapper,  BorderLayout.CENTER);
        gameArea.add(bottomCorners,  BorderLayout.SOUTH);

        // 3. Finally stitch everything into the frame:
        window.getContentPane().removeAll();
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(buttonPanel,    BorderLayout.NORTH);
        window.getContentPane().add(gameArea,      BorderLayout.CENTER);
        window.getContentPane().add(rightPanel, BorderLayout.EAST);
        window.getContentPane().add(leftPanel, BorderLayout.WEST);


        // making the frame visible
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
    private static Icon scaledIcon(String path, int w, int h) {
        ImageIcon raw = new ImageIcon(path);
        Image img = raw.getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
