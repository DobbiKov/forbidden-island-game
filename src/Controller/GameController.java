package Controller;

import Model.BoardGame;
import Model.Zone;

public class GameController {
    private BoardGame boardGame;
    public GameController() {
        this.boardGame = new BoardGame(5);
    }
    public Zone[][] getZones() {
        return this.boardGame.getBoard();
    }
}
