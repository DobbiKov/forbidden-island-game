package Controller;

import Errors.InvalidMoveForCurrentGameState;
import Errors.InvalidZoneToMove;
import Model.*;

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
       //inondation de trois zones
       int zone_flooded = 0;
       int must_be_flooded = 3;
       if(boardGame.getNumOfActiveZones() < must_be_flooded){
            boardGame.floodAllZones();
       }
       else {
           while (zone_flooded < must_be_flooded) {
               Random rand = new Random();
               int x = rand.nextInt(boardGame.getSize());
               int y = rand.nextInt(boardGame.getSize());
               if (boardGame.getZone(x, y).getZone_state() == ZoneState.Inaccessible) {
                   continue;
               }
               boardGame.floodZone(x, y);
               zone_flooded++;
           }
       }

       //suite
    }
    public Player addPlayerToTheGame(String playerName){
        return boardGame.addPlayer(playerName);
    }

    public Player[] getPlayers(){
        return boardGame.getPlayers();
    }
    public void startGame(){
        boardGame.startGame();
        this.makeNextTurnPlayer();
    }

    public Player makeNextTurnPlayer(){
        this.boardGame.nextPlayerTurn();
        return this.boardGame.getPlayerForTheTurn();
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
        // TODO: verify that it's possible

        this.boardGame.setGame_state(GameState.PlayerChooseWhereToMove);
    }
    public boolean isPlayerChoosingZoneToMove(){
        return this.boardGame.isPlayerChoosingToMove();
    }

    public void movePlayerToTheZone(Player player, Zone zone){
        if(!this.isPlayerChoosingZoneToMove()){
            throw new InvalidMoveForCurrentGameState("The player is not currently choosing a zone to move");
        }
        if(!this.getZonesForPlayerToMove(player).contains(zone)){
            throw new InvalidZoneToMove("The zone you choose is not in the zone");
        }
        this.boardGame.movePlayerToZone(player, zone);
        this.boardGame.setGame_state(GameState.Playing);
    }

}
