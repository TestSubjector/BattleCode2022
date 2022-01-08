package BuildBot;

import battlecode.common.*;

public class BotMiner extends Util{

    public static boolean isMinedThisTurn;
    public static int numOfMiners;

    public static void initBotMiner(){
        isMinedThisTurn = false;
        RubbleMap.initRubbleMap();
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


    public static void minerComms() throws GameActionException {
        Comms.updateChannelValueBy1(Comms.CHANNEL_MINER_COUNT);
    }


    /**
    * Run a single turn for a Miner.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        //TODO: Move away from Archon to make space for it in first turn
        minerComms();
        isMinedThisTurn = false;
        mineAdjacentLocations(); // Consumes around 500 bytecodes

        // Direction dir = PathFinder.findPathAStar(Globals.currentLocation, Globals.currentDestination);
        // if (!isMinedThisTurn && rc.canMove(dir)) {
        //     rc.move(dir);
        //     // System.out.println("I moved!");
        // }
        // if (Clock.getBytecodesLeft() < 1000) System.out.println("1: " + String.valueOf(Clock.getBytecodesLeft()));
        if (!isMinedThisTurn) Movement.goToDirect(Globals.currentDestination); // Consumes around 400 bytecodes
        // if (Clock.getBytecodesLeft() < 1000) System.out.println("2: " + String.valueOf(Clock.getBytecodesLeft()));
        if(turnCount != BIRTH_ROUND) RubbleMap.rubbleMapFormation(rc); // To skip hungry init turn. Don't write to shared array in birth round since that area is well explored
        // System.out.println("R1: " + String.valueOf(Clock.getBytecodesLeft()));
        if(turnCount != BIRTH_ROUND) RubbleMap.updateRubbleMap(); // TODO: Optimise, consuming 5000 bytecodes
        // System.out.println("R2: " + String.valueOf(Clock.getBytecodesLeft()));
    }

}