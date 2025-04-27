import Controller.GameController;
import View.SwingView.GUI;
import View.contract.GameView;
import View.SwingView.ResourceMapper;
import Model.ZoneCard;
import Model.PlayerRole;
import Model.CardType;
import Model.Artefact;

import javax.swing.*;
import java.net.URL;

/**
 * Main entry point for the Forbidden Island game application.
 * Initializes the MVC components (Model implicitly via Controller, View, Controller)
 * and starts the application. Includes image preloading for smoother UI experience.
 */
public class Main {

    /**
     * Preloads essential game images in a background thread using SwingWorker.
     * This aims to load images into the ResourceMapper cache before they are
     * strictly needed by the UI, potentially reducing initial lag.
     */
    private static void preloadImages(){
        SwingWorker<Void,Void> preload = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                for (ZoneCard z : ZoneCard.values()) ResourceMapper.getZoneCardImage(z);
                for (PlayerRole r : PlayerRole.values()) ResourceMapper.getRoleImage(r,70,70);
                for (CardType c : CardType.values()) ResourceMapper.getCardImage(c,80,80);
                for (Artefact a : Artefact.values()) ResourceMapper.getArtefactIcon(a,50,50);
                return null;
            }
        };
        preload.execute();
    }

    /**
     * Main method. Creates the GameView (GUI) and GameController,
     * links them, and initializes the view, starting the game setup process.
     * Calls image preloading first.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        preloadImages(); // Start preloading images in the background

        // Initialize MVC components
        GameView gameView = new GUI();
        GameController gameController = new GameController(gameView);
        gameView.initialize(gameController); // Initialize the view, which typically shows the initial setup screen
    }
}
