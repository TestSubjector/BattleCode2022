package ThirdBot;

import java.util.Arrays;

import battlecode.common.*;

public class BotMiner extends Util{

    public static int commIndex;
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
        
    }

    // TODO : Massive improvements required: remember mining location to mine till its finished, then change location
    public static void mineAdjacentLocations() throws GameActionException {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if(!rc.isActionReady()) return;
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


    public static void rubbleMapFormation(RobotController rc) throws GameActionException{
        if (commIndex == -1) commIndex = (myRobotCount - archonCount - 1) % Comms.CHANNEL_RUBBLE_STOP;
        // if (commIndex >= Comms.CHANNEL_RUBBLE_STOP) return;
        if(isOnEdge(currentLocation)) return; // Do not send information when on edge of Map
        int rubbleValue = RubbleMap.computeRubbleValue(currentLocation);
        int turnFlag = (turnCount % 2);
        // TODO: If rubbleValue == 0, then some other bot's vision's rubble info should be used
        int intMapLoc = intFromMapLocation(currentLocation);
        rc.writeSharedArray(commIndex, Comms.createRubbleMessage(intMapLoc, rubbleValue, turnFlag));
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
        // System.out.println("0: " + String.valueOf(Clock.getBytecodesLeft()));
        mineAdjacentLocations(); // Consumes around 500 bytecodes

        // Direction dir = PathFinder.findPath(Globals.currentLocation, Globals.rememberedEnemyArchonLocation);
        // if (rc.canMove(dir)) {
        //     rc.move(dir);
        //     // System.out.println("I moved!");
        // }
        // if (Clock.getBytecodesLeft() < 1000) System.out.println("1: " + String.valueOf(Clock.getBytecodesLeft()));
        if (!isMinedThisTurn) Movement.goToDirect(Globals.rememberedEnemyArchonLocation); // Consumes around 400 bytecodes
        // if (Clock.getBytecodesLeft() < 1000) System.out.println("2: " + String.valueOf(Clock.getBytecodesLeft()));
        if(turnCount != BIRTH_ROUND) BotMiner.rubbleMapFormation(rc); // To skip hungry init turn. Don't write to shared array in birth round since that area is well explored
        // if (Clock.getBytecodesLeft() < 1000) System.out.println("3: " + String.valueOf(Clock.getBytecodesLeft()));
        RubbleMap.updateRubbleMap(); // TODO: Optimise, consuming 5000 bytecodes
    }

}