package Model;

public class Player {
    private static int player_count = 0;
    private final int player_id;
    private final String player_name;
    private Zone player_zone;
    private int actions_remaining;
    private PlayerRole player_role;
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
        if(this.player_zone != null){
            this.player_zone.removePlayerFromZone(this);
        }
        this.player_zone = zone;
        zone.addPlayerToZone(this);
    }

    public int getPlayer_id() {return player_id;}

    public Zone getPlayer_zone() {return player_zone;}

    public String getPlayer_name() {return player_name;}

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

    public java.awt.Color getPlayer_color(){
        switch(player_id){
            case 0: return java.awt.Color.BLUE;
            case 1: return java.awt.Color.RED;
            case 2: return java.awt.Color.GREEN;
            case 3: return java.awt.Color.YELLOW;
            default: return java.awt.Color.BLACK;
        }
    }

}
