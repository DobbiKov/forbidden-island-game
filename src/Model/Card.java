package Model;

public class Card {
    private final CardType type;

    public Card(CardType type) {
        this.type = type;
    }

    public CardType getType(){
        return type;
    }

    public boolean isTreasure() {
        return type.isTreasure();
    }

    public boolean isAction() {
        return type.isAction();
    }

    public boolean isWaterRise() {
        return type.isWaterRise();
    }

    @Override
    public String toString() {
        return type.name();
    }
}
