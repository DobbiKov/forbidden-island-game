package test;

import Errors.InvalidNumberOfPlayersException;
import Errors.MaximumNumberOfPlayersReachedException;
import Errors.NoPlayersException;
import Model.BoardGame;
import Model.GameState;
import Model.Player;
import Model.Zone;
import Model.ZoneCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// Assuming you have a mock GameView or test double
// import View.contract.GameView; // Not needed directly in BoardGame tests, but useful for controller tests

class BoardGameSetupTest {

    private BoardGame boardGame;
    // Need to reset Player.player_count for consistent Player IDs in tests
    // Add Player.resetPlayerCount() method to Model.Player

    @BeforeEach
    void setUp() {
        Player.resetPlayerCount(); // Reset for each test
        boardGame = new BoardGame();
        // Ensure ZoneFactory doesn't run out of cards prematurely if needed
        // For setup tests, the default factory should be fine.
    }

    @Test
    @DisplayName("BoardGame initializes with correct size and initial state")
    void initializesCorrectly() {
        assertNotNull(boardGame.getBoard());
        assertEquals(5, boardGame.getSize());
        assertEquals(5, boardGame.getBoard().length);
        assertEquals(5, boardGame.getBoard()[0].length);
        assertEquals(GameState.SettingUp, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(0, BoardGameTestHelper.getPlayerCount(boardGame));
        assertEquals(-1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame)); // Initial actions should be 3 maybe? Or only set on turn start? Code sets it on turn start. Let's check the code. Yes, starts at 3.
        assertEquals(0, BoardGameTestHelper.getWaterMeter(boardGame).getLevel());

        // Check center zone is inaccessible
        assertFalse(boardGame.getZone(2, 2).isAccessible());

        // Check other zones are initially accessible (mostly, assuming random factory works)
        int inaccessibleCount = 0;
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(!boardGame.getZone(i,j).isAccessible()){
                    inaccessibleCount++;
                }
            }
        }
        assertEquals(1, inaccessibleCount, "Only the center zone should be initially inaccessible"); // Assuming default factory doesn't create more
    }

    @Test
    @DisplayName("addPlayer adds a player and assigns role and start zone")
    void addPlayerAddsPlayerAndAssignsRoleAndZone() {
        String playerName = "TestPlayer";
        Player player = boardGame.addPlayer(playerName);

        assertNotNull(player);
        assertEquals(playerName, player.getPlayer_name());
        assertEquals(1, BoardGameTestHelper.getPlayerCount(boardGame)); // playerCount is incremented after association
        // Player count is incremented *after* adding the player panel in the controller's wrapper handler
        // Let's test the model method directly then
        BoardGame bgDirect = new BoardGame(); // Use a new instance to test model directy
        Player.resetPlayerCount();

        Player p1 = bgDirect.addPlayer("P1");
        assertNotNull(p1.getPlayer_role());
        assertNotNull(p1.getPlayer_zone());
        assertTrue(p1.getPlayer_zone() instanceof Model.PlayerStartZone || p1.getPlayer_zone() instanceof Model.HelicopterZone);
        assertTrue(((Model.PlayerStartZone)p1.getPlayer_zone()).isAssociatedToAPlayer());
        assertEquals(p1, ((Model.PlayerStartZone)p1.getPlayer_zone()).getAssociatedPlayer());

        Player p2 = bgDirect.addPlayer("P2");
        assertNotNull(p2.getPlayer_role());
        assertNotNull(p2.getPlayer_zone());
        assertNotEquals(p1.getPlayer_role(), p2.getPlayer_role());
        assertNotEquals(p1.getPlayer_zone(), p2.getPlayer_zone());

        // Check players array size isn't fully used, but slot is filled
        Player[] playersArray = BoardGameTestHelper.getPlayers(bgDirect);
        assertEquals(4, playersArray.length);
        assertEquals(p1, playersArray[0]);
        assertEquals(p2, playersArray[1]);
        assertNull(playersArray[2]);
        assertNull(playersArray[3]);

        assertEquals(2, BoardGameTestHelper.getPlayerCount(bgDirect)); // Correct player count in model
    }


    @Test
    @DisplayName("addPlayer throws MaximumNumberOfPlayersReachedException for more than 4 players")
    void addPlayerThrowsForMoreThanFour() {
        boardGame.addPlayer("P1");
        boardGame.addPlayer("P2");
        boardGame.addPlayer("P3");
        boardGame.addPlayer("P4");

        assertThrows(MaximumNumberOfPlayersReachedException.class, () -> boardGame.addPlayer("P5"));
        assertEquals(4, BoardGameTestHelper.getPlayerCount(boardGame));
    }

    @Test
    @DisplayName("addPlayer throws InvalidParameterException for name > 12 chars")
    void addPlayerThrowsForLongName() {
        String longName = "ThisNameIsTooLongToFit"; // 22 chars
        assertThrows(InvalidParameterException.class, () -> boardGame.addPlayer(longName));
        assertEquals(0, BoardGameTestHelper.getPlayerCount(boardGame)); // No player added
    }

    @Test
    @DisplayName("startGame throws NoPlayersException if no players added")
    void startGameThrowsIfNoPlayers() {
        // Player count is 0 initially
        assertThrows(NoPlayersException.class, () -> boardGame.startGame());
        assertEquals(GameState.SettingUp, BoardGameTestHelper.getGameState(boardGame)); // State should not change
    }

    @Test
    @DisplayName("startGame throws InvalidNumberOfPlayersException if 1 player added")
    void startGameThrowsIfOnePlayer() {
        boardGame.addPlayer("P1");
        // Player count is 1
        assertThrows(InvalidNumberOfPlayersException.class, () -> boardGame.startGame());
        assertEquals(GameState.SettingUp, BoardGameTestHelper.getGameState(boardGame)); // State should not change
    }

    @Test
    @DisplayName("startGame succeeds with 2 players")
    void startGameSucceedsWithTwoPlayers() {
        boardGame.addPlayer("P1");
        boardGame.addPlayer("P2");

        boardGame.startGame();

        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(2, BoardGameTestHelper.getPlayerCount(boardGame));
        assertEquals(0, BoardGameTestHelper.getPlayerTurnId(boardGame)); // First player's turn (index 0)
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame)); // Actions reset for the turn
        assertFalse(BoardGameTestHelper.getTreasureDrawnThisTurn(boardGame)); // Flag reset

        // Check initial card draw
        Player[] players = BoardGameTestHelper.getPlayers(boardGame);
        assertEquals(2, players[0].getHand().getSize());
        assertEquals(2, players[1].getHand().getSize());
        assertEquals(28 - 4, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize()); // 25 initial - 4 drawn
        assertEquals(0, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize());

        // Check Water Rise cards added to Treasure Deck
        assertTrue(BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().stream().anyMatch(c -> c.getType() == Model.CardType.WATER_RISE));
        assertEquals(3, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().stream().filter(c -> c.getType() == Model.CardType.WATER_RISE).count());
    }

    @Test
    @DisplayName("startGame succeeds with 3 players")
    void startGameSucceedsWithThreePlayers() {
        boardGame.addPlayer("P1");
        boardGame.addPlayer("P2");
        boardGame.addPlayer("P3");

        boardGame.startGame();

        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(3, BoardGameTestHelper.getPlayerCount(boardGame));
        assertEquals(0, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));
        assertFalse(BoardGameTestHelper.getTreasureDrawnThisTurn(boardGame));

        // Check initial card draw
        Player[] players = BoardGameTestHelper.getPlayers(boardGame);
        assertEquals(2, players[0].getHand().getSize());
        assertEquals(2, players[1].getHand().getSize());
        assertEquals(2, players[2].getHand().getSize());
        assertEquals(28 - 6, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize()); // 25 initial - 6 drawn
        assertEquals(0, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize());
        assertEquals(3, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().stream().filter(c -> c.getType() == Model.CardType.WATER_RISE).count());
    }

    @Test
    @DisplayName("startGame succeeds with 4 players")
    void startGameSucceedsWithFourPlayers() {
        boardGame.addPlayer("P1");
        boardGame.addPlayer("P2");
        boardGame.addPlayer("P3");
        boardGame.addPlayer("P4");

        boardGame.startGame();

        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(4, BoardGameTestHelper.getPlayerCount(boardGame));
        assertEquals(0, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));
        assertFalse(BoardGameTestHelper.getTreasureDrawnThisTurn(boardGame));


        // Check initial card draw
        Player[] players = BoardGameTestHelper.getPlayers(boardGame);
        assertEquals(2, players[0].getHand().getSize());
        assertEquals(2, players[1].getHand().getSize());
        assertEquals(2, players[2].getHand().getSize());
        assertEquals(2, players[3].getHand().getSize());
        assertEquals(28 - 8, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize()); // 25 initial - 8 drawn
        assertEquals(0, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize());
        assertEquals(3, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().stream().filter(c -> c.getType() == Model.CardType.WATER_RISE).count());
    }

    @Test
    @DisplayName("startGame throws RuntimeException if not in SettingUp state")
    void startGameThrowsIfNotSettingUp() {
        boardGame.addPlayer("P1");
        boardGame.addPlayer("P2");
        boardGame.startGame(); // Valid start

        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame));

        // Attempt to start again
        assertThrows(RuntimeException.class, () -> boardGame.startGame());
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State should not change
    }
}