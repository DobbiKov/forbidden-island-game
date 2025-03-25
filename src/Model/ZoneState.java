package Model;

public enum ZoneState {
    Normal,
    Flooded,
    Inaccessible;

    @Override
    public String toString() {
        switch (this) {
            case Normal: return "Normal";
            case Flooded: return "Flooded";
            case Inaccessible: return "Inaccessible";
        }
        return "";
    }
}
