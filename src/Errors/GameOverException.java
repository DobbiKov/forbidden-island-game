package Errors;

public class GameOverException extends RuntimeException {
    public GameOverException(String reason) {
        super("Game over: " + reason);
    }
}