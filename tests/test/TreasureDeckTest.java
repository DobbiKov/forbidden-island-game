package test;

import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Test
    @DisplayName("TreasureDeck initializes with correct card counts")
    void initializesWithCorrectCounts() {
        TreasureDeck deck = new TreasureDeck();
        List<Card> cards = deck.getDrawCards();

        Map<CardType, Long> counts = cards.stream()
                .collect(Collectors.groupingBy(Card::getType, Collectors.counting()));

        assertEquals(5, counts.get(CardType.EARTH_CARD));
        assertEquals(5, counts.get(CardType.AIR_CARD));
        assertEquals(5, counts.get(CardType.FIRE_CARD));
        assertEquals(5, counts.get(CardType.WATER_CARD));
        assertEquals(3, counts.get(CardType.HELICOPTER_LIFT));
        assertEquals(2, counts.get(CardType.SANDBAGS));
        assertFalse(counts.containsKey(CardType.WATER_RISE)); // Water Rise not in initial deck

        assertEquals(25, deck.getDrawSize()); // 5*4 + 3 + 2 = 25
        assertEquals(0, deck.getDiscardSize());
    }

    @Test
    @DisplayName("addWaterRiseCards adds 3 water rise cards and reshuffles")
    void addWaterRiseCardsAddsAndReshuffles() {
        TreasureDeck deck = new TreasureDeck();
        int initialSize = deck.getDrawSize();
        List<Card> cardsBefore = deck.getDrawCards(); // Get reference BEFORE shuffle

        deck.addWaterRiseCards();

        assertEquals(initialSize + 3, deck.getDrawSize());

        List<Card> cardsAfter = deck.getDrawCards();
        long waterRiseCount = cardsAfter.stream()
                .filter(Card::isWaterRise)
                .count();
        assertEquals(3, waterRiseCount);

        // Check if shuffle happened (probabilistic test)
        // Draw cards until you find a Water Rise or original card
        boolean foundWaterRise = false;
        boolean foundOriginal = false;
        for(int i=0; i < deck.getDrawSize(); i++){
            Card drawn = deck.getDrawCards().get(i);
            if(drawn.isWaterRise()) foundWaterRise = true;
            else foundOriginal = true;
            if(foundWaterRise && foundOriginal) break; // Found both types, implies mixing
        }
        assertTrue(foundWaterRise, "Should contain Water Rise cards after adding");
        assertTrue(foundOriginal, "Should contain original cards after adding (implies mixing/reshuffle)");
    }

}
