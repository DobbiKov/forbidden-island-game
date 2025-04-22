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
        this.boardGame = new BoardGame(8);
    }
    public Zone[][] getZones() {
        return this.boardGame.getBoard();
    }

    public void finDeTour(){
        try {
            this.boardGame.finDeTour();
            GUI.updatePlayerPanels();
            GUI.updateZonePanels();
        }
        catch (NotAllPlayersFinishedTheirTurns ex){
            GUI.showErrorMess("Impossible to finish", "All the players have to finish their turns!");
        }
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

    public ArrayList<Zone> getZonesForPlayerToMove(Player player){
        return this.boardGame.getZonesForPlayerToMove(player);
    }

    public void setPlayerChooseZoneToMoveTo(){
        this.boardGame.setPlayerChooseZoneToMoveTo();
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public boolean isPlayerChoosingZoneToMove(){
        return this.boardGame.isPlayerChoosingToMove();
    }

    public void movePlayerToTheZone(Player player, Zone zone){
        this.boardGame.movePlayerToZone(player, zone);
        GUI.updateZonePanels();
        GUI.updatePlayerPanels();
    }

    public int getCurrentPlayerActionsNumber(){
        return this.boardGame.getCurrent_player_actions_num();
    }

    public void playerFinishTurn(PlayerPanel playerPanel){
        this.boardGame.playerFinishTurn();
        playerPanel.removeActions();
        GUI.updatePlayerPanels();
    }

}
