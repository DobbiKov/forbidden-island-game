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

    public String toImgString(){
        switch (this){
            case Fire: return "crystal_of_fire";
            case Water: return "oceans_chalice";
            case Wind: return "statue_of_wind";
            case Earth: return "earth_stone";
            default: return "";
        }
    }
}
