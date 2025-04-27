package test;

import Model.HelicopterZone;
import Model.PlayerColor;
import Model.ZoneCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelicopterZoneTest {

    @Test
    @DisplayName("HelicopterZone is created with correct card, type, and color")
    void constructorSetsCardTypeAndColor() {
        HelicopterZone heliZone = new HelicopterZone(2, 2);

        assertEquals(ZoneCard.fodls_landing, heliZone.getZoneCard());
        assertEquals(Model.ZoneType.Helicopter, heliZone.getZone_type());
        assertEquals(PlayerColor.Blue, heliZone.getCard_player_color()); // Helicopter zone is associated with blue player color
        assertTrue(heliZone.isAccessible()); // Should be Normal initially
        assertEquals(Model.ZoneState.Normal, heliZone.getZone_state());
    }
}
