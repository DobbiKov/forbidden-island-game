package Model;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoardGame {
    private int size;
    private Zone[][] board;
    private int numPlayers;
    public BoardGame(int size, int numPlayers) {
        assert numPlayers > 0 && numPlayers <= 4;

        this.size = size;
        this.board = new Zone[size][size];
        this.numPlayers = numPlayers;

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                this.board[i][j] = new Zone();
            }
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
    public boolean verifyConstraints(){
        int fire_artefacts = 0;
        int water_artefacts = 0;
        int earth_artefacts = 0;
        int air_artefacts = 0;
        int helicopter = 0;
        int player_start = 0;
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                switch(this.getZone(i, j).getZone_type()){
                    case Casual: break;
                    case Helicopter:{
                        helicopter++;
                        break;
                    }
                    case PlayerStart: {
                        player_start++;
                        break;
                    }
                    case ArtefactAssociated:{
                        Artefact art = this.getZone(i, j).getArtefact();
                        if(art == null){
                            return false;
                        }
                        switch (art){
                            case Fire: {
                                fire_artefacts++;
                                break;
                            }
                            case Water: {
                                water_artefacts++;
                                break;
                            }
                            case Earth: {
                                earth_artefacts++;
                                break;
                            }
                            case Air: {
                                air_artefacts++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(fire_artefacts != 1 || water_artefacts != 1 || helicopter != 1 || air_artefacts != 1 || earth_artefacts != 1){
            return false;
        }
        return true;
    }
}
