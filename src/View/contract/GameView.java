package View.contract;

import Controller.GameController;
import Helper.AddPlayerCallback;
import Helper.ChoosablePlayerCallback;
import Model.Artefact;
import Model.Card;
import Model.Player;
import Model.Zone;

import java.util.HashSet;

/**
 * Interface for the View layer of the MVC architecture.
 * Defines the contract between the View and Controller.
 */
public interface GameView {
    /**
     * Update the zone panels in the view
     */
    void updateZonePanels();
    
    /**
     * Update the player panels in the view
     */
    void updatePlayerPanels();
    
    /**
     * Update the display of artefacts in the corner of the game board
     */
    void updateCornerArtefacts();
    
    /**
     * Remove the actions for the current player's panel
     */
    void removeActionsForPlayerPanel();
    
    /**
     * Add a player panel to the view
     * @param player The player to add
     */
    void addPlayerPanel(Player player);
    
    /**
     * Update the view to reflect the game has started
     */
    void startGameHandleView();
    
    /**
     * Make player panels selectable with a callback when clicked
     * @param players The players to make selectable
     * @param callback The callback to call when a player is selected
     */
    void makePlayersChoosable(HashSet<Player> players, ChoosablePlayerCallback callback);
    
    /**
     * Make all player panels unselectable
     */
    void makePlayersUnChoosable();
    
    /**
     * Show an error message to the user
     * @param title The title of the error message
     * @param message The error message
     */
    void showErrorMess(String title, String message);
    
    /**
     * Show an information message to the user
     * @param title The title of the information message
     * @param message The information message
     */
    void showInfoMess(String title, String message);


    /**
     * Initialize the view with a controller
     * @param controller The controller to initialize the view with
     */
    void initialize(GameController controller);

    /**
     * Called when the game is over so the UI can lock down.
     */
    void onGameOver();
}
