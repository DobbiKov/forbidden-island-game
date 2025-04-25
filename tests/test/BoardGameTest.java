package test;

import Model.*;
import Errors.NoPlayersException;
import Errors.InvalidNumberOfPlayersException;
import Errors.MaximumNumberOfPlayersReachedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class BoardGameTest {
    private BoardGame game;

    @BeforeEach
    void init() {
        game = new BoardGame();
        Player.resetPlayerCount();
    }

    @Test
    void testPlayerCountLimits() {
        assertDoesNotThrow(() -> {
            game.addPlayer("A");
            game.addPlayer("B");
            game.addPlayer("C");
            game.addPlayer("D");
        });
        assertThrows(MaximumNumberOfPlayersReachedException.class,
                () -> game.addPlayer("E"));
    }

    @Test
    void testStartGameValidations() {
        assertThrows(NoPlayersException.class, game::startGame);
        game.addPlayer("Yehor");
        assertThrows(InvalidNumberOfPlayersException.class, game::startGame);
    }

    @Test
    void testTurnOrderAndActions() {
        game.addPlayer("Yehor");
        game.addPlayer("Ivan");
        game.startGame();
        Player first = game.getPlayerForTheTurn();
        assertEquals(3, game.getCurrent_player_actions_num());
        game.nextPlayerTurn();
        assertNotEquals(first, game.getPlayerForTheTurn());
    }

    @Test
    void testFinDeTourFloodsViaDeck() {
        game.addPlayer("Yehor");
        game.addPlayer("Ivan");
        game.startGame();
        int beforeDraw = game.getFloodDeck().getDrawSize();
        int beforeDiscard = game.getFloodDeck().getDiscardSize();
        game.finDeTour();
        assertEquals(beforeDraw - 2, game.getFloodDeck().getDrawSize());
        assertEquals(beforeDiscard + 2, game.getFloodDeck().getDiscardSize());
    }

    @Test
    void explorerCanMoveDiagonally() {
        Player explorer = new Player("Anton", PlayerRole.Explorer);
        Zone center = game.getZone(1, 1);
        explorer.setPlayerToZone(center);
        var moves = game.getZonesForPlayerToMove(explorer);
        assertTrue(moves.stream().anyMatch(z -> z.getX() == 0 && z.getY() == 0),
                "Explorer should move diagonally");
    }

    @Test
    void testInitialDeal() {
        game.addPlayer("Yehor"); game.addPlayer("Prikol");
        game.startGame();
        for (Player p : game.getPlayers()) {
            if (p == null) continue;
            assertEquals(2, p.getHand().getSize(),
                    "Players should start with 2 treasure cards");
        }
    }
}
