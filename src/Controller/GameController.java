package Controller;

import Errors.*;
import Model.*;
import View.GUI;

import java.security.InvalidParameterException;
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
        try {
            this.boardGame.finDeTour();
        } catch (TooManyCardsInTheHand ex) {
            GUI.showErrorMess("Too Many Cards", ex.getMessage());
        }
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


    // play actions:

    //---------------
    //general getters
//    public ArrayList<PlayerAction> getPossibleActionsForPlayer(Player player){
//        return this.boardGame.getPossiblePlayerActions(player);
//    }
    public ArrayList<PlayerAction> getPossiblePlayerActionsForCurrentTurn(Player player){
        return this.boardGame.getPossiblePlayerActionsForCurrentTurn(player);
    }

    public Player getPlayerForTheTurn(){
        return this.boardGame.getPlayerForTheTurn();
    }

    public ArrayList<Zone> getZonesPossibleForChoosing(){
       return this.boardGame.getZonesPossibleForChoosing();
    }
    //end general getters
    //---------------

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
    public void setNavigatorChoosePlayerToMove(){
        this.boardGame.setNavigatorChoosePlayerToMove();
        HashSet<Player> players = this.boardGame.getPlayersToChoose();
        GUI.makePlayersChoosable(players, this::choosePlayerByNavigator);
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
    public boolean isNavgiatorChoosingAPlayerToMove(){
        return this.boardGame.isNavgiatorChoosingAPlayerToMove();
    }
    public boolean isNavgiatorChoosingAZoneToMovePlayerTo(){
        return this.boardGame.isNavgiatorChoosingAZoneToMovePlayerTo();
    }
    public boolean isPlayerChoosingZoneToShoreUpWithCard() {
        return this.boardGame.isPlayerChoosingZoneToShoreUpWithCard();
    }
    public boolean isPlayerChoosingZoneToFlyWithCard() {
        return this.boardGame.isPlayerChoosingZoneToFlyWithCard();
    }
    public boolean isPlayerChoosingCardToDiscard(){
        return this.boardGame.isPlayerChoosingCardToDiscard();
    }
    //end is player choosing
    //------------

    //------------
    //actions
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
    public void playerShoreUpZone(Zone zone) {
        try {
            this.boardGame.playerShoreUpZone(zone);
            GUI.updateZonePanels();
            GUI.updatePlayerPanels();
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("No actions left", "You used all your actions!");
        }
    }
    private void choosePlayerByNavigator(Player chosen_player) {
        this.boardGame.choosePlayerByNavigator(chosen_player);
        GUI.makePlayersUnChoosable();
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void choosePlayerToFlyWithCard(Player chosen_player){
        try {
            this.boardGame.choosePlayerToFlyWithCard(chosen_player);
            GUI.makePlayersUnChoosable();
            GUI.makePlayersChoosable(this.boardGame.getPlayersToChoose(), this::choosePlayerByNavigator);
            GUI.updatePlayerPanels();
        }catch (Exception ex){}
    }

    public void movePlayerToTheZoneByNavigator(Zone zone) {
        try {
            this.boardGame.movePlayerToZoneByNavigator(zone);
            GUI.updateZonePanels();
            GUI.updatePlayerPanels();
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("No actions left", "You used all your actions!");
        }
    }
    //------------

    public int getCurrentPlayerActionsNumber(){
        return this.boardGame.getCurrent_player_actions_num();
    }
    public ArrayList<Card> getCurrentPlayerCards(Player player){
        return this.boardGame.getCurrentPlayerCards(player);
    }



    public void discardCardFromCurrentPlayer(Card c) {
        boardGame.getPlayerForTheTurn().discardCard(c, boardGame.getTreasureDeck());
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }

    public void playerUseActionCard(Player player, Card card) {
        try {
            this.boardGame.playerUseActionCard(player, card);
            if(card.getType() == CardType.HELICOPTER_LIFT){
                HashSet<Player> players = this.boardGame.getPlayersToChoose();
                GUI.makePlayersChoosable(players, this::choosePlayerToFlyWithCard);
            }
            GUI.updatePlayerPanels();
            GUI.updateZonePanels();
        }catch (InvalidParameterException ex){
            GUI.showErrorMess("Invalid card", "You don't have such a card!");
        }
    }

    public void flyPlayerToZoneWithCard(Zone zone) {
        this.boardGame.flyPlayerToZoneWithCard(zone);
        GUI.makePlayersUnChoosable();
    }

    public void shoreUpZoneWithCard(Zone zone) {
        this.boardGame.shoreUpZoneWithCard(zone);
    }

    public void setPlayerGiveTreasureCards() {
        this.boardGame.setPlayerGiveTreasureCards();
        GUI.updatePlayerPanels();
    }

    public boolean isThisPlayerChoosingCardToGive(Player player) {
        return this.boardGame.isThisPlayerChoosingCardToGive(player);
    }

    public void playerChooseCardToGive(Player p, Card c) {
        this.boardGame.playerChooseCardToGive(p, c);
        GUI.updatePlayerPanels();
        HashSet<Player> players = this.boardGame.getPlayersToChoose();
        GUI.makePlayersChoosable(players, this::choosePlayerToGiveCardTo);
    }

    public void choosePlayerToGiveCardTo(Player player) {
        this.boardGame.choosePlayerToGiveCardTo(player);
        GUI.updatePlayerPanels();
    }

    public void setPlayerDiscardCard() {
        this.boardGame.setPlayerDiscardCard();
        GUI.updatePlayerPanels();
    }

    public void playerDiscardCard(Player player, Card c) {
        this.boardGame.playerDiscardCard(player, c);
        GUI.updatePlayerPanels();
    }

    public boolean isThisPlayerChoosingCardToDiscard(Player player) {
        return this.boardGame.isThisPlayerChoosingCardToDiscard(player);
    }
}
