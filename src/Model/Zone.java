package Model;




public class Zone {
    private ZoneState zone_state;
    private String zone_name;
    private ZoneType zone_type;
    // TODO: players on the zone
    public Zone(){
        this.zone_state = ZoneState.Normal;
        this.zone_name = "";
        this.zone_type = ZoneType.Casual;
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
}
