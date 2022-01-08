package BuildBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static boolean RubbleEnabled;
    private static MapLocation destAdjacent;
    private static MapLocation buildLocation;
    private static RobotType buildType;

    public static void initBotBuilder(){
        RubbleEnabled = true;
        if (RubbleEnabled) RubbleMap.initRubbleMap();
        destAdjacent = null;

        // TODO: Remove Hardcoded Init:
        buildLocation = centerOfTheWorld;
        buildType = RobotType.WATCHTOWER;
    }


    public static MapLocation findAdjacentLocationForBuilding(MapLocation dest){
        int x = dest.x, y = dest.y;
        for (int dx = -2; dx++ < 1;)
            for (int dy = -2; dy++ < 1;){
                int curX = dx + x, curY = dy + y;
                MapLocation loc = new MapLocation(curX, curY);
                if (!rc.canSenseRobotAtLocation(loc))
                    return loc;
            }
        return null;
    }


    private static boolean moveToLocationForBuilding(MapLocation dest) throws GameActionException{
        // TODO: Write helper function to see if moving to optimum destAdjacent location is better than building from right here
        if (currentLocation.distanceSquaredTo(dest) == 1){
            destAdjacent = currentLocation;
            return true;
        }
        if (destAdjacent == null) destAdjacent = PathFinder.findOptimumAdjacentLocation(dest);
        // if (rc.getRoundNum() > 40){
            
        //     System.out.println("destAdjacent: " + destAdjacent);
        // }
        if(!currentLocation.equals(destAdjacent)){
            Movement.moveToDest(destAdjacent);
            return currentLocation.equals(destAdjacent);
        }
        return true;
    }


    private static boolean makeBuilding(MapLocation dest, RobotType type) throws GameActionException{
        Direction dir = currentLocation.directionTo(dest);
        // if (rc.)
        if (rc.canBuildRobot(type, dir)){
            rc.buildRobot(type, dir);
            return true;
        }
        else{
            dest = findAdjacentLocationForBuilding(dest);
            if (dest == null) return false;
            if (rc.canBuildRobot(type, dir)){
                rc.buildRobot(type, dir);
                return true;
            }
            // makeBuilding(dest, type);
        }
        destAdjacent = null;
        return false;
    }
    
    
    public static void builderComms() throws GameActionException {
        Comms.updateChannelValueBy1(Comms.CHANNEL_BUILDER_COUNT);
    }


    static void runBuilder(RobotController rc) throws GameActionException {
        builderComms();
        // Movement.goToDirect(centerOfTheWorld);
        // Movement.moveRandomly();
        // Receive buildLocation and buildType somehow;
        if (moveToLocationForBuilding(buildLocation)){
            makeBuilding(buildLocation, buildType);
        }

    
        if (RubbleEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            RubbleMap.updateRubbleMap();
        }
    }
}
