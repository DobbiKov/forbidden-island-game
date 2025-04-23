package test;

import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TreasureDeckTest {
    private TreasureDeck deck;

    @BeforeEach
    void setUp() {
        deck = new TreasureDeck();
    }

    @Test
    void testInitialDrawAndDiscardSizes() {
        assertEquals(25, deck.getDrawSize(), "Initial draw pile should have 25 cards");
        assertEquals(0, deck.getDiscardSize(), "Discard pile should start empty");
    }

    @Test
    void testAddWaterRiseCardsIncreasesDrawSize() {
        deck.addWaterRiseCards();
        assertEquals(28, deck.getDrawSize(), "After adding water-rise, draw pile should have 28 cards");
    }

    @Test
    void testDrawRemovesFromDrawPileAndDiscardAddsToDiscardPile() {
        int before = deck.getDrawSize();
        Card c = deck.draw();
        assertEquals(before - 1, deck.getDrawSize(), "Drawing should remove one card from draw pile");
        deck.discard(c);
        assertEquals(1, deck.getDiscardSize(), "Discarding should add one card to discard pile");
    }

    @Test
    void testShufflePreservesBothPiles() {
        int drawBefore = deck.getDrawSize();
        int discardBefore = deck.getDiscardSize();
        deck.shuffle();
        assertEquals(drawBefore, deck.getDrawSize(), "Shuffle must not change draw pile size");
        assertEquals(discardBefore, deck.getDiscardSize(), "Shuffle must not change discard pile size");
    }

    @Test
    void testReshuffleSucceedsWhenDiscardIsNonEmpty() {
        Card c1 = deck.draw();
        Card c2 = deck.draw();
        deck.discard(c1);
        deck.discard(c2);

        while (deck.getDrawSize() > 0) {
            deck.draw();
        }
        Card next = deck.draw();
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
