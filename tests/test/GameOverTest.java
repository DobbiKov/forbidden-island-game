package test;

import Model.*;
import Errors.GameOverException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
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
    void playerDead_throwsGameOver() {
        Player player = game.getPlayers()[0];
        Zone startZone = player.getPlayer_zone();
        startZone.floodZone();
        startZone.floodZone();

        assertThrows(GameOverException.class, () -> game.endTurn(), "Expected GameOverException when a player is stranded");
    }

}
