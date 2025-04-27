package Model;

/**
 * Represents a single card in the game, primarily defined by its {@link CardType}.
 * Provides convenience methods to check the nature of the card (Treasure, Action, Water Rise).
 */
public class Card {
    private final CardType type;

    /**
     * Creates a new Card instance of the specified type.
     * @param type The {@link CardType} of this card.
     */
    public Card(CardType type) {
        this.type = type;
    }

    /**
     * Gets the type of this card.
     * @return The {@link CardType} enum value.
     */
    public CardType getType(){
        return type;
    }

    /**
     * Checks if this card is a Treasure card.
     * Delegates to {@link CardType#isTreasure()}.
     * @return true if it's a Treasure card, false otherwise.
     */
    public boolean isTreasure() {
        return type.isTreasure();
    }

    /**
     * Checks if this card is a Special Action card.
     * Delegates to {@link CardType#isAction()}.
     * @return true if it's a Special Action card, false otherwise.
     */
    public boolean isAction() {
        return type.isAction();
    }

    /**
     * Checks if this card is a Water Rise card.
     * Delegates to {@link CardType#isWaterRise()}.
     * @return true if it's a Water Rise card, false otherwise.
     */
    public boolean isWaterRise() {
        return type.isWaterRise();
    }

    /**
     * Returns the name of the card type as a String.
     * @return The name of the {@link CardType}.
     */
    @Override
    public String toString() {
        return type.name();
    }
}
