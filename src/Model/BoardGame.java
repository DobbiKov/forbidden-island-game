package Model;

import Errors.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoardGame {
    private int size;
    private Zone[][] board;
    private Player[] players;
    private int player_count;
    private GameState game_state;
    private int player_turn_id; // idx in the array of players or -1
    private int current_player_actions_num;
    private final TreasureDeck treasureDeck;
    private final FloodDeck floodDeck;
    private int shore_ups_left = 0; // for engineer to count 2 shore ups per action
    private Player chosen_player_by_navigator = null;
    private ZoneFactory zone_factory;
    private PlayerFactory player_factory;

    public BoardGame() {
        // zone init
        this.player_count = 0;
        this.zone_factory = new ZoneFactory();
        this.player_factory = new PlayerFactory();
        this.treasureDeck = new TreasureDeck();
        this.floodDeck = new FloodDeck();
        this.game_state = GameState.SettingUp;
        this.size = 5;
        this.board = new Zone[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                boolean is_accessible = !(i == 2 && j == 2);
                if(!is_accessible) {
                    this.board[i][j] = zone_factory.createInaccessibleZone(i, j);
                }
                else {
                    this.board[i][j] = zone_factory.createRandomZone(i, j);
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
        player_turn_id = -1;
        current_player_actions_num = 3;
    }
    public void startGame() {
        if(game_state != GameState.SettingUp) {
            throw new RuntimeException("Can't start the game because the game isn't in the state of setting up");
        }
        if(player_count == 0){
            throw new NoPlayersException();
        }
        if(player_count < 2 || player_count > 4){
            String message = "The number of players is less then 2";
            if(player_count > 4){
                message = "The number of players is greater than 4";
            }
            throw new InvalidNumberOfPlayersException(message);
        }
        this.game_state = GameState.Playing;
        for(int i = 0; i< player_count; i++){
            Player p = players[i];
            p.takeCard(treasureDeck.draw());
            p.takeCard(treasureDeck.draw());
        }
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
    public void floodZone(int x, int y){
        this.board[x][y].floodZone();
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
        if(player_count > 3){
            throw new MaximumNumberOfPlayersReachedException();
        }
        Player player = this.player_factory.createPlayer(name);
        Zone new_zone = this.chooseZoneForPlayer(player);
        if(new_zone == null){
            throw new MaximumNumberOfPlayersReachedException();
        }
        player.setPlayerToZone(new_zone);
        ((PlayerStartZone)new_zone).associatePlayer(player);
        this.players[player_count++] = player;
        return player;
    }

    public int getPlayerTurnId(){
        return player_turn_id;
    }
    public Player getPlayerForTheTurn(){
        if (this.getPlayerTurnId() == -1){
            return null;
        }
        return this.players[this.getPlayerTurnId()];
    }
    /// Gives the turn to the next player
    public void nextPlayerTurn(){
        player_turn_id++;
        if(player_turn_id >= this.player_count){
            this.player_turn_id = 0;
        }
    }
    public Player moveTurnToNextPlayer(){
        this.nextPlayerTurn();
        return this.getPlayerForTheTurn();
    }
    public boolean isGameSettingUp(){
        return this.game_state == GameState.SettingUp;
    }
    public boolean isPlayerChoosingToMove(){
        return this.game_state == GameState.PlayerChooseWhereToMove;
    }

    public boolean isGamePlaying(){
        return !this.isGameSettingUp();
    }

    public ArrayList<PlayerAction> getPossiblePlayerActionsForCurrentTurn(Player player){
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
        return possibleActions;
    }



    public void setGame_state(GameState game_state){
        this.game_state = game_state;
    }

    private boolean isEnoughActions(){
        return this.current_player_actions_num > 0;
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
        this.setGame_state(GameState.Playing);
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
        this.setGame_state(GameState.Playing);
        this.useOneAction();
    }
    public void choosePlayerByNavigator(Player player){
        if(!this.isNavgiatorChoosingAPlayerToMove()){
            throw new InvalidActionForTheCurrentState("The navigator is not currently choosing a player to move");
        }
        this.chosen_player_by_navigator = player;
        this.setNavigatorChooseZoneToMoveThePlayerTo();
    }


    private void useOneAction() {
        if(this.current_player_actions_num <= 0){
            throw new NoActionsLeft();
        }
        this.current_player_actions_num--;
    }



    public int getCurrent_player_actions_num() {
        return this.current_player_actions_num;
    }

    public void finDeTour()
    {
        for (int i = 0; i < 3; i++) {
            ZoneCard card;
            try {
                card = floodDeck.draw();
            } catch (IllegalStateException e) {
                break;
            }
            Zone z = getZoneByCard(card);
            if (z != null) {
                z.floodZone();
            }
            floodDeck.discard(card);
        }
        playerFinishTurn();
    }

    private void playerFinishTurn() {
        Player p = this.getPlayerForTheTurn();
        p.takeCard(treasureDeck.draw());
        p.takeCard(treasureDeck.draw());
        this.nextPlayerTurn();
        this.setDefaultActionsNum();
    }
    private void setDefaultActionsNum(){
        this.current_player_actions_num = 3;
    }

    public HashSet<Player> getPlayersToChoose(){
        if(this.isPlayerChoosingZoneToMove()){
            throw new InvalidActionForRole("This player is not a navgiator");
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

    //------------
    //get zones

    public ArrayList<Zone> getZonesPossibleForChoosing() {
        if(this.isPlayerChoosingZoneToMove()){
            return this.getZonesForPlayerToMove(this.getPlayerForTheTurn());
        }else if(this.isPlayerChoosingZoneToShoreUp()){
           return this.getZonesToForPlayerShoreUp(this.getPlayerForTheTurn());
        }else if(this.isPilotChoosingZoneToFly()){
            return this.getZonesForPlayerToFlyTo(this.getPlayerForTheTurn());
        }else if(this.isNavgiatorChoosingAZoneToMovePlayerTo()){
            return this.getZonesForNavigatorToMovePlayerTo();
        }
        return new ArrayList<>();
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
        HashSet<Zone> res_zones = new HashSet<>(this.getAdjacentZones(zone, false, Zone::isDry));
        LinkedList<Zone> floodedQueue = new LinkedList<>(this.getAdjacentZones(zone, true, z -> !z.isDry()));
        HashSet<Zone> exploredFlooded = new HashSet<>();

        while(!floodedQueue.isEmpty()){
            Zone floodedZone = floodedQueue.poll();
            exploredFlooded.add(floodedZone);
            ArrayList<Zone> dryZones = this.getAdjacentZones(floodedZone, true, z -> {
                if(res_zones.contains(z)){ return false;}
                return z.isDry();
            });
            res_zones.addAll(dryZones);

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

    private ArrayList<Zone> getZonesToForPlayerShoreUp(Player player) {
        ArrayList<Zone> res = this.getAdjacentZones(player.getPlayer_zone(), true, Zone::isFlooded);

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
                if(curr.isDry() && curr != player.getPlayer_zone()){
                    res.add(curr);
                }
            }
        }
        return res;
    }
    private ArrayList<Zone> getZonesForNavigatorToMovePlayerTo(){
        Player player_to_move = this.chosen_player_by_navigator;
        if(player_to_move == null){
            throw new InvalidStateOfTheGameException("The player to move is null!");
        }
        return this.getAdjacentZones(player_to_move.getPlayer_zone(), true, Zone::isDry);
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
        this.setGame_state(GameState.Playing);
        if(player.getPlayer_role() == PlayerRole.Engineer){
            if(shore_ups_left == 0)
            {
                this.useOneAction();
                shore_ups_left = 1;
            }else{
                shore_ups_left=0;
            }
        }
        else {
            this.useOneAction();
        }
    }
    public void movePlayerToZoneByNavigator(Zone zone) {
        Player player_to_move = this.chosen_player_by_navigator;
        if(player_to_move == null){
            throw new InvalidStateOfTheGameException("The player to move is null!");
        }

        placePlayerToZone(player_to_move, zone);
        this.chosen_player_by_navigator = null;
        this.useOneAction();
        this.setGame_state(GameState.Playing);
    }

    //----------------
    //is player choosing
    public boolean isPlayerChoosingSomething() {
        return
                   this.isPlayerChoosingZoneToShoreUp()
                || this.isPlayerChoosingZoneToMove()
                || this.isPilotChoosingZoneToFly()
                || this.isNavgiatorChoosingAPlayerToMove()
                || this.isNavgiatorChoosingAZoneToMovePlayerTo();
    }
    public boolean isPlayerChoosingZoneToMove() {
        return this.game_state == GameState.PlayerChooseWhereToMove;
    }
    public boolean isPilotChoosingZoneToFly() {
        return this.game_state == GameState.PilotChooseWhereToFly;
    }
    public boolean isPlayerChoosingZoneToShoreUp() {
        return this.game_state == GameState.PlayerChooseWhereToShoreUp;
    }
    public boolean isNavgiatorChoosingAPlayerToMove(){
        return this.game_state == GameState.NavigatorChooseAPlayerToMove;
    }
    public boolean isNavgiatorChoosingAZoneToMovePlayerTo(){
        return this.game_state == GameState.NavigatorChooseAZoneToMovePlayerTo;
    }
    //----------------

    //-------------
    //set player choose
    public void setPilotChooseWhereToFlyTo() {
        this.setGame_state(GameState.PilotChooseWhereToFly);
    }
    public void setNavigatorChoosePlayerToMove() {
        this.setGame_state(GameState.NavigatorChooseAPlayerToMove);
    }
    private void setNavigatorChooseZoneToMoveThePlayerTo() {
        this.setGame_state(GameState.NavigatorChooseAZoneToMovePlayerTo);
    }
    public void setPlayerChooseZoneToMoveTo(){
        // TODO: verify that it's possible
        this.game_state = GameState.PlayerChooseWhereToMove;
    }

    public void setPlayerChooseZoneToShoreUp() {
        this.setGame_state(GameState.PlayerChooseWhereToShoreUp);
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
}
