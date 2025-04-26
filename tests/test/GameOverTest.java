package test;

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
        }

        zone2.floodZone();
        zone2.floodZone();

        assertThrows(GameOverException.class, () -> game.endTurn(),
                "Expected GameOverException when both Fire artefact zones are gone");
    }

    @Test
    void testPlayerDeath_StrandedWithNoEscape_throwsGameOver() throws Exception {
        // Set up a 2-player game and start it
        BoardGame game = new BoardGame();
        game.addPlayer("Yehor");
        game.addPlayer("Ivan");
        game.startGame();

        Zone corner = game.getZone(0, 0);
        game.getPlayers()[0].setPlayerToZone(corner);

        // Make both adjacent zones inaccessible so there's nowhere to run
        Zone east  = game.getZone(1, 0);
        Zone south = game.getZone(0, 1);
        east.floodZone(); east.floodZone();
        south.floodZone(); south.floodZone();

        // Skip the random treasure‐draw phase
        Field treasureFlag = BoardGame.class.getDeclaredField("treasureDrawnThisTurn");
        treasureFlag.setAccessible(true);
        treasureFlag.set(game, true);

        // Force the flood phase to draw only the corner card (so it sinks)
        Field floodField = BoardGame.class.getDeclaredField("floodDeck");
        floodField.setAccessible(true);
        FloodDeck floodDeck = (FloodDeck) floodField.get(game);

        Field drawField = FloodDeck.class.getSuperclass()
                .getDeclaredField("drawCards");
        drawField.setAccessible(true);
        drawField.set(floodDeck, new ArrayList<>(Collections.singletonList(corner.getZoneCard())));

        assertThrows(GameOverException.class, game::endTurn,
                "Expected GameOverException when a stranded player has no adjacent escape route");
    }

    @Test
    void testPlayerDeath_StrandedWithEscape_doesNotThrow() throws Exception {
        // Set up and start as before
        BoardGame game = new BoardGame();
        game.addPlayer("Yehor");
        game.addPlayer("Ivan");
        game.startGame();

        // Move player 0 onto the corner zone (0,0)
        Zone corner = game.getZone(0, 0);
        game.getPlayers()[0].setPlayerToZone(corner);

        //Make only one adjacent zone inaccessible, leave the other free
        Zone east  = game.getZone(1, 0);
        east.floodZone(); east.floodZone();


        //Skip treasure draw
        Field treasureFlag = BoardGame.class.getDeclaredField("treasureDrawnThisTurn");
        treasureFlag.setAccessible(true);
        treasureFlag.set(game, true);

        //flood deck to sink only the corner tile
        Field floodField = BoardGame.class.getDeclaredField("floodDeck");
        floodField.setAccessible(true);
        FloodDeck floodDeck = (FloodDeck) floodField.get(game);

        Field drawField = FloodDeck.class.getSuperclass()
                .getDeclaredField("drawCards");
        drawField.setAccessible(true);
        drawField.set(floodDeck, new ArrayList<>(Collections.singletonList(corner.getZoneCard())));

        assertDoesNotThrow(game::endTurn,
                "Should not throw when a stranded player still has at least one adjacent accessible zone");
    }




}
