package Model;

import Errors.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents the main game logic and state for Forbidden Island.
 * Manages the game board (Zones), players, decks (Treasure and Flood),
 * game state transitions, player turns, actions, and win/lose conditions.
 */
public class BoardGame {
    private int size;
    private Zone[][] board;
    private Player[] players;
    private int playerCount;
    private GameState gameState;
    private int playerTurnId; // idx in the array of players or -1
    private int currentPlayerActionsNum;
    private final TreasureDeck treasureDeck;
    private final FloodDeck floodDeck;
    private int shoreUpsLeft = 0; // for engineer to count 2 shore ups per action
    private Player chosenPlayerByNavigator = null;
    private ZoneFactory zoneFactory;
    private PlayerFactory playerFactory;
    private boolean treasureDrawnThisTurn = false;
    private Player playerChoosingCardToUse = null;
    private final EnumSet<Artefact> claimedArtefacts = EnumSet.noneOf(Artefact.class);
    private ArrayList<Player> playersToFlyWith;
    private Card cardToGiveByPlayer;
    private WaterMeter waterMeter;
    private ArrayList<Player> playersOnInaccessibleZones;
    private Player currentPlayerRunningFromInaccessibleZone;
    private static final int TREASURES_PER_TURN = 2; // Number of treasure cards drawn per turn

    /**
     * Initializes a new game of Forbidden Island.
     * Sets up the board with zones using ZoneFactory, initializes player array,
     * creates Treasure and Flood decks, sets up the WaterMeter,
     * and sets the initial game state to SettingUp.
     * A central zone is made inaccessible initially.
     */
    public BoardGame() {
        // zone init
        this.playerCount = 0;
        this.zoneFactory = new ZoneFactory();
        this.playerFactory = new PlayerFactory();
        this.treasureDeck = new TreasureDeck();
        this.floodDeck = new FloodDeck();
        this.waterMeter = new WaterMeter();
        this.playersOnInaccessibleZones = new ArrayList<>();
        this.currentPlayerRunningFromInaccessibleZone = null;
        this.gameState = GameState.SettingUp;
        this.size = 5;
        this.cardToGiveByPlayer = null;
        this.board = new Zone[size][size];
        this.playersToFlyWith = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                boolean is_accessible = !(i == 2 && j == 2);
                if(!is_accessible) {
                    this.board[i][j] = zoneFactory.createInaccessibleZone(i, j);
                }
                else {
                    this.board[i][j] = zoneFactory.createRandomZone(i, j);
                }
            }
        }
        this.board[2][2].makeInaccessible();


        // player init
        this.players = new Player[4];
        for (int i = 0; i < this.players.length; i++) {
            this.players[i] = null;
        }

        // turn
        playerTurnId = -1;
        currentPlayerActionsNum = 3;
    }

    /**
     * Transitions the game from the SettingUp state to the Playing state.
     * Validates that there are 2-4 players. Deals initial hands to players.
     * Adds Water Rise cards to the Treasure Deck and shuffles it.
     * Starts the first player's turn.
     *
     * @throws RuntimeException if the game is not in the SettingUp state.
     * @throws NoPlayersException if no players have been added.
     * @throws InvalidNumberOfPlayersException if the player count is not between 2 and 4.
     */
    public void startGame() {
        if(gameState != GameState.SettingUp) {
            throw new RuntimeException("Can't start the game because the game isn't in the state of setting up");
        }
        if(playerCount == 0){
            throw new NoPlayersException();
        }
        if(playerCount < 2 || playerCount > 4){
            String message = "The number of players is less then 2";
            if(playerCount > 4){
                message = "The number of players is greater than 4";
            }
            throw new InvalidNumberOfPlayersException(message);
        }
        this.gameState = GameState.Playing;
        for(int i = 0; i< playerCount; i++){
            Player p = players[i];
            p.takeCard(treasureDeck.draw());
            p.takeCard(treasureDeck.draw());
        }
        treasureDeck.addWaterRiseCards();
        this.moveTurnToNextPlayer();
    }

    public Zone[][] getBoard() {
        return board;
    }
    public int getSize() {
        return this.size;
    }
    public Player[] getPlayers() {
        return this.players;
    }
    public Zone getZone(int x, int y){
        return this.board[x][y];
    }

    /**
     * Finds a Zone on the board based on its corresponding ZoneCard identifier.
     * Used primarily for resolving Flood card draws.
     *
     * @param zone_card The ZoneCard identifier of the zone to find.
     * @return The Zone object corresponding to the card.
     * @throws IllegalStateException if no zone with the given card is found on the board.
     */
    public Zone getZoneByCard(ZoneCard zone_card){
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                Zone zone = this.board[x][y];
                if(zone.getZoneCard() == zone_card){
                    return zone;
                }
            }
        }
        throw new IllegalStateException("No zone found for flood card " + zone_card);
    }

    public int getNumOfActiveZones(){
        int count = 0;
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(this.getZone(i, j).getZone_state() == ZoneState.Inaccessible){
                    continue;
                }
                count++;
            }
        }
        return count;
    }
    private void forAllZones(Consumer<Zone> func){
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                func.accept(this.board[i][j]);
            }
        }
    }
    public void floodAllZones(){
        this.forAllZones(Zone::floodZone);
    }
    /**
     * Selects an available starting zone for a new player based on their assigned color/role.
     * Iterates through potential starting zones and finds one that matches the player's color
     * and is not already associated with another player.
     *
     * @param player The player for whom to find a starting zone.
     * @return The chosen PlayerStartZone, or null if no suitable zone is available.
     */
    private Zone chooseZoneForPlayer(Player player){
        for(int i = 0; i < this.board.length; i++){
            for(int j = 0; j < this.board[i].length; j++){
                Zone zone = this.board[i][j];
                if(zone.getZone_type() != ZoneType.PlayerStart && zone.getZone_type() != ZoneType.Helicopter){continue;}
                if(((PlayerStartZone)zone).getCard_player_color() != player.getPlayerColor())  {continue;}
                return zone;
            }
        }
        return null;
    }

    /**
     * Adds a new player to the game during the setup phase.
     * Creates the player using PlayerFactory, finds an appropriate starting zone,
     * places the player on that zone, associates the player with the zone,
     * and adds the player to the game's player list.
     *
     * @param name The name for the new player (max 12 characters).
     * @return The newly created Player object.
     * @throws InvalidParameterException if the name is longer than 12 characters.
     * @throws MaximumNumberOfPlayersReachedException if 4 players already exist or no starting zone is available.
     */
    public Player addPlayer(String name){
        if(name.length() > 12){
            throw new InvalidParameterException("The name is too long, must be at most 12 characters");
        }
        if(playerCount > 3){
            throw new MaximumNumberOfPlayersReachedException();
        }
        Player player = this.playerFactory.createPlayer(name);
        Zone new_zone = this.chooseZoneForPlayer(player);
        if(new_zone == null){
            throw new MaximumNumberOfPlayersReachedException();
        }
        player.setPlayerToZone(new_zone);
        ((PlayerStartZone)new_zone).associatePlayer(player);
        this.players[playerCount++] = player;
        return player;
    }

    public int getPlayerTurnId(){
        return playerTurnId;
    }
    public Player getPlayerForTheTurn(){
        if (this.getPlayerTurnId() == -1){
            return null;
        }
        return this.players[this.getPlayerTurnId()];
    }
    /// Gives the turn to the next player
    public void nextPlayerTurn(){
        playerTurnId++;
        if(playerTurnId >= this.playerCount){
            this.playerTurnId = 0;
        }
    }
    public Player moveTurnToNextPlayer(){
        this.nextPlayerTurn();
        return this.getPlayerForTheTurn();
    }
    public boolean isGameSettingUp(){
        return this.gameState == GameState.SettingUp;
    }
    public boolean isPlayerChoosingToMove(){
        return this.gameState == GameState.PlayerChooseWhereToMove;
    }

    public boolean isGamePlaying(){
        return !this.isGameSettingUp();
    }
    /**
     * Returns the list of actions available specifically when a player must run from a sinking zone.
     * Currently, only allows the RunFromInaccessibleZone action.
     * @return A list containing only PlayerAction.RunFromInaccessibleZone.
     */
    private ArrayList<PlayerAction> getActionsToRunFromInaccessibleZone(){
        ArrayList<PlayerAction> actions = new ArrayList<>();
        actions.add(PlayerAction.RunFromInaccessibleZone);
        return actions;
    }
    /**
     * Gets the list of possible actions for a given player based on the current game state and turn.
     * If players are currently running from an inaccessible zone, it returns specific escape actions
     * only for those players who need to escape.
     * Otherwise, if it's the given player's turn, it calculates standard possible actions.
     * If it's not the player's turn, returns an empty list.
     *
     * @param player The player for whom to get actions.
     * @return A list of available PlayerActions.
     */
    public ArrayList<PlayerAction> getPossiblePlayerActionsForCurrentTurn(Player player){
        if(this.arePlayersRunningFromInaccesbleZone())
        {
            if(this.playersOnInaccessibleZones.contains(player)) {
                return this.getActionsToRunFromInaccessibleZone();
            }else {
                return new ArrayList<>();
            }
        }
        if(player == this.getPlayerForTheTurn())
            return getPossiblePlayerActions(player);
        return new ArrayList<>();
    }
    /**
     * Calculates the standard set of possible actions for a player, assuming it's their turn
     * and the game is in the standard 'Playing' state (not resolving escapes, discards, etc.).
     * Considers basic actions (Move, Drain), role-specific actions (Fly, MovePlayer),
     * context-dependent actions (GiveTreasureCard, TakeArtefact), and forced actions (DiscardCard).
     *
     * @param player The player whose possible actions are being calculated.
     * @return A list of possible PlayerActions for a standard turn. Returns empty list if player is null or game is setting up.
     */
    public ArrayList<PlayerAction> getPossiblePlayerActions(Player player){
        ArrayList<PlayerAction> possibleActions = new ArrayList<>();
        if(player == null || this.isGameSettingUp()){
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
        if (player == getPlayerForTheTurn() && getPlayerForTheTurn().getHand().getSize() > 5) {
            possibleActions.add(PlayerAction.DiscardCard);
        }
        if(player.getPlayer_zone() instanceof ArtefactZone){
            ArtefactZone artefact_zone = (ArtefactZone) player.getPlayer_zone();
            Artefact artefact = artefact_zone.getArtefact();
            CardType needed = cardTypeFor(artefact);
            if (!claimedArtefacts.contains(artefact) && hasAtLeast(needed)) {
                possibleActions.add(PlayerAction.TakeArtefact);
            }
        }
        return possibleActions;
    }



    public void setGameState(GameState gameState){
        this.gameState = gameState;
    }

    /**
     * Checks if the current player has actions remaining in their turn.
     * @return true if actions > 0, false otherwise.
     */
    private boolean isEnoughActions(){
        return this.currentPlayerActionsNum > 0;
    }

    /**
     * Handler for a player choosing zone to move while using move action and verifies all the constraints
     * @param zone a zone that player chose to move to
     */
    public void movePlayerToZone(Zone zone){
        Player player = this.getPlayerForTheTurn();
        if(!this.isEnoughActions()){
            throw new NoActionsLeft();
        }

        if(!this.isPlayerChoosingZoneToMove()){
            throw new InvalidMoveForCurrentGameState("The player is not currently choosing a zone to move");
        }
        if(!this.getZonesForPlayerToMove(player).contains(zone)){
            throw new InvalidZoneToMove("The zone you choose is not in the zone");
        }

        player.getPlayer_zone().removePlayerFromZone(player);
        player.move_Player(zone);
        zone.addPlayerToZone(player);
        this.setGameState(GameState.Playing);
        this.useOneAction();
    }

    /**
     * Handler for a pilot choosing zone to fly to while using fly action and verifies all the constraints
     * @param zone a zone that player chose to fly to
     */
    public void flyPilotToZone(Zone zone){
        Player player = this.getPlayerForTheTurn();
        if(!this.isEnoughActions()){
            throw new NoActionsLeft();
        }

        if(!this.isPilotChoosingZoneToFly()){
            throw new InvalidMoveForCurrentGameState("The player is not currently choosing a zone to move");
        }
        if(player.getPlayer_role() != PlayerRole.Pilot){
            throw new InvalidActionForRole("This player is not a pilot");
        }
        if(!this.getZonesForPlayerToFlyTo(player).contains(zone)){
            throw new InvalidZoneToMove("The zone you choose is not in the zone");
        }

        player.getPlayer_zone().removePlayerFromZone(player);
        player.move_Player(zone);
        zone.addPlayerToZone(player);
        this.setGameState(GameState.Playing);
        this.useOneAction();
    }
    public void choosePlayerByNavigator(Player player){
        if(!this.isNavgiatorChoosingAPlayerToMove()){
            throw new InvalidActionForTheCurrentState("The navigator is not currently choosing a player to move");
        }
        this.chosenPlayerByNavigator = player;
        this.setNavigatorChooseZoneToMoveThePlayerTo();
    }


    private void useOneAction() {
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.currentPlayerActionsNum--;
    }



    public int getCurrentPlayerActionsNum() {
        return this.currentPlayerActionsNum;
    }

    /**
     * Performs checks for various game-losing conditions before ending a turn.
     * Checks water level, player deaths, helicopter pad sinking, and artefact tile sinking.
     * May throw GameOverException if a lose condition is met.
     */
    private void checkLose(){
        checkWaterMeterMax();
        checkPlayerDead();                                    // may throw
        checkHelicopterZone();
        checkArtefactLost();
    }

    /**
     * Executes the end-of-turn sequence for the current player.
     * 1. Checks for win/lose conditions.
     * 2. Ensures players aren't currently escaping a sunk tile.
     * 3. Handles the Treasure Card drawing phase (draws 2 cards, handles Water Rise, checks hand limit).
     * 4. Handles the Flood Card drawing phase (draws cards based on water level, floods zones, potentially strands players).
     * 5. Advances turn to the next player, resets their actions.
     * 6. Checks for lose conditions again (e.g., if flooding caused a loss).
     * 7. Throws WaterRiseException if a Water Rise card was drawn (handled by controller/view).
     *
     * @throws GameOverException if a lose condition is met during the checks.
     * @throws GameWonException if the win condition is met.
     * @throws InvalidActionForTheCurrentState if trying to end turn while players must escape.
     * @throws TooManyCardsInTheHand if the player ends the turn over the hand limit after drawing.
     * @throws WaterRiseException if a Water Rise card was drawn during the treasure phase.
     */
    public void endTurn() {
        /* ---------- 0. victory / defeat checks ------ */
        checkLose();
        checkWin();


        /* ---------- 0.5 move from inaccessible ------ */
        ensureNotResolvingInaccessibleRun();                  // ❶ state-guard

        Player current = getPlayerForTheTurn();

        /* ---------- 1. treasure-card phase ---------- */
        boolean waterRiseTriggered = handleTreasurePhase(current);

        /* ---------- 2. flood-deck phase ------------- */
        resolveFloodPhase(waterMeter.getCurrentFloodRate());


        /* ---------- 4. prepare next player ---------- */
        treasureDrawnThisTurn = false;       // reset for next player
        nextPlayerTurn();
        setDefaultActionsNum();

        if (gameState != GameState.PlayersRunningFromAnInaccessibleZone) {
            gameState = GameState.Playing;   // normal flow resumes
        }

        // check lose again
        checkLose();

        /* ---------- 5. notify view if needed -------- */
        if (waterRiseTriggered) {
            throw new WaterRiseException();  // controller pops the dialog
        }
    }

    // ============
    // end turn helper
    /** Guard method: Ensures the game isn't in the state where players must escape a sinking tile before ending the turn. */
    private void ensureNotResolvingInaccessibleRun() {
        if (gameState == GameState.PlayersRunningFromAnInaccessibleZone) {
            throw new InvalidActionForTheCurrentState(
                    "You must move the stranded players before ending the turn");
        }
    }

    /**
     * Handles the treasure card drawing phase at the end of a turn.
     * Draws {@link #TREASURES_PER_TURN} cards. If a Water Rise card is drawn,
     * handles its effects (increase water level, reshuffle flood discard) and sets a flag.
     * Other cards are added to the player's hand. Finally, enforces the hand limit.
     * Skips drawing if cards were already drawn this turn (e.g., due to resolving hand limit).
     *
     * @param current The player whose turn is ending.
     * @return true if a Water Rise card was drawn, false otherwise.
     * @throws TooManyCardsInTheHand if the player is over the hand limit after drawing.
     */
    private boolean handleTreasurePhase(Player current) {
        // If we already did it this turn, only hand-limit may still apply
        if (treasureDrawnThisTurn) {
            enforceHandLimitOrContinue(current);
            return false;
        }

        boolean sawWaterRise = false;

        for (int i = 0; i < TREASURES_PER_TURN; i++) {
            Card c = treasureDeck.draw();
            if (c.isWaterRise()) {
                sawWaterRise = true;
                treasureDeck.discard(c);

                // 1. increase level
                if (waterMeter.increaseLevel()) {
                    throw new GameOverException("water level has reached maximum");
                }
                // 2. reshuffle flood discard into draw pile
                floodDeck.reshuffleDiscardIntoDraw();
            } else {
                current.takeCard(c);
            }
        }
        treasureDrawnThisTurn = true;
        enforceHandLimitOrContinue(current);

        return sawWaterRise;
    }

    /**
     * Checks if the player's hand is over the limit (5 cards).
     * If it is, sets the game state to Discarding and throws TooManyCardsInTheHand.
     * Otherwise, ensures the game state is Playing.
     *
     * @param current The player whose hand to check.
     * @throws TooManyCardsInTheHand if hand size > 5.
     */
    private void enforceHandLimitOrContinue(Player current) {
        if (current.getHand().isOverflow()) {
            gameState = GameState.Discarding;
            throw new TooManyCardsInTheHand();
        }
        gameState = GameState.Playing;
    }

    /**
     * Handles the flood card drawing phase at the end of a turn.
     * Draws a number of cards specified by the current flood rate on the WaterMeter.
     * For each card drawn, finds the corresponding zone and floods it (Dry -> Flooded, Flooded -> Inaccessible).
     * If a zone becomes inaccessible, any players on it are marked for escape.
     * Discards the drawn flood card.
     *
     * @param cardsToDraw The number of flood cards to draw.
     * @throws IslandFloodedException if the flood deck runs out completely.
     * @throws GameOverException if flooding causes a lose condition (e.g., Helicopter pad sinks).
     */
    private void resolveFloodPhase(int cardsToDraw) {
        for (int i = 0; i < cardsToDraw; i++) {

            if (floodDeck.getDrawSize() == 0 && floodDeck.getDiscardSize() == 0) {
                throw new IllegalStateException(
                        "Flood deck exhausted – rule-set violation");
            }

            ZoneCard drawn = floodDeck.draw();
            Zone z = getZoneByCard(drawn);

            if (z != null) {
                z.floodZone();

                if (!z.isAccessible() && !z.getPlayers_on_zone().isEmpty()) {
                    strandPlayersOn(z);
                }
            }
            floodDeck.discard(drawn);
        }
    }

    /**
     * Marks players currently on a given zone as needing to escape.
     * Adds them to the `playersOnInaccessibleZones` list and sets the game state
     * to `PlayersRunningFromAnInaccessibleZone`.
     *
     * @param z The Zone that just became inaccessible.
     */
    private void strandPlayersOn(Zone z) {
        if (playersOnInaccessibleZones == null) {
            playersOnInaccessibleZones = new ArrayList<>();
        }
        for (Player p : z.getPlayers_on_zone()) {
            if (!playersOnInaccessibleZones.contains(p)) {
                playersOnInaccessibleZones.add(p);
            }
        }
        currentPlayerRunningFromInaccessibleZone = null;
        gameState = GameState.PlayersRunningFromAnInaccessibleZone;
    }
    // end of end turn helpers
    // ====================

    /** Resets the current player's action count to the default (3). */
    private void setDefaultActionsNum(){
        this.currentPlayerActionsNum = 3;
    }

    //-------------
    //get players logic
    /**
     * Gets the set of players the Navigator can choose to move (all players except themselves).
     * @return A HashSet of Players the Navigator can target.
     * @throws InvalidActionForRole if called when not in the Navigator player selection state.
     */
    private HashSet<Player> getPlayerToChooseForNavigator(){
        if(this.isPlayerChoosingZoneToMove()){
            throw new InvalidActionForRole("This player is not a navigator");
        }
        Player current_player = this.getPlayerForTheTurn();
        HashSet<Player> res = new HashSet<>();
        for(Player p : this.getPlayers()){
            if(p == current_player || p == null){
                continue;
            }
            res.add(p);
        }
        return res;
    }

    /**
     * Gets the set of players eligible to be flown with a Helicopter Lift card.
     * Includes players on the same tile as the card user, excluding the user themselves
     * and any players already chosen to fly.
     * @return A HashSet of Players eligible to fly.
     * @throws InvalidStateOfTheGameException if not currently choosing players for Helicopter Lift.
     */
    private HashSet<Player> getPlayersToChooseToFlyWithCard(){
        if(!this.isPlayerChoosingZoneToFlyWithCard()){
            throw new InvalidStateOfTheGameException("The player is not currently choosing a player to fly with card");
        }
        Player current_player = this.playerChoosingCardToUse;
        HashSet<Player> res = new HashSet<>();
        for(Player p : this.getPlayers()){
            if(
                    p == current_player
                            || p == null
                            || this.playersToFlyWith.contains(p)
                            || !current_player.getPlayer_zone().getPlayers_on_zone().contains(p)
            ){
                continue;
            }
            res.add(p);
        }
        return res;
    }
    /**
     * Gets the set of players eligible to receive a treasure card.
     * Includes players on the same tile (or any tile for Messenger), excluding the current player,
     * and only if the recipient has less than 5 cards.
     * @return A HashSet of Players eligible to receive a card.
     * @throws InvalidStateOfTheGameException if not currently choosing a player to give a card to.
     */
    private HashSet<Player> getPlayersToChooseForGivingCard(){
       if(!this.isPlayerChoosingPlayerToGiveCardTo()){
           throw new InvalidStateOfTheGameException("The player is not currently choosing a player to give card");
       }
       HashSet<Player> res = new HashSet<>();
       Player current_player = this.getPlayerForTheTurn();
       for(Player p : this.getPlayers()){
           if(p == current_player || p == null){
               continue;
           }
           if(p.getHand().getSize() >= 5) { continue; }
           if(!current_player.getPlayer_zone().getPlayers_on_zone().contains(p) && current_player.getPlayer_role() != PlayerRole.Messenger){ continue; }
           res.add(p);
       }
       return res;
    }

    /**
     * Generic method to get the set of players that can be chosen in the current game state.
     * Delegates to specific methods based on whether the Navigator is choosing,
     * Helicopter Lift is being used, or a card is being given.
     * @return A HashSet of choosable Players.
     * @throws InvalidStateOfTheGameException if the game is not in a state where a player needs to be chosen.
     */
    public HashSet<Player> getPlayersToChoose(){
        if(!this.isPlayerChoosingZoneToFlyWithCard() && !this.isNavgiatorChoosingAPlayerToMove() && !this.isPlayerChoosingPlayerToGiveCardTo()){
           throw new InvalidStateOfTheGameException("The player is not currently choosing any player!");
        }
        if(this.isNavgiatorChoosingAPlayerToMove()){
            return this.getPlayerToChooseForNavigator();
        }else if(this.isPlayerChoosingZoneToFlyWithCard()){
            return this.getPlayersToChooseToFlyWithCard();
        }else if(this.isPlayerChoosingPlayerToGiveCardTo()){
            return this.getPlayersToChooseForGivingCard();
        }
        return new HashSet<>();
    }
    //-------------

    //------------
    //get zones logic

    /**
     * Gets the list of zones the current player can choose based on the current game action state.
     * Delegates to specific methods for moving, shoring up, flying, etc.
     * @return An ArrayList of choosable Zones. Returns empty list if no zone selection is active.
     */
    public ArrayList<Zone> getZonesPossibleForChoosing() {
        if(this.isPlayerChoosingZoneToRunFromInaccesbleZone()){
            return this.getZonesToRunFromInaccessibleZone();
        }
        else if(this.isPlayerChoosingZoneToMove()){
            return this.getZonesForPlayerToMove(this.getPlayerForTheTurn());
        }else if(this.isPlayerChoosingZoneToShoreUp()){
           return this.getZonesToForPlayerShoreUp(this.getPlayerForTheTurn());
        }else if(this.isPilotChoosingZoneToFly()){
            return this.getZonesForPlayerToFlyTo(this.getPlayerForTheTurn());
        }else if(this.isNavgiatorChoosingAZoneToMovePlayerTo()){
            return this.getZonesForNavigatorToMovePlayerTo();
        }else if(this.isPlayerChoosingZoneToFlyWithCard()){
            return this.getZonesToFlyWithCard();
        }else if(this.isPlayerChoosingZoneToShoreUpWithCard()){
            return this.getZonesToShoreUpWithCard();
        }
        return new ArrayList<>();
    }
    /**
     * Gets the list of zones a player can escape to from an inaccessible zone.
     * Considers the player's role for special movement capabilities (Pilot, Diver, Explorer).
     * @return An ArrayList of valid escape Zones.
     */
    public ArrayList<Zone> getZonesToRunFromInaccessibleZone(){
        Player current_player = this.currentPlayerRunningFromInaccessibleZone;
        switch(current_player.getPlayer_role()){
            case Pilot:
                return this.getZonesForPlayerToFlyTo(current_player);
            case Diver:
                return this.getZonesForDiverRunningFromInaccessible(current_player.getPlayer_zone());
            case Explorer:
                return this.getAdjacentZones(current_player.getPlayer_zone(), true, Zone::isAccessible);
        }
        return this.getAdjacentZones(current_player.getPlayer_zone(), false, Zone::isAccessible);
    }
    /**
     * Helper method to get adjacent zones to a given zone.
     * Can include orthogonal only or orthogonal + diagonal.
     * Can filter results based on a predicate (e.g., Zone::isAccessible, Zone::isDry).
     *
     * @param zone The central zone.
     * @param accept_diagonals If true, includes diagonal zones.
     * @param filter A Predicate to filter the resulting zones.
     * @return An ArrayList of adjacent zones matching the criteria.
     */
    private ArrayList<Zone> getAdjacentZones(Zone zone, boolean accept_diagonals, Predicate<Zone> filter){
        ArrayList<Zone> adjacentZones = new ArrayList<>();
        for(int i : new int[]{-1, 0, 1}){
            for(int j : new int[]{-1, 0, 1}) {
                if(i == 0 && j == 0){continue;}
                if(i != 0 && j != 0 && !accept_diagonals){continue;}
                int new_x = zone.getX() + i;
                int new_y = zone.getY() + j;
                if(new_x < 0 || new_y < 0 || new_x >= this.board.length || new_y >= this.board[0].length){continue;}
                Zone zone_to_add = this.getZone(new_x, new_y);
                if(filter == null || filter.test(zone_to_add)){
                    adjacentZones.add(zone_to_add);
                }
            }
        }
        return adjacentZones;
    }

    /**
     * Gets the possible move destinations for a Diver during a normal move action.
     * Includes adjacent dry tiles and reachable dry tiles through connected flooded/sunk tiles.
     * @param zone The Diver's current zone.
     * @return An ArrayList of valid move destinations.
     */
    private ArrayList<Zone> getZonesForDiver(Zone zone){
       return this.getZonesForDiverWithChoice(zone, true);
    }
    /**
     * Gets the possible escape destinations for a Diver running from an inaccessible zone.
     * Includes adjacent accessible tiles and reachable accessible tiles through connected flooded/sunk tiles.
     * @param zone The Diver's current (sinking) zone.
     * @return An ArrayList of valid escape destinations.
     */
    private ArrayList<Zone> getZonesForDiverRunningFromInaccessible(Zone zone){
        return this.getZonesForDiverWithChoice(zone, false);
    }
    /**
     * Core logic for Diver movement calculation (both normal move and escape).
     * Uses a breadth-first search starting from the origin zone, exploring through
     * adjacent flooded/sunk tiles to find reachable target tiles matching the `is_dry` criteria.
     *
     * @param zone The starting zone.
     * @param is_dry If true, finds reachable dry zones (normal move). If false, finds reachable accessible (dry or flooded) zones (escape move).
     * @return An ArrayList of reachable zones based on the criteria.
     */
    private ArrayList<Zone> getZonesForDiverWithChoice(Zone zone, boolean is_dry){
        HashSet<Zone> res_zones = new HashSet<>(this.getAdjacentZones(zone, false, z -> {
            if(is_dry) return z.isDry();
            return z.isAccessible();
        }));
        LinkedList<Zone> floodedQueue = new LinkedList<>(this.getAdjacentZones(zone, true, z -> !z.isDry()));
        HashSet<Zone> exploredFlooded = new HashSet<>();

        while(!floodedQueue.isEmpty()){
            Zone floodedZone = floodedQueue.poll();
            exploredFlooded.add(floodedZone);
            if(is_dry) {
                ArrayList<Zone> dryZones = this.getAdjacentZones(floodedZone, true, z -> {
                    if (res_zones.contains(z)) {
                        return false;
                    }
                    return z.isDry();
                });
                res_zones.addAll(dryZones);
            }else{

                ArrayList<Zone> accZones = this.getAdjacentZones(floodedZone, true, z -> {
                    if (res_zones.contains(z)) {
                        return false;
                    }
                    return z.isAccessible();
                });
                res_zones.addAll(accZones);
            }

            ArrayList<Zone> floodedZones = this.getAdjacentZones(floodedZone, true, z -> {
                if(exploredFlooded.contains(z)){ return false;}
                return !z.isDry();
            });
            floodedQueue.addAll(floodedZones);
        }

        return new ArrayList<>(res_zones);
    }
    /**
     * Gets the list of zones a player can move to during a standard move action.
     * Considers the player's role for special movement (Diver, Explorer).
     * @param player The player who is moving.
     * @return An ArrayList of valid destination Zones.
     */
    public ArrayList<Zone> getZonesForPlayerToMove(Player player) {
        if(player.getPlayer_role() == PlayerRole.Diver){
            return this.getZonesForDiver(player.getPlayer_zone());
        }
        if(player.getPlayer_role() == PlayerRole.Explorer){
            return this.getAdjacentZones(player.getPlayer_zone(), true, Zone::isAccessible);
        }
        return this.getAdjacentZones(player.getPlayer_zone(), false, Zone::isAccessible);
    }

    /**
     * Gets the list of zones a player can target with the Shore Up action.
     * Includes the player's current zone (if flooded) and adjacent flooded zones.
     * Explorer can target diagonally adjacent flooded zones as well.
     * @param player The player performing the Shore Up action.
     * @return An ArrayList of valid Zones to shore up.
     */
    public ArrayList<Zone> getZonesToForPlayerShoreUp(Player player) {
        ArrayList<Zone> res = this.getAdjacentZones(player.getPlayer_zone(), player.getPlayer_role() == PlayerRole.Explorer, Zone::isFlooded);

        Zone curr = player.getPlayer_zone(); // adding current if it's also flooded
        if(curr.isFlooded()){
            res.add(curr);
        }
        return res;
    }
    /**
     * Gets the list of all accessible zones on the board. Used for the Pilot's fly action.
     * Excludes the Pilot's current zone.
     * @param player The Pilot performing the action.
     * @return An ArrayList of all accessible Zones (excluding the current one).
     */
    private ArrayList<Zone> getZonesForPlayerToFlyTo(Player player) {
        ArrayList<Zone> res = new ArrayList<>();
        for(int i = 0; i < this.board.length; i++){
            for(int j = 0; j < this.board[i].length; j++){
                Zone curr = this.board[i][j];
                if(curr.isAccessible() && curr != player.getPlayer_zone()){
                    res.add(curr);
                }
            }
        }
        return res;
    }
    /**
     * Gets the list of zones the Navigator can move the chosen player to.
     * Includes zones adjacent (orthogonally and diagonally) to the chosen player's current zone,
     * provided they are dry.
     * @return An ArrayList of valid destination Zones for the Navigator's action.
     * @throws InvalidStateOfTheGameException if no player has been chosen by the Navigator yet.
     */
    private ArrayList<Zone> getZonesForNavigatorToMovePlayerTo(){
        Player player_to_move = this.chosenPlayerByNavigator;
        if(player_to_move == null){
            throw new InvalidStateOfTheGameException("The player to move is null!");
        }
        return this.getAdjacentZones(player_to_move.getPlayer_zone(), true, Zone::isDry);
    }
    /**
     * Gets the list of all flooded zones on the board. Used for the Sandbags card action.
     * @return An ArrayList of all flooded Zones.
     */
    private ArrayList<Zone> getZonesToShoreUpWithCard(){
        ArrayList<Zone> res = new ArrayList<>();
        for(int i = 0; i < this.board.length; i++){
            for(int j = 0; j < this.board[i].length; j++){
                Zone curr = this.board[i][j];
                if(curr.isFlooded()){
                    res.add(curr);
                }
            }
        }
        return res;
    }
    /**
     * Gets the list of all accessible zones on the board. Used for the Helicopter Lift card action.
     * @return An ArrayList of all accessible Zones.
     */
    private ArrayList<Zone> getZonesToFlyWithCard(){
        ArrayList<Zone> res = new ArrayList<>();
        for(int i = 0; i < this.board.length; i++){
            for(int j = 0; j < this.board[i].length; j++){
                Zone curr = this.board[i][j];
                if(curr.isAccessible()){
                    res.add(curr);
                }
            }
        }
        return res;
    }
    //end get zones
    //------------

    /**
     * Executes the Shore Up action after the player has chosen a target zone.
     * Validates the action, shores up the zone, changes game state, and consumes an action.
     * Handles the Engineer's ability to shore up twice for one action (partially implemented with shoreUpsLeft).
     *
     * @param zone The Zone the player chose to shore up.
     * @throws NoActionsLeft if the player has no actions remaining.
     * @throws InvalidMoveForCurrentGameState if the game state is not PlayerChooseWhereToShoreUp.
     * @throws InvalidZoneToMove if the chosen zone is not a valid target for shoring up.
     */
    public void playerShoreUpZone(Zone zone) {
        Player player = this.getPlayerForTheTurn();
        if(!this.isEnoughActions()){
            throw new NoActionsLeft();
        }

        if(!this.isPlayerChoosingZoneToShoreUp()){
            throw new InvalidMoveForCurrentGameState("The player is not currently choosing a zone to shore up");
        }
        if(!this.getZonesToForPlayerShoreUp(player).contains(zone)){
            throw new InvalidZoneToMove("The zone you choose is not in the zone");
        }

        zone.shoreUp();
        this.setGameState(GameState.Playing);
        if(player.getPlayer_role() == PlayerRole.Engineer){
            if(shoreUpsLeft == 0)
            {
                this.useOneAction();
                shoreUpsLeft = 1;
            }else{
                shoreUpsLeft =0;
            }
        }
        else {
            this.useOneAction();
        }
    }

    /**
     * Executes the Navigator's action to move another player after the destination zone is chosen.
     * Validates the state, moves the chosen player, resets the navigator's choice,
     * consumes an action, and sets the game state back to Playing.
     *
     * @param zone The destination Zone chosen by the Navigator.
     * @throws InvalidStateOfTheGameException if no player was chosen by the Navigator beforehand.
     * @throws InvalidZoneToMove if the chosen zone is not a valid destination for the chosen player.
     */
    public void movePlayerToZoneByNavigator(Zone zone) {
        Player player_to_move = this.chosenPlayerByNavigator;
        if(player_to_move == null){
            throw new InvalidStateOfTheGameException("The player to move is null!");
        }

        placePlayerToZone(player_to_move, zone);
        this.chosenPlayerByNavigator = null;
        this.useOneAction();
        this.setGameState(GameState.Playing);
    }

    //----------------
    //is player choosing state checks
    /** Checks if the game is currently waiting for the player to select *any* zone or player. */
    public boolean isPlayerChoosingSomething() {
        return
                   this.isPlayerChoosingZoneToShoreUp()
                || this.isPlayerChoosingZoneToMove()
                || this.isPilotChoosingZoneToFly()
                || this.isNavgiatorChoosingAPlayerToMove()
                || this.isNavgiatorChoosingAZoneToMovePlayerTo()
                || this.isPlayerChoosingZoneToShoreUpWithCard()
                || this.isPlayerChoosingZoneToFlyWithCard()
                || this.isPlayerChoosingPlayerToGiveCardTo()
                || this.isPlayerChoosingZoneToShoreUpWithCard()
                || this.isPlayerChoosingZoneToRunFromInaccesbleZone()
                ;
    }
    /** Checks if the game state is waiting for the player to choose a zone to move to. */
    public boolean isPlayerChoosingZoneToMove() {
        return this.gameState == GameState.PlayerChooseWhereToMove;
    }
    /** Checks if the game state is waiting for the Pilot to choose a zone to fly to. */
    public boolean isPilotChoosingZoneToFly() {
        return this.gameState == GameState.PilotChooseWhereToFly;
    }
    /** Checks if the game state is waiting for the player to choose a zone to shore up. */
    public boolean isPlayerChoosingZoneToShoreUp() {
        return this.gameState == GameState.PlayerChooseWhereToShoreUp;
    }
    /** Checks if the game state is waiting for the Navigator to choose another player to move. */
    public boolean isNavgiatorChoosingAPlayerToMove(){
        return this.gameState == GameState.NavigatorChooseAPlayerToMove;
    }
    /** Checks if the game state is waiting for the Navigator to choose a zone to move the selected player to. */
    public boolean isNavgiatorChoosingAZoneToMovePlayerTo(){
        return this.gameState == GameState.NavigatorChooseAZoneToMovePlayerTo;
    }
    /** Checks if the game state is waiting for the player to choose a zone to shore up using a Sandbags card. */
    public boolean isPlayerChoosingZoneToShoreUpWithCard() {
        return this.gameState == GameState.PlayerChooseAZoneToShoreUpWithCard;
    }
    /** Checks if the game state is waiting for the player to choose a zone to fly to using a Helicopter Lift card. */
    public boolean isPlayerChoosingZoneToFlyWithCard() {
        return this.gameState == GameState.PlayerChooseAZoneToFlyWithCard;
    }
    /** Checks if the game state is waiting for the current player to choose a card to give. */
    public boolean isPlayerChoosingCardToGive() {
        return this.gameState == GameState. PlayerChoosingCardToGive;
    }
    /** Checks if the specified player is the one currently choosing a card to give. */
    public boolean isThisPlayerChoosingCardToGive(Player player) {
        return this.isPlayerChoosingCardToGive() && this.getPlayerForTheTurn() == player;
    }
    /** Checks if the game state is waiting for the current player to choose another player to give a card to. */
    public boolean isPlayerChoosingPlayerToGiveCardTo(){
        return this.gameState == GameState.PlayerChoosePlayerToGiveCardTo;
    }
    /** Checks if the game state is waiting for the current player to choose a card to discard (due to hand limit). */
    public boolean isPlayerChoosingCardToDiscard(){
        return this.gameState == GameState.PlayerChooseCardToDiscard;
    }
    /** Checks if the game state is currently resolving players escaping from inaccessible zones. */
    public boolean arePlayersRunningFromInaccesbleZone(){
        return this.gameState == GameState.PlayersRunningFromAnInaccessibleZone;
    }
    /** Checks if a specific player is currently choosing a zone to escape to from an inaccessible zone. */
    public boolean isPlayerChoosingZoneToRunFromInaccesbleZone(){
        return this.gameState == GameState.PlayersRunningFromAnInaccessibleZone && this.currentPlayerRunningFromInaccessibleZone != null;
    }
    //----------------

    //-------------
    //set player choose state setters
    /** Sets the game state to allow the Pilot to choose a destination zone for their special flight action. */
    public void setPilotChooseWhereToFlyTo() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.PilotChooseWhereToFly);
    }
    /** Sets the game state to allow the Navigator to choose another player to move. */
    public void setNavigatorChoosePlayerToMove() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.NavigatorChooseAPlayerToMove);
    }
    /** Sets the game state to allow the Navigator to choose a destination zone for the selected player. */
    private void setNavigatorChooseZoneToMoveThePlayerTo() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        this.setGameState(GameState.NavigatorChooseAZoneToMovePlayerTo);
    }
    /** Sets the game state to allow the current player to choose a destination zone for a standard move action. */
    public void setPlayerChooseZoneToMoveTo(){
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.gameState = GameState.PlayerChooseWhereToMove;
    }

    /** Sets the game state to allow the current player to choose a zone to shore up. */
    public void setPlayerChooseZoneToShoreUp() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.PlayerChooseWhereToShoreUp);
    }
    /** Sets the game state to allow the current player to choose a treasure card from their hand to give away. */
    public void setPlayerGiveTreasureCards() {
        if(this.getCurrentPlayerActionsNum() <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.PlayerChoosingCardToGive);
        this.cardToGiveByPlayer = null;
    }
    /** Sets the game state to force the current player to choose a card to discard (due to hand limit). */
    public void setPlayerDiscardCard() {
        this.gameState = GameState.PlayerChooseCardToDiscard;
    }

    //end set player choose
    //---------------

    /** Helper method to move a player from their current zone to a new zone. */
    private void placePlayerToZone(Player player, Zone zone) {
        player.getPlayer_zone().removePlayerFromZone(player);
        player.move_Player(zone);
        zone.addPlayerToZone(player);
    }

    public TreasureDeck getTreasureDeck() {
        return this.treasureDeck;
    }
    public FloodDeck getFloodDeck() {
        return this.floodDeck;
    }

    /**
     * Handles the action of discarding a card when over the hand limit.
     * Removes the card from the player's hand and adds it to the treasure discard pile.
     * If the hand size is now valid, advances the turn.
     *
     * @param card The card chosen to discard.
     * @throws IllegalStateException if the game is not in the Discarding state.
     */
    public void discardTreasureCard(Card card) {
        if (gameState != GameState.Discarding) {
            throw new IllegalStateException("Not currently discarding");
        }
        Player p = getPlayerForTheTurn();
        p.getHand().remove(card);
        this.treasureDeck.discard(card);

        if (!p.getHand().isOverflow()) {
            nextPlayerTurn();
            setDefaultActionsNum();
            this.gameState = GameState.Playing;
        }
    }

    /**
     * Gets a copy of the cards currently in the specified player's hand.
     * @param player The player whose hand to retrieve.
     * @return An ArrayList containing the cards in the player's hand.
     */
    public ArrayList<Card> getCurrentPlayerCards(Player player) {
        return new ArrayList<>(this.players[player.getPlayer_id()].getHand().getCards());
    }

    /**
     * Initiates the process of using a special action card (Helicopter Lift or Sandbags).
     * Sets the appropriate game state (choosing zone to fly/shore up) and stores
     * which player is using the card.
     *
     * @param player The player using the action card.
     * @param card The action card being used (must be HELICOPTER_LIFT or SANDBAGS).
     * @throws InvalidActionForTheCurrentState if attempting to use a card while players must escape.
     * @throws InvalidParameterException if the player doesn't have the specified card or it's not an action card.
     */
    public void playerUseActionCard(Player player, Card card) {
        if(this.arePlayersRunningFromInaccesbleZone()){
            throw new InvalidActionForTheCurrentState("You can't use a card now!");
        }
        if(!player.getHand().getCards().contains(card)){
            throw new InvalidParameterException("You do not have such a card!");
        }
        switch(card.getType()){
            case HELICOPTER_LIFT:
                this.setGameState(GameState.PlayerChooseAZoneToFlyWithCard);
                this.playerChoosingCardToUse = player;
                this.playersToFlyWith = new ArrayList<>();
                break;
            case SANDBAGS:
                this.playerChoosingCardToUse = player;
                this.setGameState(GameState.PlayerChooseAZoneToShoreUpWithCard);
                break;
        }
    }

    /**
     * Completes the Helicopter Lift action after the destination zone and accompanying players are chosen.
     * Moves the user and chosen players to the destination zone.
     * Removes the Helicopter Lift card from the user's hand and discards it.
     * Resets the game state and temporary variables.
     *
     * @param zone The chosen destination Zone.
     * @throws InvalidStateOfTheGameException if not currently resolving a Helicopter Lift action or if the card is missing.
     */
    public void flyPlayerToZoneWithCard(Zone zone) {
        Player player = this.playerChoosingCardToUse;
        if(!this.isPlayerChoosingZoneToFlyWithCard()){
            throw new InvalidStateOfTheGameException("Player is not choosing to fly to a zone!");
        }
        this.setGameState(GameState.Playing);
        Card card = null;
        for(Card c : player.getHand().getCards()){
            if(c.getType() == CardType.HELICOPTER_LIFT){
                card = c;
                break;
            }
        }
        if(card == null) {
            throw new InvalidStateOfTheGameException("The player doesn't have a card to fly!");
        }
        for(Player p: this.playersToFlyWith){ //TODO
            this.placePlayerToZone(p, zone);
        }
        this.placePlayerToZone(player, zone);

        player.getHand().remove(card);
        this.treasureDeck.discard(card);
        this.playersToFlyWith = new ArrayList<>();
        this.playerChoosingCardToUse = null;
    }

    /**
     * Completes the Sandbags action after the target zone is chosen.
     * Shores up the chosen zone.
     * Removes the Sandbags card from the user's hand and discards it.
     * Resets the game state and temporary variables.
     *
     * @param zone The chosen Zone to shore up.
     * @throws InvalidStateOfTheGameException if not currently resolving a Sandbags action or if the card is missing.
     */
    public void shoreUpZoneWithCard(Zone zone) {
        Player player = this.playerChoosingCardToUse;
        if(!this.isPlayerChoosingZoneToShoreUpWithCard()){
            throw new InvalidStateOfTheGameException("Player is not choosing to shore up a zone!");
        }
        this.setGameState(GameState.Playing);
        Card card = null;
        for(Card c : player.getHand().getCards()){
            if(c.getType() == CardType.SANDBAGS){
                card = c;
                break;
            }
        }
        if(card == null) {
            throw new InvalidStateOfTheGameException("The player doesn't have a card to shore up!");
        }
        player.getHand().remove(card);
        this.treasureDeck.discard(card);
        zone.shoreUp();

        this.playerChoosingCardToUse = null;
    }


    /** Gets the set of artefacts that have already been claimed by any player. */
    public EnumSet<Artefact> getClaimedArtefacts() {
        return EnumSet.copyOf(claimedArtefacts);
    }

    /**
     * Executes the action to claim an artefact.
     * Validates that the player is on the correct ArtefactZone, the artefact hasn't been claimed,
     * and the player has the required 4 matching treasure cards.
     * Discards the 4 treasure cards, adds the artefact to the claimed set and the player's inventory,
     * and consumes an action.
     *
     * @throws NoActionsLeft if the player has no actions.
     * @throws InvalidStateOfTheGameException if not on an artefact zone, artefact already claimed, or insufficient cards.
     */
    public void takeArtefact() {
        Player p = getPlayerForTheTurn();
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        Zone current_zone = p.getPlayer_zone();
        if(!(current_zone instanceof ArtefactZone)){
            throw new InvalidStateOfTheGameException("You must stand on the zone with artefact");
        }
        ArtefactZone artefactZone = (ArtefactZone) current_zone;
        Artefact artefact = artefactZone.getArtefact();
        if(claimedArtefacts.contains(artefact)){
            throw new InvalidStateOfTheGameException("This artefact is already taken!");
        }
        CardType needed = cardTypeFor(artefact);
        if (!hasAtLeast(needed)) {
            throw new IllegalStateException("Not enough " + needed + " cards");
        }
        int discards = 0;
        List<Card> handCards = new ArrayList<>(p.getHand().getCards());
        for (Card c : handCards) {
            if (c.getType() == needed && discards < 4) {
                p.getHand().remove(c);
                this.treasureDeck.discard(c);
                discards++;
            }
        }
        claimedArtefacts.add(artefact);
        this.useOneAction();
        p.addArtefact(artefact);

    }

    /**
     * Checks if the current player has at least 4 cards of the specified treasure type.
     * @param cardType The CardType to count (e.g., FIRE_CARD).
     * @return true if the player has 4 or more cards of that type, false otherwise.
     */
    private boolean hasAtLeast(CardType cardType) {
        Player p = getPlayerForTheTurn();
        int cpt = 0;
        for(Card card : p.getHand().getCards()) {
            if(card.getType() == cardType) {
                cpt++;
                if(cpt >= 4){return true;}
            }
        }
        return false;
    }

    /**
     * Maps an Artefact enum value to its corresponding treasure CardType needed to claim it.
     * @param art The Artefact.
     * @return The required CardType.
     * @throws IllegalArgumentException for invalid Artefact input.
     */
    private CardType cardTypeFor(Artefact art) {
        switch (art) {
            case Fire:  return CardType.FIRE_CARD;
            case Water: return CardType.WATER_CARD;
            case Wind:  return CardType.AIR_CARD;
            case Earth: return CardType.EARTH_CARD;
            default:    throw new IllegalArgumentException();
        }
    }

    /**
     * Adds a player to the list of players accompanying the user of a Helicopter Lift card.
     * Validates that the game is in the correct state and the chosen player is eligible.
     *
     * @param chosen_player The player to add to the flight group.
     * @throws InvalidStateOfTheGameException if not currently choosing players for Helicopter Lift.
     * @throws InvalidParameterException if the chosen player is already selected or ineligible.
     */
    public void choosePlayerToFlyWithCard(Player chosen_player) {
        if(!this.isPlayerChoosingZoneToFlyWithCard()){
            throw new InvalidStateOfTheGameException("The player is not currently choosing a player to fly with card");
        }
        if(this.playersToFlyWith.contains(chosen_player)){
            throw new InvalidParameterException("This player is already chosen!");
        }
        if(!this.getPlayersToChoose().contains(chosen_player)){
            throw new InvalidParameterException("This player is not choosable!");
        }
        this.playersToFlyWith.add(chosen_player);
    }


    /**
     * Handles the current player selecting a card from their hand to give away.
     * Stores the chosen card and transitions the game state to allow choosing the recipient.
     *
     * @param p The player choosing the card (must be the current player).
     * @param c The Card chosen from the hand.
     * @throws InvalidStateOfTheGameException if not in the correct state or if p is not the current player.
     */
    public void playerChooseCardToGive(Player p, Card c) {
        if(!this.isPlayerChoosingCardToGive()){
            throw new InvalidStateOfTheGameException("The player is not currently choosing a card to give");
        }
        if(this.getPlayerForTheTurn() != p){
            throw new InvalidStateOfTheGameException("This player doesn't have it's turn right now!");
        }
        this.cardToGiveByPlayer = c;
        this.setGameState(GameState.PlayerChoosePlayerToGiveCardTo);
    }

    /**
     * Completes the Give Treasure Card action after the recipient player is chosen.
     * Validates the state, checks recipient eligibility (hand size, location for non-Messengers).
     * Transfers the card, consumes an action, and resets the game state.
     *
     * @param player The chosen recipient Player.
     * @throws InvalidStateOfTheGameException if not in the correct state, no card was chosen, or recipient hand is full.
     * @throws InvalidParameterException if trying to give to self or if recipient is out of range (for non-Messengers).
     */
    public void choosePlayerToGiveCardTo(Player player) {
        if(!this.isPlayerChoosingPlayerToGiveCardTo()){
            throw new InvalidStateOfTheGameException("The player is not currently choosing a player to give to give the card to");
        }
        if(this.getPlayerForTheTurn() == player){
            throw new InvalidParameterException("You can't choose yourself");
        }
        if(this.cardToGiveByPlayer == null){
            throw new InvalidStateOfTheGameException("You haven't chosen any card!");
        }
        if(player.getHand().getSize() >= 5){
            throw new InvalidStateOfTheGameException("This player can't take more cards!");
        }
        if(this.getPlayerForTheTurn().getPlayer_role() != PlayerRole.Messenger && player.getPlayer_zone() != this.getPlayerForTheTurn().getPlayer_zone()){
            throw new InvalidParameterException("Chosen player must stand on the same zone as you!");
        }
        this.getPlayerForTheTurn().getHand().remove(this.cardToGiveByPlayer);
        player.getHand().add(this.cardToGiveByPlayer);
        this.cardToGiveByPlayer = null;
        this.gameState = GameState.Playing;
        this.useOneAction();
    }

    public void playerDiscardCard(Player player, Card c) {
        if(!this.isPlayerChoosingCardToDiscard()){
            throw new InvalidStateOfTheGameException("The player is not currently choosing a card to discard");
        }
        if(player != this.getPlayerForTheTurn()){
            throw new InvalidStateOfTheGameException("This player doesn't have it's turn right now!");
        }
        if(!player.getHand().getCards().contains(c)){
            throw new InvalidStateOfTheGameException("This player doesn't have this card!");
        }

        this.setGameState(GameState.Playing);
        player.getHand().remove(c);
        treasureDeck.discard(c);
        if(player.getHand().getSize() > 5){
            this.setGameState(GameState.PlayerChooseCardToDiscard);
        }
    }

    public boolean isThisPlayerChoosingCardToDiscard(Player player) {
        return this.isPlayerChoosingCardToDiscard() && this.getPlayerForTheTurn() == player;
    }

    /**
     * Checks if the specified player can perform standard actions (Move, Shore Up, role actions).
     * Currently, this is only restricted if the player is over the hand limit (must discard first).
     * @param player The player to check.
     * @return true if the player's hand size is 5 or less, false otherwise.
     */
    public boolean canPlayerUseBasicAction(Player player){
        return player.getHand().getSize() <= 5;
    }

    /** Gets the current level of the WaterMeter. */
    public int getWaterMeterLevel() {
        return waterMeter.getLevel();
    }

    public int getFloodRate(){
        return waterMeter.getCurrentFloodRate();
    }

    /** Checks if the Helicopter Landing zone (Fools' Landing) has sunk. Throws GameOverException if it has. */
    private void checkHelicopterZone(){
        Zone heli = getZoneByCard(ZoneCard.fodls_landing);
        if (!heli.isAccessible()) {
            throw new GameOverException("the helicopter landing site has sunk");
        }
    }

    /**
     * Checks if any zones required to claim an *unclaimed* artefact have sunk.
     * If both zones for an unclaimed artefact are inaccessible, throws GameOverException.
     */
    private void checkArtefactLost(){
        for(Artefact artefact : EnumSet.complementOf(claimedArtefacts)) {
            int cpt = 0;
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    Zone z = board[x][y];
                    if (z instanceof ArtefactZone && ((ArtefactZone) z).getArtefact() == artefact) {
                        if (z.getZone_state() == ZoneState.Inaccessible) {
                            cpt++;
                            if(cpt == 2){
                                throw new GameOverException("you have lost the artefact!");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if any player currently marked as needing to escape is unable to do so
     * (i.e., all adjacent/reachable zones according to their role are also inaccessible).
     * Throws GameOverException if a player is trapped.
     */
    private void checkPlayerDead() {
        if (playersOnInaccessibleZones == null || playersOnInaccessibleZones.isEmpty()) {
            return;  // no one stranded
        }

        for (Player p : playersOnInaccessibleZones) {
            Zone current = p.getPlayer_zone();
            List<Zone> escapeZones;

            // determine legal “run from inaccessible” zones depending on role
            switch (p.getPlayer_role()) {
                case Pilot:
                    // pilot may fly to any accessible tile
                    escapeZones = getZonesForPlayerToFlyTo(p);
                    break;
                case Diver:
                    // diver may swim through flooded/inaccessible to adjacent accessible
                    escapeZones = getZonesForDiverRunningFromInaccessible(current);
                    break;
                case Explorer:
                    // explorer may move diagonally but only to accessible tiles
                    escapeZones = getAdjacentZones(current, true, Zone::isAccessible);
                    break;
                default:
                    // everyone else only orthogonally to accessible tiles
                    escapeZones = getAdjacentZones(current, false, Zone::isAccessible);
                    break;
            }

            if (escapeZones.isEmpty()) {
                throw new GameOverException("you have lost the player!");
            }
        }
    }

    /** Checks if the water level has reached the maximum (skull and crossbones). Throws GameOverException if it has. */
    private void checkWaterMeterMax() {
        if (waterMeter.getLevel() >= WaterMeter.MAX_LEVEL) {
            throw new GameOverException("water level has reached maximum");
        }
    }



    /** Checks if a specific artefact has already been claimed. */
    public boolean isArtefactTaken(Artefact artefact) {
        return this.claimedArtefacts.contains(artefact);
    }

    /**
     * Sets the game state to allow a specific player (who is on an inaccessible zone)
     * to choose an adjacent accessible zone to escape to.
     * @param player The player who needs to escape.
     * @throws InvalidStateOfTheGameException if another player is already choosing their escape route.
     */
    public void setPlayerChooseZoneToRunFromInaccessibleZone(Player player) {
        if(this.currentPlayerRunningFromInaccessibleZone != null){
            throw new InvalidStateOfTheGameException("There is already a player running from inaccessible zone");
        }
        this.currentPlayerRunningFromInaccessibleZone = player;
    }

    /**
     * Executes the escape move after the player has chosen a destination zone.
     * Validates the state and zone accessibility. Moves the player, removes them
     * from the list of players needing to escape. If no more players need to escape,
     * returns the game state to Playing (or proceeds with the turn end sequence).
     *
     * @param zone The chosen accessible destination Zone.
     * @throws InvalidStateOfTheGameException if not currently resolving an escape move.
     * @throws InvalidZoneToMove if the chosen zone is not accessible.
     */
    public void chooseZoneToRunFromInaccessible(Zone zone) {
        if(!this.isPlayerChoosingZoneToRunFromInaccesbleZone()){
            throw new InvalidStateOfTheGameException("Nobody is trying to run from inaccessible zone");
        }
        if(!zone.isAccessible()){
            throw new InvalidParameterException("This zone is not accessbile!");
        }
        this.placePlayerToZone(this.currentPlayerRunningFromInaccessibleZone, zone);
        this.playersOnInaccessibleZones.remove(this.currentPlayerRunningFromInaccessibleZone);
        this.currentPlayerRunningFromInaccessibleZone = null;

        if(!this.playersOnInaccessibleZones.isEmpty()){
            this.setGameState(GameState.PlayersRunningFromAnInaccessibleZone);
        }
        else{
            this.setGameState(GameState.Playing);
        }
    }

    /**
     * Checks if the conditions for winning the game have been met:
     * 1. All artefacts have been claimed.
     * 2. All players are on the Helicopter Landing zone.
     * 3. At least one player has a Helicopter Lift card in hand.
     * Throws GameWonException if all conditions are met.
     */
    private void checkWin(){
        if(claimedArtefacts.size() < Artefact.values().length) return;

        Zone heli = getZoneByCard(ZoneCard.fodls_landing);
        for (Player p : players) {
            if (p == null) continue;
            if (p.getPlayer_zone() != heli) return;
        }
        boolean hasLift = false;
        for (Player p : players) {
            if (p == null) continue;
            for (Card c : p.getHand().getCards()) {
                if (c.getType() == CardType.HELICOPTER_LIFT) {
                    hasLift = true;
                    break;
                }
            }
            if (hasLift) break;
        }
        if (!hasLift) return;

        throw new GameWonException();
    }
}
