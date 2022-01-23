package OCommBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    public static void initBotBuilder(){
        RubbleMap.initRubbleMap();
    }

    static void runBuilder(RobotController rc) throws GameActionException {
        MapLocation centerOfTheWorld = new MapLocation(MAP_HEIGHT/2, MAP_WIDTH/2);
        Movement.goToDirect(centerOfTheWorld);
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        if(turnCount != BIRTH_ROUND) RubbleMap.rubbleMapFormation(rc);
        if(turnCount != BIRTH_ROUND) RubbleMap.updateRubbleMap();
    }
}
