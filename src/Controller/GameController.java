package Controller;

import Model.*;
import View.contract.GameView;

import java.util.ArrayList;
import java.util.HashSet;

public class GameController {
    private BoardGame boardGame;
    private GameView gameView;
    public GameController(GameView gameView) {
        this.gameView = gameView;
        this.boardGame = new BoardGame();
    }
    public Zone[][] getZones() {
        return this.boardGame.getBoard();
    }

    public void endTurn(){
            this.gameView.removeActionsForPlayerPanel();
            modelActionHandler.handleModelAction(() -> this.boardGame.endTurn(), gameView);
            this.gameView.updatePlayerPanels();
            this.gameView.updateZonePanels();
    }
    public void addPlayerToTheGame(String playerName){
        modelActionHandler.handleModelAction(() ->{
            Player p = boardGame.addPlayer(playerName);
            this.gameView.addPlayerPanel(p);
        }, gameView);
    }

    public void startGame(){
        modelActionHandler.handleModelAction(() -> {
            boardGame.startGame();
            this.gameView.startGameHandleView();
        }, gameView);
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
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerChooseZoneToMoveTo();
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }
    public void setPlayerChooseZoneToShoreUp(){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerChooseZoneToShoreUp();

        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }
    public void setPilotChooseWhereToFlyTo(){
        modelActionHandler.handleModelAction(() -> {
        this.boardGame.setPilotChooseWhereToFlyTo();
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }
    public void setNavigatorChoosePlayerToMove(){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setNavigatorChoosePlayerToMove();
            HashSet<Player> players = this.boardGame.getPlayersToChoose();
            this.gameView.makePlayersChoosable(players, this::choosePlayerByNavigator);
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }
    public void setPlayerChooseZoneToRunFromInaccessbileZone(Player player){
        modelActionHandler.handleModelAction(() ->  {
            this.boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player);
        }, gameView);
        this.gameView.updateZonePanels();
        this.gameView.updatePlayerPanels();
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
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.movePlayerToZone(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }
    public void flyPilotToTheZone(Zone zone){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.flyPilotToZone(zone);
        },gameView);
    }
    public void playerShoreUpZone(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerShoreUpZone(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        },gameView);
    }
    private void choosePlayerByNavigator(Player chosen_player) {
        this.boardGame.choosePlayerByNavigator(chosen_player);
        this.gameView.makePlayersUnChoosable();
        this.gameView.updateZonePanels();
        this.gameView.updatePlayerPanels();
    }
    public void choosePlayerToFlyWithCard(Player chosen_player){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.choosePlayerToFlyWithCard(chosen_player);
            this.gameView.makePlayersUnChoosable();
            HashSet<Player> remaining = boardGame.getPlayersToChoose();
            this.gameView.makePlayersChoosable(this.boardGame.getPlayersToChoose(), this::choosePlayerToFlyWithCard);
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    public void movePlayerToTheZoneByNavigator(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.movePlayerToZoneByNavigator(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        },gameView);
    }
    //------------

    public int getCurrentPlayerActionsNumber(){
        return this.boardGame.getCurrentPlayerActionsNum();
    }
    public ArrayList<Card> getCurrentPlayerCards(Player player){
        return this.boardGame.getCurrentPlayerCards(player);
    }

    public void playerUseActionCard(Player player, Card card) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerUseActionCard(player, card);
            if(card.getType() == CardType.HELICOPTER_LIFT){
                HashSet<Player> players = this.boardGame.getPlayersToChoose();
                this.gameView.makePlayersChoosable(players, this::choosePlayerToFlyWithCard);
            }
            this.gameView.updatePlayerPanels();
            this.gameView.updateZonePanels();
        },gameView);
    }

    public void flyPlayerToZoneWithCard(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.flyPlayerToZoneWithCard(zone);
            this.gameView.makePlayersUnChoosable();
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    public void shoreUpZoneWithCard(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.shoreUpZoneWithCard(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    public void setPlayerGiveTreasureCards() {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerGiveTreasureCards();
        }, gameView);
        this.gameView.updatePlayerPanels();
    }

    public boolean isThisPlayerChoosingCardToGive(Player player) {
        return this.boardGame.isThisPlayerChoosingCardToGive(player);
    }

    public void playerChooseCardToGive(Player p, Card c) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerChooseCardToGive(p, c);
            this.gameView.updatePlayerPanels();
            HashSet<Player> players = this.boardGame.getPlayersToChoose();
            this.gameView.makePlayersChoosable(players, this::choosePlayerToGiveCardTo);
        }, gameView);
    }

    public void choosePlayerToGiveCardTo(Player player) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.choosePlayerToGiveCardTo(player);
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    public void setPlayerDiscardCard() {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerDiscardCard();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    public void playerDiscardCard(Player player, Card c) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerDiscardCard(player, c);
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    public boolean isThisPlayerChoosingCardToDiscard(Player player) {
        return this.boardGame.isThisPlayerChoosingCardToDiscard(player);
    }

    public void takeArtefact() {
        modelActionHandler.handleModelAction(() -> {
            boardGame.takeArtefact();
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
        this.gameView.updateCornerArtefacts();
    }

    public boolean isArtefactTaken(Artefact artefact) {
        return this.boardGame.isArtefactTaken(artefact);
    }

    public void chooseZoneToRunFromInaccessible(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.chooseZoneToRunFromInaccessible(zone);
        },gameView);

        this.gameView.updateZonePanels();
        this.gameView.updatePlayerPanels();
    }
}
