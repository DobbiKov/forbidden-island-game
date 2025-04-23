package Model;

import Errors.InvalidStateOfTheGameException;

import java.security.InvalidParameterException;

public class PlayerStartZone extends Zone {
    private PlayerColor card_player_color;
    public PlayerStartZone(Zone zone_from, Player player){
        super(zone_from);
        this.start_for_player = player;
        this.zone_type = ZoneType.PlayerStart;
    }
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
    public PlayerColor getCard_player_color() {
        return card_player_color;
    }

//    public PlayerStartZone(int x, int y, Player player, ZoneCard card){
//        super(x, y, true, card);
//        this.start_for_player = player;
//        this.zone_type = ZoneType.PlayerStart;
//    }
    public void associatePlayer(Player player){
        if(player == null){
            throw new IllegalArgumentException("Player cannot be null");
        }
        if(this.isAssociatedToAPlayer()){
            throw new InvalidStateOfTheGameException("Player already associated to a player");
        }
        this.start_for_player = player;
    }
    public Player getAssociatedPlayer(){
        return this.start_for_player;
    }
    public boolean isAssociatedToAPlayer(){
        return getAssociatedPlayer() != null;
    }
}
