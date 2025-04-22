package Model;


import Errors.ZoneIsInaccessibleException;

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

    public Zone(int x, int y){
        this.zone_state = ZoneState.Normal;
        this.zone_name = "";
        this.zone_type = ZoneType.Casual;
        this.players_on_zone = new HashSet<>();
        this.X = x;
        this.Y = y;
    }

    public boolean isAdjecantTo(Zone other){
        return !(Math.abs(this.getX() - other.getX()) > 1 || Math.abs(this.getY() - other.getY()) > 1);
    }
    public boolean isDiagonalTo(Zone other){
        if(!isAdjecantTo(other)){ return false;}
        return Math.abs(this.getX() - other.getX()) != 0 && Math.abs(this.getY() - other.getY()) != 0;
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

    public boolean isDry() {
        return this.zone_state == ZoneState.Normal;
    }
    public boolean isFlooded(){
        return this.zone_state == ZoneState.Flooded;
    }
    public void shoreUp(){
        if(this.zone_state == ZoneState.Inaccessible){
            throw new ZoneIsInaccessibleException();
        }
        this.zone_state = ZoneState.Normal;
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
    public void makeInaccessible(){
        this.zone_state = ZoneState.Inaccessible;
    }
}
