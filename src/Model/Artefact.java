package Model;

public enum Artefact {
    Fire,
    Water,
    Wind,
    Earth;

    @Override
    public String toString() {
        switch (this) {
            case Fire: return "Fire";
            case Water: return "Water";
            case Wind: return "Wind";
            case Earth: return "Earth";
            default: return "";
        }
    }
}
