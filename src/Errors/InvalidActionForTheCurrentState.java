package Errors;

public class InvalidActionForTheCurrentState extends RuntimeException {
    public InvalidActionForTheCurrentState(String message) {
        super(message);
    }
}
