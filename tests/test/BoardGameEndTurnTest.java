package test;

import Errors.*;
import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardGameEndTurnTest {

    private BoardGame boardGame;
    private Player player1, player2;
    private Zone startZone1, startZone2;
    private Zone helicopterZone;
    private Zone artefactZoneEarth1, artefactZoneEarth2;
    private Zone zoneToFlood1, zoneToFlood2, zoneToFlood3, zoneToFlood4, zoneToFlood5, zoneToFlood6; // Need zones to flood


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
        helicopterZone = new Model.HelicopterZone(2, 2); // Heli zone at 2,2

        artefactZoneEarth1 = new Model.ArtefactZone(1, 2, ZoneCard.temple_of_the_moon, Model.Artefact.Earth);
        artefactZoneEarth2 = new Model.ArtefactZone(2, 1, ZoneCard.temle_of_the_sun, Model.Artefact.Earth);

        // Create zones to control flooding
        zoneToFlood1 = new Zone(0,1,true, ZoneCard.cliffs_of_abandon);
        zoneToFlood2 = new Zone(1,0,true, ZoneCard.twilight_hollow);
        zoneToFlood3 = new Zone(1,1,true, ZoneCard.phantom_rock);
        zoneToFlood4 = new Zone(0,3,true, ZoneCard.whispering_garden);
        zoneToFlood5 = new Zone(3,0,true, ZoneCard.watchtower);
        zoneToFlood6 = new Zone(4,1,true, ZoneCard.gold_gate);


        // Place specific zones
        board[0][0] = startZone1; board[0][4] = startZone2;
        board[2][2] = helicopterZone;
        board[1][2] = artefactZoneEarth1; board[2][1] = artefactZoneEarth2;
        board[0][1] = zoneToFlood1; board[1][0] = zoneToFlood2; board[1][1] = zoneToFlood3;
        board[0][3] = zoneToFlood4; board[3][0] = zoneToFlood5; board[4][1] = zoneToFlood6;


        // Fill in remaining zones with simple dry zones
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(board[i][j] == null) {
                    board[i][j] = new Zone(i,j,true, ZoneCard.dunes_of_deception);
                }
            }
        }

        // Add players
        player1 = new Player("P1", PlayerRole.Pilot);
        player2 = new Player("P2", PlayerRole.Engineer);

        BoardGameTestHelper.setPlayers(boardGame, player1, player2);
        BoardGameTestHelper.setPlayerCount(boardGame, 2);

        // Assign players to zones
        putPlayersOnZone(startZone1, player1); // P1 at 0,0
        putPlayersOnZone(startZone2, player2); // P2 at 0,4

        // Start the game state manually for end turn tests
        BoardGameTestHelper.setGameState(boardGame, GameState.Playing);
        BoardGameTestHelper.setPlayerTurnId(boardGame, 0); // P1's turn
        BoardGameTestHelper.setCurrentPlayerActionsNum(boardGame, 0); // Assume actions are spent

        // Clear treasure and flood decks for controlled tests
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDiscardCards().clear();

        // Ensure Water Meter is at level 0
        BoardGameTestHelper.getWaterMeter(boardGame).resetLevel();

        // Clear any stranded players
        BoardGameTestHelper.setPlayersOnInaccessibleZones(boardGame, new ArrayList<>());
        BoardGameTestHelper.setCurrentPlayerRunningFromInaccessibleZone(boardGame, null);

        // Clear claimed artefacts
        BoardGameTestHelper.setClaimedArtefacts(boardGame, EnumSet.noneOf(Model.Artefact.class));
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

    // --- End Turn: Treasure Phase ---

    @Test
    @DisplayName("End turn draws 2 treasure cards if available")
    void endTurnDrawsTwoTreasures() {
        // Put 2 non-Water Rise cards in draw pile
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.FIRE_CARD));
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.WATER_CARD));
//        BoardGameTestHelper.getTreasureDeck(boardGame).addWaterRiseCards(); // Add WR to ensure they are distinct

        setCurrentPlayer(player1, 0);
        int initialHandSize = player1.getHand().getSize();

        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);
        boardGame.endTurn();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        assertEquals(initialHandSize + 2, player1.getHand().getSize());
        assertEquals(0, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize()); // Drawn cards aren't discarded
        assertFalse(BoardGameTestHelper.getTreasureDrawnThisTurn(boardGame)); // Flag reset for next turn
        assertEquals(0, BoardGameTestHelper.getWaterMeter(boardGame).getLevel()); // No water rise happened

        // Check turn moved to P2
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame)); // Actions reset
    }


    @Test
    @DisplayName("End turn draws 1 treasure and triggers Water Rise")
    void endTurnDrawsTreasureAndWaterRise() {
        // Put 1 non-Water Rise and 1 Water Rise card in draw pile
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.FIRE_CARD));
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.WATER_RISE));
//        BoardGameTestHelper.getTreasureDeck(boardGame).addWaterRiseCards(); // Add other WRs

        int initialHandSize = player1.getHand().getSize();
        int initialDrawSize = BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize();

        // Add some cards to flood discard so reshuffle can be tested
        BoardGameTestHelper.getFloodDeck(boardGame).getDiscardCards().add(ZoneCard.cliffs_of_abandon);
        BoardGameTestHelper.getFloodDeck(boardGame).getDiscardCards().add(ZoneCard.bronze_gate);

        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        int initialFloodDiscardSize = BoardGameTestHelper.getFloodDeck(boardGame).getDiscardSize();
        int initialFloodDrawSize = BoardGameTestHelper.getFloodDeck(boardGame).getDrawSize();


        // Expecting WaterRiseException
//        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);
//        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.gold_gate);
//        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.cliffs_of_abandon);
        WaterRiseException exception = assertThrows(WaterRiseException.class, () -> boardGame.endTurn());

        assertEquals(initialHandSize + 1, player1.getHand().getSize()); // Only 1 treasure card drawn
        assertEquals(1, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize()); // Water Rise card discarded
        assertEquals(CardType.WATER_RISE, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().get(0).getType());
        assertEquals(1, BoardGameTestHelper.getWaterMeter(boardGame).getLevel()); // Water level increased
        assertEquals(1, BoardGameTestHelper.getFloodDeck(boardGame).getDrawSize()); // Flood discard reshuffled
        assertEquals(1, BoardGameTestHelper.getFloodDeck(boardGame).getDiscardSize()); // Flood discard empty after reshuffle

        // Turn should still pass despite exception (handler deals with dialog, not control flow interruption)
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));
    }

    @Test
    @DisplayName("End turn draws 2 Water Rise cards")
    void endTurnDrawsTwoWaterRise() {
        // Put 2 Water Rise cards in draw pile
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.WATER_RISE));
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.WATER_RISE));
        BoardGameTestHelper.getTreasureDeck(boardGame).addWaterRiseCards(); // Add other WRs to discard

        BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().clear();
        BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().add(new Card(CardType.AIR_CARD));
        BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().add(new Card(CardType.AIR_CARD));
        BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardCards().add(new Card(CardType.AIR_CARD));


        int initialHandSize = player1.getHand().getSize();
        int initialDrawSize = BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize();

        // Expecting WaterRiseException (only throws the first one encountered)
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);
        WaterRiseException exception = assertThrows(WaterRiseException.class, () -> boardGame.endTurn());
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        assertEquals(initialHandSize, player1.getHand().getSize()); // No treasure cards drawn
        assertEquals(2 + 3, BoardGameTestHelper.getTreasureDeck(boardGame).getDiscardSize()); // Both WR cards discarded + the 3 initially added ones
        assertEquals(2, BoardGameTestHelper.getWaterMeter(boardGame).getLevel()); // Water level increased by 2
        // Reshuffle happened twice technically, but state is the same as one large reshuffle

        // Turn should still pass
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));
    }

    @Test
    @DisplayName("End turn throws GameOverException if water level reaches max")
    void endTurnThrowsGameOverIfWaterLevelMax() {
        // Set water level to 9
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        for(int i=0; i<9; i++) wm.increaseLevel();
        assertEquals(9, wm.getLevel());

        // Put a Water Rise card in draw pile
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.WATER_RISE));

        // End turn - drawing WR increases level to 10
        GameOverException exception = assertThrows(GameOverException.class, () -> boardGame.endTurn());
        assertEquals("Game over: water level has reached maximum", exception.getMessage());

        assertEquals(10, wm.getLevel()); // Level is 10
        // Game over should stop further end turn processing or lead to a game over state
        // The handler calls onGameOver on the view.
    }

    @Test
    @DisplayName("End turn enforces hand limit, setting state to Discarding")
    void endTurnEnforcesHandLimit() {
        setCurrentPlayer(player1, 3); // P1's turn
        // Give P1 5 cards
        for(int i=0; i<5; i++) {
            player1.takeCard(new Card(CardType.FIRE_CARD));
        }
        assertEquals(5, player1.getHand().getSize());

        // Put 2 treasure cards in draw pile
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.WATER_CARD));
        BoardGameTestHelper.getTreasureDeck(boardGame).getDrawCards().add(new Card(CardType.EARTH_CARD));

        // End turn - draws 2 cards, hand becomes 7
        TooManyCardsInTheHand exception = assertThrows(TooManyCardsInTheHand.class, () -> boardGame.endTurn());
        assertEquals("You have too many cards in the hand", exception.getMessage());

        assertEquals(7, player1.getHand().getSize()); // Hand is 7
        assertEquals(GameState.Discarding, BoardGameTestHelper.getGameState(boardGame)); // State is discarding
        // Turn should NOT have passed to the next player
        assertEquals(0, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame)); // Actions are not reset yet
    }

    @Test
    @DisplayName("End turn does not draw treasures if treasureDrawnThisTurn is true")
    void endTurnDoesNotDrawTreasuresIfAlreadyDrawn() {
        setCurrentPlayer(player1, 3);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true); // Simulate drawing treasure already
        int initialHandSize = player1.getHand().getSize();
        int initialDrawSize = BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize();

        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);

        boardGame.endTurn();

        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        assertEquals(initialHandSize, player1.getHand().getSize()); // Hand size unchanged
        assertEquals(initialDrawSize, BoardGameTestHelper.getTreasureDeck(boardGame).getDrawSize()); // Draw pile unchanged
        // Flag should reset for next turn
        assertFalse(BoardGameTestHelper.getTreasureDrawnThisTurn(boardGame));

        // Check turn passed to P2
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));

    }


    // --- End Turn: Flood Phase ---

    @Test
    @DisplayName("End turn floods N zones based on water level")
    void endTurnFloodsNZones() {
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        wm.increaseLevel(); // Level 1, Flood Rate 3
        assertEquals(3, wm.getCurrentFloodRate());

        // Put 3 specific cards in flood draw pile
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood1)); // 0,1
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood2)); // 1,0
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood3)); // 1,1
        BoardGameTestHelper.getFloodDeck(boardGame).getDiscardCards().clear(); // Ensure discard is empty initially

        // End turn (assume treasure phase passed without errors/overflow)
        // We need to ensure treasure phase doesn't interfere, or test flood phase in isolation if possible.
        // Let's mock treasure phase outcome by setting treasureDrawnThisTurn = true and hand not overflow
        setCurrentPlayer(player1, 0); // P1's turn, actions spent
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);
        // Ensure hand is not overflowing (player1 starts with 0, drawing 0 still results in 0 < 5)


        boardGame.endTurn();

        assertEquals(ZoneState.Flooded, zoneToFlood1.getZone_state());
        assertEquals(ZoneState.Flooded, zoneToFlood2.getZone_state());
        assertEquals(ZoneState.Flooded, zoneToFlood3.getZone_state());

        assertEquals(0, BoardGameTestHelper.getFloodDeck(boardGame).getDrawSize()); // 3 cards drawn
        assertEquals(3, BoardGameTestHelper.getFloodDeck(boardGame).getDiscardSize()); // 3 cards discarded
        assertTrue(BoardGameTestHelper.getFloodDeck(boardGame).getDiscardCards().contains(getZoneCardFromZone(zoneToFlood1)));

        // Check turn passed to P2
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));
        assertEquals(GameState.Playing, BoardGameTestHelper.getGameState(boardGame)); // Should be Playing if no stranding
    }

    @Test
    @DisplayName("End turn floods zones from Flooded to Inaccessible")
    void endTurnFloodsToInaccessible() {
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        wm.increaseLevel(); // Level 1, Flood Rate 3

        // Flood some zones first
        zoneToFlood1.floodZone(); // Normal -> Flooded
        zoneToFlood2.floodZone(); // Normal -> Flooded
        zoneToFlood3.floodZone(); // Normal -> Flooded

        // Put cards for these flooded zones in flood draw pile
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood1)); // 0,1
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood2)); // 1,0
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood3)); // 1,1


        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        boardGame.endTurn();

        assertEquals(ZoneState.Inaccessible, zoneToFlood1.getZone_state()); // Flooded -> Inaccessible
        assertEquals(ZoneState.Inaccessible, zoneToFlood2.getZone_state()); // Flooded -> Inaccessible
        assertEquals(ZoneState.Inaccessible, zoneToFlood3.getZone_state()); // Flooded -> Inaccessible

        assertEquals(0, BoardGameTestHelper.getFloodDeck(boardGame).getDrawSize());
        assertEquals(3, BoardGameTestHelper.getFloodDeck(boardGame).getDiscardSize());
    }

    @Test
    @DisplayName("End turn strands players on zones that become inaccessible")
    void endTurnStrandsPlayers() {
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        wm.increaseLevel(); // Level 1, Flood Rate 3

        // Put player2 on zoneToFlood1 (0,1)
        putPlayersOnZone(zoneToFlood1, player2);
        assertEquals(zoneToFlood1, player2.getPlayer_zone());
        assertEquals(ZoneState.Normal, zoneToFlood1.getZone_state());

        // Flood zoneToFlood1 first (Normal -> Flooded)
        zoneToFlood1.floodZone();
        assertEquals(ZoneState.Flooded, zoneToFlood1.getZone_state());
        assertTrue(zoneToFlood1.getPlayers_on_zone().contains(player2)); // Player stays on zone

        // Put card for zoneToFlood1 in flood draw pile
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood1)); // 0,1

        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);


        boardGame.endTurn(); // This will flood zoneToFlood1 again (Flooded -> Inaccessible)

        assertEquals(ZoneState.Inaccessible, zoneToFlood1.getZone_state()); // Zone becomes Inaccessible
        assertTrue(zoneToFlood1.getPlayers_on_zone().contains(player2)); // Player is still on the zone initially

        List<Player> stranded = BoardGameTestHelper.getPlayersOnInaccessibleZones(boardGame);
        assertNotNull(stranded);
        assertEquals(1, stranded.size());
        assertTrue(stranded.contains(player2)); // Player2 should be marked as stranded

        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame)); // State changes
        // currentPlayerRunningFromInaccessibleZone is null *after* endTurn, set *before* choosing zone.
        assertNull(BoardGameTestHelper.getCurrentPlayerRunningFromInaccessibleZone(boardGame));

        // Turn should pass despite stranding
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
    }

    @Test
    @DisplayName("End turn throws GameOverException if Helicopter Zone sinks")
    void endTurnThrowsGameOverIfHelicopterZoneSinks() {
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        wm.increaseLevel(); // Level 1, Flood Rate 3

        // Flood Helicopter Zone once (Normal -> Flooded)
        helicopterZone.floodZone();
        assertEquals(ZoneState.Flooded, helicopterZone.getZone_state());

        // Put card for Helicopter Zone in flood draw pile
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(helicopterZone)); // 2,2

        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        // End turn - floods Helicopter Zone again (Flooded -> Inaccessible)
        GameOverException exception = assertThrows(GameOverException.class, () -> boardGame.endTurn());
        assertEquals("Game over: the helicopter landing site has sunk", exception.getMessage());

        assertEquals(ZoneState.Inaccessible, helicopterZone.getZone_state()); // Heli zone is inaccessible
    }

    @Test
    @DisplayName("End turn throws GameOverException if both Artefact Zones for a type sink before collecting")
    void endTurnThrowsGameOverIfArtefactLost() {
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        wm.increaseLevel(); // Level 1, Flood Rate 3

        // Flood both Earth Artefact Zones once (Normal -> Flooded)
        artefactZoneEarth1.floodZone(); // 1,2
        artefactZoneEarth2.floodZone(); // 2,1
        assertEquals(ZoneState.Flooded, artefactZoneEarth1.getZone_state());
        assertEquals(ZoneState.Flooded, artefactZoneEarth2.getZone_state());

        // Put cards for both Earth Artefact Zones in flood draw pile
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(artefactZoneEarth1));
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(artefactZoneEarth2));
        // Add other cards so the flood rate is met without drawing only these two if rate is higher
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood1)); // Draw 3 cards total

        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);


        // End turn - floods both Earth Artefact Zones again (Flooded -> Inaccessible)
        GameOverException exception = assertThrows(GameOverException.class, () -> boardGame.endTurn());
        assertEquals("Game over: you have lost the artefact!", exception.getMessage());

        assertEquals(ZoneState.Inaccessible, artefactZoneEarth1.getZone_state()); // Both inaccessible
        assertEquals(ZoneState.Inaccessible, artefactZoneEarth2.getZone_state());
        assertTrue(BoardGameTestHelper.getClaimedArtefacts(boardGame).isEmpty()); // Artefact was not claimed
    }

    @Test
    @DisplayName("End turn does NOT throw GameOverException if Artefact was already claimed")
    void endTurnDoesNotThrowGameOverIfArtefactClaimed() {
        WaterMeter wm = BoardGameTestHelper.getWaterMeter(boardGame);
        wm.increaseLevel(); // Level 1, Flood Rate 3

        // Flood both Earth Artefact Zones once (Normal -> Flooded)
        artefactZoneEarth1.floodZone();
        artefactZoneEarth2.floodZone();

        // Manually claim the Earth artefact
        BoardGameTestHelper.getClaimedArtefacts(boardGame).add(Artefact.Earth);
        assertTrue(BoardGameTestHelper.getClaimedArtefacts(boardGame).contains(Artefact.Earth));

        // Put cards for both Earth Artefact Zones in flood draw pile
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(artefactZoneEarth1));
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(artefactZoneEarth2));
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(getZoneCardFromZone(zoneToFlood1)); // Draw 3 total

        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        // End turn - floods both Earth Artefact Zones again
        assertDoesNotThrow(() -> boardGame.endTurn());

        assertEquals(ZoneState.Inaccessible, artefactZoneEarth1.getZone_state()); // Both inaccessible
        assertEquals(ZoneState.Inaccessible, artefactZoneEarth2.getZone_state());
        assertTrue(BoardGameTestHelper.getClaimedArtefacts(boardGame).contains(Artefact.Earth)); // Artefact remains claimed

        // Turn should pass normally
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
        assertEquals(3, BoardGameTestHelper.getCurrentPlayerActionsNum(boardGame));
    }


    // --- End Turn: Win Condition ---

    @Test
    @DisplayName("End turn throws GameWonException if win conditions met")
    void endTurnThrowsGameWonIfWinConditionsMet() {
        setCurrentPlayer(player1, 0); // P1's turn, actions spent
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true); // Simulate treasure phase done

        // 1. All 4 Artefacts claimed
        BoardGameTestHelper.setClaimedArtefacts(boardGame, EnumSet.allOf(Artefact.class));
        assertEquals(4, BoardGameTestHelper.getClaimedArtefacts(boardGame).size());

        // 2. All players on Helicopter Zone
        putPlayersOnZone(helicopterZone, player1, player2); // Only 2 players in this setup
        assertEquals(helicopterZone, player1.getPlayer_zone());
        assertEquals(helicopterZone, player2.getPlayer_zone());

        // 3. At least one player has a Helicopter Lift card
        player1.takeCard(new Card(CardType.HELICOPTER_LIFT));
        assertEquals(1, player1.getHand().getSize());


        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);
        GameWonException exception = assertThrows(GameWonException.class, () -> boardGame.endTurn());
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();
        assertEquals("Congratulations! You have won the game!", exception.getMessage());

        // Game should be over, state might not matter after this
    }

    @Test
    @DisplayName("End turn does NOT throw GameWonException if not all artefacts claimed")
    void endTurnDoesNotThrowWinIfNotAllArtefacts() {
        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        // Claim only 3 artefacts
        BoardGameTestHelper.setClaimedArtefacts(boardGame, EnumSet.of(Artefact.Earth, Artefact.Wind, Artefact.Fire));
        assertEquals(3, BoardGameTestHelper.getClaimedArtefacts(boardGame).size());

        putPlayersOnZone(helicopterZone, player1, player2); // All players on heli
        player1.takeCard(new Card(CardType.HELICOPTER_LIFT)); // Player has lift card

        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);
        assertDoesNotThrow(() -> boardGame.endTurn()); // No win exception
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        // Turn passes normally
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
    }

    @Test
    @DisplayName("End turn does NOT throw GameWonException if not all players on Helicopter Zone")
    void endTurnDoesNotThrowWinIfNotAllPlayersOnHeli() {
        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        // Claim all 4 artefacts
        BoardGameTestHelper.setClaimedArtefacts(boardGame, EnumSet.allOf(Artefact.class));

        // Only P1 on Helicopter Zone, P2 is not
        putPlayersOnZone(helicopterZone, player1);
        putPlayersOnZone(startZone2, player2); // P2 at 0,4

        player1.takeCard(new Card(CardType.HELICOPTER_LIFT)); // Player has lift card


        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.gold_gate);
        assertDoesNotThrow(() -> boardGame.endTurn()); // No win exception
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        // Turn passes normally
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
    }

    @Test
    @DisplayName("End turn does NOT throw GameWonException if no player has Helicopter Lift card")
    void endTurnDoesNotThrowWinIfNoLiftCard() {
        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);

        // Claim all 4 artefacts
        BoardGameTestHelper.setClaimedArtefacts(boardGame, EnumSet.allOf(Artefact.class));

        // All players on Helicopter Zone
        putPlayersOnZone(helicopterZone, player1, player2);

        // No player has a Helicopter Lift card (default state after setup)

        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().add(ZoneCard.bronze_gate);
        assertDoesNotThrow(() -> boardGame.endTurn()); // No win exception
        BoardGameTestHelper.getFloodDeck(boardGame).getDrawCards().clear();

        // Turn passes normally
        assertEquals(1, BoardGameTestHelper.getPlayerTurnId(boardGame));
    }

    // --- End Turn: Other Checks ---

    @Test
    @DisplayName("End turn throws InvalidActionForTheCurrentState if trying to end while RunningFromInaccessibleZone")
    void endTurnThrowsIfRunningFromInaccessible() {
        setCurrentPlayer(player1, 0);
        BoardGameTestHelper.setTreasureDrawnThisTurn(boardGame, true);
        // Simulate being in the running state
        BoardGameTestHelper.setGameState(boardGame, GameState.PlayersRunningFromAnInaccessibleZone);
        BoardGameTestHelper.setPlayersOnInaccessibleZones(boardGame, new ArrayList<>(List.of(player2))); // P2 is stranded


        InvalidActionForTheCurrentState exception = assertThrows(InvalidActionForTheCurrentState.class, () -> boardGame.endTurn());
        assertEquals("You must move the stranded players before ending the turn", exception.getMessage());

        // State should not change, turn should not pass
        assertEquals(GameState.PlayersRunningFromAnInaccessibleZone, BoardGameTestHelper.getGameState(boardGame));
        assertEquals(0, BoardGameTestHelper.getPlayerTurnId(boardGame));
    }
}
