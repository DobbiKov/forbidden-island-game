package Model;

public class HelicopterZone extends PlayerStartZone {
    public HelicopterZone(int x, int y) {
        super(x, y, ZoneCard.fodls_landing);
        this.zone_type = ZoneType.Helicopter;
    }
}
