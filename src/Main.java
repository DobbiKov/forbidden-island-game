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


public class Main {
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
    public static void main(String[] args) {

        GameView gameView = new GUI();
        GameController gameController = new GameController(gameView);
        gameView.initialize(gameController);
    }
}
