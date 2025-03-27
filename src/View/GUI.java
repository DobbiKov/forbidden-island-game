package View;
import Controller.GameController;
import Errors.MaximumNumberOfPlayersReachedException;
import Helper.Callback;
import Model.Player;
import Model.Zone;
import Model.ZoneState;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.*;
import java.text.AttributedCharacterIterator;
import javax.swing.*;
import javax.swing.border.Border;

public class GUI {
    private static GameController gameController;
    private static int w_width = 800;
    private static int w_height = 600;

    private static Zone[][] zones;
    private static int zone_size;

    private static JPanel[][] zonePanels;

    private static JPanel[] panelPlayers;
    private static int player_panel_size = 0;

    private static JPanel rightPanel;
    private static JPanel leftPanel;
    private static JFrame window;

    private static JPanel getZone(){
        // Creating instance of JButton
        JPanel button = new JPanel(
        );
        button.setSize(zone_size, zone_size);

        return button;
    }

    private static void updatePlayerZones(){
        // TODO
    }

    private static void updateZonePanels() {
        zones = gameController.getZones(); // get the new state
        for (int i = 0; i < zones.length; i++) {
            for (int j = 0; j < zones[i].length; j++) {
                ZoneState state = zones[i][j].getZone_state();
                JPanel panel = zonePanels[i][j];
                panel.removeAll();

                switch (state) {
                    case Normal:
                        panel.setBackground(Color.GREEN);
                        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                        break;
                    case Flooded:
                        panel.setBackground(new Color(173, 216, 230));
                        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
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
            }
        }
        window.validate();
    }
    public static void updatePlayerPanels(){
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


    public static void main(String[] args)
    {
        gameController = new GameController();
        zones = gameController.getZones();

        // init player panels
        rightPanel = new JPanel();
        leftPanel = new JPanel();
        panelPlayers = new JPanel[4];
        for(int i = 0; i < panelPlayers.length; i++){
            panelPlayers[i] = new JPanel();
        }

        JPanel boardPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        window = new JFrame();

        zone_size = 50;

        zonePanels = new JPanel[zones.length][zones.length];



        JButton add_player = new JButton("Add Player");

        JButton start_game = new JButton("Start Game");
        start_game.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               try{
                   gameController.startGame();
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
                   window.validate();
               }
               catch (Exception ex){
                   // TODO show error mess
               }
            }
        });


        add_player.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddPlayerPopup panel = new AddPlayerPopup();
                PopupFactory factory = new PopupFactory();
                Popup po = factory.getPopup(window, panel, window.getX() + (window.getSize().width/2), window.getY() + (window.getSize().height/2));
                panel.addMainCallback(new Callback() {
                    @Override
                    public void callHide() {
                        po.hide();
                    }

                    @Override
                    public void callAddPlayer(String name){
                        try {
                            Player new_player = gameController.addPlayerToTheGame(name);
                            panelPlayers[player_panel_size++] = new PlayerPanel(new_player);
                            updatePlayerPanels();
                            updateZonePanels();
                        }
                        catch (MaximumNumberOfPlayersReachedException e){
                            System.out.println("Maximum number of players reached");
                            // TODO message that maximun players is reached
                        }

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
                zonePanels[i][j] = getZone();

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
