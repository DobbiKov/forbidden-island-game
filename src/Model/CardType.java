package Model;

public enum CardType {
    EARTH_CARD, AIR_CARD, FIRE_CARD, WATER_CARD,
    HELICOPTER_LIFT, SANDBAGS, WATER_RISE;

    public boolean isTreasure(){
        switch (this){
            case EARTH_CARD:
            case AIR_CARD:
            case FIRE_CARD:
            case WATER_CARD:
                return true;
            default:
                return false;
        }
    }

    public String getImgString(){
        switch (this){
            case EARTH_CARD: return "earth_stone_artefact";
            case AIR_CARD: return "statue_of_the_wind_artefact";
            case FIRE_CARD: return "fire_artefact";
            case WATER_CARD: return "oceans_chalice_artefact";
            case SANDBAGS: return "sand_bags";
            case HELICOPTER_LIFT: return "helicopter";
            default: return "";
        }
    }

    public boolean isAction(){
        return this == HELICOPTER_LIFT || this == SANDBAGS;
    }

    public boolean isWaterRise(){
        return this == WATER_RISE;
    }
}
