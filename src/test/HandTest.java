package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Model.*;

class HandTest {
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
}

