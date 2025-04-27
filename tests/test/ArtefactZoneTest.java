package test;

import Model.Artefact;
import Model.ArtefactZone;
import Model.ZoneCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtefactZoneTest {

    @Test
    @DisplayName("ArtefactZone is created with correct artefact and type")
    void constructorSetsArtefactAndType() {
        Artefact artefact = Artefact.Earth;
        ZoneCard card = ZoneCard.temle_of_the_sun;
        ArtefactZone artefactZone = new ArtefactZone(1, 1, card, artefact);

        assertEquals(artefact, artefactZone.getArtefact());
        assertEquals(Model.ZoneType.ArtefactAssociated, artefactZone.getZone_type());
        assertEquals(card, artefactZone.getZoneCard());
        assertTrue(artefactZone.isAccessible()); // Should be Normal initially
        assertEquals(Model.ZoneState.Normal, artefactZone.getZone_state());
    }
}
