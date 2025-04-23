package Model;

import java.util.ArrayList;
import java.util.List;

public class FloodDeck extends Deck<ZoneCard> {
    public FloodDeck() {
        super(collectAllZoneCards());
        shuffle();
    }

    private static List<ZoneCard> collectAllZoneCards() {
        List<ZoneCard> cards = new ArrayList<>();
        for(int i = 0; i < 24; i++){
            cards.add(ZoneCard.fromInt(i));
        }
        return cards;
    }



}
