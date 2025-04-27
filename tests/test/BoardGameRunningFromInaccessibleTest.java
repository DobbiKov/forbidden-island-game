package test;

import Errors.GameOverException;
import Errors.InvalidStateOfTheGameException;
import Model.BoardGame;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoardGameRunningFromInaccessibleTest {

    private BoardGame boardGame;
    private Player player1, player2, player3, player4; // Pilot, Engineer, Diver, Explorer
    private Zone inaccessibleZone;
    private Zone escapeZoneDryAdjacent;
    private Zone escapeZoneDryDiagonal;
    private Zone escapeZoneAccessibleFloodedAdjacent; // For Diver
    private Zone escapeZoneAccessibleFloodedDiagonal; // For Diver
    private Zone escapeZoneDryReachableThroughFlooded; // For Diver
    private Zone escapeZoneAccessibleAnywhere; // For Pilot
    private Zone floodedForDiver;

    @BeforeEach
    void setUp() {
        Player.resetPlayerCount(); // Reset for each test
        boardGame = new BoardGame();

        // Manually set up players and zones for predictable testing
        Zone[][] board = new Zone[5][5];
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "board", board);
        BoardGameTestHelper.setPrivateFieldValue(boardGame, "size", 5);

        // Create specific zones for testing escape
        inaccessibleZone = new Zone(2, 2, true, ZoneCard.fodls_landing); // Zone that will become inaccessible

        escapeZoneDryAdjacent = new Zone(2, 3, true, ZoneCard.cliffs_of_abandon); // Adjacent dry

        escapeZoneDryDiagonal = new Zone(3, 3, true, ZoneCard.coral_palace); // Diagonal dry

        escapeZoneAccessibleFloodedAdjacent = new Zone(2, 1, true, ZoneCard.twilight_hollow); // Adjacent flooded
        escapeZoneAccessibleFloodedAdjacent.floodZone();

        escapeZoneAccessibleFloodedDiagonal = new Zone(3, 1, true, ZoneCard.phantom_rock); // Diagonal flooded
        escapeZoneAccessibleFloodedDiagonal.floodZone();

        floodedForDiver = new Zone(1, 2, true, ZoneCard.bronze_gate);
        floodedForDiver.floodZone();

        // Setup path for Diver: InaccessibleZone(2,2) -> Flooded(1,2) -> Dry(0,2)
        Zone floodedForDiver = new Zone(1, 2, true, ZoneCard.bronze_gate);
        floodedForDiver.floodZone();
        escapeZoneDryReachableThroughFlooded = new Zone(0, 2, true, ZoneCard.whispering_garden);


        // Place specific zones
        board[2][2] = inaccessibleZone;
        board[2][3] = escapeZoneDryAdjacent;
        board[3][3] = escapeZoneDryDiagonal;
        board[2][1] = escapeZoneAccessibleFloodedAdjacent;
        board[3][1] = escapeZoneAccessibleFloodedDiagonal;
        board[1][2] = floodedForDiver;
        board[0][2] = escapeZoneDryReachableThroughFlooded;
        // Add some faraway accessible zone for Pilot escape
        escapeZoneAccessibleAnywhere = new Zone(4, 4, true, ZoneCard.gold_gate);
        board[4][4] = escapeZoneAccessibleAnywhere;


        // Fill in remaining zones with simple dry zones
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(board[i][j] == null) {
                    board[i][j] = new Zone(i,j,true, ZoneCard.dunes_of_deception);
                }
            }
        }

        // Add players with specific roles
        player1 = new Player("P1", PlayerRole.Pilot);
        player2 = new Player("P2", PlayerRole.Engineer);
        player3 = new Player("P3", PlayerRole.Diver);
        player4 = new Player("P4", PlayerRole.Explorer);

        BoardGameTestHelper.setPlayers(boardGame, player1, player2, player3, player4);
        BoardGameTestHelper.setPlayerCount(boardGame, 4);

        // Start the game state manually
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing);
        BoardGameTestHelper.setPlayerTurnId(boardGame, 0); // P1's turn
        BoardGameTestHelper.setCurrentPlayerActionsNum(boardGame, 3); // Actions don't matter when running

        // Clear any stranded players from previous tests
        BoardGameTestHelper.setPlayersOnInaccessibleZones(boardGame, new ArrayList<>());
        BoardGameTestHelper.setCurrentPlayerRunningFromInaccessibleZone(boardGame, null);

        // Simulate players being stranded on the inaccessible zone (2,2)
        putPlayersOnZone(inaccessibleZone, player1, player2, player3, player4); // All players stranded
        inaccessibleZone.makeInaccessible(); // Start inaccessible
        BoardGameTestHelper.setPlayersOnInaccessibleZones(boardGame, new ArrayList<>(List.of(player1, player2, player3, player4)));
        BoardGameTestHelper.setGameState(boardGame, GameState.PlayersRunningFromAnInaccessibleZone);

    }

    // --- Helper to put players on a zone ---
    private void putPlayersOnZone(Zone zone, Player... players) {
        zone.getPlayers_on_zone().clear(); // Clear existing players
        for(Player p : players) {
            if (p.getPlayer_zone() != null) {
                if(p.getPlayer_zone().getPlayers_on_zone().contains(p)) p.getPlayer_zone().removePlayerFromZone(p);
            }
            p.move_Player(zone); // Use player method which updates both sides
            zone.addPlayerToZone(p);
        }
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
    // --- Helper to get ZoneCard from a Zone (handle nulls) ---
    private ZoneCard getZoneCardFromZone(Zone z) {
        return z != null ? z.getZoneCard() : null;
    }
    // --- Get Actions / State Checks ---

    @Test
    @DisplayName("getPossiblePlayerActionsForCurrentTurn returns only Run for stranded players")
    void getPossibleActionsForStrandedPlayer() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertTrue(BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).contains(player1)); // P1 is stranded

        List<Model.PlayerAction> actions = boardGame.getPossiblePlayerActionsForCurrentTurn(player1);
        assertEquals(1, actions.size());
        assertTrue(actions.contains(Model.PlayerAction.RunFromInaccessibleZone));
    }

    @Test
    @DisplayName("getPossiblePlayerActionsForCurrentTurn returns empty list for non-stranded players")
    void getPossibleActionsForNonStrandedPlayer() {
        // Simulate P1 escaping, leaving P2, P3, P4 stranded. P1 is not stranded anymore.
        BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).clear();
        BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).add(player2);
        BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).add(player3);
        BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).add(player4);
        putPlayersOnZone(escapeZoneDryAdjacent, player1); // P1 is safe


        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertFalse(BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).contains(player1)); // P1 is not stranded

        List<Model.PlayerAction> actions = boardGame.getPossiblePlayerActionsForCurrentTurn(player1);
        assertTrue(actions.isEmpty()); // Non-stranded players have no actions while others are running
    }


    @Test
    @DisplayName("isPlayerChoosingZoneToRunFromInaccesbleZone is true when currentPlayerRunningFromInaccessibleZone is set")
    void isChoosingRunZoneIsTrueWhenPlayerSet() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Initially null

        // Simulate setting the current player running
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1);
        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        assertTrue(boardGame.isPlayerChoosingZoneToRunFromInaccesbleZone());
    }

    @Test
    @DisplayName("arePlayersRunningFromInaccesbleZone is true in the running state")
    void arePlayersRunningIsTrueInRunningState() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertTrue(boardGame.arePlayersRunningFromInaccesbleZone());
    }

    @Test
    @DisplayName("isPlayerChoosingSomething is true in the running state when a player is chosen")
    void isChoosingSomethingIsTrueWhenRunningAndPlayerChosen() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Initially null
        assertFalse(boardGame.isPlayerChoosingSomething()); // Not choosing a zone yet

        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1);
        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        assertTrue(boardGame.isPlayerChoosingSomething()); // Now choosing the zone
    }


    // --- Set Player Choose Zone to Run ---

    @Test
    @DisplayName("setPlayerChooseZoneToRunFromInaccessbileZone sets the current player running")
    void setPlayerChooseRunZoneSetsPlayer() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));

        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1);

        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State doesn't change
    }

    @Test
    @DisplayName("setPlayerChooseZoneToRunFromInaccessbileZone throws if a player is already running")
    void setPlayerChooseRunZoneThrowsIfAlreadyRunning() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1); // Set P1 running

        assertThrows(InvalidStateOfTheGameException.class, () -> boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player2)); // Try to set P2 running

        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // P1 still running
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
    }


    // --- Get Zones Possible For Choosing (Run) ---

    @Test
    @DisplayName("getZonesToRunFromInaccessibleZone returns correct zones for Standard/Engineer/Messenger")
    void getRunZonesStandard() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        inaccessibleZone.makeAccessible();
        putPlayersOnZone(inaccessibleZone, player2); // Engineer (standard movement rules for run)
        inaccessibleZone.makeInaccessible();
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player2); // Set P2 running

        List<Zone> possibleZones = boardGame.getZonesPossibleForChoosing();
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State doesn't change

        // Standard can move orthogonally to accessible (Dry or Flooded) zones
        // From 2,2: Orthogonal are 1,2 (Flooded), 2,1 (Flooded), 2,3 (Dry), 3,2 (Dry - need to add this to setup)
        Zone dryAdjacentOther = new Zone(3,2,true, ZoneCard.watchtower); boardGame.getBoard()[3][2] = dryAdjacentOther;

        Set<Zone> expected = Set.of(escapeZoneDryAdjacent, escapeZoneAccessibleFloodedAdjacent, dryAdjacentOther, floodedForDiver); // 2,3, 2,1, 3,2, 1,2 (adjacent accessible)
        Set<Zone> actual = new HashSet<>(possibleZones);

        assertEquals(4, actual.size()); // Expect 4 orthogonal accessible zones
        assertFalse(actual.contains(escapeZoneDryDiagonal)); // Not orthogonal
        assertFalse(actual.contains(inaccessibleZone)); // Cannot run to same zone
    }

    @Test
    @DisplayName("getZonesToRunFromInaccessibleZone returns correct zones for Pilot")
    void getRunZonesPilot() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        inaccessibleZone.makeAccessible();
        putPlayersOnZone(inaccessibleZone, player1); // Pilot
        inaccessibleZone.makeInaccessible();
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1); // Set P1 running

        List<Zone> possibleZones = boardGame.getZonesPossibleForChoosing();
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State doesn't change

        // Pilot can fly to *any* accessible zone
        // Count all accessible zones on the board
        Set<Zone> allAccessibleZones = new HashSet<>();
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                Zone z = boardGame.getZone(i, j);
                if(z.isAccessible()){
                    allAccessibleZones.add(z);
                }
            }
        }
        allAccessibleZones.remove(inaccessibleZone); // Cannot fly back to the inaccessible zone

        Set<Zone> actual = new HashSet<>(possibleZones);


        // The total number of accessible zones depends on the randomized setup and our manual zones.
        // We need to count the specific zones added plus the remaining dry ones.
        // Manually added: inaccessibleZone(2,2) - skip, escapeZoneDryAdjacent(2,3), escapeZoneDryDiagonal(3,3),
        // escapeZoneAccessibleFloodedAdjacent(2,1), escapeZoneAccessibleFloodedDiagonal(3,1), floodedForDiver(1,2),
        // escapeZoneDryReachableThroughFlooded(0,2), escapeZoneAccessibleAnywhere(4,4), dryAdjacentOther(3,2)
        // Total manually added accessible: 8
        // Plus 25 total zones - 1 inaccessible = 24 accessible normally. Our manual setup replaces some.
        // Let's check against our known accessible zones:
        Set<Zone> knownAccessible = Set.of(
                escapeZoneDryAdjacent, escapeZoneDryDiagonal, escapeZoneAccessibleFloodedAdjacent,
                escapeZoneAccessibleFloodedDiagonal, floodedForDiver,
                escapeZoneDryReachableThroughFlooded, escapeZoneAccessibleAnywhere,
                boardGame.getBoard()[2][2], // Center is now a dry zone
                boardGame.getBoard()[0][0], boardGame.getBoard()[0][4], boardGame.getBoard()[4][0]  // Start zones
        ); // Add other setup zones

        // This is getting complicated with manual setup. A better way is to count them dynamically.
        int accessibleCount = 0;
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(boardGame.getBoard()[i][j].isAccessible()){
                    accessibleCount++;
                }
            }
        }
        // Accessible zones should be all board zones (25) minus the one inaccessible zone (2,2) = 24.
        // However, our setup made 2,2 inaccessible, then replaced it with an accessible one, and made a *new* one inaccessible.
        // The inaccessible zone IS 2,2 in this test setup. So all OTHER 24 zones should be accessible.
        assertEquals(24, accessibleCount, "Should be 24 accessible zones if only 2,2 is inaccessible");

        assertEquals(24, actual.size());
        assertTrue(actual.contains(escapeZoneDryAdjacent)); // Should contain various types
        assertTrue(actual.contains(escapeZoneDryDiagonal));
        assertTrue(actual.contains(escapeZoneAccessibleFloodedAdjacent));
        assertTrue(actual.contains(escapeZoneAccessibleFloodedDiagonal));
        assertTrue(actual.contains(escapeZoneDryReachableThroughFlooded)); // Accessible (Dry)
        assertTrue(actual.contains(escapeZoneAccessibleAnywhere)); // Accessible (Dry)
        assertFalse(actual.contains(inaccessibleZone)); // Cannot fly back to the inaccessible zone
    }

    @Test
    @DisplayName("getZonesToRunFromInaccessibleZone returns correct zones for Explorer")
    void getRunZonesExplorer() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));

        inaccessibleZone.makeAccessible();
        putPlayersOnZone(inaccessibleZone, player4); // Explorer
        inaccessibleZone.makeInaccessible();
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player4); // Set P4 running

        List<Zone> possibleZones = boardGame.getZonesPossibleForChoosing();
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State doesn't change

        // Explorer can move orthogonally or diagonally to *accessible* zones
        // From 2,2:
        // Orthogonal accessible: 1,2(Flooded), 2,1(Flooded), 2,3(Dry), 3,2(Dry)
        // Diagonal accessible: 1,1(Dry), 1,3(Flooded), 3,1(Flooded), 3,3(Dry)
        Zone dryAdjacentOther = boardGame.getZone(1, 3); if(dryAdjacentOther.isFlooded()) dryAdjacentOther.shoreUp();
        Zone dryDiagonalUR = boardGame.getZone(1,1); if(dryDiagonalUR.isFlooded()) dryDiagonalUR.shoreUp();
        Zone dryDiagonalDR = boardGame.getZone(3,3); if(dryDiagonalDR.isFlooded()) dryDiagonalDR.shoreUp();
        Zone dryDiagonal2 = boardGame.getZone(3, 2); if(dryDiagonal2.isFlooded()) dryDiagonal2.shoreUp();


        Set<Zone> expected = Set.of(escapeZoneDryAdjacent, escapeZoneAccessibleFloodedAdjacent, dryAdjacentOther, floodedForDiver, // Orthogonal accessible
                dryDiagonalUR, escapeZoneAccessibleFloodedDiagonal, dryDiagonalDR, dryDiagonal2 // Diagonal accessible
        );

        Set<Zone> actual = new HashSet<>(possibleZones);

        assertEquals(expected.size(), actual.size()); // Expect 8 adjacent/diagonal accessible zones
        assertFalse(actual.contains(escapeZoneDryReachableThroughFlooded)); // Not directly adjacent/diagonal
        assertFalse(actual.contains(inaccessibleZone)); // Cannot run to same zone
    }


    // --- Choose Zone to Run ---

    @Test
    @DisplayName("chooseZoneToRunFromInaccessible moves the player and updates state (single player)")
    void chooseRunZoneMovesPlayerAndUpdatesStateSingle() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        // Simulate only one player stranded
        BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).clear();
        BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).add(player1); // P1 is Pilot

        inaccessibleZone.makeAccessible();
        putPlayersOnZone(inaccessibleZone, player1); // P1 on inaccessible zone (2,2)
        inaccessibleZone.makeInaccessible();

        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1); // Set P1 running
        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));

        Zone escapeZone = escapeZoneAccessibleAnywhere; // Pilot can run anywhere accessible (4,4)

        boardGame.chooseZoneToRunFromInaccessible(escapeZone); // P1 escapes to 4,4

        assertEquals(escapeZone, player1.getPlayer_zone()); // Player moved
        assertFalse(inaccessibleZone.getPlayers_on_zone().contains(player1)); // Not on old zone
        assertTrue(escapeZone.getPlayers_on_zone().contains(player1)); // On new zone

        List<Player> stranded = BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame);
        assertTrue(stranded.isEmpty()); // Player removed from stranded list

        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Running player nulled
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State changes back to Playing
    }

    @Test
    @DisplayName("chooseZoneToRunFromInaccessible moves player and state remains Running if others are stranded")
    void chooseRunZoneMovesPlayerStateRemainsRunning() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        // P1, P2, P3, P4 are all stranded in setup
        assertEquals(4, BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).size());

        inaccessibleZone.makeAccessible();
        putPlayersOnZone(inaccessibleZone, player1, player2, player3, player4); // All on inaccessible (2,2)
        inaccessibleZone.makeInaccessible();

        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1); // Set P1 running (Pilot)
        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));

        Zone escapeZoneP1 = escapeZoneAccessibleAnywhere; // P1 escapes to 4,4

        boardGame.chooseZoneToRunFromInaccessible(escapeZoneP1); // P1 escapes

        assertEquals(escapeZoneP1, player1.getPlayer_zone()); // P1 moved
        assertFalse(inaccessibleZone.getPlayers_on_zone().contains(player1)); // Not on old zone

        List<Player> stranded = BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame);
        assertEquals(3, stranded.size()); // P1 removed from stranded list
        assertFalse(stranded.contains(player1));
        assertTrue(stranded.contains(player2));
        assertTrue(stranded.contains(player3));
        assertTrue(stranded.contains(player4));


        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Running player nulled AFTER escape
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State remains Running


        // Simulate selecting the next player manually (Controller/UI responsibility)
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player2); // Set P2 running (Engineer)
        assertEquals(player2, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));

        Zone escapeZoneP2 = escapeZoneDryAdjacent; // P2 can run to 2,3 (adjacent dry)

        boardGame.chooseZoneToRunFromInaccessible(escapeZoneP2); // P2 escapes

        assertEquals(escapeZoneP2, player2.getPlayer_zone());
        assertFalse(inaccessibleZone.getPlayers_on_zone().contains(player2));

        stranded = BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame);
        assertEquals(2, stranded.size()); // P2 removed
        assertFalse(stranded.contains(player2));
        assertTrue(stranded.contains(player3));
        assertTrue(stranded.contains(player4));

        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State remains Running

        // ... continue until all players escape ...
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player3); // P3 running (Diver)
        assertEquals(player3, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        Zone escapeZoneP3 = escapeZoneDryReachableThroughFlooded; // Diver escapes to 0,2 (through flooded)
        boardGame.chooseZoneToRunFromInaccessible(escapeZoneP3);
        assertEquals(1, BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).size());
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));


        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player4); // P4 running (Explorer)
        assertEquals(player4, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        Zone escapeZoneP4 = escapeZoneDryDiagonal; // Explorer escapes to 3,3 (diagonal dry)
        boardGame.chooseZoneToRunFromInaccessible(escapeZoneP4);
        assertEquals(0, BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).size()); // All players escaped
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State finally returns to Playing
    }


    @Test
    @DisplayName("chooseZoneToRunFromInaccessible throws if not in PlayersRunningFromAnInaccessibleZone state")
    void chooseRunZoneThrowsIfNotRunningState() {
        // Simulate state being Playing instead of Running
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing);
        // Simulate having a player set as running (shouldn't happen in valid flow)
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1);

        Zone escapeZone = escapeZoneDryAdjacent;

        assertThrows(InvalidStateOfTheGameException.class, () -> boardGame.chooseZoneToRunFromInaccessible(escapeZone));

        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertNotNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Running player still set
    }

    @Test
    @DisplayName("chooseZoneToRunFromInaccessible throws if currentPlayerRunningFromInaccessibleZone is null")
    void chooseRunZoneThrowsIfNoPlayerRunning() {
        // Simulate being in the correct state, but no player set as running (shouldn't happen in valid flow)
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Ensure null

        Zone escapeZone = escapeZoneDryAdjacent;

        assertThrows(InvalidStateOfTheGameException.class, () -> boardGame.chooseZoneToRunFromInaccessible(escapeZone));

        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Still null
    }

    @Test
    @DisplayName("chooseZoneToRunFromInaccessible throws if choosing an inaccessible zone")
    void chooseRunZoneThrowsIfInaccessibleZone() {
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player1); // Set P1 running

        // Attempt to choose the inaccessible zone they are on, or another inaccessible zone
        Zone invalidZone = inaccessibleZone; // The zone they are on

        assertThrows(InvalidParameterException.class, () -> boardGame.chooseZoneToRunFromInaccessible(invalidZone));

        assertEquals(inaccessibleZone, player1.getPlayer_zone()); // Player did not move
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State unchanged
        assertEquals(player1, BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame)); // Still set
    }


    // --- Game Over: Player Dead ---

    @Test
    @DisplayName("End turn throws GameOverException if a stranded player has no escape zones")
    void endTurnThrowsGameOverIfPlayerCannotEscape() {
        // Setup: Make a zone inaccessible such that a player on it has no adjacent/diagonal accessible zones.
        // For a standard player (Engineer), this means all orthogonal tiles are inaccessible.
        Zone deathTrap = new Zone(1,1,false, ZoneCard.phantom_rock); deathTrap.makeInaccessible(); boardGame.getBoard()[1][1] = deathTrap; // Center death trap
        // Make all orthogonal neighbors inaccessible too
        boardGame.getBoard()[0][1].makeInaccessible(); // 0,1
        boardGame.getBoard()[1][0].makeInaccessible(); // 1,0
        boardGame.getBoard()[1][2].makeInaccessible(); // 1,2
        boardGame.getBoard()[2][1].makeInaccessible(); // 2,1

        // Put Engineer (P2) on the death trap
        deathTrap.makeAccessible();
        putPlayersOnZone(deathTrap, player2);
        deathTrap.makeInaccessible();
        assertEquals(deathTrap, player2.getPlayer_zone());

        // Simulate end turn where this zone becomes inaccessible and P2 is stranded (setup already did this)
        // The check for impossibility happens *within* the endTurn -> checkPlayerDead call *after* flooding.
        // So, we need to simulate the endTurn process resulting in the stranded player having no escape.
        // Let's clear playersOnInaccessibleZones and put P2 on the death trap, then run endTurn.
        // The endTurn must cause the trap zone to become inaccessible.
        // Reset board/players to simplify this test
        setUp();
        // Create a simple 3x3 section where center and neighbours become inaccessible
        Zone trapCenter = new Zone(1,1,true, ZoneCard.phantom_rock); boardGame.getBoard()[1][1] = trapCenter;
        Zone trapN = new Zone(0,1,false, ZoneCard.cliffs_of_abandon); boardGame.getBoard()[0][1] = trapN;
        Zone trapW = new Zone(1,0,false, ZoneCard.twilight_hollow); boardGame.getBoard()[1][0] = trapW;
        Zone trapE = new Zone(1,2,false, ZoneCard.bronze_gate); boardGame.getBoard()[1][2] = trapE;
        Zone trapS = new Zone(2,1,false, ZoneCard.watchtower); boardGame.getBoard()[2][1] = trapS;

        // Put P2 (Engineer) on trapCenter
        putPlayersOnZone(trapCenter, player2);
        trapCenter.makeInaccessible();
        BoardGameTestHelper.setPlayers(boardGame, player2); // Only P2 in game
        BoardGameTestHelper.setPlayerCount(boardGame, 1);
        setCurrentPlayer(player2, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);


        // Flood cards to make trapCenter and all its orthogonal neighbours inaccessible in one go
        BoardGameTestHelper.getWaterMeter(boardGame).resetLevel(); // Level 0, Rate 2
        BoardGameTestHelper.getWaterMeter(boardGame).increaseLevel(); // Level 1, Rate 3
        BoardGameTestHelper.getWaterMeter(boardGame).increaseLevel(); // Level 2, Rate 3
        BoardGameTestHelper.getWaterMeter(boardGame).increaseLevel(); // Level 3, Rate 4
        // Need rate 5 to sink center (Flooded->Inaccessible) and flood 4 neighbours (Normal->Flooded)
        BoardGameTestHelper.getWaterMeter(boardGame).increaseLevel(); // Level 4, Rate 4
        BoardGameTestHelper.getWaterMeter(boardGame).increaseLevel(); // Level 5, Rate 5
        assertEquals(5, BoardGameTestHelper.getWaterMeter(boardGame).getLevel());
        assertEquals(5, BoardGameTestHelper.getWaterMeter(boardGame).getCurrentFloodRate());

        // Flood cards: center (twice), N, W, E, S (once each)
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(trapCenter)); // 1st flood
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(trapN));
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(trapW));
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(trapE));
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(trapCenter)); // 2nd flood -> Inaccessible
        // Add other cards to meet flood rate 5 if needed, but these 5 are enough for this test

        // Set trapCenter and its neighbours to Normal state initially for this flow


        // End turn - this should sink the trap zones and strand P2 without escape
        GameOverException exception = assertThrows(GameOverException.class, () -> boardGame.endTurn());
        assertEquals("Game over: you have lost the player!", exception.getMessage());

        // Verify states after end turn
        assertEquals(ZoneState.Inaccessible, trapCenter.getZone_state()); // Center trap is inaccessible
        assertEquals(ZoneState.Inaccessible, trapN.getZone_state()); // Neighbours are flooded (accessible)
        assertEquals(ZoneState.Inaccessible, trapW.getZone_state());
        assertEquals(ZoneState.Inaccessible, trapE.getZone_state());
        assertEquals(ZoneState.Inaccessible, trapS.getZone_state());

        assertTrue(BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).contains(player2)); // Player is marked stranded

        // The checkPlayerDead method within endTurn should have found no valid escape zones for the Engineer from the inaccessible center surrounded by flooded tiles.
        // Engineer cannot move onto flooded tiles unless running from inaccessible, AND cannot move diagonally.
        // Let's verify the escape zones logic specifically for an Engineer from inaccessible surrounded by flooded:
        // From 1,1 (inaccessible), Engineer's run escape zones are orthogonal accessible.
        // Orthogonal neighbours are 0,1(Flooded), 1,0(Flooded), 1,2(Flooded), 2,1(Flooded). These are all accessible.
        // My setup/understanding was slightly off. An Engineer CAN run from inaccessible *to* an adjacent accessible (flooded or dry).
        // The test needs a scenario where *all* adjacent/diagonal zones are inaccessible.
        // Let's make a 3x3 block of inaccessible zones.
        setUp(); // Reset
        Zone center = boardGame.getBoard()[2][2]; center.makeInaccessible(); // 2,2
        boardGame.getBoard()[2][3].makeInaccessible(); boardGame.getBoard()[2][1].makeInaccessible(); // Orthogonal
        boardGame.getBoard()[3][2].makeInaccessible(); boardGame.getBoard()[1][2].makeInaccessible();
        boardGame.getBoard()[3][3].makeInaccessible(); boardGame.getBoard()[3][1].makeInaccessible(); // Diagonal
        boardGame.getBoard()[1][3].makeInaccessible(); boardGame.getBoard()[1][1].makeInaccessible(); // Diagonal
        // Now 2,2 and all adjacent/diagonal are inaccessible.

        center.makeAccessible();
        putPlayersOnZone(center, player2); // Put Engineer on 2,2
        center.makeInaccessible();
        BoardGameTestHelper.setPlayers(boardGame, player2); // Only P2 in game
        BoardGameTestHelper.setPlayerCount(boardGame, 1);
        setCurrentPlayer(player2, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        BoardGameTestHelper.setPlayersOnInaccessibleZones(boardGame, new ArrayList<>(List.of(player2))); // Manually set stranded


        GameOverException exception2 = assertThrows(GameOverException.class, () -> boardGame.endTurn());
        assertEquals("Game over: you have lost the player!", exception2.getMessage());

        // Verify player is still stranded
        assertTrue(BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame).contains(player2));

        boardGame.setPlayerChooseZoneToRunFromInaccessibleZone(player2);
        // verify checkPlayerDead finds no escape routes for Engineer from 2,2
        List<Zone> escapeZones = boardGame.getZonesToRunFromInaccessibleZone(); // Need to manually trigger this after setting running player
        escapeZones = boardGame.getZonesPossibleForChoosing();

        assertTrue(escapeZones.isEmpty()); // Should be no escape zones

    }
}
