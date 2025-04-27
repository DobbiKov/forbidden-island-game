package test;

import Model.*;
import Errors.ZoneIsInaccessibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

class ZoneTest {
    private Zone z;
    private Zone zone;

    @BeforeEach
    void setUp() {

        z = new Zone(2, 3, true, ZoneCard.cliffs_of_abandon);
        zone = new Zone(0, 0, true, ZoneCard.fodls_landing);
    }

    @Test
    void testInitialCoordinatesAndState() {
        assertEquals(2, z.getX());
        assertEquals(3, z.getY());
        assertEquals(ZoneState.Normal, z.getZone_state());
        assertTrue(z.isDry());
    }

    @Test
    void testFlooding() {
        z.floodZone();
        assertEquals(ZoneState.Flooded, z.getZone_state());
        z.floodZone();
        assertEquals(ZoneState.Inaccessible, z.getZone_state());
    }

    @Test
    void testShoreUp() {
        z.floodZone();
        z.shoreUp();
        assertEquals(ZoneState.Normal, z.getZone_state());
    }

    @Test
    void testShoreUpInvalid() {
        z.floodZone(); z.floodZone();
        assertThrows(ZoneIsInaccessibleException.class, z::shoreUp);
    }

    @Test
    void testAddRemovePlayer() {
        Player p = new Player("X", PlayerRole.Explorer);
        z.addPlayerToZone(p);
        assertTrue(z.getPlayers_on_zone().contains(p));
        z.removePlayerFromZone(p);
        assertFalse(z.getPlayers_on_zone().contains(p));
    }

    @Test
    void testAddPlayerToInaccessible() {
        z.floodZone(); z.floodZone();
        Player p = new Player("Y", PlayerRole.Diver);
        assertThrows(ZoneIsInaccessibleException.class,
                () -> z.addPlayerToZone(p));
    }
    @Test
    @DisplayName("Initial state is Normal and accessible")
    void initialState() {
        assertEquals(ZoneState.Normal, zone.getZone_state());
        assertTrue(zone.isAccessible());
        assertTrue(zone.isDry());
        assertFalse(zone.isFlooded());
    }

    @Test
    @DisplayName("floodZone changes state from Normal to Flooded")
    void floodFromNormalToFlooded() {
        zone.floodZone();
        assertEquals(ZoneState.Flooded, zone.getZone_state());
        assertTrue(zone.isAccessible()); // Flooded is still accessible
        assertFalse(zone.isDry());
        assertTrue(zone.isFlooded());
    }

    @Test
    @DisplayName("floodZone changes state from Flooded to Inaccessible")
    void floodFromFloodedToInaccessible() {
        zone.floodZone(); // Normal -> Flooded
        zone.floodZone(); // Flooded -> Inaccessible
        assertEquals(ZoneState.Inaccessible, zone.getZone_state());
        assertFalse(zone.isAccessible());
        assertFalse(zone.isDry());
        assertFalse(zone.isFlooded()); // It's inaccessible, not just flooded
    }

    @Test
    @DisplayName("floodZone does nothing if already Inaccessible")
    void floodFromInaccessibleStaysInaccessible() {
        zone.makeInaccessible(); // Set state directly for test setup
        zone.floodZone();
        assertEquals(ZoneState.Inaccessible, zone.getZone_state());
        assertFalse(zone.isAccessible());
    }

    @Test
    @DisplayName("shoreUp changes state from Flooded to Normal")
    void shoreUpFromFloodedToNormal() {
        zone.floodZone(); // Normal -> Flooded
        assertEquals(ZoneState.Flooded, zone.getZone_state());

        zone.shoreUp();
        assertEquals(ZoneState.Normal, zone.getZone_state());
        assertTrue(zone.isAccessible());
        assertTrue(zone.isDry());
        assertFalse(zone.isFlooded());
    }

    @Test
    @DisplayName("shoreUp does nothing if state is Normal")
    void shoreUpFromNormalStaysNormal() {
        assertThrows(InvalidParameterException.class, () -> zone.shoreUp());
        assertEquals(ZoneState.Normal, zone.getZone_state());
    }

    @Test
    @DisplayName("shoreUp throws ZoneIsInaccessibleException if state is Inaccessible")
    void shoreUpFromInaccessibleThrowsException() {
        zone.makeInaccessible();
        assertEquals(ZoneState.Inaccessible, zone.getZone_state());

        assertThrows(ZoneIsInaccessibleException.class, () -> zone.shoreUp());
        assertEquals(ZoneState.Inaccessible, zone.getZone_state()); // State should not change
    }

    @Test
    @DisplayName("makeInaccessible sets state directly to Inaccessible")
    void makeInaccessibleSetsState() {
        zone.floodZone(); // Make it flooded first
        assertEquals(ZoneState.Flooded, zone.getZone_state());

        zone.makeInaccessible();
        assertEquals(ZoneState.Inaccessible, zone.getZone_state());
        assertFalse(zone.isAccessible());
    }

    @Test
    @DisplayName("addPlayerToZone adds player to zone's player set")
    void addPlayerToZoneAddsPlayer() {
        Player player = new Player("TestPlayer", Model.PlayerRole.Pilot);
        assertTrue(zone.getPlayers_on_zone().isEmpty());

        zone.addPlayerToZone(player);
        assertEquals(1, zone.getPlayers_on_zone().size());
        assertTrue(zone.getPlayers_on_zone().contains(player));
    }

    @Test
    @DisplayName("addPlayerToZone throws exception if player is already on zone")
    void addPlayerToZoneThrowsIfPlayerAlreadyThere() {
        Player player = new Player("TestPlayer", Model.PlayerRole.Pilot);
        zone.addPlayerToZone(player); // Add once

        assertThrows(RuntimeException.class, () -> zone.addPlayerToZone(player)); // Add again
        assertEquals(1, zone.getPlayers_on_zone().size()); // Still only one instance
    }

    @Test
    @DisplayName("addPlayerToZone throws ZoneIsInaccessibleException if zone is Inaccessible")
    void addPlayerToZoneThrowsIfInaccessible() {
        Player player = new Player("TestPlayer", Model.PlayerRole.Pilot);
        zone.makeInaccessible();

        assertThrows(ZoneIsInaccessibleException.class, () -> zone.addPlayerToZone(player));
        assertTrue(zone.getPlayers_on_zone().isEmpty()); // Player should not be added
    }

    @Test
    @DisplayName("removePlayerFromZone removes player from zone's player set")
    void removePlayerFromZoneRemovesPlayer() {
        Player player1 = new Player("TestPlayer1", Model.PlayerRole.Pilot);
        Player player2 = new Player("TestPlayer2", Model.PlayerRole.Diver);
        zone.addPlayerToZone(player1);
        zone.addPlayerToZone(player2);
        assertEquals(2, zone.getPlayers_on_zone().size());

        zone.removePlayerFromZone(player1);
        assertEquals(1, zone.getPlayers_on_zone().size());
        assertFalse(zone.getPlayers_on_zone().contains(player1));
        assertTrue(zone.getPlayers_on_zone().contains(player2));
    }

    @Test
    @DisplayName("removePlayerFromZone throws exception if player is not on zone")
    void removePlayerFromZoneThrowsIfPlayerNotThere() {
        Player player1 = new Player("TestPlayer1", Model.PlayerRole.Pilot);
        Player player2 = new Player("TestPlayer2", Model.PlayerRole.Diver);
        zone.addPlayerToZone(player1);
        assertEquals(1, zone.getPlayers_on_zone().size());

        assertThrows(RuntimeException.class, () -> zone.removePlayerFromZone(player2)); // Player 2 is not there
        assertEquals(1, zone.getPlayers_on_zone().size()); // Size should not change
    }

    @Test
    @DisplayName("isAdjecantTo returns true for orthogonal neighbors")
    void isAdjacentToOrthogonal() {
        Zone zone1 = new Zone(1, 1, true, ZoneCard.bronze_gate);
        Zone zoneRight = new Zone(1, 2, true, ZoneCard.copper_gate);
        Zone zoneLeft = new Zone(1, 0, true, ZoneCard.gold_gate);
        Zone zoneDown = new Zone(2, 1, true, ZoneCard.iron_gate);
        Zone zoneUp = new Zone(0, 1, true, ZoneCard.silver_gate);

        assertTrue(zone1.isAdjecantTo(zoneRight));
        assertTrue(zone1.isAdjecantTo(zoneLeft));
        assertTrue(zone1.isAdjecantTo(zoneDown));
        assertTrue(zone1.isAdjecantTo(zoneUp));
    }

    @Test
    @DisplayName("isAdjecantTo returns true for diagonal neighbors")
    void isAdjacentToDiagonal() {
        Zone zone1 = new Zone(1, 1, true, ZoneCard.bronze_gate);
        Zone zoneDiagDR = new Zone(2, 2, true, ZoneCard.copper_gate);
        Zone zoneDiagDL = new Zone(2, 0, true, ZoneCard.gold_gate);
        Zone zoneDiagUR = new Zone(0, 2, true, ZoneCard.iron_gate);
        Zone zoneDiagUL = new Zone(0, 0, true, ZoneCard.silver_gate);

        assertTrue(zone1.isAdjecantTo(zoneDiagDR));
        assertTrue(zone1.isAdjecantTo(zoneDiagDL));
        assertTrue(zone1.isAdjecantTo(zoneDiagUR));
        assertTrue(zone1.isAdjecantTo(zoneDiagUL));
    }

    @Test
    @DisplayName("isAdjecantTo returns false for non-adjacent zones")
    void isAdjacentToFalseForNonAdjacent() {
        Zone zone1 = new Zone(1, 1, true, ZoneCard.bronze_gate);
        Zone zoneFar = new Zone(3, 3, true, ZoneCard.copper_gate);
        Zone zoneTwoStepsRight = new Zone(1, 3, true, ZoneCard.gold_gate);

        assertFalse(zone1.isAdjecantTo(zoneFar));
        assertFalse(zone1.isAdjecantTo(zoneTwoStepsRight));
        assertFalse(zone1.isAdjecantTo(zone1)); // A zone is not adjacent to itself
    }

    @Test
    @DisplayName("isDiagonalTo returns true for diagonal neighbors")
    void isDiagonalToTrueForDiagonal() {
        Zone zone1 = new Zone(1, 1, true, ZoneCard.bronze_gate);
        Zone zoneDiagDR = new Zone(2, 2, true, ZoneCard.copper_gate);
        Zone zoneDiagDL = new Zone(2, 0, true, ZoneCard.gold_gate);
        Zone zoneDiagUR = new Zone(0, 2, true, ZoneCard.iron_gate);
        Zone zoneDiagUL = new Zone(0, 0, true, ZoneCard.silver_gate);

        assertTrue(zone1.isDiagonalTo(zoneDiagDR));
        assertTrue(zone1.isDiagonalTo(zoneDiagDL));
        assertTrue(zone1.isDiagonalTo(zoneDiagUR));
        assertTrue(zone1.isDiagonalTo(zoneDiagUL));
    }

    @Test
    @DisplayName("isDiagonalTo returns false for orthogonal neighbors")
    void isDiagonalToFalseForOrthogonal() {
        Zone zone1 = new Zone(1, 1, true, ZoneCard.bronze_gate);
        Zone zoneRight = new Zone(1, 2, true, ZoneCard.copper_gate);
        Zone zoneLeft = new Zone(1, 0, true, ZoneCard.gold_gate);
        Zone zoneDown = new Zone(2, 1, true, ZoneCard.iron_gate);
        Zone zoneUp = new Zone(0, 1, true, ZoneCard.silver_gate);

        assertFalse(zone1.isDiagonalTo(zoneRight));
        assertFalse(zone1.isDiagonalTo(zoneLeft));
        assertFalse(zone1.isDiagonalTo(zoneDown));
        assertFalse(zone1.isDiagonalTo(zoneUp));
    }

    @Test
    @DisplayName("isDiagonalTo returns false for non-adjacent zones")
    void isDiagonalToFalseForNonAdjacent() {
        Zone zone1 = new Zone(1, 1, true, ZoneCard.bronze_gate);
        Zone zoneFar = new Zone(3, 3, true, ZoneCard.copper_gate);
        Zone zoneTwoStepsRight = new Zone(1, 3, true, ZoneCard.gold_gate);

        assertFalse(zone1.isDiagonalTo(zoneFar));
        assertFalse(zone1.isDiagonalTo(zoneTwoStepsRight));
        assertFalse(zone1.isDiagonalTo(zone1)); // A zone is not diagonal to itself
    }
}