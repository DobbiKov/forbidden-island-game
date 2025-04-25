package test;

import Model.*;
import Errors.ZoneIsInaccessibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZoneTest {
    private Zone z;

    @BeforeEach
    void setUp() {
        z = new Zone(2, 3, true, ZoneCard.cliffs_of_abandon);
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
}