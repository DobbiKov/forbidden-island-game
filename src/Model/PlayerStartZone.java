package Model;

public class PlayerStartZone extends Zone {
    public PlayerStartZone(int x, int y, Player player){
        super(x, y);
        this.start_for_player = player;
        this.zone_type = ZoneType.PlayerStart;
    }
}
