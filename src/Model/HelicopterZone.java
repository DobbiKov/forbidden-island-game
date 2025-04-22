package Model;

public class HelicopterZone extends Zone {
    public HelicopterZone(int x, int y) {
        super(x, y, true);
        this.zone_type = ZoneType.Helicopter;
    }
}
