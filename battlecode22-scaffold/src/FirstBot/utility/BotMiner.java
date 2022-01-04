package utility;

import battlecode.common.*;

// import utility.*;
public class BotMiner extends Util{


    public static int[][] rubbleMap;
    public static int commIndex;
    public static int[][] convolutionKernel;
    
    public BotMiner(){
        rubbleMap = new int[MAP_WIDTH][MAP_HEIGHT];
        convolutionKernel = new int[3][3];
        commIndex = -1;

        // TODO: Check if initialzation to 0 is needed or not
        for(int i = 0; i++ < MAP_WIDTH;)
            for(int j = 0; j++ < MAP_HEIGHT;)
                rubbleMap[i][j] = 0;
        
        
        for(int i = 0; i++ < 3;)
            for(int j = 0; j++ < 3;)
                convolutionKernel[i][j] = 1;
    }


    public static boolean checkAdjacentLocations(MapLocation current) throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(current, 1);
        for (MapLocation loc : adjacentLocations){
            if (rc.canMineGold(loc) || rc.canMineLead(loc))
                return true;
        }
        return false;
    }


    public static int computeGradedRubbleValue(int rubbleValue){
        return (int)((rubbleValue/(MAX_RUBBLE - MIN_RUBBLE)) * 7);
    }


    public static int computeRubbleValue(MapLocation loc){
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, 1);
        int rubbleValue = 0, locationCount = 0;
        for(MapLocation adjLoc : adjacentLocations){
            if (rc.canSenseLocation(adjLoc)){
                rubbleValue += rc.senseRubble(adjLoc);
                locationCount++;
            }
        }
        return computeGradedRubbleValue(rubbleValue/locationCount);
        
    }


    public static int computeWriteValue(int locationValue, int rubbleValue, int turnFlag){
        return (locationValue << 3 | rubbleValue | (turnFlag << 15));
    }


    public static int getRubbleValue(int num){
        return (num & (~((num >> 3) << 3)));
    }


    public static void updateRubbleMapByReadValue(int readValue){
        if ((readValue>>15) == flipTurnFlag()){
            MapLocation loc = mapLocationFromInt(setKthBitByInput(readValue>>3, 12, getTurnFlag()));
            int x = loc.x, y = loc.y;
            rubbleMap[x][y] = getRubbleValue(readValue);
        }
    }


    public static void updateRubbleMap(){
        for (int i = 0; i++ < SHARED_ARRAY_LENGTH;){
            int curVal = rc.readSharedArray(i);
            updateRubbleMapByReadValue(curVal);
        }
    }


    public static void runMiner(RobotController rc) throws GameActionException{
        if (commIndex == -1)
            commIndex = myRobotCount - archonCount - 1;
        
        if (commIndex >= SHARED_ARRAY_LENGTH)
            return;
        
        int rubbleValue = computeRubbleValue(currentLocation), turnFlag = (turnCount % 2);
        rc.writeSharedArray(commIndex, computeWriteValue(intFromMapLocation(currentLocation), rubbleValue, turnFlag));
        
        // Rubble Map Updation:
        updateRubbleMap();
    }
}