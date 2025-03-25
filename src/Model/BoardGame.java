package Model;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoardGame {
    private int size;
    private Zone[][] board;
    public BoardGame(int size) {
        this.size = size;
        this.board = new Zone[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                this.board[i][j] = new Zone();
            }
        }
        this.board[1][1].floodZone();
        this.board[2][3].floodZone();
        this.board[2][3].floodZone();
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
}
