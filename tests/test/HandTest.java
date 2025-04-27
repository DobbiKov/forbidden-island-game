package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Model.*;

import java.util.List;

class HandTest {
    private Hand hand;
    private Card card1, card2, card3, card4, card5, card6;

    @BeforeEach
    void setUp() {
        hand = new Hand();
        card1 = new Card(CardType.EARTH_CARD);
        card2 = new Card(CardType.FIRE_CARD);
        card3 = new Card(CardType.WATER_CARD);
        card4 = new Card(CardType.AIR_CARD);
        card5 = new Card(CardType.HELICOPTER_LIFT);
        card6 = new Card(CardType.SANDBAGS);
    }

    @Test
    void testAddRemoveAndSize() {
        Hand h = new Hand();
        Card c1 = new Card(CardType.FIRE_CARD);
        Card c2 = new Card(CardType.WATER_CARD);
        h.add(c1);
        h.add(c2);
        assertEquals(2, h.getSize());
        h.remove(c1);
        assertEquals(1, h.getSize());
        assertTrue(h.getCards().contains(c2));
    }

    @Test
    void testOverflow() {
        Hand h = new Hand();
        for (int i = 0; i < 5; i++) {
            h.add(new Card(CardType.AIR_CARD));
        }
        assertFalse(h.isOverflow());
        h.add(new Card(CardType.AIR_CARD));
        assertTrue(h.isOverflow());
    }
    @Test
    @DisplayName("Initial hand size is 0")
    void initialSizeIsZero() {
        assertEquals(0, hand.getSize());
    }

    @Test
    @DisplayName("Add card increases size")
    void addCardIncreasesSize() {
        hand.add(card1);
        assertEquals(1, hand.getSize());
        hand.add(card2);
        assertEquals(2, hand.getSize());
    }

    @Test
    @DisplayName("Get cards returns unmodifiable list")
    void getCardsReturnsUnmodifiableList() {
        hand.add(card1);
        List<Card> cards = hand.getCards();
        assertEquals(1, cards.size());
        assertEquals(card1, cards.get(0));

        assertThrows(UnsupportedOperationException.class, () -> cards.add(card6));
        assertThrows(UnsupportedOperationException.class, () -> cards.remove(0));
    }

    @Test
    @DisplayName("Remove card decreases size")
    void removeCardDecreasesSize() {
        hand.add(card1);
        hand.add(card2);
        assertEquals(2, hand.getSize());

        hand.remove(card1);
        assertEquals(1, hand.getSize());
        assertFalse(hand.getCards().contains(card1));
        assertTrue(hand.getCards().contains(card2));
    }

    @Test
    @DisplayName("Remove card not in hand throws IllegalArgumentException")
    void removeCardNotInHandThrowsException() {
        hand.add(card1);
        assertThrows(IllegalArgumentException.class, () -> hand.remove(card2));
        assertEquals(1, hand.getSize()); // Size should not change
    }

    @Test
    @DisplayName("isOverflow returns false when hand size is <= 5")
    void isOverflowReturnsFalseWhenNotOverflowing() {
        hand.add(card1);
        hand.add(card2);
        hand.add(card3);
        hand.add(card4);
        hand.add(card5);
        assertEquals(5, hand.getSize());
        assertFalse(hand.isOverflow());
    }

    @Test
    @DisplayName("isOverflow returns true when hand size is > 5")
    void isOverflowReturnsTrueWhenOverflowing() {
        hand.add(card1);
        hand.add(card2);
        hand.add(card3);
        hand.add(card4);
        hand.add(card5);
        hand.add(card6); // 6th card
        assertEquals(6, hand.getSize());
        assertTrue(hand.isOverflow());
    }
}

