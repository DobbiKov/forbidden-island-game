package Model;

public enum GameState {
    SettingUp,
    Playing,
    PlayerChooseWhereToMove, // when a player is choosing a zone to move
    PlayerChooseWhereToShoreUp, // when a player is choosing a zone to shore up
    PilotChooseWhereToFly, // when a pilot is choosing where to fly
}
