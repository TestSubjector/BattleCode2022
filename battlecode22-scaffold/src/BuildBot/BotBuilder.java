package BuildBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static MapLocation destAdjacent;
    private static MapLocation buildLocation;
    private static RobotType buildType;
    private static int desperationIndex;
    private static boolean healMode;
    private static MapLocation healLocation;
    private static final int MIN_LATTICE_DIST = 10;

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


    public static MapLocation buildInLattice(){
        try {
            MapLocation lCurrentLocation = currentLocation;
            MapLocation lArchonLocation = Util.getClosestArchonLocation();
            MapLocation bestLoc = null;
            MapLocation myLoc = rc.getLocation();

            int bestDist = 0;
            int byteCodeSaver=0;
            if (lArchonLocation == null) return null;
            int congruence = (lArchonLocation.x + lArchonLocation.y + 1) % 2;

            if ((myLoc.x + myLoc.y)%2 == congruence && myLoc.distanceSquaredTo(lArchonLocation) >= MIN_LATTICE_DIST){
                bestDist = myLoc.distanceSquaredTo(lArchonLocation);
                bestLoc = myLoc;
                // return bestLoc;
            }

            for (int i = droidVisionDirs.length; i-- > 0; ) {
                if (Clock.getBytecodesLeft() < 2000) return bestLoc;
                lCurrentLocation = lCurrentLocation.add(droidVisionDirs[i]);
                if ((lCurrentLocation.x + lCurrentLocation.y) % 2 != congruence) continue;
                if (!rc.onTheMap(lCurrentLocation)) continue;
                if (rc.isLocationOccupied(lCurrentLocation)) continue;

                int estimatedDistance = lCurrentLocation.distanceSquaredTo(lArchonLocation);

                if (estimatedDistance < MIN_LATTICE_DIST) continue;

                if (bestLoc == null  || estimatedDistance < bestDist){
                    bestLoc = lCurrentLocation;
                    bestDist = estimatedDistance;
                    byteCodeSaver++;
                }
                // if(byteCodeSaver == 5){
                //     return bestLoc;
                // }
            }
            if (bestLoc != null){
                return bestLoc;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    static void runBuilder(RobotController rc) throws GameActionException {
        builderComms();
        // TODO: Tackle cases when makeBuilding can't make the building

        if (healMode){
            healBuilding();
            return;
        }

        // System.out.println("Bytecodes left: " + Clock.getBytecodesLeft());
        if(buildLocation == null) buildLocation = buildInLattice();
        // System.out.println("Bytecodes left: " + Clock.getBytecodesLeft());

        if (buildLocation != null){
            if (moveToLocationForBuilding(buildLocation))
                if(makeBuilding(buildLocation, buildType)) buildLocation = null;
            // else{
                // desperationIndex++;
            // }
            // if (desperationIndex > 10){
            //     destAdjacent = null;
            //     if(createBuildingInRandomDirection(buildType)) buildLocation = null;
            // }
        }
    
        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            // RubbleMap.updateRubbleMap();
        }
    }
}
