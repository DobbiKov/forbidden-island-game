package Model;



public class ArtefactZone extends Zone {
    public ArtefactZone(int x, int y, Artefact artefact) {
        super(x, y);

        this.associated_artefact = artefact;
        this.zone_state = ZoneState.Normal;
        this.zone_name = "";
        this.zone_type = ZoneType.ArtefactAssociated;
    }
}
