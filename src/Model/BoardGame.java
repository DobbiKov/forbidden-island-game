package Model;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoardGame {
    private int size;
    private Zone[][] board;
    private Player[] players;
    private static int player_conut = 0;


    public BoardGame(int size) {
        // zone init
        this.size = size;
        this.board = new Zone[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                this.board[i][j] = new Zone();
            }
        }

        // player init
        this.players = new Player[4];
        for (int i = 0; i < this.players.length; i++) {
            this.players[i] = null;
        }
    }
    public Zone[][] getBoard() {
        return board;
    }
    public int getSize() {
        return this.size;
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
    private Zone chooseZoneForPlayer(Player player){
        int mid = size/2;
        int last = size-1;
        switch (player_conut){
            case 0:{
                this.board[0][mid] = new PlayerStartZone(player);
                return this.board[0][mid];
            }
            case 1: {
                this.board[mid][last] = new PlayerStartZone(player);
                return this.board[mid][last];
            }
            case 2: {
                this.board[last][mid] = new PlayerStartZone(player);
                return this.board[last][mid];
            }
            case 3: {
                this.board[mid][0] = new PlayerStartZone(player);
                return this.board[mid][0];
            }
            default: return null;

        }
    }
    public void addPlayer(String name, PlayerRole role){
        if(player_conut > 3){
            throw new RuntimeException("The maximum number of players is reached.");
        }
        String temp_name = "";
        PlayerRole temp_role = PlayerRole.temp;
        Player player = new Player(temp_name, temp_role);

        this.players[player_conut] = player;
        Zone new_zone = this.chooseZoneForPlayer(player);
        if(new_zone == null){
            throw new RuntimeException("The maximum number of players is reached.");
        }
        player.setPlayerToZone(new_zone);
        player_conut++;
    }
}
