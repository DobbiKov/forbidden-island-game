package Controller;

import Model.*;
import View.contract.GameView;
import Helper.ChoosablePlayerCallback; // Assuming this is needed for makePlayersChoosable

import java.util.ArrayList;
import java.util.HashSet;

/**
 * The main controller in the MVC pattern for the Forbidden Island game.
 * It acts as an intermediary between the View (UI) and the Model (BoardGame).
 * It receives user actions from the View, translates them into calls to the Model,
 * and updates the View based on changes in the Model's state.
 * Uses modelActionHandler to manage exceptions from the Model.
 */
public class GameController {
    private BoardGame boardGame; // The game state and logic
    private GameView gameView;   // The user interface

    /**
     * Creates a new GameController.
     * @param gameView The GameView instance this controller will interact with.
     */
    public GameController(GameView gameView) {
        this.gameView = gameView;
        this.boardGame = new BoardGame();
    }

    /**
     * Retrieves the current state of the game board zones from the model.
     * @return A 2D array of Zone objects representing the board.
     */
    public Zone[][] getZones() {
        return this.boardGame.getBoard();
    }

    /**
     * Handles the user action to end the current player's turn.
     * Notifies the view, calls the model's endTurn logic (via handler),
     * and triggers updates in the view.
     */
    public void endTurn(){
            this.gameView.removeActionsForPlayerPanel();
            modelActionHandler.handleModelAction(() -> this.boardGame.endTurn(), gameView);
            this.gameView.updatePlayerPanels();
            this.gameView.updateZonePanels();
            this.gameView.updateWaterMeter();
    }

    /**
     * Handles the user action to add a new player during game setup.
     * Calls the model to add the player (via handler) and updates the view
     * to display the new player panel.
     * @param playerName The name for the new player.
     */
    public void addPlayerToTheGame(String playerName){
        modelActionHandler.handleModelAction(() ->{
            Player p = boardGame.addPlayer(playerName);
            this.gameView.addPlayerPanel(p);
        }, gameView);
    }

    /**
     * Handles the user action to start the game after setup.
     * Calls the model's startGame logic (via handler) and updates the view
     * to reflect the start of the game (e.g., hiding setup controls).
     */
    public void startGame(){
        modelActionHandler.handleModelAction(() -> {
            boardGame.startGame();
            this.gameView.startGameHandleView();
        }, gameView);
    }


    // play actions:

    //---------------
    //general getters

    /**
     * Gets the list of possible actions for the given player based on the current turn and game state.
     * Delegates to the model.
     * @param player The player whose actions are requested.
     * @return An ArrayList of available PlayerActions.
     */
    public ArrayList<PlayerAction> getPossiblePlayerActionsForCurrentTurn(Player player){
        return this.boardGame.getPossiblePlayerActionsForCurrentTurn(player);
    }

    /**
     * Gets the player whose turn it currently is from the model.
     * @return The current Player object, or null if the game hasn't started.
     */
    public Player getPlayerForTheTurn(){
        return this.boardGame.getPlayerForTheTurn();
    }

    /**
     * Gets the list of zones that can be selected based on the current player action state
     * (e.g., zones to move to, zones to shore up). Delegates to the model.
     * @return An ArrayList of selectable Zone objects.
     */
    public ArrayList<Zone> getZonesPossibleForChoosing(){
       return this.boardGame.getZonesPossibleForChoosing();
    }
    //end general getters
    //---------------

    //------------
    //set player choose state setters (Initiate an action requiring selection)

    /**
     * Initiates the 'Move' action. Sets the model state to expect a zone selection
     * and updates the view to highlight possible move destinations.
     */
    public void setPlayerChooseZoneToMoveTo(){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerChooseZoneToMoveTo();
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }

    /**
     * Initiates the 'Shore Up' action. Sets the model state to expect a zone selection
     * and updates the view to highlight possible shoring up targets.
     */
    public void setPlayerChooseZoneToShoreUp(){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerChooseZoneToShoreUp();

        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }

    /**
     * Initiates the Pilot's 'Fly' action. Sets the model state to expect a zone selection
     * and updates the view to highlight possible flight destinations.
     */
    public void setPilotChooseWhereToFlyTo(){
        modelActionHandler.handleModelAction(() -> {
        this.boardGame.setPilotChooseWhereToFlyTo();
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }

    /**
     * Initiates the Navigator's 'Move Player' action. Sets the model state to expect a player selection,
     * retrieves the list of eligible players from the model, and instructs the view
     * to make those players selectable.
     */
    public void setNavigatorChoosePlayerToMove(){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setNavigatorChoosePlayerToMove();
            HashSet<Player> players = this.boardGame.getPlayersToChoose();
            this.gameView.makePlayersChoosable(players, this::choosePlayerByNavigator);
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
    }

    /**
     * Initiates the 'Run from Inaccessible Zone' action for a specific player.
     * Sets the model state to expect a zone selection for the escaping player
     * and updates the view to highlight possible escape routes.
     * @param player The player who needs to escape.
     */
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
    //is player choosing state checks (Query the current selection state)

    /** Checks if the game is currently waiting for any kind of player or zone selection. */
    public boolean isPlayerChoosingSomething(){
        return this.boardGame.isPlayerChoosingSomething();
    }
    /** Checks if the game is waiting for the player to choose a zone to move to. */
    public boolean isPlayerChoosingZoneToMove(){
        return this.boardGame.isPlayerChoosingToMove();
    }
    /** Checks if the game is waiting for the player to choose a zone to shore up. */
    public boolean isPlayerChoosingZoneToShoreUp(){
        return this.boardGame.isPlayerChoosingZoneToShoreUp();
    }
    /** Checks if the game is waiting for the Pilot to choose a zone to fly to. */
    public boolean isPlayerChoosingZoneToFlyTo(){
        return this.boardGame.isPilotChoosingZoneToFly();
    }
    /** Checks if the game is waiting for the Navigator to choose another player to move. */
    public boolean isNavgiatorChoosingAPlayerToMove(){
        return this.boardGame.isNavgiatorChoosingAPlayerToMove();
    }
    /** Checks if the game is waiting for the Navigator to choose a zone to move the selected player to. */
    public boolean isNavgiatorChoosingAZoneToMovePlayerTo(){
        return this.boardGame.isNavgiatorChoosingAZoneToMovePlayerTo();
    }
    /** Checks if the game is waiting for the player to choose a zone to shore up using Sandbags. */
    public boolean isPlayerChoosingZoneToShoreUpWithCard() {
        return this.boardGame.isPlayerChoosingZoneToShoreUpWithCard();
    }
    /** Checks if the game is waiting for the player to choose a zone to fly to using Helicopter Lift. */
    public boolean isPlayerChoosingZoneToFlyWithCard() {
        return this.boardGame.isPlayerChoosingZoneToFlyWithCard();
    }
    /** Checks if the game is waiting for the player to choose a card to discard. */
    public boolean isPlayerChoosingCardToDiscard(){
        return this.boardGame.isPlayerChoosingCardToDiscard();
    }
    /** Checks if the game is in the state where players must escape sinking zones. */
    public boolean arePlayersRunningFromInaccessibleZone(){
        return this.boardGame.arePlayersRunningFromInaccesbleZone();
    }
    /** Checks if a specific player is currently choosing a zone to escape to. */
    public boolean isPlayerChoosingZoneToRunFromInaccesbleZone(){
        return this.boardGame.isPlayerChoosingZoneToRunFromInaccesbleZone();
    }
    //end is player choosing
    //------------

    //------------
    //actions (Complete an action after selection)

    /**
     * Completes the 'Move' action after a zone has been selected by the user in the view.
     * Calls the model to perform the move (via handler) and updates the view.
     * @param zone The selected destination Zone.
     */
    public void movePlayerToTheZone(Zone zone){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.movePlayerToZone(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Completes the Pilot's 'Fly' action after a zone has been selected.
     * Calls the model to perform the flight (via handler) and updates the view.
     * @param zone The selected destination Zone.
     */
    public void flyPilotToTheZone(Zone zone){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.flyPilotToZone(zone);
            gameView.updatePlayerPanels();
            gameView.updateZonePanels();
        },gameView);
    }

    /**
     * Completes the 'Shore Up' action after a zone has been selected.
     * Calls the model to perform the shoring up (via handler) and updates the view.
     * @param zone The selected Zone to shore up.
     */
    public void playerShoreUpZone(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerShoreUpZone(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        },gameView);
    }

    /**
     * Handles the Navigator selecting another player to move.
     * Calls the model to register the choice, updates the view to make zones selectable
     * for the chosen player's move.
     * @param chosen_player The Player selected by the Navigator.
     */
    private void choosePlayerByNavigator(Player chosen_player) {
        this.boardGame.choosePlayerByNavigator(chosen_player);
        this.gameView.makePlayersUnChoosable();
        this.gameView.updateZonePanels();
        this.gameView.updatePlayerPanels();
    }

    /**
     * Handles the user selecting a player to accompany them on a Helicopter Lift.
     * Calls the model to register the choice, updates the view to potentially allow
     * selecting more players or the destination zone.
     * @param chosen_player The Player selected to fly with.
     */
    public void choosePlayerToFlyWithCard(Player chosen_player){
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.choosePlayerToFlyWithCard(chosen_player);
            this.gameView.makePlayersUnChoosable();
            HashSet<Player> remaining = boardGame.getPlayersToChoose();
            this.gameView.makePlayersChoosable(this.boardGame.getPlayersToChoose(), this::choosePlayerToFlyWithCard);
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Completes the Navigator's 'Move Player' action after the destination zone has been selected.
     * Calls the model to perform the move (via handler) and updates the view.
     * @param zone The selected destination Zone.
     */
    public void movePlayerToTheZoneByNavigator(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.movePlayerToZoneByNavigator(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        },gameView);
    }
    //------------

    /**
     * Gets the number of actions remaining for the current player from the model.
     * @return The number of actions left.
     */
    public int getCurrentPlayerActionsNumber(){
        return this.boardGame.getCurrentPlayerActionsNum();
    }

    /**
     * Gets the list of cards in the specified player's hand from the model.
     * @param player The player whose hand is requested.
     * @return An ArrayList of Card objects.
     */
    public ArrayList<Card> getCurrentPlayerCards(Player player){
        return this.boardGame.getCurrentPlayerCards(player);
    }

    /**
     * Handles the user clicking on a special action card (Sandbags, Helicopter) in their hand.
     * Calls the model to initiate the card usage (via handler), which sets the appropriate
     * game state (e.g., choosing zone/player). Updates the view accordingly.
     * @param player The player using the card.
     * @param card The Card being used.
     */
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

    /**
     * Completes the Helicopter Lift action after the destination zone is selected.
     * Calls the model to perform the flight (via handler) and updates the view.
     * @param zone The selected destination Zone.
     */
    public void flyPlayerToZoneWithCard(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.flyPlayerToZoneWithCard(zone);
            this.gameView.makePlayersUnChoosable();
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Completes the Sandbags action after the target zone is selected.
     * Calls the model to perform the shoring up (via handler) and updates the view.
     * @param zone The selected Zone to shore up.
     */
    public void shoreUpZoneWithCard(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.shoreUpZoneWithCard(zone);
            this.gameView.updateZonePanels();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Initiates the 'Give Treasure Card' action. Sets the model state to expect a card selection
     * from the current player's hand and updates the view.
     */
    public void setPlayerGiveTreasureCards() {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerGiveTreasureCards();
        }, gameView);
        this.gameView.updatePlayerPanels();
    }

    /**
     * Checks if the specified player is the one currently choosing a card to give away.
     * @param player The player to check.
     * @return true if this player is choosing a card to give, false otherwise.
     */
    public boolean isThisPlayerChoosingCardToGive(Player player) {
        return this.boardGame.isThisPlayerChoosingCardToGive(player);
    }

    /**
     * Handles the player selecting a specific card from their hand to give away.
     * Calls the model to register the card choice (via handler), gets the list of valid recipients,
     * and updates the view to make those recipients selectable.
     * @param p The player giving the card.
     * @param c The Card selected to give.
     */
    public void playerChooseCardToGive(Player p, Card c) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerChooseCardToGive(p, c);
            this.gameView.updatePlayerPanels();
            HashSet<Player> players = this.boardGame.getPlayersToChoose();
            this.gameView.makePlayersChoosable(players, this::choosePlayerToGiveCardTo);
        }, gameView);
    }

    /**
     * Completes the 'Give Treasure Card' action after the recipient player is selected.
     * Calls the model to transfer the card (via handler) and updates the view.
     * @param player The selected recipient Player.
     */
    public void choosePlayerToGiveCardTo(Player player) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.choosePlayerToGiveCardTo(player);
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Initiates the 'Discard Card' action, typically due to exceeding the hand limit.
     * Sets the model state to expect a card selection from the current player's hand
     * and updates the view to highlight the hand for discarding.
     */
    public void setPlayerDiscardCard() {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.setPlayerDiscardCard();
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Handles the player selecting a card to discard from their hand.
     * Calls the model to perform the discard (via handler) and updates the view.
     * @param player The player discarding the card.
     * @param c The Card selected to discard.
     */
    public void playerDiscardCard(Player player, Card c) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.playerDiscardCard(player, c);
            this.gameView.updatePlayerPanels();
        }, gameView);
    }

    /**
     * Checks if the specified player is the one currently required to discard a card.
     * @param player The player to check.
     * @return true if this player is choosing a card to discard, false otherwise.
     */
    public boolean isThisPlayerChoosingCardToDiscard(Player player) {
        return this.boardGame.isThisPlayerChoosingCardToDiscard(player);
    }

    /**
     * Handles the user action to take an artefact.
     * Calls the model to attempt the action (via handler) and updates the view
     * (player panels for cards, zone panels for artefact presence, corner artefacts display).
     */
    public void takeArtefact() {
        modelActionHandler.handleModelAction(() -> {
            boardGame.takeArtefact();
        }, gameView);
        this.gameView.updatePlayerPanels();
        this.gameView.updateZonePanels();
        this.gameView.updateCornerArtefacts();
    }

    /**
     * Checks if a specific artefact has already been claimed in the game.
     * @param artefact The Artefact to check.
     * @return true if the artefact is claimed, false otherwise.
     */
    public boolean isArtefactTaken(Artefact artefact) {
        return this.boardGame.isArtefactTaken(artefact);
    }

    /**
     * Completes the 'Run from Inaccessible Zone' action after the escape zone is selected.
     * Calls the model to perform the move (via handler) and updates the view.
     * @param zone The selected escape destination Zone.
     */
    public void chooseZoneToRunFromInaccessible(Zone zone) {
        modelActionHandler.handleModelAction(() -> {
            this.boardGame.chooseZoneToRunFromInaccessible(zone);
        },gameView);

        this.gameView.updateZonePanels();
        this.gameView.updatePlayerPanels();
    }
    /**
     * Gets the current flood rate (number of cards drawn per flood phase) from the model.
     * @return The current flood rate.
     */
    public int getFloodRate() {
        return this.boardGame.getFloodRate();
    }

    /**
     * Gets the current water level from the model.
     * @return The current water level (0-10).
     */
    public int getWaterMeterLevel() {
        return this.boardGame.getWaterMeterLevel();
    }
}
