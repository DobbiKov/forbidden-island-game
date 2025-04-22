package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Model.*;

class DeckTest {
    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
        deck.addWaterRise();
    }
    @Test
    void testInitialSizes() {
        int expected = 5*4 + 3 + 2 + 3;
        assertEquals(expected, deck.getDrawCardsSize());
        assertEquals(0, deck.getDiscardCardsSize());
    }


    @Test
    void testDrawAndDiscard() {
        int before = deck.getDrawCardsSize();
        Card c = deck.draw();
        assertEquals(before - 1, deck.getDrawCardsSize());
        deck.discard(c);
        assertEquals(1, deck.getDiscardCardsSize());
    }

    @Test
    void testShufflePreservesSizes() {
        Deck deck = new Deck();
        int drawBefore= deck.getDrawCardsSize();
        int discardBefore = deck.getDiscardCardsSize();
        deck.shuffle();
        assertEquals(drawBefore,deck.getDrawCardsSize());
        assertEquals(discardBefore, deck.getDiscardCardsSize());
    }

    @Test
    void testReshuffleSucceedsWhenDiscarded() {
        Deck d = new Deck();

        Card c1 = d.draw();
        Card c2 = d.draw();
        d.discard(c1);
        d.discard(c2);

        while (d.getDrawCardsSize() > 0) {
            d.draw();
        }
        Card next = d.draw();
        assertNotNull(next, "Expected to get a card after reshuffle");
    }

    @Test
    void testReshuffleThrowsWhenNoDiscard() {
        Deck d = new Deck();
        while (d.getDrawCardsSize() > 0) {
            d.draw();
        }
        assertThrows(IllegalStateException.class, d::draw);
    }




}
