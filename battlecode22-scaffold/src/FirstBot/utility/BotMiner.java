package utility;

import java.util.Arrays;

import battlecode.common.*;

// import utility.*;
public class BotMiner extends Util{


    public static int[][] rubbleMap;
    public static int commIndex;
    public static int[][] convolutionKernel;
    
    public static void initBotMiner(){
        rubbleMap = new int[MAP_WIDTH][MAP_HEIGHT];
        convolutionKernel = new int[3][3];
        commIndex = -1;

        // TODO: Check if initialzation to 0 is needed or not
        for(int i = 0; i < MAP_WIDTH; ++i)
            for(int j = 0; j < MAP_HEIGHT; ++j)
                rubbleMap[i][j] = 0;
        
        
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                convolutionKernel[i][j] = 0;
        
        convolutionKernel[1][1] = 1;
    }


    public static boolean checkAdjacentLocations(MapLocation current) throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(current, 1);
        for (MapLocation loc : adjacentLocations){
            if (rc.canMineGold(loc) || rc.canMineLead(loc))
                return true;
        }
        return false;
    }


    public static int computeGradedRubbleValue(int rubbleValue) throws GameActionException{
        return (int)((rubbleValue/(MAX_RUBBLE - MIN_RUBBLE)) * 7);
    }


    public static int computeRubbleValue(MapLocation loc) throws GameActionException{
        // MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, 1);
        int rubbleValue = 0, locationCount = 1;
        rubbleValue = rc.senseRubble(loc);
        // for(MapLocation adjLoc : adjacentLocations){
        //     if (rc.canSenseLocation(adjLoc)){
        //         // rubbleValue += rc.senseRubble(adjLoc) * convolutionKernel;
        //         // locationCount++;
        //     }
        // }
        return computeGradedRubbleValue(rubbleValue/locationCount);
        
    }


    public static int computeWriteValue(int locationValue, int rubbleValue, int turnFlag) throws GameActionException{
        return (locationValue << 3 | rubbleValue | (turnFlag << 15));
    }


    public static int getRubbleValue(int num) throws GameActionException{
        return (num & 7);
    }


    public static void updateRubbleMapByReadValue(int readValue) throws GameActionException{
        if ((readValue>>15) == flipTurnFlag()){
            MapLocation loc = mapLocationFromInt((readValue & (0x7FF8))>>>3);
            // MapLocation loc = mapLocationFromInt(setKthBitByInput(readValue>>3, 13, getTurnFlag()));
            int x = loc.x, y = loc.y;
            // System.out.println("location: " + loc);
            rubbleMap[x][y] = getRubbleValue(readValue);
        }
    }


    public static void updateRubbleMap() throws GameActionException{
        for (int i = 0; i < SHARED_ARRAY_LENGTH; ++i){
            int curVal = rc.readSharedArray(i);
            updateRubbleMapByReadValue(curVal);
        }
    }


    public static void rubbleMapFormation(RobotController rc) throws GameActionException{
        if (commIndex == -1)
            commIndex = myRobotCount - archonCount - 1;
        
        if (commIndex >= SHARED_ARRAY_LENGTH)
            return;
        
        int rubbleValue = computeRubbleValue(currentLocation), turnFlag = (turnCount % 2);
        rc.writeSharedArray(commIndex, computeWriteValue(intFromMapLocation(currentLocation), rubbleValue, turnFlag));
        
        // Rubble Map Updation:
        updateRubbleMap();

        if (turnCount == 100){
            // for(int i = 0; i < )
            System.out.println("Printing rubbleMap:");
            System.out.println(Arrays.deepToString(rubbleMap));
            // for (int i = 0; i < MAP_WIDTH; ++i){
            //     for (int j = 0; j < MAP_HEIGHT; ++j){
            //         System.out.print(rubbleMap[i][j]);
            //         // System.out.print(", ");
            //     }
            //     System.out.println(".");
            // }
            // System.out.println(Arrays.toString(rubbleMap));
        }
    }
}