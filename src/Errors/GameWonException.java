package Errors;

public class GameWonException extends RuntimeException {
    public GameWonException() {
        super("Congratulations! You have won the game!");
    }
}
