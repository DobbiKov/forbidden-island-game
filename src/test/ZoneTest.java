package test;

import Errors.ZoneIsInaccessibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Model.*;

class ZoneTest {
    private Zone z;

    @BeforeEach
    void setUp() {
        z = new Zone(2, 3);
    }

    @Test
    void testInitialStateAndCoordinates() {
        assertEquals(2, z.getX());
        assertEquals(3, z.getY());
        assertEquals(ZoneState.Normal, z.getZone_state());
        assertTrue(z.isDry());
    }

    @Test
    void testFloodingTransitions() {
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
        z.floodZone();
        z.floodZone(); // now inaccessible
        assertThrows(ZoneIsInaccessibleException.class, () -> z.shoreUp());
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
        z.floodZone();
        z.floodZone(); // inaccessible
        Player p = new Player("Y", PlayerRole.Diver);
        assertThrows(ZoneIsInaccessibleException.class, () -> z.addPlayerToZone(p));
    }
}
