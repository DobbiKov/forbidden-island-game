package Controller;

import Errors.*;
import Model.*;
import View.ErrorPopup;
import View.GUI;
import View.PlayerPanel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class GameController {
    private BoardGame boardGame;
    public GameController() {
        this.boardGame = new BoardGame();
    }
    public Zone[][] getZones() {
        return this.boardGame.getBoard();
    }

    public void finDeTour(){
            GUI.removeActionsForPlayerPanel();
            this.boardGame.finDeTour();
            GUI.updatePlayerPanels();
            GUI.updateZonePanels();
    }
    public void addPlayerToTheGame(String playerName){
        try {
            Player p = boardGame.addPlayer(playerName);
            GUI.addPlayerPanel(p);
        }
        catch (MaximumNumberOfPlayersReachedException e){
            GUI.showErrorMess("max reached", "Maximum number of players reached");

        }
    }

    public Player[] getPlayers(){
        return boardGame.getPlayers();
    }
    public void startGame(){
        try {
            boardGame.startGame();
            GUI.startGameHandleView();
        }
        catch (NoPlayersException ex){
            GUI.showErrorMess("No Players", "The game can't be started without players!");
        }
        catch (InvalidNumberOfPlayersException ex){
            GUI.showErrorMess("Invalid number of players", ex.getMessage());
        }
    }

    public Player getPlayerForTheTurn(){
        return this.boardGame.getPlayerForTheTurn();
    }

    // play actions:

    public ArrayList<PlayerAction> getPossibleActionsForPlayer(Player player){
        return this.boardGame.getPossiblePlayerActions(player);
    }

    public ArrayList<Zone> getZonesPossibleForChoosing(){
       return this.boardGame.getZonesPossibleForChoosing();
    }

    //------------
    //set player choose
    public void setPlayerChooseZoneToMoveTo(){
        this.boardGame.setPlayerChooseZoneToMoveTo();
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void setPlayerChooseZoneToShoreUp(){
        this.boardGame.setPlayerChooseZoneToShoreUp();
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void setPilotChooseWhereToFlyTo(){
        this.boardGame.setPilotChooseWhereToFlyTo();
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }

    //end set player choose
    //------------

    //------------
    //is player choosing
    public boolean isPlayerChoosingSomething(){
        return this.boardGame.isPlayerChoosingSomething();
    }
    public boolean isPlayerChoosingZoneToMove(){
        return this.boardGame.isPlayerChoosingToMove();
    }
    public boolean isPlayerChoosingZoneToShoreUp(){
        return this.boardGame.isPlayerChoosingZoneToShoreUp();
    }
    public boolean isPlayerChoosingZoneToFlyTo(){
        return this.boardGame.isPilotChoosingZoneToFly();
    }
    //end is player choosing
    //------------

    public void movePlayerToTheZone(Zone zone){
        try {
            this.boardGame.movePlayerToZone(zone);
            GUI.updateZonePanels();
            GUI.updatePlayerPanels();
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("No actions left", "You used all your actions!");
        }
    }
    public void flyPilotToTheZone(Zone zone){
        try{
            this.boardGame.flyPilotToZone(zone);
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("No actions left", "You used all your actions!");
        }catch (InvalidActionForRole ex){
            GUI.showErrorMess("Invalid action", "Only a pilot can fly");
        }
    }

    public int getCurrentPlayerActionsNumber(){
        return this.boardGame.getCurrent_player_actions_num();
    }


    public void playerShoreUpZone(Zone zone) {
        try {
            this.boardGame.playerShoreUpZone(zone);
            GUI.updateZonePanels();
            GUI.updatePlayerPanels();
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("No actions left", "You used all your actions!");
        }
    }
}
