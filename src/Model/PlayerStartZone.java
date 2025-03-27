package Model;

public class PlayerStartZone extends Zone {
    public PlayerStartZone(Player player){
        super();
        this.start_for_player = player;
        this.zone_type = ZoneType.PlayerStart;
    }
}
