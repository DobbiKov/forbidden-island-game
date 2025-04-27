package Controller;

import Errors.*;
import View.contract.GameView;

/**
 * A utility class responsible for handling exceptions that might arise
 * from interacting with the game model (BoardGame).
 * It catches specific game-related exceptions and translates them into
 * user-friendly messages displayed via the GameView.
 */
public class modelActionHandler {

    /**
     * Executes a Runnable that interacts with the game model and handles
     * potential custom game exceptions thrown by the model.
     * Each caught exception type triggers a specific message dialog via the GameView.
     * Also handles general game over/won conditions.
     *
     * @param r The Runnable containing the code that might throw game exceptions (e.g., boardGame.movePlayer(...)).
     * @param gameView The GameView instance used to display error or info messages.
     */
    public static void handleModelAction(Runnable r, GameView gameView){
       try{
           r.run();
       }
       // Catch specific game rule violations or state issues
       catch(AllTheCardsAreUsedException ex){
           gameView.showErrorMess("Invalid action", "You have used all the cards!");
       }
       // Catch game ending conditions (lose)
       catch(GameOverException ex){
           gameView.showErrorMess("Game Over", ex.getMessage());
           gameView.onGameOver();
       }
       // Catch game ending conditions (win)
       catch(GameWonException ex){
           gameView.showInfoMess("Congratulations!", "You have won the game!");
           gameView.onGameOver();
       }
       // Catch action invalid for the player's role
       catch(InvalidActionForRole ex){
           gameView.showErrorMess("Invalid action for your role", ex.getMessage());
       }
       // Catch action invalid in the current game phase/state
       catch(InvalidActionForTheCurrentState ex){
           gameView.showErrorMess("Invalid action", ex.getMessage());
       }
       // Catch invalid move target selection
       catch(InvalidMoveForCurrentGameState ex){ // Consider renaming exception for clarity (e.g., InvalidTargetZoneException)
          gameView.showErrorMess("Invalid move", ex.getMessage());
       }
       // Catch issues during setup
       catch(InvalidNumberOfPlayersException ex){
           gameView.showErrorMess("Invalid number of players", ex.getMessage());
       }
       // Catch general logical errors in game state
       catch(InvalidStateOfTheGameException ex){
          gameView.showErrorMess("Invalid state of game", ex.getMessage());
       }
       // Catch invalid zone selection for movement/action
       catch(InvalidZoneToMove ex){
           gameView.showErrorMess("Invalid zone to move", ex.getMessage());
       }
       // Catch condition where the island sinks entirely (flood deck empty)
       catch(IslandFloodedException ex){
           gameView.showErrorMess("Island flooded", ex.getMessage());
       }
       // Catch trying to add too many players
       catch(MaximumNumberOfPlayersReachedException ex){
           gameView.showErrorMess("Maximum number of players reached", "You can't have more than 4 players!");
       }
       // Catch trying to perform an action with no actions left
       catch(NoActionsLeft ex){
          gameView.showErrorMess("Impossible to use an action", "You have no actions left!");
       }
       // Catch trying to start game with no players
       catch(NoPlayersException ex){
           gameView.showErrorMess("Impossible to start", "You don't have enough players!");
       }
       // Catch error if no roles are available during setup
       catch(NoRoleToAssignError ex){
           gameView.showErrorMess("Impossible to assign role", "You have no role to assign!");
       }
       // Catch trying to end turn/draw with too many cards
       catch (TooManyCardsInTheHand ex){
           gameView.showErrorMess("Impossible to continue", ex.getMessage());
       }
       // Catch notification for Water Rise card effect
       catch(WaterRiseException ex){
           gameView.showInfoMess("Attention", "The level of the water is rising!");
       }
       // Catch trying to interact with a sunk zone
       catch(ZoneIsInaccessibleException ex){
          gameView.showErrorMess("Invalid zone", "This zone is not accessible!");
       }
       // Catch any other unexpected exceptions
       catch(Exception e){
            // Consider logging the generic exception e.printStackTrace();
            gameView.showErrorMess("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
       }
    }
}
