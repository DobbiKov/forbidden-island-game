package test;

import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    @Test
    @DisplayName("FloodDeck initializes with all 24 ZoneCards")
    void initializesWithAllZoneCards() {
        FloodDeck deck = new FloodDeck();
        assertEquals(24, deck.getDrawSize());
        assertEquals(0, deck.getDiscardSize());

        List<ZoneCard> drawnCards = Stream.generate(deck::draw)
                .limit(24)
                .collect(Collectors.toList());

        assertEquals(24, drawnCards.size());
        // Verify all unique ZoneCards are present
        assertEquals(24, drawnCards.stream().distinct().count());
        assertTrue(drawnCards.containsAll(List.of(ZoneCard.values())));
    }

    @Test
    @DisplayName("reshuffleDiscardIntoDraw moves discard pile to draw and shuffles")
    void reshuffleDiscardIntoDrawMovesAndShuffles() {
        FloodDeck deck = new FloodDeck();
        // Draw some cards and discard them
        ZoneCard card1 = deck.draw();
        ZoneCard card2 = deck.draw();
        deck.discard(card1);
        deck.discard(card2);

        assertEquals(22, deck.getDrawSize());
        assertEquals(2, deck.getDiscardSize());

        deck.reshuffleDiscardIntoDraw();

        assertEquals(24, deck.getDrawSize()); // Discard moved back to draw
        assertEquals(0, deck.getDiscardSize()); // Discard is empty

        // Check if cards from discard are now in draw (probabilistic check via drawing)
        List<ZoneCard> allCardsAfter = Stream.generate(deck::draw)
                .limit(24)
                .collect(Collectors.toList());

        assertTrue(allCardsAfter.contains(card1));
        assertTrue(allCardsAfter.contains(card2));

        // Basic shuffle check (order changes)
        List<ZoneCard> beforeReshuffleDraw = List.of(ZoneCard.values()); // Initial ordered list (for comparison only, real draw pile is shuffled)
        deck.reshuffleDiscardIntoDraw();

    }

    @Test
    @DisplayName("reshuffleDiscardIntoDraw does nothing if discard is empty")
    void reshuffleDiscardIntoDrawDoesNothingIfDiscardEmpty() {
        FloodDeck deck = new FloodDeck();
        int initialDrawSize = deck.getDrawSize();

        deck.reshuffleDiscardIntoDraw(); // Discard is initially empty

        assertEquals(initialDrawSize, deck.getDrawSize());
        assertEquals(0, deck.getDiscardSize());
    }
}
