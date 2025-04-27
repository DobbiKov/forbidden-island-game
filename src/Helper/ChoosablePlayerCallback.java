package Helper;

import Model.Player;

/**
 * A functional interface defining a callback method used when a player
 * needs to be selected from a list or group (e.g., by the Navigator).
 */
@FunctionalInterface
public interface ChoosablePlayerCallback {
    /**
     * Method called when a specific player is chosen.
     * @param player The Player object that was selected.
     */
    void choose(Player player);
}
