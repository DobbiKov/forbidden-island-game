package Model;

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
}
