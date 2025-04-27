package Controller;

import Errors.*;
import View.contract.GameView;

public class modelActionHandler {
    public static void handleModelAction(Runnable r, GameView gameView){
       try{
           r.run();
       }
       catch(AllTheCardsAreUsedException ex){
           gameView.showErrorMess("Invalid action", "You have used all the cards!");
       }
       catch(GameOverException ex){
           gameView.showErrorMess("Game Over", ex.getMessage());
           gameView.onGameOver();
       }
       catch(GameWonException ex){
           gameView.showInfoMess("Congratulations!", "You have won the game!");
       }
       catch(InvalidActionForRole ex){
           gameView.showErrorMess("Invalid action for your role", ex.getMessage());
       }
       catch(InvalidActionForTheCurrentState ex){
           gameView.showErrorMess("Invalid action", ex.getMessage());
       }
       catch(InvalidMoveForCurrentGameState ex){
          gameView.showErrorMess("Invalid move", ex.getMessage());
       }
       catch(InvalidNumberOfPlayersException ex){
           gameView.showErrorMess("Invalid number of players", ex.getMessage());
       }
       catch(InvalidStateOfTheGameException ex){
          gameView.showErrorMess("Invalid state of game", ex.getMessage());
       }
       catch(InvalidZoneToMove ex){
           gameView.showErrorMess("Invalid zone to move", ex.getMessage());
       }
       catch(IslandFloodedException ex){
           gameView.showErrorMess("Island flooded", ex.getMessage());
       }
       catch(MaximumNumberOfPlayersReachedException ex){
           gameView.showErrorMess("Maximum number of players reached", "You can't have more than 4 players!");
       }
       catch(NoActionsLeft ex){
          gameView.showErrorMess("Impossible to use an action", "You have no actions left!");
       }
       catch(NoPlayersException ex){
           gameView.showErrorMess("Impossible to start", "You don't have enough players!");
       }
       catch(NoRoleToAssignError ex){
           gameView.showErrorMess("Impossible to assign role", "You have no role to assign!");
       }
       catch (TooManyCardsInTheHand ex){
           gameView.showErrorMess("Impossible to continue", ex.getMessage());
       }
       catch(WaterRiseException ex){
           gameView.showInfoMess("Attention", "The level of the water is rising!");
       }
       catch(ZoneIsInaccessibleException ex){
          gameView.showErrorMess("Invalid zone", "This zone is not accessible!");
       }
       catch(Exception e){

       }
    }
}
