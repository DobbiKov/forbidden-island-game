package Errors;

public class InvalidActionForRole extends RuntimeException {
    public InvalidActionForRole(String message) {
        super(message);
    }
}
