package View.SwingView;
import Controller.GameController;
import Helper.AddPlayerCallback;
import Helper.ChoosablePlayerCallback;
import Model.*;
import View.contract.GameView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import javax.swing.*;

public class GUI extends JFrame implements GameView {
    private GameController gameController;
    private int w_width = 800;
    private int w_height = 600;

    private Zone[][] zones;
    private int zone_size = 150;
    private int artefactSize = (int)(zone_size*(0.7));

    private JPanel[][] zonePanels;

    private PlayerPanel[] panelPlayers;
    private int player_panel_size = 0;

    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel boardPanel;
    private JPanel buttonPanel;
    private JButton add_player;

    private JLabel topLeft;
    private JLabel topRight;

    private JLabel botLeft;
    private JLabel botRight;

    private JButton start_game;

    public void updateCornerArtefacts(){
        if(gameController.isArtefactTaken(Artefact.Earth)){
            topLeft.setVisible(false);
            topLeft.revalidate();
            topLeft.repaint();
        }
        if(gameController.isArtefactTaken(Artefact.Wind)){
            topRight.setVisible(false);
            topRight.revalidate();
            topRight.repaint();
        }
        if(gameController.isArtefactTaken(Artefact.Fire)){
            botLeft.setVisible(false);
            botLeft.revalidate();
            botLeft.repaint();
        }
        if(gameController.isArtefactTaken(Artefact.Water)){
            botRight.setVisible(false);
            botRight.revalidate();
            botRight.repaint();
        }

    }

    private JPanel createPanelForZone(Zone z){
        return new FilteredImagePanel(z, zone_size);
    }

    public void makePlayersChoosable(HashSet<Player> players, ChoosablePlayerCallback callback){
        for(PlayerPanel panel : panelPlayers){
            if(players.contains(panel.getPlayer())){
                panel.makeChoosable(() -> {
                    callback.choose(panel.getPlayer());
                });
            }
        }
    }
    public void makePlayersUnChoosable(){
        for(PlayerPanel panel : panelPlayers){
            panel.makeUnchoosable();
        }
    }
    public void removeActionsForPlayerPanel(){
        for(PlayerPanel p : panelPlayers){
            if(p.getPlayer() == gameController.getPlayerForTheTurn()){
                p.removeActions();
            }
        }
    }
    public void showErrorMess(String title, String message){
        ErrorDialog dlg = new ErrorDialog(this, title, message);
        dlg.setVisible(true);
    }
    public void showInfoMess(String title, String message){
        InfoDialog dlg = new InfoDialog(this, title, message);
        dlg.setVisible(true);
    }


    public void addPlayerPanel(Player new_player){
        panelPlayers[player_panel_size++] = new PlayerPanel(gameController, new_player);
        this.remove(rightPanel);
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 1));
        rightPanel.add(panelPlayers[0]);
        rightPanel.add(panelPlayers[1]);
        rightPanel.setVisible(true);
        this.add(rightPanel, BorderLayout.EAST);

        this.remove(leftPanel);
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1));
        leftPanel.add(panelPlayers[2]);
        leftPanel.add(panelPlayers[3]);
        leftPanel.setVisible(true);
        this.add(leftPanel, BorderLayout.WEST);

        updatePlayerPanels();
        updateZonePanels();
    }

    public void updateZonePanels() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateZonePanels);
            return;
        }
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
                        java.awt.Color color = ResourceMapper.getAwtColor(player.getPlayerColor());
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
                            if(gameController.isPlayerChoosingZoneToRunFromInaccesbleZone()){
                                gameController.chooseZoneToRunFromInaccessible(zones[finalI][finalJ]);
                            }
                            else if(gameController.isPlayerChoosingZoneToShoreUp()){
                                gameController.playerShoreUpZone(zones[finalI][finalJ]);
                            }
                            else if(gameController.isPlayerChoosingZoneToMove()) {
                                gameController.movePlayerToTheZone(zones[finalI][finalJ]);
                            }else if(gameController.isPlayerChoosingZoneToFlyTo()){
                                gameController.flyPilotToTheZone(zones[finalI][finalJ]);
                            }else if(gameController.isNavgiatorChoosingAZoneToMovePlayerTo()){
                                gameController.movePlayerToTheZoneByNavigator(zones[finalI][finalJ]);
                            }else if(gameController.isPlayerChoosingZoneToFlyWithCard()){
                                gameController.flyPlayerToZoneWithCard(zones[finalI][finalJ]);
                            } else if(gameController.isPlayerChoosingZoneToShoreUpWithCard()){
                                gameController.shoreUpZoneWithCard(zones[finalI][finalJ]);
                            }
                            updatePlayerPanels();
                            updateZonePanels();
                            repaint();
                        }
                    });
                }
                panel.validate();
            }
        }
    }
    public void updatePlayerPanels(){
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updatePlayerPanels);
            return;
        }
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


        this.revalidate();
        this.repaint();
    }

    public void startGameHandleView(){
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
        this.validate();
        this.repaint();
    }

    public GUI(){
        super("Forbidden Island");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void initialize(GameController controller) {
        this.gameController = controller;
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
                AddPlayerDialog dlg = new AddPlayerDialog(GUI.this, new AddPlayerCallback() {
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

        boardPanel.setLayout(new GridBagLayout());
        int prefered_board_size = (int)(zone_size * 1.1) * zones.length;
        boardPanel.setPreferredSize(new Dimension(prefered_board_size, prefered_board_size));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,0,0,0);
        final int PAD = 6;
        for(int i = 0; i < zones.length; i++){
            for(int j = 0; j < zones[i].length; j++){
                gbc.gridx = i;
                gbc.gridy = j;
                gbc.insets = new Insets(PAD, PAD, PAD, PAD);
                zonePanels[i][j] = createPanelForZone(zones[i][j]);

                // adding button in JFrame
                boardPanel.add(zonePanels[i][j], gbc);
            }
        }
        updateZonePanels();


//        boardPanel.setSize(zones.length * zone_size, zones.length * zone_size);
//        boardPanel.setLayout(new GridLayout(zones.length, zones.length));
        boardPanel.setVisible(true);

        this.pack();
        this.setResizable(false);
        this.setSize(1480, 1080);


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

        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(rightPanel, BorderLayout.EAST);
        this.add(leftPanel, BorderLayout.WEST);

        // top row
        JPanel topCorners = new JPanel();
        topCorners.setOpaque(false);
        topCorners.setLayout(new BoxLayout(topCorners, BoxLayout.X_AXIS));
        topCorners.setPreferredSize(new Dimension(0, artefactSize));

        topLeft = new JLabel(ResourceMapper.getArtefactIcon(Artefact.Earth, artefactSize, artefactSize));
        topRight = new JLabel(ResourceMapper.getArtefactIcon(Artefact.Wind, artefactSize, artefactSize));

        botLeft  = new JLabel(ResourceMapper.getArtefactIcon(Artefact.Fire, artefactSize, artefactSize));
        botRight = new JLabel(ResourceMapper.getArtefactIcon(Artefact.Water, artefactSize, artefactSize));

        topCorners.add(topLeft);
        topCorners.add(Box.createHorizontalGlue());
        topCorners.add(topRight);

// bottom row
        JPanel bottomCorners = new JPanel();
        bottomCorners.setOpaque(false);
        bottomCorners.setLayout(new BoxLayout(bottomCorners, BoxLayout.X_AXIS));
        bottomCorners.setPreferredSize(new Dimension(0, artefactSize));


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

        this.getContentPane().removeAll();
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(buttonPanel,    BorderLayout.NORTH);
        this.getContentPane().add(gameArea,      BorderLayout.CENTER);
        this.getContentPane().add(rightPanel, BorderLayout.EAST);
        this.getContentPane().add(leftPanel, BorderLayout.WEST);


        // making the frame visible
        this.setVisible(true);
    }
    private static Icon scaledIcon(String path, int w, int h) {
        ImageIcon raw = new ImageIcon(path);
        Image img = raw.getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
