package Helper;

/**
 * Interface defining callback methods for the Add Player dialog.
 * Allows the dialog to communicate back to its owner (e.g., the main GUI)
 * when a player name is submitted or the dialog is cancelled/hidden.
 */
public interface AddPlayerCallback {
    /**
     * Called when the dialog should be hidden (e.g., Cancel button clicked).
     */
    void callHide(); // Consider renaming to onCancel or onDismiss for clarity

    /**
     * Called when the "Add" button is clicked and a valid name is entered.
     * @param name The name entered by the user for the new player.
     */
    void callAddPlayer(String name);
}
