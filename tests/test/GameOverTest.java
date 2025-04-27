package test;

import Errors.WaterRiseException;
import Model.*;
import Errors.GameOverException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameOverTest {

    private BoardGame game;

    @BeforeEach
    void setUp() {
        game = new BoardGame();
        game.addPlayer("Yehor");
        game.addPlayer("Ivan");
        game.startGame();
    }

    @Test
    void helicopterZoneSunk_throwsGameOver() {
        Zone heli = game.getZoneByCard(ZoneCard.fodls_landing);
        heli.floodZone();
        heli.floodZone();

        assertThrows(GameOverException.class, () -> game.endTurn(), "Expected GameOverException when helicopter site sinks");
    }

    @Test
    void artefactLost_throwsGameOverOnlyWhenBothZonesSink() {

        List<ArtefactZone> fireZones = new ArrayList<>();
        for (Zone[] row : game.getBoard()) {
            for (Zone z : row) {
                if (z instanceof ArtefactZone) {
                    ArtefactZone az = (ArtefactZone) z;
                    if (az.getArtefact() == Artefact.Fire) {
                        fireZones.add(az);
                    }
                }
            }
        }
        assertEquals(2, fireZones.size(), "There should be exactly 2 Fire artefact zones");

        ArtefactZone zone1 = fireZones.get(0);
        ArtefactZone zone2 = fireZones.get(1);

        zone1.floodZone();
        zone1.floodZone();

        try {
            game.endTurn();
        } catch (GameOverException goe) {
            fail("GameOver should NOT be thrown when only one artefact zone is lost");
        }catch(WaterRiseException ignore){} // the water rise exception sometimes trigers failing

        zone2.floodZone();
        zone2.floodZone();

        assertThrows(GameOverException.class, () -> game.endTurn(),
                "Expected GameOverException when both Fire artefact zones are gone");
    }


    @Test
    void testPlayerDeath_StrandedWithEscape_doesNotThrowPlayerDeath() throws Exception {
        // place in the (0,0)
        Zone corner = game.getZone(0, 0);
        game.getPlayers()[0].setPlayerToZone(corner);

        // flood so there is an escape
        Zone east = game.getZone(1, 0);
        east.floodZone();
        east.floodZone();

        // skip the draw treasure part, so there are no problems
        Field treasureFlag = BoardGame.class
                .getDeclaredField("treasureDrawnThisTurn");
        treasureFlag.setAccessible(true);
        treasureFlag.set(game, true);

        // force to draw only corner
        Field floodField = BoardGame.class
                .getDeclaredField("floodDeck");
        floodField.setAccessible(true);
        FloodDeck deck = (FloodDeck) floodField.get(game);

        Field drawField = Deck.class
                .getDeclaredField("drawCards");
        drawField.setAccessible(true);
        drawField.set(deck, new ArrayList<>(
                Collections.singletonList(corner.getZoneCard())
        ));

        try {
            game.endTurn();
        } catch (GameOverException ex) {
            String msg = ex.getMessage().toLowerCase();
            if (msg.contains("you have lost the player")) {
                fail("Unexpected player‐death: " + ex.getMessage());
            }
        }
    }





}
