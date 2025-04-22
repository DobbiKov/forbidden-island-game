package test;

import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PlayerTest {
    private Player p;
    @BeforeEach
    void setUp() {
        p = new Player("Yehor", PlayerRole.Pilot);
    }

    @Test
    void testInitialActionsAndRole() {
        assertEquals(3, p.getActions_remaining());
        assertEquals(PlayerRole.Pilot, p.getPlayer_role());
        assertEquals("Yehor", p.getPlayer_name());
    }

    @Test
    void testMoveConsumesAction() {
        Zone z1 = new Zone(0,0, true), z2 = new Zone(0,1, true);
        p.setPlayerToZone(z1);
        p.move_Player(z2);
        assertEquals(2, p.getActions_remaining());
        assertEquals(z2, p.getPlayer_zone());
    }

    @Test
    void testTakeAndDiscardCard() {
        Deck deck = new Deck();
        Card c = deck.draw();
        p.takeCard(c);
        assertTrue(p.getHand().getCards().contains(c));
        p.discardCard(c, deck);
        assertFalse(p.getHand().getCards().contains(c));
        assertEquals(1, deck.getDiscardCardsSize());
    }
  
}