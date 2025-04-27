package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import Model.*;
class WaterMeterTest {

    @Test
    @DisplayName("Initial level should be 0")
    void initialLevelIsZero() {
        WaterMeter meter = new WaterMeter();
        assertEquals(0, meter.getLevel());
    }

    @Test
    @DisplayName("Increase level should increase level")
    void increaseLevelIncreasesLevel() {
        WaterMeter meter = new WaterMeter();
        meter.increaseLevel();
        assertEquals(1, meter.getLevel());
    }

    @Test
    @DisplayName("Increase level should return false if not at max")
    void increaseLevelReturnsFalseIfNotMax() {
        WaterMeter meter = new WaterMeter();
        assertFalse(meter.increaseLevel()); // level 1
        assertFalse(meter.increaseLevel()); // level 2
        assertFalse(meter.increaseLevel()); // level 3
        assertFalse(meter.increaseLevel()); // level 4
        assertFalse(meter.increaseLevel()); // level 5
        assertFalse(meter.increaseLevel()); // level 6
        assertFalse(meter.increaseLevel()); // level 7
        assertFalse(meter.increaseLevel()); // level 8
        assertFalse(meter.increaseLevel()); // level 9
    }

    @Test
    @DisplayName("Increase level should return true if reaches max")
    void increaseLevelReturnsTrueIfReachesMax() {
        WaterMeter meter = new WaterMeter();
        for (int i = 0; i < WaterMeter.MAX_LEVEL - 1; i++) {
            meter.increaseLevel(); // reach level MAX_LEVEL - 1 (9)
        }
        assertTrue(meter.increaseLevel()); // reaches level MAX_LEVEL (10)
    }

    @Test
    @DisplayName("Level should not exceed max")
    void levelDoesNotExceedMax() {
        WaterMeter meter = new WaterMeter();
        for (int i = 0; i < WaterMeter.MAX_LEVEL + 5; i++) { // Increase past max
            meter.increaseLevel();
        }
        assertEquals(WaterMeter.MAX_LEVEL, meter.getLevel());
    }

    @Test
    @DisplayName("GetCurrentFloodRate should return correct rate based on level")
    void getCurrentFloodRateReturnsCorrectRate() {
        WaterMeter meter = new WaterMeter(); // level 0
        assertEquals(2, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 1
        assertEquals(3, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 2
        assertEquals(3, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 3
        assertEquals(4, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 4
        assertEquals(4, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 5
        assertEquals(5, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 6
        assertEquals(5, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 7
        assertEquals(6, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 8
        assertEquals(6, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 9
        assertEquals(6, meter.getCurrentFloodRate());

        meter.increaseLevel(); // level 10 (MAX_LEVEL)
        assertEquals(6, meter.getCurrentFloodRate());
    }

    @Test
    @DisplayName("Reset level should set level back to 0")
    void resetLevelSetsLevelToZero() {
        WaterMeter meter = new WaterMeter();
        meter.increaseLevel();
        meter.increaseLevel();
        assertEquals(2, meter.getLevel());
        meter.resetLevel();
        assertEquals(0, meter.getLevel());
    }
}
