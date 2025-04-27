package Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a generic deck of cards (e.g., Treasure Deck, Flood Deck).
 * Manages a draw pile and a discard pile. Supports drawing, discarding,
 * shuffling, and automatically reshuffling the discard pile into the draw pile
 * when the draw pile is empty.
 *
 * @param <T> The type of items stored in the deck (e.g., Card, ZoneCard).
 */
public class Deck<T> {
    protected final List<T> drawCards = new ArrayList<>(); // Cards available to be drawn
    protected final List<T> discardCards = new ArrayList<>(); // Cards that have been discarded

    /**
     * Creates a new deck initialized with a collection of items.
     * The initial items are added directly to the draw pile.
     * Consider shuffling after creation if needed.
     *
     * @param initial The initial collection of items to populate the deck's draw pile.
     */
    public Deck(Collection<T> initial) {
        drawCards.addAll(initial);
    }

    /**
     * Shuffles the cards currently in the draw pile randomly.
     */
    public void shuffle() {
        Collections.shuffle(drawCards);
    }

    /**
     * Returns the number of cards currently in the draw pile.
     * @return The size of the draw pile.
     */
    public int getDrawSize() {
        return drawCards.size();
    }

    /**
     * Returns the number of cards currently in the discard pile.
     * @return The size of the discard pile.
     */
    public int getDiscardSize() {
        return discardCards.size();
    }

    /**
     * Returns the list of cards currently in the draw pile.
     * Note: Modifying the returned list directly might break the deck's state.
     * @return The list of draw cards.
     */
    public List<T> getDrawCards() {
        return drawCards;
    }
    /**
     * Returns the list of cards currently in the discard pile.
     * Note: Modifying the returned list directly might break the deck's state.
     * @return The list of discard cards.
     */
    public List<T> getDiscardCards() {
        return discardCards;
    }

    /**
     * Draws the top card from the draw pile.
     * If the draw pile is empty, it first attempts to reshuffle the discard pile
     * into the draw pile.
     *
     * @return The card drawn from the top of the draw pile.
     * @throws IllegalStateException if both the draw and discard piles are empty.
     */
    public T draw() {
        if (drawCards.isEmpty()) {
            reshuffleDiscardIntoDraw();
        }
        if (drawCards.isEmpty()) {
            throw new IllegalStateException("No cards left to draw or reshuffle.");
        }

        return drawCards.remove(drawCards.size() - 1);
    }

    /**
     * Adds a card to the top of the discard pile.
     * @param card The card to discard.
     */
    public void discard(T card) {
        discardCards.add(card);
    }

    /**
     * Moves all cards from the discard pile to the draw pile and shuffles the draw pile.
     * Does nothing if the discard pile is empty.
     */
    private void reshuffleDiscardIntoDraw() {
        if (discardCards.isEmpty()) return;
        drawCards.addAll(discardCards);
        discardCards.clear();
        shuffle();
    }
}
