package Model;

import java.util.ArrayList;
import java.util.List;

public class TreasureDeck extends Deck<Card> {

    public TreasureDeck() {
        super(initTreasureCards());
        shuffle();
    }

    public static List<Card> initTreasureCards() {
        List<Card> all = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            all.add(new Card(CardType.EARTH_CARD));
            all.add(new Card(CardType.AIR_CARD));
            all.add(new Card(CardType.FIRE_CARD));
            all.add(new Card(CardType.WATER_CARD));
        }

        for (int i = 0; i < 3; i++) {all.add(new Card(CardType.HELICOPTER_LIFT));}
        for (int i = 0; i < 2; i++) {all.add(new Card(CardType.SANDBAGS));}
        return all;
    }

    public void addWaterRiseCards() {
        for (int i = 0; i < 3; i++) {
            drawCards.add(new Card(CardType.WATER_RISE));
        }
        shuffle();
    }
}
