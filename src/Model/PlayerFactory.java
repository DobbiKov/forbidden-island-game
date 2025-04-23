package Model;

import Errors.InvalidStateOfTheGameException;
import Errors.NoRoleToAssignError;

import java.util.HashSet;
import java.util.Random;

public class PlayerFactory {
    private HashSet<PlayerRole> used_roles;

    public PlayerFactory() {
        this.used_roles = new HashSet<>();
    }
    public Player createPlayer(String player_name){
        PlayerRole role_to_assign = this.getAvailibleRole();
        Player player = new Player(player_name, role_to_assign);
        this.used_roles.add(role_to_assign);

        return player;
    }
    private PlayerRole getAvailibleRole(){
        if(this.used_roles.size() >= 6){
            throw new NoRoleToAssignError();
        }
        Random random = new Random();
        int n = random.nextInt(6);
        PlayerRole role = PlayerRole.getByNum(n);
        while(used_roles.contains(role)){
            n = random.nextInt(6);
            role = PlayerRole.getByNum(n);
        }
        return role;
    }
}
