package BuildBot;

import BuildBot.Comms.SHAFlag;
import battlecode.common.*;

public class BotMiner extends Util{

    public static boolean isMinedThisTurn;
    public static int numOfMiners;
    private static MapLocation miningLocation;
    private static boolean inPlaceForMining;
    // private static int numMineStoppings;
    private static int toWipeChannel;

    public static void initBotMiner(){
        isMinedThisTurn = false;
        RubbleMap.initRubbleMap();
        miningLocation = null;
        inPlaceForMining = false;
        // numMineStoppings = 0;
        toWipeChannel = -1;
    }

    
    public static void mine(MapLocation loc) throws GameActionException{
        if (!rc.isActionReady())
            return;
        while(rc.canMineGold(loc)){
            isMinedThisTurn = true;
            rc.mineGold(loc);
        }
        int leadAmount = rc.senseLead(loc);
        if (leadAmount == 0) {  // No more lead on location for some reason
            miningLocation = null;
            inPlaceForMining = false;
        }
        while (rc.canMineLead(loc)) {
            if (leadAmount == 1){
                // numMineStoppings++;
                break;
            }
            isMinedThisTurn = true;
            rc.mineLead(loc);
            leadAmount--;
        }
    }


    public static void minerComms() throws GameActionException {
        Comms.updateChannelValueBy1(Comms.CHANNEL_MINER_COUNT);
    }


    public static void setIndex(int[] arr, int val, int x, int y){
        arr[x] = (arr[x] | (val << y));
    }


    public static boolean nearbyMiner(int x, int y, int[] tally){
        // int curX = 4000, curY = 4000;
        // int dx, dy;
        boolean answer = ((tally[x] & (1 << y)) == 1);
        // System.out.println("The tally answer is : " + answer);
        return answer;
        // for(dx = -2; dx++ < 1;){
        //     for(dy = -2; dy++ < 1;){
        //         curX = x + dx;
        //         curY = y + dy;
        //         if (curX < 0 || curX > 8 || curY < 0 || curY > 8)
        //             continue;
        //         if ((tally[curX] & (1 << curY)) == 1){
        //             return true;
        //         }
        //     }
        // }
        // return false;
    }


    public static MapLocation findOpenMiningLocationNearby() throws GameActionException{
        // TODO: Could implement proper vetting of PotentialOpenMining Locations by tracking its lead values
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS);
        int[] tally = new int[9];

        // int x = currentLocation.x, y = currentLocation.y;
        // int ox = x-4, oy = y-4;
        // visibleAllies = rc.senseNearbyRobots(-1, MY_TEAM);
        // for (RobotInfo info : visibleAllies){
        //     MapLocation loc = info.location;
        //     setIndex(tally, 1, loc.x - ox, loc.y - oy);
        // }
        // MapLocation viableLocation = null;
        for (MapLocation loc : potentialMiningLocations){
            // System.out.println("The tally answer is : " +);
            // if (!nearbyMiner(loc.x - ox, loc.y - oy, tally) && !Comms.checkIfLeadLocationTaken(loc)){
            //     return loc;
            // }   
            if(!rc.isLocationOccupied(loc))
            {
                return loc;
            }
        }
        return null;
    }


    public static void goToMine() throws GameActionException{
        if (miningLocation != null){
            if (!inPlaceForMining){
                int curDist = currentLocation.distanceSquaredTo(miningLocation);
                if (curDist == 0) { // Reached location
                    inPlaceForMining = true;
                    Comms.wipeChannel(toWipeChannel);
                    mine(miningLocation);
                }
                // If outside of vision radius and the location has lead and is unoccupied
                else if (curDist > MINER_VISION_RADIUS || (rc.senseLead(miningLocation) != 0 && !rc.isLocationOccupied(miningLocation))){
                    Movement.moveToDest(miningLocation);
                }
                else{ // Location is occupied or no lead // TODO: Check if occupied by miner/enemy miner, mine adjacent to deplete it
                    miningLocation = null;
                    getMiningLocation(); // Get new mining location
                    Comms.wipeChannel(toWipeChannel);
                    inPlaceForMining = false;
                }
            }
            else{
                mine(miningLocation);
            }
        }
    }

    public static void getMiningLocation() throws GameActionException{
        MapLocation dest = null;
        if (miningLocation == null // || numMineStoppings > 2
        ) dest = findOpenMiningLocationNearby();
        // moveToDes either runs BugNav or PathFinder's AStar based on usingOnlyBugNav global boolean flag
        if (dest != null){ 
            miningLocation = dest;
            toWipeChannel = Comms.writeCommMessage(intFromMapLocation(miningLocation), SHAFlag.TAKEN_LEAD_LOCATION);
        }
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
        getMiningLocation();
        goToMine();
        
        // if (Clock.getBytecodesLeft() < 1000) System.out.println("2: " + String.valueOf(Clock.getBytecodesLeft()));
        if(turnCount != BIRTH_ROUND) RubbleMap.rubbleMapFormation(rc); // To skip hungry init turn. Don't write to shared array in birth round since that area is well explored
        // System.out.println("R1: " + String.valueOf(Clock.getBytecodesLeft()));
        if(turnCount != BIRTH_ROUND) RubbleMap.updateRubbleMap(); // TODO: Optimise, consuming 5000 bytecodes
        // System.out.println("R2: " + String.valueOf(Clock.getBytecodesLeft()));
    }

    // // TODO : Massive improvements required: remember mining location to mine till its finished, then change location
    // // Consumes 400 bytecodes
    // public static void mineAdjacentLocations() throws GameActionException {
    //     for (int dx = -1; dx <= 1; dx++) {
    //         for (int dy = -1; dy <= 1; dy++) {
    //             if(!rc.isActionReady()) return;
    //             MapLocation mineLocation = new MapLocation(currentLocation.x + dx, currentLocation.y + dy);
    //             while (rc.canMineGold(mineLocation)) {
    //                 isMinedThisTurn = true;
    //                 rc.mineGold(mineLocation);
    //             }
    //             while (rc.canMineLead(mineLocation)) {
    //                 isMinedThisTurn = true;
    //                 rc.mineLead(mineLocation);
    //             }
    //         }
    //     }
    // }

}