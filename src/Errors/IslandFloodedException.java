package Errors;

public class IslandFloodedException extends RuntimeException {
    public IslandFloodedException() {
        super("The island has flooded. Game over!");
    }
}
