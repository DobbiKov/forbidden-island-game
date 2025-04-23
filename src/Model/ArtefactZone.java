package Model;



public class ArtefactZone extends Zone {
    public ArtefactZone(int x, int y, ZoneCard card, Artefact artefact) {
        super(x, y, true, card);

        this.associated_artefact = artefact;
        this.zone_state = ZoneState.Normal;
        this.zone_name = "";
        this.zone_type = ZoneType.ArtefactAssociated;
    }
}
