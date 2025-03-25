package View;
import Controller.GameController;
import Model.Zone;
import Model.ZoneState;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import javax.swing.border.Border;

public class GUI {
    private static GameController gameController;
    private static int w_width = 800;
    private static int w_height = 600;

    private static Zone[][] zones;
    private static int zone_size;

    private static JPanel[][] zonePanels;

    private static JPanel getZone(){
        // Creating instance of JButton
        JPanel button = new JPanel(
        );
        button.setSize(zone_size, zone_size);

        return button;
    }

    private static void updateZonePanels() {
        zones = gameController.getZones(); // get the new state
        for (int i = 0; i < zones.length; i++) {
            for (int j = 0; j < zones[i].length; j++) {
                ZoneState state = zones[i][j].getZone_state();
                JPanel panel = zonePanels[i][j];

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
            }
        }
    }


    public static void main(String[] args)
    {
        gameController = new GameController();
        zones = gameController.getZones();
        // Creating instance of JFrame
        JPanel boardPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JFrame window = new JFrame();

        zone_size = 50;

        zonePanels = new JPanel[zones.length][zones.length];

        JButton fin_de_tour = new JButton("Fin de Tour");
        fin_de_tour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameController.finDeTour();
                updateZonePanels();
            }
        });
        fin_de_tour.setSize(100, 50);
        fin_de_tour.setVisible(true);

        buttonPanel.add(fin_de_tour);

        JButton verify_constraints = new JButton("Temp verify constraints");
        verify_constraints.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean res = gameController.verifyConstraints();
                System.out.println(res);
            }
        });
        verify_constraints.setSize(100, 50);
        verify_constraints.setVisible(true);

        buttonPanel.add(verify_constraints);
        window.add(buttonPanel, BorderLayout.NORTH);

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

        // making the frame visible
        window.add(boardPanel);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}
