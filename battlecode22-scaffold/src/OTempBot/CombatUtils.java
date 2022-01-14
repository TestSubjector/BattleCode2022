package OTempBot;

import battlecode.common.*;

public class CombatUtils extends Util{

    public static boolean isMilitaryUnit(RobotType type){
        switch (type){
            case SOLDIER:
            case WATCHTOWER:
            case ARCHON:
            case SAGE:
                return true;
            default:
                return false;
        }
    }

    public static int militaryCount(RobotInfo[] visibleRobots){
        int count = 0;
        for (int i = visibleRobots.length; --i >= 0;) {
            if (isMilitaryUnit(visibleRobots[i].type)) count++;
        }
        return count;
    }
}
