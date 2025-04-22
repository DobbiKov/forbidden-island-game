package Errors;

public class InvalidZoneToMove extends RuntimeException {
    public InvalidZoneToMove(String message) {
        super(message);
    }
}
