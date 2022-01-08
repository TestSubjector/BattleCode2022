package BuildBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static boolean RubbleEnabled;
    private static MapLocation destAdjacent;
    private static MapLocation buildLocation;
    private static RobotType buildType;
    private static int desperationIndex;

    public static void initBotBuilder(){
        RubbleEnabled = true;
        if (RubbleEnabled) RubbleMap.initRubbleMap();
        destAdjacent = null;

        // TODO: Remove Hardcoded Init:
        buildLocation = centerOfTheWorld;
        buildType = RobotType.WATCHTOWER;
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


    public static boolean createBuildingInRandomDirection(RobotType type) throws GameActionException{
        for (Direction direc : directions) {
            if (rc.canBuildRobot(type, direc)){
                rc.buildRobot(type, direc);
                desperationIndex = 0;
                return true;
            }
        }
        return false;
    }


    private static boolean makeBuilding(MapLocation dest, RobotType type) throws GameActionException{
        Direction dir = currentLocation.directionTo(dest);
        destAdjacent = null;
        if (rc.canBuildRobot(type, dir)){
            rc.buildRobot(type, dir);
            desperationIndex = 0;
            return true;
        }
        else{
            return createBuildingInRandomDirection(type);
        }
    }
    
    
    public static void builderComms() throws GameActionException {
        Comms.updateChannelValueBy1(Comms.CHANNEL_BUILDER_COUNT);
    }


    static void runBuilder(RobotController rc) throws GameActionException {
        builderComms();
        // Movement.goToDirect(centerOfTheWorld);
        // Movement.moveRandomly();
        // Receive buildLocation and buildType somehow;
        
        // TODO: Tackle cases when makaBuilding can't make the building
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

    
        if (RubbleEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            RubbleMap.updateRubbleMap();
        }
    }
}
