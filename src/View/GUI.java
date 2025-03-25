package View;
import Controller.GameController;
import Model.Zone;
import Model.ZoneState;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.Border;

public class GUI {
    private static GameController gameController;
    private static int w_width = 800;
    private static int w_height = 600;

    private static Zone[][] zones;
    private static int zone_size;
    private static JPanel getZone(int i, int j){
        // Creating instance of JButton
        JPanel button = new JPanel(
        );
        button.setSize(zone_size, zone_size);
        switch(zones[i][j].getZone_state()){
            case Normal: {
                button.setBackground(Color.GREEN);
                break;
            }
            case Flooded: {
                button.setBackground(new Color(173, 216, 230));
                break;
            }
            case Inaccessible: {
                button.setBackground(Color.WHITE);
                return button;
            }
        }
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return button;
    }


    public static void main(String[] args)
    {
        gameController = new GameController();
        zones = gameController.getZones();
        // Creating instance of JFrame
        JPanel boardPanel = new JPanel();
        JFrame window = new JFrame();

        zone_size = 50;



        for(int i = 0; i < zones.length; i++){
            for(int j = 0; j < zones[i].length; j++){
                JPanel button = getZone(i, j);

                // adding button in JFrame
                boardPanel.add(button);
            }
        }


        boardPanel.setSize(zones.length * zone_size, zones.length * zone_size);
        boardPanel.setLayout(new GridLayout(zones.length, zones.length));
        boardPanel.setVisible(true);
        window.setSize(w_width, w_height);

//        boardPanel.setPreferredSize(new Dimension((int)(w_width * 0.8), (int)(w_height * 0.8)));

        // using no layout managers
//        boardPanel.setLayout();

        // making the frame visible
        window.add(boardPanel);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}
