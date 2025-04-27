package Model;

/**
 * Enumerates the different types of cards found in the Treasure Deck.
 * Includes Treasure cards (matching Artefacts), Special Action cards, and Water Rise cards.
 */
public enum CardType {
    /** Treasure card corresponding to the Earth Stone artefact. */
    EARTH_CARD,
    /** Treasure card corresponding to the Statue of the Wind artefact. */
    AIR_CARD,
    /** Treasure card corresponding to the Crystal of Fire artefact. */
    FIRE_CARD,
    /** Treasure card corresponding to the Ocean's Chalice artefact. */
    WATER_CARD,
    /** Special action card allowing movement between any two tiles. */
    HELICOPTER_LIFT,
    /** Special action card allowing shoring up any flooded tile. */
    SANDBAGS,
    /** Special card that increases the water level and triggers flooding. */
    WATER_RISE;

    /**
     * Checks if this card type is a Treasure card (Earth, Air, Fire, Water).
     * @return true if it's a Treasure card, false otherwise.
     */
    public boolean isTreasure(){
        switch (this){
            case EARTH_CARD:
            case AIR_CARD:
            case FIRE_CARD:
            case WATER_CARD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if this card type is a Special Action card (Helicopter Lift, Sandbags).
     * @return true if it's a Special Action card, false otherwise.
     */
    public boolean isAction(){
        return this == HELICOPTER_LIFT || this == SANDBAGS;
    }

    /**
     * Checks if this card type is a Water Rise card.
     * @return true if it's a Water Rise card, false otherwise.
     */
    public boolean isWaterRise(){
        return this == WATER_RISE;
    }
}
