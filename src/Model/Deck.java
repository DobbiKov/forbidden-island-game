package Model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> drawCards = new ArrayList<>();
    private final List<Card> discardCards = new ArrayList<>();

    public Deck() {
        initDeck();
        shuffle();
    }


    public void initDeck(){
        for (int i = 0; i < 5; i++) {
            drawCards.add(new Card(CardType.EARTH_CARD));
            drawCards.add(new Card(CardType.AIR_CARD));
            drawCards.add(new Card(CardType.FIRE_CARD));
            drawCards.add(new Card(CardType.WATER_CARD));
        }

        for (int i = 0; i < 3; i++) {
            drawCards.add(new Card(CardType.HELICOPTER_LIFT));
        }

        for (int i = 0; i < 2; i++) {
            drawCards.add(new Card(CardType.SANDBAGS));
        }
    }

    public void addWaterRise(){
        for (int i = 0; i < 3; i++) {
            drawCards.add(new Card(CardType.WATER_RISE));
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(drawCards);
    }

    public int getDrawCardsSize(){
        return drawCards.size();
    }

    public int getDiscardCardsSize(){
        return discardCards.size();
    }

    public Card draw() {
        if (drawCards.isEmpty()) {
            reshuffleDiscardIntoDraw();
        }
        return drawCards.remove(drawCards.size() - 1);
    }

    private void reshuffleDiscardIntoDraw() {
        if (discardCards.isEmpty()) {
            throw new IllegalStateException("No cards left to draw or reshuffle.");
        }
        drawCards.addAll(discardCards);
        discardCards.clear();
        shuffle();
    }

    public void discard(Card card) {
        discardCards.add(card);
    }


}
