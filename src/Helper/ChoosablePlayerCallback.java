package Helper;

import Model.Player;

@FunctionalInterface
public interface ChoosablePlayerCallback {
    void choose(Player player);
}
