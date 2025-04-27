package test;

import Model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private List<String> initialCards;
    private Deck<String> deck;

    @BeforeEach
    void setUp() {
        initialCards = new ArrayList<>(Arrays.asList("CardA", "CardB", "CardC", "CardD", "CardE"));
        deck = new Deck<>(new ArrayList<>(initialCards)); // Use copy to not modify original list
    }

    @Test
    @DisplayName("Initial draw pile size is correct")
    void initialDrawPileSizeIsCorrect() {
        assertEquals(initialCards.size(), deck.getDrawSize());
        assertEquals(0, deck.getDiscardSize());
    }

    @Test
    @DisplayName("Draw reduces draw pile size")
    void drawReducesDrawPileSize() {
        deck.draw();
        assertEquals(initialCards.size() - 1, deck.getDrawSize());
    }

    @Test
    @DisplayName("Discard adds to discard pile")
    void discardAddsToDiscardPile() {
        String card = deck.draw();
        deck.discard(card);
        assertEquals(initialCards.size() - 1, deck.getDrawSize());
        assertEquals(1, deck.getDiscardSize());
    }

    @Test
    @DisplayName("Draw from empty draw pile reshuffles discard")
    void drawFromEmptyDrawPileReshufflesDiscard() {
        // Draw all initial cards
        for (int i = 0; i < initialCards.size(); i++) {
            String drawnCard = deck.draw();
            deck.discard(drawnCard);
        }
        assertEquals(0, deck.getDrawSize());
        assertEquals(initialCards.size(), deck.getDiscardSize());

        // Draw one more card, should trigger reshuffle
        String drawnAfterReshuffle = deck.draw();
        assertEquals(initialCards.size() - 1, deck.getDrawSize());
        assertEquals(0, deck.getDiscardSize());
        assertTrue(initialCards.contains(drawnAfterReshuffle)); // Card should be one of the originals
    }

    @Test
    @DisplayName("Drawing from an empty deck (draw and discard empty) throws exception")
    void drawingFromEmptyDeckThrowsException() {
        // Empty draw pile
        for (int i = 0; i < initialCards.size(); i++) {
            deck.draw();
        }
        assertEquals(0, deck.getDrawSize());

        // Discard all cards
        for(String c : deck.getDiscardCards()) deck.discard(c); // Simulate discarding all cards
        assertEquals(0, deck.getDiscardSize());


        // Attempt to draw
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> deck.draw());
        assertEquals("No cards left to draw or reshuffle.", exception.getMessage());
    }

    @Test
    @DisplayName("Shuffle changes card order")
    void shuffleChangesCardOrder() {
        List<String> beforeShuffle = new ArrayList<>(deck.getDrawCards());
        deck.shuffle();
        List<String> afterShuffle = new ArrayList<>(deck.getDrawCards());

        // This test has a small chance of failing if shuffle results in the same order,
        // but it's highly unlikely with a reasonable number of cards.
        // A more robust test might check if all initial cards are still present.
        assertNotEquals(beforeShuffle, afterShuffle, "Shuffle did not change card order (unlikely but possible)");
        assertTrue(afterShuffle.containsAll(initialCards) && initialCards.containsAll(afterShuffle), "Shuffled deck does not contain the same cards");
    }
}
