package Controller;

import Errors.*;
import Model.*;
import View.ErrorPopup;
import View.GUI;

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
        this.boardGame.finDeTour();
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

    public Player makeNextTurnPlayer(){
        return this.boardGame.moveTurnToNextPlayer();
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
    }
    public boolean isPlayerChoosingZoneToMove(){
        return this.boardGame.isPlayerChoosingToMove();
    }

    public void movePlayerToTheZone(Player player, Zone zone){
        this.boardGame.movePlayerToZone(player, zone);
    }

}
