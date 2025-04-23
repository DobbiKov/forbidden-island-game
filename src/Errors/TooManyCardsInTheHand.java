package Errors;

public class TooManyCardsInTheHand extends RuntimeException{
    public TooManyCardsInTheHand(){super("You have too many cards in the hand");}
}
