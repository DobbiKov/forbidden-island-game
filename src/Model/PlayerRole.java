package Model;

public enum PlayerRole {
    Pilot, // once per turn can fly to any tile on the board
    Engineer, // can shore up to two tiles per action
    Diver, // can move through flooded tiles
    Messenger, // can give treasure cards from his hand to any player
    Navigator, // can move other players(up to two tiles per action)
    Explorer; // can move and shore up dioganally

    @Override
    public String toString() {
        switch (this) {
            case Pilot: return "Pilot";
            case Engineer: return "Engineer";
            case Diver: return "Diver";
            case Messenger: return "Messenger";
            case Navigator: return "Navigator";
            case Explorer: return "Explorer";
        }
        return "";
    }

    public static PlayerRole getByNum(int num){
        switch (num){
            case 0: return Pilot;
            case 1: return Engineer;
            case 2: return Diver;
            case 3: return Messenger;
            case 4: return Navigator;
            case 5: return Explorer;
            default: return null;
        }
    }
    public PlayerColor getColor(){
        switch (this){
            case Pilot: return PlayerColor.Blue;
            case Navigator: return PlayerColor.Gold;
            case Explorer: return PlayerColor.Copper;
            case Engineer: return PlayerColor.Bronze;
            case Diver: return PlayerColor.Iron;
            case Messenger: return PlayerColor.Silver;
            default: return null;
        }
    }
}
