package Errors;

public class InvalidStateOfTheGameException extends RuntimeException {
    public InvalidStateOfTheGameException(String message) {
        super(message);
    }
}
