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

    public boolean isAction(){
        return this == HELICOPTER_LIFT || this == SANDBAGS;
    }

    public boolean isWaterRise(){
        return this == WATER_RISE;
    }
}
