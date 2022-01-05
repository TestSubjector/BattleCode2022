package FourthBot;

import battlecode.common.*;

public class BotArchon extends Util{

    /**
    * Run a single turn for an Archon.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runArchon(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = Globals.directions[Globals.rng.nextInt(Globals.directions.length)];
        if (Globals.rng.nextBoolean()) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
    }
    
}
