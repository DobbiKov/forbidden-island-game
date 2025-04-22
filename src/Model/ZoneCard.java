package Model;

public enum ZoneCard {
    fodls_landing,
    cliffs_of_abandon,
    coral_palace,
    twilight_hollow,
    phantom_rock,
    bronze_gate,
    whispering_garden,
    watchtower,
    gold_gate,
    dunes_of_deception,
    crimson_forest,
    copper_gate,
    iron_gate,
    temple_of_the_moon,
    observatory,
    cave_of_embers,
    lost_lagoon,
    howling_garden,
    temle_of_the_sun,
    silver_gate,
    breakers_bridge,
    cave_of_shadows,
    tidal_palace,
    misty_marsh;

    @Override
    public String toString() {
        return this.name();
    }

    public static ZoneCard fromInt(int index) {
        return ZoneCard.values()[index];
    }
}
