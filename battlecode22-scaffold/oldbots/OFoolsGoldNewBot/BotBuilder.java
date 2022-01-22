package OFoolsGoldNewBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static MapLocation destAdjacent;
    private static MapLocation buildLocation;
    private static RobotType buildType;
    private static int desperationIndex;
    private static boolean healMode;
    private static MapLocation healLocation;
    private static final int MIN_LATTICE_DIST = 15;

    public static void initBotBuilder(){
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
         // TODO: Reorganise to sprint
        if (destAdjacent == null && rc.canSenseLocation(dest)) destAdjacent = PathFinder.findOptimumAdjacentLocation(dest);
        if (destAdjacent == null){
            if(!currentLocation.equals(dest)){
                Movement.moveToDest(dest);
                return currentLocation.equals(dest);
            }
        }
        else if(!currentLocation.equals(destAdjacent)){
            Movement.moveToDest(destAdjacent);
            return currentLocation.equals(destAdjacent);
        }
        return false;
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


    // public static boolean createBuildingInRandomDirection(RobotType type) throws GameActionException{
    //     for (Direction dir : directions) {
    //         if (buildRobot(type, dir))
    //             return true;
    //     }
    //     return false;
    // }


    private static boolean makeBuilding(MapLocation dest, RobotType type) throws GameActionException{
        Direction dir = currentLocation.directionTo(dest);
        destAdjacent = null;
        return buildRobot(type, dir);
        // if (!buildRobot(type, dir))
        //     return createBuildingInRandomDirection(type);
        // return true;
    }
    
    
    public static void builderComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_BUILDER_COUNT);
        Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
        Comms.updateComms(); 
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
        // TODO: Tackle cases when makeBuilding can't make the building

        if (healMode){
            healBuilding();
            return;
        }

        // System.out.println("Bytecodes left: " + Clock.getBytecodesLeft());
        if (buildLocation == null) buildLocation = Movement.moveToLattice(MIN_LATTICE_DIST, 0);
        if (buildLocation == null) Movement.moveToDest(CENTER_OF_THE_MAP);
        // System.out.println("Bytecodes left: " + Clock.getBytecodesLeft());

        if (buildLocation != null){
            if(rc.canSenseLocation(buildLocation) && rc.canSenseRobotAtLocation(buildLocation)){
                buildLocation = null;
            }
            else if (moveToLocationForBuilding(buildLocation))
                if(makeBuilding(buildLocation, buildType)) buildLocation = null;
            // else{
                // desperationIndex++;
            // }
            // if (desperationIndex > 10){
            //     destAdjacent = null;
            //     if(createBuildingInRandomDirection(buildType)) buildLocation = null;
            // }
        }
    
    }
}
