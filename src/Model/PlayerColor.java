package Model;

import java.awt.*;

public enum PlayerColor {
    Gold,
    Silver,
    Iron,
    Copper,
    Bronze,
    Blue;

    public static PlayerColor fromInt(int color) {
        switch (color) {
            case 0: return Gold;
            case 1: return Silver;
            case 2: return Iron;
            case 3: return Copper;
            case 4: return Bronze;
            case 5: return Blue;
            default: return Gold;
        }
    }
}
