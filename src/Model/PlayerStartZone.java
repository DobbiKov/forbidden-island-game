package Model;

public class PlayerStartZone extends Zone {
    public PlayerStartZone(Zone zone_from, Player player){
        super(zone_from);
        this.start_for_player = player;
        this.zone_type = ZoneType.PlayerStart;
    }
    public PlayerStartZone(int x, int y, Player player){
        super(x, y, true);
        this.start_for_player = player;
        this.zone_type = ZoneType.PlayerStart;
    }
}
