package test;

import Errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Model.*;

import java.util.ArrayList;


class BoardGameTest {
    private BoardGame game;

    @BeforeEach
    void setUp() {
        game = new BoardGame();
        Player.resetPlayerCount();
        Zone.resetUsedZoneCards();
    }

    @Test
    void testAddUpToFourPlayersSucceeds() {
        game.addPlayer("A");
        game.addPlayer("B");
        game.addPlayer("C");
        game.addPlayer("D");
    }

    @Test
    void testAddingFifthPlayerFails() {
        game.addPlayer("A");
        game.addPlayer("B");
        game.addPlayer("C");
        game.addPlayer("D");
        assertThrows(Exception.class,
                () -> game.addPlayer("E"));
    }

    @Test
    void testStartGameFailsWhenNoPlayers() {
        assertThrows(NoPlayersException.class,
                game::startGame);
    }

    @Test
    void testStartGameFailsWithOnlyOnePlayer() {
        game.addPlayer("Solo");
        assertThrows(InvalidNumberOfPlayersException.class,
                game::startGame);
    }

    @Test
    void testTurnOrderAndActions() {
        game.addPlayer("P1"); game.addPlayer("P2");
        game.startGame();
        Player first = game.getPlayerForTheTurn();
        assertEquals(3, game.getCurrent_player_actions_num());
        game.nextPlayerTurn();
        assertNotEquals(first, game.getPlayerForTheTurn());
    }

    @Test
    void testFloodingEndOfTurn() {
        game.addPlayer("X"); game.addPlayer("Y");
        game.startGame();
        // reduce board to <3 tiles
        int total = game.getNumOfActiveZones();
        for (int i = 0; i < total - 2; i++) {
            game.floodAllZones();
        }
        game.finDeTour();
        game.finDeTour();
        assertTrue(game.getNumOfActiveZones() == 0);
    }

    @Test
    void explorerCanMoveDiagonally() {
        Player explorer = new Player("Eve", PlayerRole.Explorer);
        // place her at (1,1) on the board
        Zone center = game.getZone(1, 1);
        explorer.setPlayerToZone(center);

        ArrayList<Zone> moves = game.getZonesForPlayerToMove(explorer);
        // (0,0) is diagonal from (1,1)
        assertTrue(moves.stream().anyMatch(z -> z.getX() == 0 && z.getY() == 0),
                "Explorer should be able to move diagonally to (0,0)");
    }

    @Test
    void testInitialDealTwoCardsPerPlayer() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");

        game.startGame();
        for (Player p : game.getPlayers()) {
            if (p == null) continue;
            assertEquals(
                    2,
                    p.getHand().getSize(),
                    "Player " + p.getPlayer_name() + " should have 2 cards at game start"
            );
        }
    }

    @Test
    void testDrawTwoCardsAtEndOfTurn() {
        game.addPlayer("Yehor");
        game.addPlayer("Zalupa");
        game.startGame();

        Player first = game.getPlayerForTheTurn();
        assertEquals(2, first.getHand().getSize(),
                "Should have been dealt 2 cards at game start");

        game.finDeTour();

        assertEquals(4, first.getHand().getSize(),
                "After ending turn, player should draw two more cards");

        Player second = game.getPlayerForTheTurn();
        assertEquals(2, second.getHand().getSize(),
                "Second player starts with only the 2 initial cards until their turn ends");
        game.finDeTour();
        assertEquals(4, second.getHand().getSize());
        game.finDeTour();
        assertEquals(6, first.getHand().getSize());
    }


}

