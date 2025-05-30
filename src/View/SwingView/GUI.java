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
import java.util.logging.Filter;
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
    public final static Color backgroundColor = new Color(173, 216, 230);

    private JPanel waterMeterPanel;
    private JProgressBar waterLevelBar;
    private JLabel floodRateLabel;


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
        return new FilteredImagePanel(z, zone_size, gameController);
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

    @Override
    public void updateWaterMeter() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateWaterMeter);
            return;
        }

        int level = gameController.getWaterMeterLevel();
        int rate = gameController.getFloodRate();

        // Ensure components exist before updating
        if (waterLevelBar != null && floodRateLabel != null) {
            waterLevelBar.setValue(level);
            waterLevelBar.setString("Level: " + level + " / " + WaterMeter.MAX_LEVEL);
            floodRateLabel.setText("Flood Rate: " + rate);

            // Optional: Change color of the bar based on level
            if (level >= 7) { // Example: dangerous levels
                waterLevelBar.setForeground(Color.RED);
            } else if (level >= 4) { // Example: medium levels
                waterLevelBar.setForeground(Color.ORANGE);
            } else { // Example: low levels
                waterLevelBar.setForeground(Color.GREEN.darker());
            }

            waterMeterPanel.revalidate();
            waterMeterPanel.repaint();
        }
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
                FilteredImagePanel panel_im = (FilteredImagePanel)panel;

                panel.removeAll();
                boolean blue = (state == ZoneState.Flooded);
                if (panel_im.isBlueFilterVisible() != blue) {
                    panel_im.setBlueFilterVisible(blue);
                }

                boolean is_inacc = (state == ZoneState.Inaccessible);
                if (panel_im.isInaccessible() != is_inacc) {
                    panel_im.setInaccessible();
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
                    panel_im.setSelectable();
                }else{
                    if(panel_im.isSelectable()){
                        panel.setBorder(null);
                        panel_im.setUnSelectable();
                    }
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
                gameController.endTurn();
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
        //glass pane that will block input
        JPanel blocker = new JPanel();
        blocker.setOpaque(false);
        blocker.addMouseListener(new MouseAdapter(){});
        setGlassPane(blocker);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void onGameOver() {
        getGlassPane().setVisible(true);
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


        // ============
        // water meter
        waterMeterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5)); // FlowLayout for simplicity
        waterMeterPanel.setBackground(backgroundColor);
        waterLevelBar = new JProgressBar(0, WaterMeter.MAX_LEVEL); // Max level from Model
        waterLevelBar.setStringPainted(true); // Show text like "Level: X/10"
        waterLevelBar.setPreferredSize(new Dimension(300, 25));
        waterLevelBar.setBorderPainted(false); // Optional: remove border
        waterLevelBar.setBackground(Color.CYAN.brighter()); // Base color for the bar track


        floodRateLabel = new JLabel("Flood Rate: ?");
        floodRateLabel.setFont(floodRateLabel.getFont().deriveFont(Font.BOLD, 14f));

        waterMeterPanel.add(new JLabel("Water Level:")); // Label before the bar
        waterMeterPanel.add(waterLevelBar);
        waterMeterPanel.add(floodRateLabel);
        // ============

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
        this.setSize(1500, 1120);


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

        JComponent glue = (JComponent) Box.createHorizontalGlue();
//        glue.setOpaque(false);
//        glue.setBackground(new Color(0, 0, 0, 0));
        bottomCorners.add(glue);

        bottomCorners.add(botRight);

        bottomCorners.setOpaque(false);
        bottomCorners.setBackground(new Color(0, 0, 0, 0));

        boardPanel.setBackground(backgroundColor);

        //board wrapper
        //-----------
        JPanel boardPanelWrapper = new JPanel(new BorderLayout());
        boardPanelWrapper.setBackground(backgroundColor);
        boardPanelWrapper.add(Box.createHorizontalStrut(artefactSize), BorderLayout.WEST);
        boardPanelWrapper.add(boardPanel,  BorderLayout.CENTER);
        boardPanelWrapper.add(Box.createHorizontalStrut(artefactSize), BorderLayout.EAST);
        //-----------

        JPanel gameArea = new JPanel(new BorderLayout());
        gameArea.setBackground(backgroundColor);
        gameArea.add(topCorners, BorderLayout.NORTH);
        gameArea.add(bottomCorners,  BorderLayout.SOUTH);
        gameArea.add(boardPanelWrapper,  BorderLayout.CENTER);

        this.getContentPane().removeAll();
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(buttonPanel,    BorderLayout.NORTH);
        this.getContentPane().add(gameArea,      BorderLayout.CENTER);
        this.getContentPane().add(rightPanel, BorderLayout.EAST);
        this.getContentPane().add(leftPanel, BorderLayout.WEST);
        this.getContentPane().add(waterMeterPanel, BorderLayout.SOUTH);


        // making the frame visible
        this.setVisible(true);
        updateWaterMeter();
    }
    private static Icon scaledIcon(String path, int w, int h) {
        ImageIcon raw = new ImageIcon(path);
        Image img = raw.getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

}
