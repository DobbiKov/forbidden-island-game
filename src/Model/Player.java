package Model;

public class Player {
    private static int player_count = 0;
    private final int player_id;
    private final String player_name;
    private Zone player_zone;
    private int actions_remaining;
    private PlayerRole player_role;
    //TODO : players role
    //TODO : hand with cards

    public Player(String player_name, PlayerRole player_role) {
        if(player_count >= 4){ throw new RuntimeException("You cant have more than 4 players");}
        this.player_id = player_count++;
        this.player_zone = null;
        this.player_name = player_name;
        this.actions_remaining = 3;
        this.player_role = player_role;
    }

    public void setPlayerToZone(Zone zone){
        this.player_zone = zone;
    }

    public int getPlayer_id() {return player_id;}

    public Zone getPlayer_zone() {return player_zone;}

    public void move_Player(Zone player_zone) {
        //TODO : check with his role
        if(this.actions_remaining > 0){
            this.actions_remaining--;
        }
    }

    public void reset_actions(){
        this.actions_remaining = 3;
    }

    public int getActions_remaining() {return actions_remaining;}


}
