package Model;

import java.awt.*;

public class Player {
    private static int player_count = 0;
    private final int player_id;
    private final String player_name;
    private Zone player_zone;
    private int actions_remaining;
    private PlayerRole player_role;
    private Hand hand;
    private PlayerColor player_color;

    public Player(String player_name, PlayerRole player_role) {
        if(player_count >= 4){ throw new RuntimeException("You cant have more than 4 players");}
        this.player_id = player_count++;
        this.player_zone = null;
        this.player_name = player_name;
        this.actions_remaining = 3;
        this.player_role = player_role;
        this.player_color = player_role.getColor();
        this.hand = new Hand();
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

    public void discardCard(Card card, TreasureDeck treasureDeck){
        hand.remove(card);
        treasureDeck.discard(card);
    }

    public static void resetPlayerCount() { // for unit tests
        player_count = 0;
    }


}
