package Model;

import java.util.ArrayList;
import java.util.List;

public class FloodDeck extends Deck<ZoneCard> {
    public FloodDeck() {
        super(collectAllZoneCards());
        shuffle();
    }

    public static List<ZoneCard> collectAllZoneCards() {
        List<ZoneCard> cards = new ArrayList<>();
        for(int i = 0; i < 24; i++){
            cards.add(ZoneCard.fromInt(i));
        }
        return cards;
    }

    public void reshuffleDiscardIntoDraw() {
        if (discardCards.isEmpty()) return;
        drawCards.addAll(discardCards);
        discardCards.clear();
        shuffle();
    }


}
