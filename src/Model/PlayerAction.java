package Model;

public enum PlayerAction {
    Move,
    Drain,
    GiveTreasureCard,
    FlyToACard,
    MovePlayer,
    FinishTurn;

    @Override
    public String toString() {
        switch (this) {
            case Move: return "move";
            case Drain: return "drain";
            case GiveTreasureCard: return "give treasure card";
            case FlyToACard: return "fly to card";
            case MovePlayer: return "move player";
            case FinishTurn: return "finish turn";
        }
        return "";
    }
}
