package test;

import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player p;

//    @BeforeEach
//    void setUp() {
//        Player.resetPlayerCount();
//        p = new Player("Yehor", PlayerRole.Pilot);
//    }
//
//    @Test
//    void testInitial() {
//        assertEquals(3, p.getActions_remaining());
//        assertEquals(PlayerRole.Pilot, p.getPlayer_role());
//        assertEquals("Yehor", p.getPlayer_name());
//    }
//
//    @Test
//    void testMoveConsumes() {
//        Zone z1 = new Zone(0, 0, true, ZoneCard.fodls_landing);
//        Zone z2 = new Zone(0, 1, true, ZoneCard.cliffs_of_abandon);
//        p.setPlayerToZone(z1);
//        p.move_Player(z2);
//        assertEquals(2, p.getActions_remaining());
//        assertEquals(z2, p.getPlayer_zone());
//    }
//
//    @Test
//    void testTakeAndDiscardCard() {
//        TreasureDeck deck = new TreasureDeck();
//        var c = deck.draw();
//        p.takeCard(c);
//        assertTrue(p.getHand().getCards().contains(c));
//        p.discardCard(c, deck);
//        assertFalse(p.getHand().getCards().contains(c));
//        assertEquals(1, deck.getDiscardSize());
//    }
}