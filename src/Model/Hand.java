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
        cards.remove(card);
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
