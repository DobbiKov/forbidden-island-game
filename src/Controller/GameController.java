package Controller;

import Model.BoardGame;
import Model.Zone;
import Model.ZoneState;

import java.util.Random;

public class GameController {
    private BoardGame boardGame;
    public GameController() {
        this.boardGame = new BoardGame(5);
    }
    public Zone[][] getZones() {
        return this.boardGame.getBoard();
    }
    public void finDeTour(){
       //inondation de trois zones
       int zone_flooded = 0;
       int must_be_flooded = 3;
       if(boardGame.getNumOfActiveZones() < must_be_flooded){
            boardGame.floodAllZones();
       }
       else {
           while (zone_flooded < must_be_flooded) {
               Random rand = new Random();
               int x = rand.nextInt(boardGame.getSize());
               int y = rand.nextInt(boardGame.getSize());
               if (boardGame.getZone(x, y).getZone_state() == ZoneState.Inaccessible) {
                   continue;
               }
               boardGame.floodZone(x, y);
               zone_flooded++;
           }
       }

       //suite
    }
}
