package Model;

import java.awt.*; // AWT Color dependency might be better placed in View or a mapping layer
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the game.
 * Holds information about the player's name, assigned role, color, current location (Zone),
 * remaining actions, hand of cards, and collected artefacts.
 */
public class Player {
    private static int player_count = 0; // Static counter for assigning unique IDs
    private final int player_id;
    private final String player_name;
    private Zone player_zone; // Current zone the player is on
    private int actions_remaining; // Actions left in the current turn
    private PlayerRole player_role; // The special role assigned to the player
    private Hand hand; // The player's hand of cards
    private PlayerColor player_color; // The color associated with the player/pawn
    private List<Artefact> artefacts; // Artefacts collected by the player

    /**
     * Creates a new Player instance.
     * Assigns a unique ID, sets the name and role, initializes actions to 3,
     * determines the player color based on the role, creates an empty hand,
     * and initializes an empty list for artefacts.
     *
     * @param player_name The name chosen for the player.
     * @param player_role The PlayerRole assigned to this player.
     */
    public Player(String player_name, PlayerRole player_role) {
        this.player_id = player_count++;
        this.player_zone = null;
        this.player_name = player_name;
        this.actions_remaining = 3;
        this.player_role = player_role;
        this.player_color = player_role.getColor();
        this.hand = new Hand();
        this.artefacts = new ArrayList<>();
    }
    public PlayerColor getPlayerColor() {
        return this.player_color;
    }

    public void setPlayerToZone(Zone zone){
        if(this.player_zone != null){
            this.player_zone.removePlayerFromZone(this);
        }
        this.player_zone = zone;
        zone.addPlayerToZone(this);
    }

    public PlayerRole getPlayer_role() {
        return player_role;
    }

    public int getPlayer_id() {return player_id;}

    public Zone getPlayer_zone() {return player_zone;}

    public String getPlayer_name() {return player_name;}

    public void move_Player(Zone player_zone) {
        //TODO : check with his role
        if(this.actions_remaining > 0){
            this.actions_remaining--;
        }
        this.player_zone = player_zone;
    }

    public void reset_actions(){
        this.actions_remaining = 3;
    }

    public int getActions_remaining() {return actions_remaining;}

    public Hand getHand(){
        return hand;
    }

    public void takeCard(Card card){
        hand.add(card);
    }

    public static void resetPlayerCount() { // for unit tests
        player_count = 0;
    }

    public List<Artefact> getArtefacts() {
        return artefacts;
    }
    public void addArtefact(Artefact artefact){
        if(artefacts.contains(artefact)){
            throw new IllegalStateException("You already have that artefact, it is not possible");
        }
        artefacts.add(artefact);
    }


}
