package View.SwingView;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Model.*; // Assuming Model classes are in this package

/**
 * Utility class for mapping game model elements (like PlayerRole, CardType, Artefact, ZoneCard)
 * to Swing UI resources (Colors, ImageIcons).
 * It also includes an image cache to avoid redundant disk reads and scaling operations.
 */
public class ResourceMapper {
    // ─── IMAGE CACHE ────────────────────────────────────────────────────────────
    private static final Map<String, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>();

    /**
     * Retrieves an ImageIcon from the cache or loads and scales it if not present.
     * Uses a cache key combining path and dimensions to handle different scaled versions.
     *
     * @param path The classpath resource path to the image file.
     * @param w    The desired width for scaling. If <= 0, original width is used.
     * @param h    The desired height for scaling. If <= 0, original height is used.
     * @return The cached or newly loaded (and possibly scaled) ImageIcon. Returns an empty ImageIcon if the resource is not found.
     */
    private static ImageIcon cachedIcon(String path, int w, int h) {
        String key = path + "#" + w + "x" + h;
        return ICON_CACHE.computeIfAbsent(key, k -> {
            URL url = ResourceMapper.class.getResource(path);
            if (url == null) return new ImageIcon();             // fallback
            ImageIcon raw = new ImageIcon(url);
            if (w <= 0 || h <= 0) return raw;                    // no scaling
            Image scaled = raw.getImage().getScaledInstance(
                    w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        });
    }

    // --- Color Mapping ---
    /** Maps PlayerColor enum to AWT Color objects for UI representation. */
    private static final Map<PlayerColor, Color> playerColorMap = new HashMap<>();
    static {
        playerColorMap.put(PlayerColor.Iron, Color.DARK_GRAY);
        playerColorMap.put(PlayerColor.Silver, Color.GRAY);
        playerColorMap.put(PlayerColor.Copper, Color.GREEN);
        playerColorMap.put(PlayerColor.Gold, Color.YELLOW);
        playerColorMap.put(PlayerColor.Blue, Color.BLUE);
        playerColorMap.put(PlayerColor.Bronze, Color.getHSBColor(25, 0.7f, 0.54f)); // Example from original code
    }

    /**
     * Gets the AWT Color associated with a specific PlayerColor.
     * @param playerColor The PlayerColor enum value.
     * @return The corresponding AWT Color, or Color.BLACK if not found.
     */
    public static Color getAwtColor(PlayerColor playerColor) {
        return playerColorMap.getOrDefault(playerColor, Color.BLACK); // Default to black
    }

    /** Maps PlayerRole enum to its corresponding PlayerColor identifier. */
    private static final Map<PlayerRole, PlayerColor> roleToPlayerColorMap = new HashMap<>();
    static {
        roleToPlayerColorMap.put(PlayerRole.Pilot, PlayerColor.Blue);
        roleToPlayerColorMap.put(PlayerRole.Navigator, PlayerColor.Gold);
        roleToPlayerColorMap.put(PlayerRole.Explorer, PlayerColor.Copper);
        roleToPlayerColorMap.put(PlayerRole.Engineer, PlayerColor.Bronze);
        roleToPlayerColorMap.put(PlayerRole.Diver, PlayerColor.Iron);
        roleToPlayerColorMap.put(PlayerRole.Messenger, PlayerColor.Silver);
    }

    /**
     * Gets the AWT Color associated with a specific PlayerRole by first mapping
     * the role to its PlayerColor.
     * @param playerRole The PlayerRole enum value.
     * @return The corresponding AWT Color, or Color.BLACK if the role or its color mapping is not found.
     */
    public static Color getAwtColor(PlayerRole playerRole) {
        PlayerColor pColor = roleToPlayerColorMap.get(playerRole);
        if (pColor != null) {
            return getAwtColor(pColor);
        }
        return Color.BLACK; // Default color
    }


    // --- Image Resource Mapping ---

    /**
     * Safely retrieves a resource URL from the classpath. Prints an error if not found.
     * @param resourcePath The path relative to the classpath root.
     * @return The URL of the resource, or null if not found.
     */
    private static URL getResourceUrl(String resourcePath) {
        URL url = ResourceMapper.class.getResource(resourcePath);
        if (url == null) {
            System.err.println("Resource not found: " + resourcePath);
        }
        return url;
    }

    /** Maps PlayerRole enum to the classpath resource path of its corresponding image. */
    private static final Map<PlayerRole, String> roleImageMap = new HashMap<>();
    static {
        // Paths are relative to the classpath root or the package containing ResourceMapper
        // Assuming images are in src/main/resources/roles_images
        roleImageMap.put(PlayerRole.Pilot, "/roles_images/pilot.png");
        roleImageMap.put(PlayerRole.Engineer, "/roles_images/engineer.png");
        roleImageMap.put(PlayerRole.Diver, "/roles_images/diver.png");
        roleImageMap.put(PlayerRole.Messenger, "/roles_images/messenger.png");
        roleImageMap.put(PlayerRole.Navigator, "/roles_images/navigator.png");
        roleImageMap.put(PlayerRole.Explorer, "/roles_images/explorer.png");
    }

    /** Maps CardType enum to the classpath resource path of its corresponding image. */
    private static final Map<CardType, String> cardImageMap = new HashMap<>();
    static {
        // Assuming images are in src/main/resources/player_cards_images
        cardImageMap.put(CardType.EARTH_CARD, "/player_cards_images/earth_stone_artefact.png");
        cardImageMap.put(CardType.AIR_CARD, "/player_cards_images/statue_of_the_wind_artefact.png");
        cardImageMap.put(CardType.FIRE_CARD, "/player_cards_images/fire_artefact.png");
        cardImageMap.put(CardType.WATER_CARD, "/player_cards_images/oceans_chalice_artefact.png");
        cardImageMap.put(CardType.SANDBAGS, "/player_cards_images/sand_bags.png");
        cardImageMap.put(CardType.HELICOPTER_LIFT, "/player_cards_images/helicopter.png");
        // WATER_RISE cards typically don't have a player hand image
    }


    /** Maps Artefact enum to the classpath resource path of its corresponding image. */
    private static final Map<Artefact, String> artefactImageMap = new HashMap<>();
    static {
        // Assuming images are in src/main/resources/artefacts_images
        artefactImageMap.put(Artefact.Fire, "/artefacts_images/crystal_of_fire.png");
        artefactImageMap.put(Artefact.Water, "/artefacts_images/oceans_chalice.png");
        artefactImageMap.put(Artefact.Wind, "/artefacts_images/statue_of_wind.png");
        artefactImageMap.put(Artefact.Earth, "/artefacts_images/earth_stone.png");
    }

    /** Maps special ZoneTypes (like Helicopter) to overlay image paths. */
    private static final Map<Model.ZoneType, String> zoneOverlayImageMap = new HashMap<>();
    static {
        // Assuming images are in src/main/resources/artefacts_images for artefacts and root for helicopter
        // Note: Helicopter might need a different path if its image isn't generic
        zoneOverlayImageMap.put(Model.ZoneType.Helicopter, "/artefacts_images/helicopter_no_background.png"); // Use the same image path as before
        // Artefact images are handled via the Artefact enum lookup above
    }

    /** Maps ZoneCard enum (representing island tiles) to the classpath resource path of its background image. */
    private static final Map<Model.ZoneCard, String> zoneCardImageMap = new HashMap<>();
    static {
        // Assuming images are in src/main/resources/island_card_images
        // Manually map each ZoneCard enum value to its image file name
        zoneCardImageMap.put(Model.ZoneCard.fodls_landing, "/island_card_images/fodls_landing.png");
        zoneCardImageMap.put(Model.ZoneCard.cliffs_of_abandon, "/island_card_images/cliffs_of_abandon.png");
        // ... add all 24 ZoneCard mappings ...
        zoneCardImageMap.put(Model.ZoneCard.coral_palace, "/island_card_images/coral_palace.png");
        zoneCardImageMap.put(Model.ZoneCard.twilight_hollow, "/island_card_images/twilight_hollow.png");
        zoneCardImageMap.put(Model.ZoneCard.phantom_rock, "/island_card_images/phantom_rock.png");
        zoneCardImageMap.put(Model.ZoneCard.bronze_gate, "/island_card_images/bronze_gate.png");
        zoneCardImageMap.put(Model.ZoneCard.whispering_garden, "/island_card_images/whispering_garden.png");
        zoneCardImageMap.put(Model.ZoneCard.watchtower, "/island_card_images/watchtower.png");
        zoneCardImageMap.put(Model.ZoneCard.gold_gate, "/island_card_images/gold_gate.png");
        zoneCardImageMap.put(Model.ZoneCard.dunes_of_deception, "/island_card_images/dunes_of_deception.png");
        zoneCardImageMap.put(Model.ZoneCard.crimson_forest, "/island_card_images/crimson_forest.png");
        zoneCardImageMap.put(Model.ZoneCard.copper_gate, "/island_card_images/copper_gate.png");
        zoneCardImageMap.put(Model.ZoneCard.iron_gate, "/island_card_images/iron_gate.png");
        zoneCardImageMap.put(Model.ZoneCard.temple_of_the_moon, "/island_card_images/temple_of_the_moon.png");
        zoneCardImageMap.put(Model.ZoneCard.temle_of_the_sun, "/island_card_images/temle_of_the_sun.png");
        zoneCardImageMap.put(Model.ZoneCard.observatory, "/island_card_images/observatory.png");
        zoneCardImageMap.put(Model.ZoneCard.cave_of_embers, "/island_card_images/cave_of_embers.png");
        zoneCardImageMap.put(Model.ZoneCard.lost_lagoon, "/island_card_images/lost_lagoon.png");
        zoneCardImageMap.put(Model.ZoneCard.howling_garden, "/island_card_images/howling_garden.png");
        zoneCardImageMap.put(Model.ZoneCard.silver_gate, "/island_card_images/silver_gate.png");
        zoneCardImageMap.put(Model.ZoneCard.breakers_bridge, "/island_card_images/breakers_bridge.png");
        zoneCardImageMap.put(Model.ZoneCard.cave_of_shadows, "/island_card_images/cave_of_shadows.png");
        zoneCardImageMap.put(Model.ZoneCard.tidal_palace, "/island_card_images/tidal_palace.png");
        zoneCardImageMap.put(Model.ZoneCard.misty_marsh, "/island_card_images/misty_marsh.png");
    }

    /**
     * Public accessor to get a potentially scaled icon using the cache.
     * @param path Classpath resource path.
     * @param w Desired width.
     * @param h Desired height.
     * @return Scaled ImageIcon.
     */
    public static ImageIcon getScaledIcon(String path, int w, int h) {
        return cachedIcon(path, w, h);
    }

    /**
     * Gets the scaled image icon for a specific PlayerRole.
     * @param r The PlayerRole.
     * @param w Desired width.
     * @param h Desired height.
     * @return Scaled ImageIcon for the role.
     */
    public static ImageIcon getRoleImage(PlayerRole r, int w, int h) {
        return cachedIcon(roleImageMap.get(r), w, h);
    }

    /**
     * Gets the scaled image icon for a specific CardType.
     * Returns an empty icon for WATER_RISE as it typically doesn't have a visual representation in hand.
     * @param t The CardType.
     * @param w Desired width.
     * @param h Desired height.
     * @return Scaled ImageIcon for the card type, or an empty icon for WATER_RISE.
     */
    public static ImageIcon getCardImage(CardType t, int w, int h) {
        if (t == CardType.WATER_RISE) return new ImageIcon();
        return cachedIcon(cardImageMap.get(t), w, h);
    }

    /**
     * Gets the scaled image icon for a specific Artefact.
     * @param a The Artefact.
     * @param w Desired width.
     * @param h Desired height.
     * @return Scaled ImageIcon for the artefact.
     */
    public static ImageIcon getArtefactIcon(Artefact a, int w, int h) {
        return cachedIcon(artefactImageMap.get(a), w, h);
    }

    /**
     * Gets the image icon for a specific ZoneCard (island tile background).
     * Returns the image at its original size (scaling parameters -1, -1).
     * @param z The ZoneCard.
     * @return ImageIcon for the zone card at its original size.
     */
    public static ImageIcon getZoneCardImage(ZoneCard z) {
        return cachedIcon(zoneCardImageMap.get(z), -1, -1);      // raw size
    }
}