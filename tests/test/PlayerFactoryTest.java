package test;

import Model.PlayerFactory;
import Model.Player;
import Model.PlayerRole;
import Errors.NoRoleToAssignError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlayerFactoryTest {

    private PlayerFactory playerFactory;

    @BeforeEach
    void setUp() {
        // Reset static player count for consistent test IDs
        Player.resetPlayerCount();
        playerFactory = new PlayerFactory();
    }

    @Test
    @DisplayName("createPlayer creates a player with a unique role")
    void createPlayerCreatesUniqueRole() {
        Set<PlayerRole> assignedRoles = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            Player player = playerFactory.createPlayer("Player" + i);
            assertNotNull(player);
            assertNotNull(player.getPlayer_role());
            assertTrue(assignedRoles.add(player.getPlayer_role()), "Role " + player.getPlayer_role() + " was assigned twice");
        }
        assertEquals(6, assignedRoles.size()); // All roles assigned
    }

    @Test
    @DisplayName("createPlayer throws NoRoleToAssignError after all 6 roles are used")
    void createPlayerThrowsWhenNoRolesLeft() {
        // Create 6 players to use all roles
        for (int i = 0; i < 6; i++) {
            playerFactory.createPlayer("Player" + i);
        }

        // Attempt to create a 7th player
        assertThrows(NoRoleToAssignError.class, () -> playerFactory.createPlayer("Player7"));
    }

    @Test
    @DisplayName("Created player has correct name and initial state")
    void createdPlayerHasCorrectInitialState() {
        String playerName = "Alice";
        Player player = playerFactory.createPlayer(playerName);

        assertNotNull(player);
        assertEquals(playerName, player.getPlayer_name());
        // Role is random, so we can't check a specific role, but it should be one of the enums
        assertTrue(Set.of(PlayerRole.values()).contains(player.getPlayer_role()));
        assertEquals(3, player.getActions_remaining());
        assertTrue(player.getHand().getCards().isEmpty());
        assertTrue(player.getArtefacts().isEmpty());
        assertNull(player.getPlayer_zone());
        assertEquals(0, player.getPlayer_id()); // Assuming it's the first player created globally or after reset
    }
}
