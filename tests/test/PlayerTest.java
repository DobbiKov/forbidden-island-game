package test;

import Model.*;
import Errors.InvalidStateOfTheGameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player p;
    private Zone startZone;

    @BeforeEach
    void setUp() {

        Player.resetPlayerCount();
        p = new Player("Yehor", PlayerRole.Pilot);


        startZone = new Zone(0, 0, true, ZoneCard.fodls_landing);
        p.setPlayerToZone(startZone);
    }

    @Test
    void testInitialState() {

        assertEquals("Yehor", p.getPlayer_name());
        assertEquals(PlayerRole.Pilot, p.getPlayer_role());
        assertEquals(3, p.getActions_remaining(), "Player should start with 3 actions");
        assertSame(startZone, p.getPlayer_zone(), "Player should be on the start zone");
    }

    @Test
    void testMoveConsumesAction() {
        Zone target = new Zone(0, 1, true, ZoneCard.cliffs_of_abandon);
        int before = p.getActions_remaining();
        p.move_Player(target);
        assertEquals(before - 1, p.getActions_remaining(), "move_Player should decrement actions by 1");
        assertSame(target, p.getPlayer_zone(), "Player should end up on the target zone");
    }

    @Test
    void testResetActions() {

        p.move_Player(new Zone(1, 0, true, ZoneCard.gold_gate));
        p.move_Player(new Zone(1, 1, true, ZoneCard.silver_gate));
        p.move_Player(new Zone(1, 2, true, ZoneCard.bronze_gate));
        assertEquals(0, p.getActions_remaining());

        p.reset_actions();
        assertEquals(3, p.getActions_remaining(), "reset_actions should restore to 3 actions");
    }

    @Test
    void testTakeCardsIntoHand() {
        TreasureDeck deck = new TreasureDeck();
        Card c1 = deck.draw();
        Card c2 = deck.draw();

        p.takeCard(c1);
        p.takeCard(c2);

        assertTrue(p.getHand().getCards().contains(c1), "Hand should contain first drawn card");
        assertTrue(p.getHand().getCards().contains(c2), "Hand should contain second drawn card");
        assertEquals(2, p.getHand().getSize(), "Hand size should be 2 after two takes");
    }

    @Test
    void testAddArtefact() {
        assertTrue(p.getArtefacts().isEmpty(), "Player should start with no artefacts");
        p.addArtefact(Artefact.Fire);
        assertTrue(p.getArtefacts().contains(Artefact.Fire), "Player should have the Fire artefact");
        assertEquals(1, p.getArtefacts().size());

    }
}
