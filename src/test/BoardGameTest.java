package test;

import Errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Model.*;

import java.lang.reflect.Field;

class BoardGameTest {
    private BoardGame game;

    @BeforeEach
    void setUp() {
        game = new BoardGame();
    }
    @BeforeEach
    void resetStaticPlayerCount() throws ReflectiveOperationException { // resets final number of players each time
        Field f = Player.class.getDeclaredField("player_count");
        f.setAccessible(true);
        f.setInt(null, 0);
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
        assertThrows(MaximumNumberOfPlayersReachedException.class,
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
        assertTrue(game.getNumOfActiveZones() <= (int)Math.pow(game.getSize(),2));
    }
}

