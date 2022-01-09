package OCommBot;

import battlecode.common.*;

public class BotSoldier extends Util{


    public static void initBotSoldier(){
        RubbleMap.initRubbleMap();
    }

    /**
    * Run a single turn for a Soldier.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
        Movement.goToDirect(Globals.currentDestination);
        if(turnCount != BIRTH_ROUND) RubbleMap.rubbleMapFormation(rc);
        if(turnCount != BIRTH_ROUND) RubbleMap.updateRubbleMap();
    }
}
