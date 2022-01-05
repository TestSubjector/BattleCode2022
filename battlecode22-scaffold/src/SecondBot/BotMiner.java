package SecondBot;

import java.util.Arrays;

import battlecode.common.*;

public class BotMiner extends Util{


    public static float[][] rubbleMap;
    public static int commIndex;
    public static int[][] convolutionKernel;
    public static boolean isMinedThisTurn;
    
    public static void initBotMiner(){
        rubbleMap = new float[MAP_WIDTH][MAP_HEIGHT];
        convolutionKernel = new int[3][3];
        commIndex = -1;
        isMinedThisTurn = false;

        // TODO: Check if initialzation to 0 is needed or not
        for(int i = 0; i < MAP_WIDTH; ++i)
            for(int j = 0; j < MAP_HEIGHT; ++j)
                rubbleMap[i][j] = 0;
        
        
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                convolutionKernel[i][j] = 1 ;
        
        // convolutionKernel[1][1] = 0;
    }


    public static boolean checkAdjacentLocations(MapLocation current) throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(current, 1);
        for (MapLocation loc : adjacentLocations){
            if (rc.canMineGold(loc) || rc.canMineLead(loc))
                return true;
        }
        return false;
    }

    public static void mineAdjacentLocations() throws GameActionException {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(currentLocation.x + dx, currentLocation.y + dy);
                while (rc.canMineGold(mineLocation)) {
                    isMinedThisTurn = true;
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    isMinedThisTurn = true;
                    rc.mineLead(mineLocation);
                }
            }
        }
    }

    public static int computeGradedRubbleValue(int rubbleValue) throws GameActionException{
        // TODO: Extra case required?
        float temp = ((float)rubbleValue/(MAX_RUBBLE - MIN_RUBBLE)) * 7.0f;
        return (int)(temp);
    }


    public static int computeRubbleValue(MapLocation loc) throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, 1);
        int rubbleValue = 0, locationCount = 0;
        // rubbleValue = rc.senseRubble(loc);
        for(MapLocation adjLoc : adjacentLocations){
            if (rc.canSenseLocation(adjLoc)){
                int i = adjLoc.x - loc.x + 1;
                int j = adjLoc.y - loc.y + 1;
                rubbleValue += rc.senseRubble(adjLoc) * convolutionKernel[i][j];
                locationCount += convolutionKernel[i][j];
            }
        }
        return computeGradedRubbleValue(rubbleValue/locationCount);
    }

    public static void updateRubbleMapByReadValue(int readValue) throws GameActionException{
        if ((readValue>>15) == flipTurnFlag()){
            MapLocation loc = Comms.getLocFromRubbleMessage(readValue);
            // MapLocation loc = mapLocationFromInt(setKthBitByInput(readValue>>3, 13, getTurnFlag()));
            int x = loc.x, y = loc.y;

            rubbleMap[x][y] = Comms.getRubbleValueFromRubbleMessage(readValue);
            for (int dx = -1; dx <= 1; ++dx){
                for (int dy = -1; dy <= 1; ++dy){
                    if (!isValidMapLocation(x+dx,y+dy)) continue;
                    // TODO: Add comment
                    rubbleMap[x+dx][y + dy] = (rubbleMap[x+dx][y + dy] + rubbleMap[x][y]) / 2.0f;
                }
            }
        }
    }


    public static void updateRubbleMap() throws GameActionException{
        for (int i = 0; i < SHARED_ARRAY_LENGTH; ++i){
            int curVal = rc.readSharedArray(i);
            if (curVal == 0) continue;
            updateRubbleMapByReadValue(curVal);
        }
    }


    public static void rubbleMapFormation(RobotController rc) throws GameActionException{
        if (commIndex == -1)
            commIndex = myRobotCount - archonCount - 1;
        
        if (commIndex >= SHARED_ARRAY_LENGTH)
            return;
        
        int rubbleValue = computeRubbleValue(currentLocation);
        int turnFlag = (turnCount % 2);
        // TODO: If rubbleValue == 0, then some other bot's vision's rubble info should be used
        rc.writeSharedArray(commIndex, Comms.getMineValueForRubbleMessage(intFromMapLocation(currentLocation), rubbleValue, turnFlag));
        
        // Rubble Map Updation:
        updateRubbleMap();

        if (turnCount == 500){
            System.out.println("Printing rubbleMap:");
            System.out.println(Arrays.deepToString(rubbleMap));
        }
    }

        /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        // rc.setIndicatorString("Hello world!");
        // rc.setIndicatorString("2nd Hello world!");

        mineAdjacentLocations();
        // Also try to move randomly.
        // Direction dir = Globals.directions[Globals.rng.nextInt(Globals.directions.length)];
        // if (rc.canMove(dir)) {
        //     rc.move(dir);
        //     // System.out.println("I moved!");
        // }
        // Direction dir = PathFinder.findPath(Globals.currentLocation, Globals.rememberedEnemyArchonLocation);
        // if (rc.canMove(dir)) {
        //     rc.move(dir);
        //     // System.out.println("I moved!");
        // }
        // System.out.println("1: " + String.valueOf(Clock.getBytecodeNum()));
        Movement.goToDirect(Globals.rememberedEnemyArchonLocation);
        // System.out.println("2: " + String.valueOf(Clock.getBytecodeNum()));
        BotMiner.rubbleMapFormation(rc);
    }

}