package OSoldierBot;

import battlecode.common.*;

public class BotWatchTower extends Util{

    public static void initBotWatchTower(){
        if (isRubbleMapEnabled) RubbleMap.initRubbleMap();
    }


    public static void watchTowerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_WATCHTOWER_COUNT);
        Comms.updateComms(); 
    }

    static void runWatchTower(RobotController rc) throws GameActionException {
        watchTowerComms();
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            RubbleMap.updateRubbleMap();
        }
    }
}
