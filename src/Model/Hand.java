package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();
    private static final int MAX_SIZE = 5;

    public void add(Card card) {
        cards.add(card);
    }

    public void remove(Card card) {
        boolean removed = cards.remove(card);
        if (!removed) {
            throw new IllegalArgumentException(
                    "Cannot remove card " + card + " because it is not in the hand."
            );
        }
    }

    public int getSize(){
        return cards.size();
    }

    public boolean isOverflow(){
        return cards.size() > MAX_SIZE;
    }

    public List<Card> getCards(){
        return Collections.unmodifiableList(cards);
    }
}
