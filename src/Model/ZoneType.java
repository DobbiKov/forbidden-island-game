package Model;

public enum ZoneType{
    Casual,
    Helicopter,
    PlayerStart,
    ArtefactAssociated;

    @Override
    public String toString() {
        switch (this){
            case ArtefactAssociated: return "ArtefactAssociated";
            case Casual: return "Casual";
            case Helicopter: return "Helicopter";
            case PlayerStart: return "PlayerStart";
        }
        return "";
    }
}
