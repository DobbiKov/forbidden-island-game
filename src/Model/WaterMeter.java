package Model;

public class WaterMeter {
    private int level;
    public static int MAX_LEVEL = 10;
    private static final int[] FLOOD_RATE = {2, 3, 3, 4, 4, 5, 5, 6, 6, 6, 6};

    public WaterMeter() {
        this.level = 0;
    }

    public boolean increaseLevel() {
        if(level < MAX_LEVEL) {
            level++;
        }
        return level >= MAX_LEVEL;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentFloodRate() {
        return FLOOD_RATE[level];
    }

    public void resetLevel() {
        level = 0;
    }
}
