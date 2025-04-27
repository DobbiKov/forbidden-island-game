package Model;

import Errors.InvalidStateOfTheGameException;

import java.security.InvalidParameterException;

/**
 * Represents a special type of Zone where a player starts the game.
 * Each PlayerStartZone is associated with a specific PlayerColor, determined by its ZoneCard.
 * It can also hold a reference to the Player object that started there.
 */
public class PlayerStartZone extends Zone {
    private PlayerColor card_player_color;

    /**
     * Constructor used internally, likely for creating a start zone based on an existing zone
     * and immediately associating a player.
     * @param zone_from The base Zone object.
     * @param player The Player starting on this zone.
     */
    public PlayerStartZone(Zone zone_from, Player player){
        super(zone_from);
        this.start_for_player = player;
        this.zone_type = ZoneType.PlayerStart;
    }

    /**
     * Creates a PlayerStartZone at specific coordinates with a given ZoneCard.
     * Determines the associated PlayerColor based on the ZoneCard.
     * The associated player is initially null.
     *
     * @param x The x-coordinate on the board.
     * @param y The y-coordinate on the board.
     * @param card The ZoneCard representing this starting location (e.g., gold_gate).
     * @throws InvalidParameterException if the provided ZoneCard is not a valid starting location card.
     */
    public PlayerStartZone(int x, int y, ZoneCard card) {
        super(x, y, true, card);
        this.start_for_player = null;
        this.zone_type = ZoneType.PlayerStart;
        switch(card){
            case gold_gate: this.card_player_color = PlayerColor.Gold; break;
            case silver_gate: this.card_player_color = PlayerColor.Silver; break;
            case bronze_gate: this.card_player_color = PlayerColor.Bronze; break;
            case copper_gate: this.card_player_color = PlayerColor.Copper; break;
            case iron_gate: this.card_player_color = PlayerColor.Iron; break;
            case fodls_landing: this.card_player_color = PlayerColor.Blue; break;
            default: throw new InvalidParameterException("Invalid card given!");
        }
    }

    /**
     * Gets the PlayerColor associated with this starting zone, derived from its ZoneCard.
     * @return The PlayerColor.
     */
    public PlayerColor getCard_player_color() {
        return card_player_color;
    }

//    public PlayerStartZone(int x, int y, Player player, ZoneCard card){
//        super(x, y, true, card);
//        this.start_for_player = player;
//        this.zone_type = ZoneType.PlayerStart;
//    }

    /**
     * Associates a Player object with this starting zone.
     * This is typically done during game setup when assigning players to their starting positions.
     *
     * @param player The Player to associate with this zone.
     * @throws IllegalArgumentException if the player is null.
     * @throws InvalidStateOfTheGameException if a player is already associated with this zone.
     */
    public void associatePlayer(Player player){
        if(player == null){
            throw new IllegalArgumentException("Player cannot be null");
        }
        if(this.isAssociatedToAPlayer()){
            throw new InvalidStateOfTheGameException("Player already associated to a player");
        }
        this.start_for_player = player;
    }

    /**
     * Gets the Player object associated with this starting zone.
     * @return The associated Player, or null if no player has been associated yet.
     */
    public Player getAssociatedPlayer(){
        return this.start_for_player;
    }

    /**
     * Checks if a player has been associated with this starting zone.
     * @return true if a player is associated, false otherwise.
     */
    public boolean isAssociatedToAPlayer(){
        return getAssociatedPlayer() != null;
    }
}
