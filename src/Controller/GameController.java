package Controller;

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

    public ArrayList<PlayerAction> getPossibleActionsForPlayer(Player player){
        ArrayList<PlayerAction> possibleActions = new ArrayList<>();
        if(player == null || this.boardGame.isGameSettingUp()){
            return possibleActions;
        }

        possibleActions.add(PlayerAction.Move);
        possibleActions.add(PlayerAction.Drain);
        switch(player.getPlayer_role()){
            case Pilot:
                possibleActions.add(PlayerAction.FlyToACard);
                break;
            case Navigator:
                possibleActions.add(PlayerAction.MovePlayer);
                break;

            default: break;
        }
        boolean player_on_same_card = false;
        for(Player p : getPlayers()){
            if(p == player || p == null){
                continue;
            }
            if(p.getPlayer_zone() == player.getPlayer_zone()){
                player_on_same_card = true;
            }
        }
        if(player_on_same_card || player.getPlayer_role() == PlayerRole.Messenger){
            possibleActions.add(PlayerAction.GiveTreasureCard);
        }
        return possibleActions;
    }
}
