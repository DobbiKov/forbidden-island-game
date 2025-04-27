package test;

import Errors.InvalidActionForTheCurrentState;
import Errors.InvalidStateOfTheGameException;
import Errors.InvalidZoneToMove;
import Errors.ZoneIsInaccessibleException;
import Model.BoardGame;
import Model.Card;
import Model.CardType;
import Model.GameState;
import Model.Player;
import Model.PlayerRole;
import Model.Zone;
import Model.ZoneCard;
import Model.ZoneState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BoardGameCardUseTest {

    private BoardGame boardGame;
    private Player player1, player2, player3, player4;
    private Zone startZone1, startZone2, startZone3, startZone4;
    private Zone adjacentDry, adjacentFlooded;
    private Zone farawayDry;
    private Zone helicopterZone;


    @BeforeEach
    void setUp() {
        Player.resetPlayerCount(); // Reset for each test
        boardGame = new BoardGame();

        // Manually set up players and zones for predictable testing
        Zone[][] board = new Zone[5][5];
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "board", board);
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "size", 5);

        startZone1 = new Model.PlayerStartZone(0, 0, ZoneCard.bronze_gate);
        startZone2 = new Model.PlayerStartZone(0, 4, ZoneCard.silver_gate);
        startZone3 = new Model.PlayerStartZone(4, 0, ZoneCard.gold_gate);
        startZone4 = new Model.PlayerStartZone(4, 4, ZoneCard.copper_gate);
        helicopterZone = new Model.HelicopterZone(0, 2); // Heli zone at 0,2

        adjacentDry = new Zone(0, 1, true, ZoneCard.cliffs_of_abandon);
        adjacentFlooded = new Zone(1, 0, true, ZoneCard.twilight_hollow);
        adjacentFlooded.floodZone(); // Make it flooded initially

        farawayDry = new Zone(3, 3, true, ZoneCard.observatory);

        // Place zones on the board
        board[0][0] = startZone1; board[0][4] = startZone2; board[4][0] = startZone3; board[4][4] = startZone4;
        board[0][1] = adjacentDry; board[1][0] = adjacentFlooded;
        board[3][3] = farawayDry; board[0][2] = helicopterZone;

        // Fill in remaining zones with simple dry zones (ensure accessibility)
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(board[i][j] == null) {
                    board[i][j] = new Zone(i,j,true, ZoneCard.dunes_of_deception);
                    try {
                        board[i][j].shoreUp(); // Ensure they are dry
                    }catch (Exception e){}
                }
            }
        }
        // Ensure 2,2 is accessible and dry as it's inaccessible by default in constructor
        board[2][2] = new Zone(2,2,true, ZoneCard.watchtower);
        try {
            board[2][2].shoreUp();
        }catch (Exception e){}


        // Add players (roles don't matter as much for card use, but good to assign)
        player1 = new Player("P1", PlayerRole.Pilot);
        player2 = new Player("P2", PlayerRole.Engineer);
        player3 = new Player("P3", PlayerRole.Diver);
        player4 = new Player("P4", PlayerRole.Explorer);

        BoardGameTestHelper.setPlayers(boardGame, player1, player2, player3, player4);
        BoardGameTestHelper.setPlayerCount(boardGame, 4);

        // Assign players to zones
        putPlayersOnZone(startZone1, player1); // P1 at 0,0
        putPlayersOnZone(startZone2, player2); // P2 at 0,4
        putPlayersOnZone(startZone3, player3); // P3 at 4,0
        putPlayersOnZone(startZone4, player4); // P4 at 4,4

        // Start the game state manually for action tests
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing);
        BoardGameTestHelper.setPlayerTurnId(boardGame, 0); // P1's turn
        BoardGameTestHelper.setCurrentPlayerActionsNum(boardGame, 3); // Actions remaining are not consumed by card use

        // Ensure decks are not empty
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().addAll(Model.TreasureDeck.initTreasureCards());
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().addAll(Model.FloodDeck.collectAllZoneCards());
        BoardGameTestHelper.getTreasureDeck(boardGame).shuffle();
        BoardGameTestHelper.getFloodDeck(boardGame).shuffle();
        BoardGameTestHelper.getTreasureDeck(boardGame).addWaterRiseCards();

        // Clear any stranded players from previous tests
        BoardGameTestHelper.setPlayersOnInaccessibleZones(boardGame, new ArrayList<>());
        BoardGameTestHelper.setCurrentPlayerRunningFromInaccessibleZone(boardGame, null);
    }

    // --- Helper to set current player ---
    private void setCurrentPlayer(Player player, int actions) {
        for (int i = 0; i < BoardGameTestHelper.getPlayers(boardGame).length; i++) {
            if (BoardGameTestHelper.getPlayers(boardGame)[i] == player) {
                BoardGameTestHelper.setPlayerTurnId(boardGame, i);
                BoardGameTestHelper.setCurrentPlayerActionsNum(boardGame, actions);
                return;
            }
        }
        fail("Player not found in the game");
    }

    // --- Helper to put players on a zone ---
    private void putPlayersOnZone(Zone zone, Player... players) {
        zone.getPlayers_on_zone().clear(); // Clear existing players
        for(Player p : players) {
            if (p.getPlayer_zone() != null) {
                if(p.getPlayer_zone().getPlayers_on_zone().contains(p)) { p.getPlayer_zone().removePlayerFromZone(p); }
            }
            p.move_Player(zone); // Use player method which updates both sides
            zone.addPlayerToZone(p);
        }
    }


    // --- Use Action Card: Sandbags ---

    @Test
    @DisplayName("Player can use Sandbags card on a flooded zone")
    void canUseSandbagsOnFloodedZone() {
        setCurrentPlayer(player1, 3); // P1's turn
        Card sandbagsCard = new Card(CardType.SANDBAGS);
        player1.takeCard(sandbagsCard); // Give P1 a Sandbags card
        assertEquals(1, player1.getHand().getSize());
        assertEquals(ZoneState.Flooded, adjacentFlooded.getZone_state());


        boardGame.playerUseActionCard(player1, sandbagsCard);
        assertEquals(GameState.PlayerChooseAZoneToShoreUpWithCard, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(player1, BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse"));

        List<Zone> possibleZones = boardGame.getZonesPossibleForChoosing();
        assertTrue(possibleZones.contains(adjacentFlooded)); // Flooded zone should be choosable
        assertFalse(possibleZones.contains(adjacentDry)); // Dry zone should not be choosable
        assertFalse(possibleZones.contains(boardGame.getZone(2,2))); // Accessible but dry center

        assertEquals(0, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize());

        boardGame.shoreUpZoneWithCard(adjacentFlooded); // Use card on the zone

        assertEquals(ZoneState.Normal, adjacentFlooded.getZone_state()); // Zone shored up
        assertFalse(player1.getHand().getCards().contains(sandbagsCard)); // Card removed from hand
        assertEquals(0, player1.getHand().getSize()); // Hand size decreased
        assertEquals(1, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize()); // Card discarded
        assertEquals(sandbagsCard, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().get(0));
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State back to Playing
        assertNull(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse")); // choosing player nulled
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame)); // Action points not used
    }

    @Test
    @DisplayName("Use Sandbags throws if player doesn't have the card")
    void useSandbagsThrowsIfCardNotInHand() {
        setCurrentPlayer(player1, 3);
        Card sandbagsCard = new Card(CardType.SANDBAGS); // P1 doesn't have this card

        assertThrows(InvalidParameterException.class, () -> boardGame.playerUseActionCard(player1, sandbagsCard));
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
    }

    @Test
    @DisplayName("Shore Up with Card throws if not in PlayerChooseAZoneToShoreUpWithCard state")
    void shoreUpWithCardThrowsIfNotChoosingZone() {
        setCurrentPlayer(player1, 3);
        Zone floodedZone = adjacentFlooded; floodedZone.floodZone();
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing); // Ensure wrong state
        // Simulate playerChoosingCardToUse being set, even though state is wrong
        Card sandbagsCard = new Card(CardType.SANDBAGS);
        player1.takeCard(sandbagsCard);
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "playerChoosingCardToUse", player1);


        assertThrows(InvalidStateOfTheGameException.class, () -> boardGame.shoreUpZoneWithCard(floodedZone));
        assertEquals(ZoneState.Inaccessible, floodedZone.getZone_state()); // Zone state unchanged
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertNotNull(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse")); // Still set
    }

    @Test
    @DisplayName("Shore Up with Card throws if choosing non-flooded zone")
    void shoreUpWithCardThrowsIfInvalidZone() {
        setCurrentPlayer(player1, 3); // P1's turn
        Card sandbagsCard = new Card(CardType.SANDBAGS);
        player1.takeCard(sandbagsCard);
        try {
            adjacentDry.shoreUp(); // Ensure zone is dry
        }catch(Exception e) {}

        boardGame.playerUseActionCard(player1, sandbagsCard); // State becomes choosing
        assertEquals(GameState.PlayerChooseAZoneToShoreUpWithCard, BoardGameTestHelper.getGameState(boardGame));

        // Attempt to use on a dry zone
        assertThrows(InvalidParameterException.class, () -> boardGame.shoreUpZoneWithCard(adjacentDry) ); // getZonesToShoreUpWithCard prevents this
        assertEquals(ZoneState.Normal, adjacentDry.getZone_state()); // Zone state unchanged
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertNotNull(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse")); // Still set
    }


    // --- Use Action Card: Helicopter Lift ---

    @Test
    @DisplayName("Player can use Helicopter Lift card on any accessible zone for self and players on the same tile")
    void canUseHelicopterLift() {
        setCurrentPlayer(player1, 3); // P1's turn
        Card heliCard = new Card(CardType.HELICOPTER_LIFT);
        player1.takeCard(heliCard); // Give P1 the card
        assertEquals(1, player1.getHand().getSize());

        // Put multiple players on P1's zone
        putPlayersOnZone(startZone1, player1, player2, player3); // P1, P2, P3 at 0,0

        Zone targetZone = farawayDry; // Any accessible zone (3,3)

        boardGame.playerUseActionCard(player1, heliCard); // Use the card
        assertEquals(GameState.PlayerChooseAZoneToFlyWithCard, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(player1, BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse"));
        assertTrue(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith") instanceof ArrayList);
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).isEmpty()); // List starts empty

        // Test choosing players on the same tile
        Set<Player> choosablePlayers = boardGame.getPlayersToChoose();
        assertEquals(2, choosablePlayers.size()); // P2 and P3 should be choosable
        assertTrue(choosablePlayers.contains(player2));
        assertTrue(choosablePlayers.contains(player3));
        assertFalse(choosablePlayers.contains(player1)); // Current player is not chosen via this mechanism

        boardGame.choosePlayerToFlyWithCard(player2); // Choose P2
        assertEquals(1, ((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).size());
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).contains(player2));
        // State remains PlayerChooseAZoneToFlyWithCard? Code seems to not change state here.
        // This requires the controller/UI to handle the selection logic. The model adds to a list.

        boardGame.choosePlayerToFlyWithCard(player3); // Choose P3
        assertEquals(2, ((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).size());
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).contains(player3));

        // Now choose the zone
        boardGame.flyPlayerToZoneWithCard(targetZone);

        // Verify players moved
        assertEquals(targetZone, player1.getPlayer_zone());
        assertEquals(targetZone, player2.getPlayer_zone());
        assertEquals(targetZone, player3.getPlayer_zone());

        // Verify they are on the new zone and not the old one
        assertTrue(targetZone.getPlayers_on_zone().contains(player1));
        assertTrue(targetZone.getPlayers_on_zone().contains(player2));
        assertTrue(targetZone.getPlayers_on_zone().contains(player3));
        assertFalse(startZone1.getPlayers_on_zone().contains(player1));
        assertFalse(startZone1.getPlayers_on_zone().contains(player2));
        assertFalse(startZone1.getPlayers_on_zone().contains(player3));

        // Verify card usage
        assertFalse(player1.getHand().getCards().contains(heliCard)); // Card removed
        assertEquals(0, player1.getHand().getSize());
        assertEquals(1, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize()); // Card discarded

        // Verify state reset
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State back to Playing
        assertNull(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse")); // choosing player nulled
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).isEmpty()); // List cleared
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame)); // Action points not used
    }

    @Test
    @DisplayName("Use Helicopter Lift throws if player doesn't have the card")
    void useHelicopterLiftThrowsIfCardNotInHand() {
        setCurrentPlayer(player1, 3); // P1's turn
        Card heliCard = new Card(CardType.HELICOPTER_LIFT); // P1 doesn't have this card

        assertThrows(InvalidParameterException.class, () -> boardGame.playerUseActionCard(player1, heliCard));
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
    }


    @Test
    @DisplayName("Choose Player to Fly With Card throws if not in PlayerChooseAZoneToFlyWithCard state")
    void choosePlayerToFlyWithCardThrowsIfNotChoosingPlayer() {
        setCurrentPlayer(player1, 3); // P1's turn
        putPlayersOnZone(startZone1, player1, player2); // P1 & P2 on same zone
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing); // Ensure wrong state

        assertThrows(InvalidStateOfTheGameException.class, () -> boardGame.choosePlayerToFlyWithCard(player2));
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).isEmpty());
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame));
    }

    @Test
    @DisplayName("Choose Player to Fly With Card throws if player not on the same tile")
    void choosePlayerToFlyWithCardThrowsIfPlayerNotOnSameTile() {
        setCurrentPlayer(player1, 3); // P1's turn
        putPlayersOnZone(startZone1, player1); // P1 at 0,0
        putPlayersOnZone(startZone2, player2); // P2 at 0,4 (different zone)

        Card heliCard = new Card(CardType.HELICOPTER_LIFT);
        player1.takeCard(heliCard);

        boardGame.playerUseActionCard(player1, heliCard); // State becomes PlayerChooseAZoneToFlyWithCard
        assertEquals(GameState.PlayerChooseAZoneToFlyWithCard, BoardGameTestHelper.getGameState(boardGame));

        Set<Player> choosablePlayers = boardGame.getPlayersToChoose();
        assertTrue(choosablePlayers.isEmpty()); // P2 should not be choosable

        // Attempt to choose P2 anyway
        assertThrows(InvalidParameterException.class, () -> boardGame.choosePlayerToFlyWithCard(player2));
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).isEmpty());
        assertEquals(GameState.PlayerChooseAZoneToFlyWithCard, BoardGameTestHelper.getGameState(boardGame));
    }

    @Test
    @DisplayName("Fly Player to Zone With Card throws if not in PlayerChooseAZoneToFlyWithCard state")
    void flyPlayerToZoneWithCardThrowsIfNotChoosingZone() {
        setCurrentPlayer(player1, 3); // P1's turn
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing); // Ensure wrong state
        Zone targetZone = farawayDry;

        // Simulate playerChoosingCardToUse and playersToFlyWith being set, even though state is wrong
        Card heliCard = new Card(CardType.HELICOPTER_LIFT);
        player1.takeCard(heliCard);
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "playerChoosingCardToUse", player1);
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "playersToFlyWith", new ArrayList<>(List.of(player2)));


        assertThrows(InvalidStateOfTheGameException.class, () -> boardGame.flyPlayerToZoneWithCard(targetZone));
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertNotNull(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse")); // Still set
        assertFalse(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).isEmpty()); // Still set
    }

    @Test
    @DisplayName("Fly Player to Zone With Card throws if choosing inaccessible zone")
    void flyPlayerToZoneWithCardThrowsIfInaccessibleZone() {
        setCurrentPlayer(player1, 3); // P1's turn
        Card heliCard = new Card(CardType.HELICOPTER_LIFT);
        player1.takeCard(heliCard);
        adjacentDry.makeInaccessible(); // Make a zone inaccessible


        boardGame.playerUseActionCard(player1, heliCard); // State becomes choosing
        assertEquals(GameState.PlayerChooseAZoneToFlyWithCard, BoardGameTestHelper.getGameState(boardGame));

        // Simulate choosing players (e.g., P2)
        putPlayersOnZone(startZone1, player1, player2);
        boardGame.choosePlayerToFlyWithCard(player2);


        // Attempt to use on an inaccessible zone
        assertThrows(ZoneIsInaccessibleException.class, () -> boardGame.flyPlayerToZoneWithCard(adjacentDry)); // getZonesToFlyWithCard prevents this
        assertEquals(ZoneState.Inaccessible, adjacentDry.getZone_state()); // Zone state unchanged

        boardGame.playerUseActionCard(player1, heliCard); // State becomes choosing
        assertEquals(GameState.PlayerChooseAZoneToFlyWithCard, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertNotNull(BoardGameTestHelper.getPrivateFieldValue(boardGame, "playerChoosingCardToUse")); // Still set
        assertTrue(((ArrayList) BoardGameTestHelper.getPrivateFieldValue(boardGame, "playersToFlyWith")).isEmpty()); // Still set (list not cleared)

        // Note: The model method flyPlayerToZoneWithCard checks `zone.isAccessible()` directly.
        // It does not filter based on the *possible* zones list from getZonesToFlyWithCard
        // Let's ensure the direct check works too.
        setUp(); // Reset state
        setCurrentPlayer(player1, 3);
        heliCard = new Card(CardType.HELICOPTER_LIFT);
        player1.takeCard(heliCard);
        adjacentDry.makeInaccessible();

        boardGame.playerUseActionCard(player1, heliCard);
        assertEquals(GameState.PlayerChooseAZoneToFlyWithCard, BoardGameTestHelper.getGameState(boardGame));

        // Simulate choosing players (e.g., P2)
        putPlayersOnZone(startZone1, player1, player2);
        boardGame.choosePlayerToFlyWithCard(player2);

        assertThrows(ZoneIsInaccessibleException.class, () -> boardGame.flyPlayerToZoneWithCard(adjacentDry));
        assertEquals(ZoneState.Inaccessible, adjacentDry.getZone_state()); // Zone state unchanged
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
    }
}
