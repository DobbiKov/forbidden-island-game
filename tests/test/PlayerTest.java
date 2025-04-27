package test;

import Model.*;
import Errors.InvalidStateOfTheGameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player p;
    private Zone startZone;


    private Player player;
    private Zone zone;


    @BeforeEach
    void setUp() {

        Player.resetPlayerCount();
        p = new Player("Yehor", PlayerRole.Pilot);


        startZone = new Zone(0, 0, true, ZoneCard.fodls_landing);
        p.setPlayerToZone(startZone);

        // test 2
        Player.resetPlayerCount();
        player = new Player("TestPlayer", PlayerRole.Pilot);
        zone = new Zone(0, 0, true, Model.ZoneCard.fodls_landing); // Accessible zone
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

    @Test
    @DisplayName("Player constructor sets initial properties")
    void constructorSetsInitialProperties() {
        assertEquals(0, player.getPlayer_id()); // Assuming this is the first player created
        assertEquals("TestPlayer", player.getPlayer_name());
        assertEquals(PlayerRole.Pilot, player.getPlayer_role());
        assertEquals(PlayerRole.Pilot.getColor(), player.getPlayerColor());
        assertNull(player.getPlayer_zone());
        assertEquals(3, player.getActions_remaining());
        assertNotNull(player.getHand());
        assertTrue(player.getHand().getCards().isEmpty());
        assertTrue(player.getArtefacts().isEmpty());
    }

    @Test
    @DisplayName("setPlayerToZone moves player and updates zone")
    void setPlayerToZoneMovesPlayerAndUpdatesZone() {
        Zone initialZone = new Zone(1, 1, true, Model.ZoneCard.bronze_gate);
        player.setPlayerToZone(initialZone); // Set initial zone via the method

        assertEquals(initialZone, player.getPlayer_zone());
        assertTrue(initialZone.getPlayers_on_zone().contains(player));

        Zone newZone = new Zone(2, 2, true, Model.ZoneCard.copper_gate);
        player.setPlayerToZone(newZone);

        assertEquals(newZone, player.getPlayer_zone());
        assertTrue(newZone.getPlayers_on_zone().contains(player));
        assertFalse(initialZone.getPlayers_on_zone().contains(player)); // Should be removed from old zone
    }

    @Test
    @DisplayName("move_Player decreases actions remaining")
    void movePlayerDecreasesActions() {
        player.setPlayerToZone(zone); // Needs a zone to move *from* conceptually
        assertEquals(3, player.getActions_remaining());

        player.move_Player(new Zone(0, 1, true, Model.ZoneCard.cliffs_of_abandon));
        assertEquals(2, player.getActions_remaining());

        player.move_Player(new Zone(0, 2, true, Model.ZoneCard.coral_palace));
        assertEquals(1, player.getActions_remaining());
    }

    @Test
    @DisplayName("move_Player does not decrease actions below zero")
    void movePlayerDoesNotDecreaseBelowZero() {
        player.setPlayerToZone(zone);
        player.move_Player(new Zone(0, 1, true, Model.ZoneCard.cliffs_of_abandon)); // 2
        player.move_Player(new Zone(0, 2, true, Model.ZoneCard.coral_palace)); // 1
        player.move_Player(new Zone(0, 3, true, Model.ZoneCard.twilight_hollow)); // 0

        player.move_Player(new Zone(0, 4, true, Model.ZoneCard.phantom_rock)); // Attempt 5th move
        assertEquals(0, player.getActions_remaining()); // Should stay at 0
    }

    @Test
    @DisplayName("reset_actions sets actions remaining to 3")
    void resetActionsSetsToThree() {
        player.setPlayerToZone(zone);
        player.move_Player(new Zone(0, 1, true, Model.ZoneCard.cliffs_of_abandon));
        assertEquals(2, player.getActions_remaining());

        player.reset_actions();
        assertEquals(3, player.getActions_remaining());
    }

    @Test
    @DisplayName("takeCard adds card to hand")
    void takeCardAddsToHand() {
        Card card = new Card(CardType.EARTH_CARD);
        assertTrue(player.getHand().getCards().isEmpty());

        player.takeCard(card);
        assertEquals(1, player.getHand().getSize());
        assertTrue(player.getHand().getCards().contains(card));
    }

    @Test
    @DisplayName("addArtefact adds artefact to player's collection")
    void addArtefactAddsArtefact() {
        assertTrue(player.getArtefacts().isEmpty());
        player.addArtefact(Artefact.Earth);
        assertEquals(1, player.getArtefacts().size());
        assertTrue(player.getArtefacts().contains(Artefact.Earth));

        player.addArtefact(Artefact.Wind);
        assertEquals(2, player.getArtefacts().size());
        assertTrue(player.getArtefacts().contains(Artefact.Wind));
    }

    @Test
    @DisplayName("addArtefact throws exception if artefact already exists")
    void addArtefactThrowsIfAlreadyHave() {
        player.addArtefact(Artefact.Earth); // Add once

        assertThrows(IllegalStateException.class, () -> player.addArtefact(Artefact.Earth)); // Add again
        assertEquals(1, player.getArtefacts().size()); // Should still only have one
    }

    @Test
    @DisplayName("resetPlayerCount resets the static counter")
    void resetPlayerCountResetsCounter() {
        Player.resetPlayerCount();
        Player p1 = new Player("P1", PlayerRole.Diver);
        Player p2 = new Player("P2", PlayerRole.Engineer);
        assertEquals(0, p1.getPlayer_id());
        assertEquals(1, p2.getPlayer_id());


        Player.resetPlayerCount();

        Player p3 = new Player("P3", PlayerRole.Messenger);
        assertEquals(0, p3.getPlayer_id()); // Counter reset and started from 0 again
    }
}
