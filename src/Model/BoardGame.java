package Model;

import Errors.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    private static final int TREASURES_PER_TURN = 2;

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
    private ArrayList<PlayerAction> getActionsToRunFromInaccessibleZone(){
        ArrayList<PlayerAction> actions = new ArrayList<>();
        actions.add(PlayerAction.RunFromInaccessibleZone);
        return actions;
    }
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

    private boolean isEnoughActions(){
        return this.currentPlayerActionsNum > 0;
    }
    /// THe player clicked move and chose the zone
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
    /// THe player clicked move and chose the zone
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

    private void checkLose(){
        checkWaterMeterMax();
        checkPlayerDead();                                    // may throw
        checkHelicopterZone();
        checkArtefactLost();
    }

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
    /** Guard: you cannot end a turn while someone still has to escape. */
    private void ensureNotResolvingInaccessibleRun() {
        if (gameState == GameState.PlayersRunningFromAnInaccessibleZone) {
            throw new InvalidActionForTheCurrentState(
                    "You must move the stranded players before ending the turn");
        }
    }

    /** Draw two treasure cards, handle Water-Rise, enforce hand limit. */
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

    /** If hand overflow -> switch state & throw, otherwise keep Playing. */
    private void enforceHandLimitOrContinue(Player current) {
        if (current.getHand().isOverflow()) {
            gameState = GameState.Discarding;
            throw new TooManyCardsInTheHand();
        }
        gameState = GameState.Playing;
    }

    /** Flood N cards; strand players if their tile just sank. */
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

    /** Move every player on a sunken tile to the “must escape” set. */
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
    private void setDefaultActionsNum(){
        this.currentPlayerActionsNum = 3;
    }

    //-------------
    //get players
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
    //get zones

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

    private ArrayList<Zone> getZonesForDiver(Zone zone){
       return this.getZonesForDiverWithChoice(zone, true);
    }
    private ArrayList<Zone> getZonesForDiverRunningFromInaccessible(Zone zone){
        return this.getZonesForDiverWithChoice(zone, false);
    }
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
    public ArrayList<Zone> getZonesForPlayerToMove(Player player) {
        if(player.getPlayer_role() == PlayerRole.Diver){
            return this.getZonesForDiver(player.getPlayer_zone());
        }
        if(player.getPlayer_role() == PlayerRole.Explorer){
            return this.getAdjacentZones(player.getPlayer_zone(), true, Zone::isDry);
        }
        return this.getAdjacentZones(player.getPlayer_zone(), false, Zone::isDry);
    }

    public ArrayList<Zone> getZonesToForPlayerShoreUp(Player player) {
        ArrayList<Zone> res = this.getAdjacentZones(player.getPlayer_zone(), player.getPlayer_role() == PlayerRole.Explorer, Zone::isFlooded);

        Zone curr = player.getPlayer_zone(); // adding current if it's also flooded
        if(curr.isFlooded()){
            res.add(curr);
        }
        return res;
    }
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
    private ArrayList<Zone> getZonesForNavigatorToMovePlayerTo(){
        Player player_to_move = this.chosenPlayerByNavigator;
        if(player_to_move == null){
            throw new InvalidStateOfTheGameException("The player to move is null!");
        }
        return this.getAdjacentZones(player_to_move.getPlayer_zone(), true, Zone::isDry);
    }
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
    //is player choosing
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
    public boolean isPlayerChoosingZoneToMove() {
        return this.gameState == GameState.PlayerChooseWhereToMove;
    }
    public boolean isPilotChoosingZoneToFly() {
        return this.gameState == GameState.PilotChooseWhereToFly;
    }
    public boolean isPlayerChoosingZoneToShoreUp() {
        return this.gameState == GameState.PlayerChooseWhereToShoreUp;
    }
    public boolean isNavgiatorChoosingAPlayerToMove(){
        return this.gameState == GameState.NavigatorChooseAPlayerToMove;
    }
    public boolean isNavgiatorChoosingAZoneToMovePlayerTo(){
        return this.gameState == GameState.NavigatorChooseAZoneToMovePlayerTo;
    }
    public boolean isPlayerChoosingZoneToShoreUpWithCard() {
        return this.gameState == GameState.PlayerChooseAZoneToShoreUpWithCard;
    }
    public boolean isPlayerChoosingZoneToFlyWithCard() {
        return this.gameState == GameState.PlayerChooseAZoneToFlyWithCard;
    }
    public boolean isPlayerChoosingCardToGive() {
        return this.gameState == GameState. PlayerChoosingCardToGive;
    }
    public boolean isThisPlayerChoosingCardToGive(Player player) {
        return this.isPlayerChoosingCardToGive() && this.getPlayerForTheTurn() == player;
    }
    public boolean isPlayerChoosingPlayerToGiveCardTo(){
        return this.gameState == GameState.PlayerChoosePlayerToGiveCardTo;
    }
    public boolean isPlayerChoosingCardToDiscard(){
        return this.gameState == GameState.PlayerChooseCardToDiscard;
    }
    public boolean arePlayersRunningFromInaccesbleZone(){
        return this.gameState == GameState.PlayersRunningFromAnInaccessibleZone;
    }
    public boolean isPlayerChoosingZoneToRunFromInaccesbleZone(){
        return this.gameState == GameState.PlayersRunningFromAnInaccessibleZone && this.currentPlayerRunningFromInaccessibleZone != null;
    }
    //----------------

    //-------------
    //set player choose
    public void setPilotChooseWhereToFlyTo() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.PilotChooseWhereToFly);
    }
    public void setNavigatorChoosePlayerToMove() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.NavigatorChooseAPlayerToMove);
    }
    private void setNavigatorChooseZoneToMoveThePlayerTo() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        this.setGameState(GameState.NavigatorChooseAZoneToMovePlayerTo);
    }
    public void setPlayerChooseZoneToMoveTo(){
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.gameState = GameState.PlayerChooseWhereToMove;
    }

    public void setPlayerChooseZoneToShoreUp() {
        if(!canPlayerUseBasicAction(this.getPlayerForTheTurn())){
            throw new InvalidActionForTheCurrentState("You have to discard a card!");
        }
        if(this.currentPlayerActionsNum <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.PlayerChooseWhereToShoreUp);
    }
    public void setPlayerGiveTreasureCards() {
        if(this.getCurrentPlayerActionsNum() <= 0){
            throw new NoActionsLeft();
        }
        this.setGameState(GameState.PlayerChoosingCardToGive);
        this.cardToGiveByPlayer = null;
    }
    public void setPlayerDiscardCard() {
        this.gameState = GameState.PlayerChooseCardToDiscard;
    }

    //end
    //---------------
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

    public ArrayList<Card> getCurrentPlayerCards(Player player) {
        return new ArrayList<>(this.players[player.getPlayer_id()].getHand().getCards());
    }

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


    public EnumSet<Artefact> getClaimedArtefacts() {
        return EnumSet.copyOf(claimedArtefacts);
    }

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

    private CardType cardTypeFor(Artefact art) {
        switch (art) {
            case Fire:  return CardType.FIRE_CARD;
            case Water: return CardType.WATER_CARD;
            case Wind:  return CardType.AIR_CARD;
            case Earth: return CardType.EARTH_CARD;
            default:    throw new IllegalArgumentException();
        }
    }

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

    public boolean canPlayerUseBasicAction(Player player){
        return player.getHand().getSize() <= 5;
    }

    public int getWaterMeterLevel() {
        return waterMeter.getLevel();
    }

    private void checkHelicopterZone(){
        Zone heli = getZoneByCard(ZoneCard.fodls_landing);
        if (!heli.isAccessible()) {
            throw new GameOverException("the helicopter landing site has sunk");
        }
    }

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

    private void checkWaterMeterMax() {
        if (waterMeter.getLevel() >= WaterMeter.MAX_LEVEL) {
            throw new GameOverException("water level has reached maximum");
        }
    }



    public boolean isArtefactTaken(Artefact artefact) {
        return this.claimedArtefacts.contains(artefact);
    }

    public void setPlayerChooseZoneToRunFromInaccessibleZone(Player player) {
        if(this.currentPlayerRunningFromInaccessibleZone != null){
            throw new InvalidStateOfTheGameException("There is already a player running from inaccessible zone");
        }
        this.currentPlayerRunningFromInaccessibleZone = player;
    }

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
