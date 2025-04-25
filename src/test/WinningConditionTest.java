package test;

import Model.BoardGame;
import Model.Zone;
import Model.ZoneCard;
import Model.Player;
import Model.Card;
import Model.CardType;
import Model.Artefact;
import Errors.GameWonException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class WinningConditionTest {
    private BoardGame game;

    @BeforeEach
    void setUp() {
        game = new BoardGame();
        game.addPlayer("Yehor");
        game.addPlayer("Ivan");
    }

    private void claimAllArtefacts() throws Exception {
        Field claimed = BoardGame.class.getDeclaredField("claimedArtefacts");
        claimed.setAccessible(true);
        EnumSet<Artefact> all = EnumSet.allOf(Artefact.class);
        claimed.set(game, all);
    }


    private void placeAllPlayersOnHeli() {
        Zone heli = game.getZoneByCard(ZoneCard.fodls_landing);
        for (Player p : game.getPlayers()) {
            if (p != null) {
                p.setPlayerToZone(heli);
            }
        }
    }

    private void giveHelicopterCardToPlayer(int idx) {
        Player p = game.getPlayers()[idx];
        p.takeCard(new Card(CardType.HELICOPTER_LIFT));
    }


    private void invokeCheckWin() throws Exception {
        Method checkWin = BoardGame.class.getDeclaredMethod("checkWin");
        checkWin.setAccessible(true);
        checkWin.invoke(game);
    }

    @Test
    void testWinConditionThrowsWhenAllConditionsMet() throws Exception {
        claimAllArtefacts();
        placeAllPlayersOnHeli();
        giveHelicopterCardToPlayer(0);

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                this::invokeCheckWin
        );
        assertInstanceOf(GameWonException.class, ex.getCause(), "Expected GameWonException as the cause");
    }

    @Test
    void testNoWinIfMissingArtefact() throws Exception {
        Field claimed = BoardGame.class.getDeclaredField("claimedArtefacts");
        claimed.setAccessible(true);
        EnumSet<Artefact> partial = EnumSet.of(
                Artefact.Fire, Artefact.Water, Artefact.Wind
        );
        claimed.set(game, partial);

        placeAllPlayersOnHeli();
        giveHelicopterCardToPlayer(0);

        assertDoesNotThrow(this::invokeCheckWin,
                "Should not throw when not all artefacts are claimed"
        );
    }

    @Test
    void testNoWinIfPlayersNotOnHeli() throws Exception {
        claimAllArtefacts();
        giveHelicopterCardToPlayer(0);

        assertDoesNotThrow(this::invokeCheckWin,
                "Should not throw when players are not all on heli pad"
        );
    }

    @Test
    void testNoWinIfNoHelicopterCard() throws Exception {
        claimAllArtefacts();
        placeAllPlayersOnHeli();

        assertDoesNotThrow(this::invokeCheckWin,
                "Should not throw when no helicopter lift card is available"
        );
    }
}
