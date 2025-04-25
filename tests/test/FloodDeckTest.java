package test;

import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FloodDeckTest {
    private FloodDeck deck;

    @BeforeEach
    void setUp() {
        deck = new FloodDeck();
    }

    @Test
    void testInitialDrawAndDiscardSizes() {
        assertEquals(24, deck.getDrawSize(), "Flood deck should start with 24 cards");
        assertEquals(0, deck.getDiscardSize(), "Discard pile should start empty");
    }

    @Test
    void testAllZoneCardsAreUnique() {
        Set<ZoneCard> seen = new HashSet<>();
        while (deck.getDrawSize() > 0) {
            seen.add(deck.draw());
        }
        assertEquals(24, seen.size(),
                "Drawing all cards once should yield 24 unique zone cards");
    }

    @Test
    void testDrawAndDiscard() {
        int before = deck.getDrawSize();
        ZoneCard c = deck.draw();
        assertEquals(before - 1, deck.getDrawSize(),
                "Drawing should remove one card from draw pile");
        deck.discard(c);
        assertEquals(1, deck.getDiscardSize(),
                "Discarding should add one card to discard pile");
    }

    @Test
    void testShufflePreservesBothPiles() {
        int drawBefore = deck.getDrawSize();
        int discardBefore = deck.getDiscardSize();
        deck.shuffle();
        assertEquals(drawBefore, deck.getDrawSize(),
                "Shuffle must not change draw pile size");
        assertEquals(discardBefore, deck.getDiscardSize(),
                "Shuffle must not change discard pile size");
    }

    @Test
    void testReshuffleSucceedsWhenDiscardIsNonEmpty() {
        ZoneCard c1 = deck.draw();
        ZoneCard c2 = deck.draw();
        deck.discard(c1);
        deck.discard(c2);

        while (deck.getDrawSize() > 0) {
            deck.draw();
        }
        ZoneCard next = deck.draw();
        assertNotNull(next, "After reshuffle, draw() should return a non-null card");
    }

    @Test
    void testDrawThrowsWhenNoCardsAnywhere() {
        while (deck.getDrawSize() > 0) {
            deck.draw();
        }
        assertThrows(IllegalStateException.class, deck::draw,
                "When both draw and discard piles are empty, draw() must throw");
    }
}
