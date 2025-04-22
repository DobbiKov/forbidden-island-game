package Errors;

public class InvalidMoveForCurrentGameState extends RuntimeException {
  public InvalidMoveForCurrentGameState(String message) {
    super(message);
  }
}
