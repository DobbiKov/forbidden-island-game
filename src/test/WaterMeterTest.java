package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import Model.*;

class WaterMeterTest {

    private WaterMeter meter;

    @BeforeEach
    void setUp() {
        meter = new WaterMeter();
    }

    @Test
    void initialLevelIsZero() {
        assertEquals(0, meter.getLevel());
        assertEquals(2, meter.getCurrentFloodRate());
    }

    @Test
    void increaseLevelsUpToMax() {
        for (int i = 1; i <= WaterMeter.MAX_LEVEL; i++) {
            boolean gameOver = meter.increaseLevel();
            assertEquals(i, meter.getLevel());
            if (i < WaterMeter.MAX_LEVEL) {
                assertFalse(gameOver);
            } else {
                assertTrue(gameOver, "Should trigger game-over at max level");
            }
        }
    }

    @Test
    void floodRateMatchesLevel() {
        meter.resetLevel();
        // levels 0..10 should map into FLOOD_RATE table
        int[] expected = {2,3,3,4,4,5,5,6,6,6,6};
        for (int lvl = 0; lvl < expected.length; lvl++) {
            while (meter.getLevel() < lvl) meter.increaseLevel();
            assertEquals(expected[lvl], meter.getCurrentFloodRate(), "Flood rate wrong at level=" + lvl);
        }
    }
}