package test;

import Model.Player;
import Model.PlayerColor;
import Model.PlayerStartZone;
import Model.ZoneCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStartZoneTest {

    @Test
    @DisplayName("PlayerStartZone constructor with ZoneCard sets type and color")
    void constructorWithCardSetsTypeAndColor() {
        ZoneCard card = ZoneCard.gold_gate;
        PlayerStartZone startZone = new PlayerStartZone(1, 1, card);

        assertEquals(Model.ZoneType.PlayerStart, startZone.getZone_type());
        assertEquals(card, startZone.getZoneCard());
        assertEquals(PlayerColor.Gold, startZone.getCard_player_color());
        assertFalse(startZone.isAssociatedToAPlayer()); // Initially not associated
        assertNull(startZone.getAssociatedPlayer()); // Initially null
    }

    @Test
    @DisplayName("PlayerStartZone constructor throws InvalidParameterException for invalid ZoneCard")
    void constructorThrowsForInvalidCard() {
        // Use a ZoneCard that isn't a PlayerStart or Helicopter zone card
        ZoneCard invalidCard = ZoneCard.coral_palace;
        assertThrows(InvalidParameterException.class, () -> new PlayerStartZone(1, 1, invalidCard));
    }

    @Test
    @DisplayName("associatePlayer associates a player with the zone")
    void associatePlayerAssociatesPlayer() {
        ZoneCard card = ZoneCard.silver_gate;
        PlayerStartZone startZone = new PlayerStartZone(1, 1, card);
        Player player = new Player("TestPlayer", Model.PlayerRole.Messenger);

        startZone.associatePlayer(player);

        assertTrue(startZone.isAssociatedToAPlayer());
        assertEquals(player, startZone.getAssociatedPlayer());
    }

    @Test
    @DisplayName("associatePlayer throws IllegalArgumentException if player is null")
    void associatePlayerThrowsIfPlayerNull() {
        ZoneCard card = ZoneCard.bronze_gate;
        PlayerStartZone startZone = new PlayerStartZone(1, 1, card);

        assertThrows(IllegalArgumentException.class, () -> startZone.associatePlayer(null));
        assertFalse(startZone.isAssociatedToAPlayer()); // Should not be associated
    }

    @Test
    @DisplayName("associatePlayer throws InvalidStateOfTheGameException if already associated")
    void associatePlayerThrowsIfAlreadyAssociated() {
        ZoneCard card = ZoneCard.copper_gate;
        PlayerStartZone startZone = new PlayerStartZone(1, 1, card);
        Player player1 = new Player("TestPlayer1", Model.PlayerRole.Explorer);
        Player player2 = new Player("TestPlayer2", Model.PlayerRole.Engineer);

        startZone.associatePlayer(player1); // Associate once

        assertThrows(Errors.InvalidStateOfTheGameException.class, () -> startZone.associatePlayer(player2)); // Associate again
        assertEquals(player1, startZone.getAssociatedPlayer()); // Should remain associated with the first player
    }
}
