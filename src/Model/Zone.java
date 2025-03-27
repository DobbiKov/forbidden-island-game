package Model;


import java.util.ArrayList;
import java.util.HashSet;

public class Zone {
    protected ZoneState zone_state;
    protected String zone_name;
    protected ZoneType zone_type;
    protected Player start_for_player;
    protected Artefact associated_artefact;
    protected HashSet<Player> players_on_zone;
    protected int X;
    protected int Y;

    // TODO: players on the zone
    public Zone(int x, int y){
        this.zone_state = ZoneState.Normal;
        this.zone_name = "";
        this.zone_type = ZoneType.Casual;
    }

    public int getX(){
        return X;
    }
    public int getY(){
        return Y;
    }


    public void floodZone(){
        switch (zone_state){
            case Normal: {
                this.zone_state = ZoneState.Flooded;
                break;
            }

            case Flooded: {
                this.zone_state = ZoneState.Inaccessible;
                break;
            }
            default: break;
        }
    }

    public ZoneState getZone_state() {
        return zone_state;
    }

    public String toString() {
        String output = "";
        output += "Zone Name: " + this.zone_name + "\n";
        output += "Zone Type: " + zone_type + "\n";
        output += "Zone State: " + zone_state + "\n";
        return output;
    }

    public ZoneType getZone_type() {
        return zone_type;
    }
    public void addPlayerToZone(Player player){
        if(zone_state.equals(ZoneState.Inaccessible)){
            throw new ZoneIsInaccessibleException();
        }
        if(players_on_zone.contains(player)){
            throw new RuntimeException("The player is already on the zone");
        }
        this.players_on_zone.add(player);
    }
    public HashSet<Player> getPlayers_on_zone() {
        return players_on_zone;
    }
    public void removePlayerFromZone(Player player){
        if(!players_on_zone.contains(player)){
            throw new RuntimeException("Player is not on the zone");
        }
        this.players_on_zone.remove(player);
    }
}
