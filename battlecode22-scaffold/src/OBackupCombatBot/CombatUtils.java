package OBackupCombatBot;

import battlecode.common.*;

public class CombatUtils extends Util{

    public static boolean isMilitaryUnit(RobotType type){
        switch (type){
            case SOLDIER:
            case WATCHTOWER:
            case SAGE:
                return true;
            default:
                return false;
        }
    }

    
}
