package BuildBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static MapLocation destAdjacent;
    private static MapLocation buildLocation;
    private static RobotType buildType;
    private static int desperationIndex;
    private static boolean healMode;
    private static MapLocation healLocation;

    public static void initBotBuilder(){
        if (isRubbleMapEnabled) RubbleMap.initRubbleMap();
        destAdjacent = null;
        buildLocation = null;
        buildType = RobotType.WATCHTOWER;
        healMode = false;
        healLocation = null;
    }

    private static boolean moveToLocationForBuilding(MapLocation dest) throws GameActionException{
        // TODO: Write helper function to see if moving to optimum destAdjacent location is better than building from right here
        if (currentLocation.distanceSquaredTo(dest) == 1){
            destAdjacent = currentLocation;
            return true;
        }
        if (destAdjacent == null) destAdjacent = PathFinder.findOptimumAdjacentLocation(dest);
        if(!currentLocation.equals(destAdjacent)){
            Movement.moveToDest(destAdjacent);
            return currentLocation.equals(destAdjacent);
        }
        return true;
    }


    public static boolean buildRobot(RobotType type, Direction dir) throws GameActionException{
        if (rc.canBuildRobot(type, dir)){
            rc.buildRobot(type, dir);
            desperationIndex = 0;
            healMode = true;
            healLocation = currentLocation.add(dir);
            return true;
        }
        return false;
    }


    public static boolean createBuildingInRandomDirection(RobotType type) throws GameActionException{
        for (Direction dir : directions) {
            if (buildRobot(type, dir))
                return true;
        }
        return false;
    }


    private static boolean makeBuilding(MapLocation dest, RobotType type) throws GameActionException{
        Direction dir = currentLocation.directionTo(dest);
        destAdjacent = null;
        if (!buildRobot(type, dir))
            return createBuildingInRandomDirection(type);
        return true;
    }
    
    
    public static void builderComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_BUILDER_COUNT);
        Comms.channelArchonStop = Comms.CHANNEL_ARCHON_START + 4*archonCount;
        Comms.commChannelStart = Comms.channelArchonStop; 
    }


    public static void repair(MapLocation loc) throws GameActionException{
        if (rc.canRepair(loc))
            rc.repair(loc);
    }


    public static void healBuilding() throws GameActionException{
        // TODO: Add code for actual healing of injured buildings; Currently it only repairs prototypes
        RobotInfo target = rc.senseRobotAtLocation(healLocation);
        if (target != null && target.getMode() == RobotMode.PROTOTYPE){
            repair(healLocation);
        }
        else{
            healLocation = null;
            healMode = false;
        }
    }


    static void runBuilder(RobotController rc) throws GameActionException {
        builderComms();
        // TODO: Tackle cases when makaBuilding can't make the building

        if (healMode){
            healBuilding();
            return;
        }

        // if(buildLocation == null){}

        if (buildLocation != null){
            if (moveToLocationForBuilding(buildLocation))
                makeBuilding(buildLocation, buildType);
            else{
                desperationIndex++;
            }
            if (desperationIndex > 2){
                destAdjacent = null;
                createBuildingInRandomDirection(buildType);
            }
        }
    
        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            RubbleMap.updateRubbleMap();
        }
    }
}
