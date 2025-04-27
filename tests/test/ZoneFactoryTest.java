package test;

import Errors.AllTheCardsAreUsedException;
import Model.Artefact;
import Model.ArtefactZone;
import Model.HelicopterZone;
import Model.PlayerStartZone;
import Model.Zone;
import Model.ZoneCard;
import Model.ZoneFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ZoneFactoryTest {

    private ZoneFactory zoneFactory;
    private Set<ZoneCard> initialUsedCards;

    @BeforeEach
    void setUp() {
        zoneFactory = new ZoneFactory();
        // Access private field for testing purposes (usually done via reflection or making it protected/package-private)
        // Let's assume 'used_cards' is accessible for testing within the package or via a helper method
        initialUsedCards = (Set<ZoneCard>) getPrivateFieldValue(zoneFactory, "used_cards");
    }

    // Helper method to access private field (requires reflection)
    private Object getPrivateFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Could not access private field for testing: " + fieldName, e);
        }
    }
    private void setPrivateFieldValue(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set private field for testing: " + fieldName, e);
        }
    }


    @Test
    @DisplayName("createRandomZone creates 24 unique zones with unique ZoneCards")
    void createRandomZoneCreatesUniqueZones() {
        Set<ZoneCard> createdCards = new HashSet<>();
        for (int i = 0; i < 24; i++) {
            Zone zone = zoneFactory.createRandomZone(0, i / 5);
            assertNotNull(zone);
            assertNotNull(zone.getZoneCard());
            assertTrue(createdCards.add(zone.getZoneCard()), "ZoneCard " + zone.getZoneCard() + " was used twice");
        }
        assertEquals(24, createdCards.size()); // All 24 unique cards used
        assertEquals(24, initialUsedCards.size()); // The internal set should also have 24
    }

    @Test
    @DisplayName("createRandomZone throws AllTheCardsAreUsedException after all 24 cards are used")
    void createRandomZoneThrowsWhenAllCardsUsed() {
        // Use all 24 cards
        for (int i = 0; i < 24; i++) {
            zoneFactory.createRandomZone(0, i / 5);
        }

        // Attempt to create one more
        assertThrows(AllTheCardsAreUsedException.class, () -> zoneFactory.createRandomZone(0, 4));
    }

    @Test
    @DisplayName("createInaccessibleZone creates a zone with Inaccessible state")
    void createInaccessibleZone() {
        Zone zone = zoneFactory.createInaccessibleZone(1, 1);
        assertNotNull(zone);
        assertEquals(Model.ZoneState.Inaccessible, zone.getZone_state());
        assertFalse(zone.isAccessible());
        assertNull(zone.getZoneCard()); // Inaccessible zones don't have a card
    }

    @Test
    @DisplayName("createPlayerZone creates a PlayerStartZone")
    void createPlayerZone() {
        ZoneCard card = ZoneCard.bronze_gate;
        Zone zone = zoneFactory.createPlayerZone(1, 1, card);
        assertNotNull(zone);
        assertTrue(zone instanceof PlayerStartZone);
        assertEquals(Model.ZoneType.PlayerStart, zone.getZone_type());
        assertEquals(card, zone.getZoneCard());
    }

    @Test
    @DisplayName("createHelicopterZone creates a HelicopterZone")
    void createHelicopterZone() {
        Zone zone = zoneFactory.createHelicopterZone(2, 2);
        assertNotNull(zone);
        assertTrue(zone instanceof HelicopterZone);
        assertEquals(Model.ZoneType.Helicopter, zone.getZone_type());
        assertEquals(ZoneCard.fodls_landing, zone.getZoneCard());
    }

    @Test
    @DisplayName("createArtefactZone creates an ArtefactZone")
    void createArtefactZone() {
        ZoneCard card = ZoneCard.temple_of_the_moon;
        Artefact artefact = Artefact.Earth;
        Zone zone = zoneFactory.createArtefactZone(1, 1, card, artefact);
        assertNotNull(zone);
        assertTrue(zone instanceof ArtefactZone);
        assertEquals(Model.ZoneType.ArtefactAssociated, zone.getZone_type());
        assertEquals(card, zone.getZoneCard());
        assertEquals(artefact, ((ArtefactZone)zone).getArtefact());
    }

    @Test
    @DisplayName("createSimpleZone creates a basic Zone")
    void createSimpleZone() {
        ZoneCard card = ZoneCard.dunes_of_deception;
        Zone zone = zoneFactory.createSimpleZone(1, 1, card);
        assertNotNull(zone);
        assertFalse(zone instanceof ArtefactZone);
        assertFalse(zone instanceof PlayerStartZone);
        assertFalse(zone instanceof HelicopterZone);
        assertEquals(Model.ZoneType.Casual, zone.getZone_type());
        assertEquals(card, zone.getZoneCard());
    }
}
