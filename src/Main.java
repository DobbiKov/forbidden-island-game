import Controller.GameController;
import View.SwingView.GUI;
import View.contract.GameView;

import javax.swing.*;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        System.out.println("Checking resource path...");
        String testPath = "/roles_images/pilot.png"; // Use one of your paths
        URL url = Main.class.getResource(testPath); // Or ResourceMapper.class.getResource(testPath)
        if (url == null) {
            System.err.println("TEST FAILED: Resource not found: " + testPath);
        } else {
            System.out.println("TEST SUCCESS: Resource found at URL: " + url);
            // Optional: Try loading the icon
            ImageIcon icon = new ImageIcon(url);
            if (icon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                System.err.println("TEST FAILED: Image did not load completely from URL: " + url);
            } else {
                System.out.println("TEST SUCCESS: Image loaded completely.");
            }
        }

        GameView gameView = new GUI();
        GameController gameController = new GameController(gameView);
        gameView.initialize(gameController);
    }
}
