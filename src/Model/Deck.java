package Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Deck<T> {
    protected final List<T> drawCards = new ArrayList<>();
    protected final List<T> discardCards = new ArrayList<>();

    public Deck(Collection<T> initial) {
        drawCards.addAll(initial);
    }

    public void shuffle() {
        Collections.shuffle(drawCards);
    }

    public int getDrawSize() {
        return drawCards.size();
    }

    public int getDiscardSize() {
        return discardCards.size();
    }

    public T draw() {
        if (drawCards.isEmpty()) {
            reshuffleDiscardIntoDraw();
        }
        if (drawCards.isEmpty()) {
            throw new IllegalStateException("No cards left to draw or reshuffle.");
        }

        return drawCards.remove(drawCards.size() - 1);
    }

    public void discard(T card) {
        discardCards.add(card);
    }

    private void reshuffleDiscardIntoDraw() {
        if (discardCards.isEmpty()) return;
        drawCards.addAll(discardCards);
        discardCards.clear();
        shuffle();
    }
}
