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
        catch(GameWonException ex){
            GUI.showInfoMess("Congratulations!", "You won the game!");
            return;
        }
        catch(WaterRiseException ex){
            GUI.showInfoMess("Attention!", "You got a water rise card, the level of water is rising!");
        }
        catch(GameOverException ex){
            GUI.showErrorMess("Game Over", ex.getMessage());
            return;
        }
        catch(InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("You got an invalid action for this state", ex.getMessage());
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
        catch(InvalidParameterException e){
            GUI.showErrorMess("Invalid data provided", e.getMessage());
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
        try {
            this.boardGame.setPlayerChooseZoneToMoveTo();
        }catch (InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("Invalid Action", ex.getMessage());
        }
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void setPlayerChooseZoneToShoreUp(){
        try{
            this.boardGame.setPlayerChooseZoneToShoreUp();

        }catch (InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("Invalid Action", ex.getMessage());
        }
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void setPilotChooseWhereToFlyTo(){
        try{
        this.boardGame.setPilotChooseWhereToFlyTo();
        }catch (InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("Invalid Action", ex.getMessage());
        }
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void setNavigatorChoosePlayerToMove(){
        try{
        this.boardGame.setNavigatorChoosePlayerToMove();
        }catch (InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("Invalid Action", ex.getMessage());
        }
        HashSet<Player> players = this.boardGame.getPlayersToChoose();
        GUI.makePlayersChoosable(players, this::choosePlayerByNavigator);
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
    }
    public void setPlayerChooseZoneToRunFromInaccessbileZone(Player player){
        try {
            this.boardGame.setPlayerChooseZoneToRunFromInaccessbileZone(player);
        }catch (InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("Invalid Action", ex.getMessage());
        }
        GUI.updateZonePanels();
        GUI.updatePlayerPanels();
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
    public boolean arePlayersRunningFromInaccessibleZone(){
        return this.boardGame.arePlayersRunningFromInaccesbleZone();
    }
    public boolean isPlayerChoosingZoneToRunFromInaccesbleZone(){
        return this.boardGame.isPlayerChoosingZoneToRunFromInaccesbleZone();
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
        }catch(InvalidActionForTheCurrentState ex){
            GUI.showErrorMess("Invalid action", ex.getMessage());
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
        try {
            this.boardGame.setPlayerGiveTreasureCards();
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("No actions left", "You used all your actions!");
        }
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

    public void takeArtefact() {
        try {
            boardGame.takeArtefact();
        } catch (IllegalStateException ex) {
            GUI.showErrorMess("Cannot Take Artefact", ex.getMessage());
        } catch (InvalidStateOfTheGameException ex){
            GUI.showErrorMess("Cannot Take Artefact", ex.getMessage());
        }catch (NoActionsLeft ex){
            GUI.showErrorMess("Cannot Take Artefact", "You used all your actions!");
        }
        GUI.updatePlayerPanels();
        GUI.updateZonePanels();
        GUI.updateCornerArtefacts();
    }

    public boolean isArtefactTaken(Artefact artefact) {
        return this.boardGame.isArtefactTaken(artefact);
    }

    public void chooseZoneToRunFromInaccessible(Zone zone) {
        try {
            this.boardGame.chooseZoneToRunFromInaccessible(zone);
        }
        catch(InvalidStateOfTheGameException ex){
            GUI.showErrorMess("Invalid action", ex.getMessage());
        }catch(InvalidParameterException ex){
            GUI.showErrorMess("Invalid action", ex.getMessage());
        }
        GUI.updateZonePanels();
        GUI.updatePlayerPanels();
    }
}
