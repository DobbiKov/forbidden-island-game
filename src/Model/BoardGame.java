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
    private int player_count = 0;
    private HashSet<PlayerRole> used_roles;
    private GameState game_state;
    private int player_turn_id; // idx in the array of players or -1
    private int current_player_actions_num;

    public BoardGame() {
        // zone init
        this.game_state = GameState.SettingUp;
        this.size = 5;
        this.board = new Zone[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(i == 2 && j == 2){
                }
                boolean is_accessible = !(i == 2 && j == 2);
                this.board[i][j] = new Zone(i, j, is_accessible);
            }
        }
        this.board[2][2].makeInaccessible();

        //roles
        used_roles = new HashSet<>();

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
    public PlayerRole getAvailibleRole(){
        if(this.used_roles.size() >= 6){
            return null;
        }
        Random random = new Random();
        int n = random.nextInt(6);
        PlayerRole role = PlayerRole.getByNum(n);
        while(used_roles.contains(role)){
            n = random.nextInt(6);
            role = PlayerRole.getByNum(n);
        }
        return role;
    }
    private Zone chooseZoneForPlayer(Player player){
        int mid = size/2;
        int last = size-1;
        switch (player_count){
            case 0:{
                this.board[0][mid] = new PlayerStartZone(this.board[0][mid], player);
                return this.board[0][mid];
            }
            case 1: {
                this.board[mid][last] = new PlayerStartZone(this.board[mid][last], player);
                return this.board[mid][last];
            }
            case 2: {
                this.board[last][mid] = new PlayerStartZone(this.board[last][mid], player);
                return this.board[last][mid];
            }
            case 3: {
                this.board[mid][0] = new PlayerStartZone(this.board[mid][0], player);
                return this.board[mid][0];
            }
            default: return null;

        }
    }
    public Player addPlayer(String name){
        if(player_count > 3){
            throw new MaximumNumberOfPlayersReachedException();
        }
        PlayerRole role_to_assign = this.getAvailibleRole();
        if (role_to_assign == null) {
            throw new NoRoleToAssignError();
        }
        Player player = new Player(name, role_to_assign);
        this.used_roles.add(role_to_assign);

        this.players[player_count] = player;
        Zone new_zone = this.chooseZoneForPlayer(player);
        if(new_zone == null){
            throw new MaximumNumberOfPlayersReachedException();
        }
        player.setPlayerToZone(new_zone);
        player_count++;
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

    private void useOneAction() {
        this.current_player_actions_num--;
    }

    private boolean isPlayerChoosingZoneToMove() {
        return this.game_state == GameState.PlayerChooseWhereToMove;
    }

    public void setPlayerChooseZoneToMoveTo(){
        // TODO: verify that it's possible
        this.game_state = GameState.PlayerChooseWhereToMove;
    }

    public int getCurrent_player_actions_num() {
        return this.current_player_actions_num;
    }

    public void finDeTour()
    {
        //inondation de trois zones
        int zone_flooded = 0;
        int must_be_flooded = 3;
        if(this.getNumOfActiveZones() < must_be_flooded){
            this.floodAllZones();
        }
        else {
            while (zone_flooded < must_be_flooded) {
                Random rand = new Random();
                int x = rand.nextInt(this.getSize());
                int y = rand.nextInt(this.getSize());
                if (this.getZone(x, y).getZone_state() == ZoneState.Inaccessible) {
                    continue;
                }
                this.floodZone(x, y);
                zone_flooded++;
            }
        }

        this.playerFinishTurn();
    }

    private void playerFinishTurn() {
        Player p = this.getPlayerForTheTurn();
        this.nextPlayerTurn();
        this.setDefaultActionsNum();
    }
    private void setDefaultActionsNum(){
        this.current_player_actions_num = 3;
    }

    public ArrayList<Zone> getZonesPossibleForChoosing() {
        if(this.isPlayerChoosingZoneToMove()){
            return this.getZonesForPlayerToMove(this.getPlayerForTheTurn());
        }else if(this.isPlayerChoosingZoneToShoreUp()){
           return this.getZonesToForPlayerShoreUp(this.getPlayerForTheTurn());
        }
        return new ArrayList<>();
    }

    private ArrayList<Zone> getZonesToForPlayerShoreUp(Player player) {
        ArrayList<Zone> res = this.getAdjacentZones(player.getPlayer_zone(), true, Zone::isFlooded);

        Zone curr = player.getPlayer_zone(); // adding current if it's also flooded
        if(curr.isFlooded()){
            res.add(curr);
        }
        return res;
    }

    public boolean isPlayerChoosingZoneToShoreUp() {
        return this.game_state == GameState.PlayerChooseWhereToShoreUp;
    }

    public void setPlayerChooseZoneToShoreUp() {
        this.setGame_state(GameState.PlayerChooseWhereToShoreUp);
    }

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
        this.useOneAction(); // TODO: if it's a specific role that can shoreup two tiles
    }
}
