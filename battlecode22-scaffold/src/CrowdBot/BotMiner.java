package CrowdBot;

import CrowdBot.Comms.SHAFlag;
import battlecode.common.*;

public class BotMiner extends Util{

    public static boolean isMinedThisTurn;
    public static int numOfMiners;
    private static MapLocation miningLocation;
    private static boolean inPlaceForMining;
    public static boolean commitSuicide;
    private static MapLocation suicideLocation;
    public static int desperationIndex;
    private static final int MIN_SUICIDE_DIST = 4;
    public static boolean prolificMiningLocationsAtBirth;


    public static boolean areMiningLocationsAbundant(){
        try{
            return (rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS).length > 50);
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static void initBotMiner(){
        isMinedThisTurn = false;
        if (isRubbleMapEnabled) RubbleMap.initRubbleMap();
        prolificMiningLocationsAtBirth = areMiningLocationsAbundant();
        resetVariables();
    }


    private static void resetVariables(){
        miningLocation = null;
        inPlaceForMining = false;
        commitSuicide = false;
        suicideLocation = null;
        desperationIndex = 0;
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
            desperationIndex++;
        }
        while (rc.canMineLead(loc)) {
            if (leadAmount == 1) break;
            isMinedThisTurn = true;
            rc.mineLead(loc);
            leadAmount--;
        }
    }


    public static void minerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_MINER_COUNT);
        Comms.channelArchonStop = Comms.CHANNEL_ARCHON_START + 4*archonCount;
        Comms.commChannelStart = Comms.channelArchonStop; 
    }


    public static MapLocation findOpenMiningLocationNearby() throws GameActionException{
        if (rc.senseLead(currentLocation) > 0 && parentArchonLocation.distanceSquaredTo(currentLocation) > 2){
            return currentLocation;
        }

        if (prolificMiningLocationsAtBirth){
            return Movement.moveToLattice(2, 0);
        }

        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS);
        
        for (MapLocation loc : potentialMiningLocations){   
            if(!rc.isLocationOccupied(loc) && parentArchonLocation.distanceSquaredTo(loc) > 1){
                // && (currentLocation.x + currentLocation.y) % 2 == parentArchonCongruence) {
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
                    mine(miningLocation);
                }
                // If outside of vision radius and the location has lead and is unoccupied
                else if (curDist > MINER_VISION_RADIUS || (rc.senseLead(miningLocation) != 0 && !rc.isLocationOccupied(miningLocation))){
                    Movement.moveToDest(miningLocation);
                }
                else{ // Location is occupied or no lead // TODO: Check if occupied by miner/enemy miner, mine adjacent to deplete it
                    miningLocation = null;
                    getMiningLocation(); // Get new mining location
                    inPlaceForMining = false;
                }
            }
            else{
                mine(miningLocation);
            }
        }
    }


    public static MapLocation checkCommsForMiningLocation() throws GameActionException{
        for(int i = Comms.commChannelStart; i < Comms.COMM_CHANNEL_STOP; ++i){
            int message = rc.readSharedArray(i);
            // TODO: Make it a non-greedy selection sometime.
            if (Comms.readSHAFlagFromMessage(message) == SHAFlag.LEAD_LOCATION){
                return Comms.readLocationFromMessage(message);
            }
        }
        return null;
    }


    public static MapLocation findGoodPlaceToDie() throws GameActionException{
        // int x = currentLocation.x, y = currentLocation.y;
        
        return Movement.moveToLattice(MIN_SUICIDE_DIST, 0);

        // MapLocation[] allLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, MINER_VISION_RADIUS);
        // for(MapLocation loc : allLocations)
        //     if (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc) && goodSpot(loc))
        //         return loc;



        // TODO: Why does the following fail?
        // for (int curX = x - 4; curX < x + 5; curX++){
        //     for (int curY = y-4; curY < x + 5; curY++){
        //         MapLocation curLoc = new MapLocation(curX, curY);
        //         if (rc.canSenseLocation(curLoc) && (!rc.isLocationOccupied(curLoc)))
        //             return curLoc;
        //     }
        // }
        // return null;
    }


    public static int getCountOfAdjacentBots(MapLocation location) throws GameActionException{
        int count = 0;
        for (Direction dir: directions){
            MapLocation loc = location.add(dir);
            if (rc.canSenseLocation(loc) && rc.isLocationOccupied(loc))
                count++;
        }
        return count;
    }


    public static boolean goodSpot(MapLocation loc) throws GameActionException{
        if (getCountOfAdjacentBots(loc) > 4)
            return false;
        return true;
    } 


    public static void getMiningLocation() throws GameActionException{
        MapLocation dest = null;
        if (miningLocation == null) dest = findOpenMiningLocationNearby();
        if (dest != null){ 
            miningLocation = dest;
            desperationIndex = 0;
        }
        else{
            commitSuicide = true;
            dest = findGoodPlaceToDie();
            if (dest != null){
                suicideLocation = dest;
            }
            else{
                commitSuicide = false;
                desperationIndex++;
            }
            
        }
    }


    public static void surveyForOpenMiningLocationsNearby() throws GameActionException{
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS);
        for (MapLocation loc : potentialMiningLocations){
            if (!rc.isLocationOccupied(loc))
                Comms.writeCommMessage(intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);
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
        if (desperationIndex > 7)
            rc.disintegrate();
        
        if (!commitSuicide){
            isMinedThisTurn = false;
            if (miningLocation == null){
                getMiningLocation();
                goToMine();
            }
            else{
                if (!inPlaceForMining)
                    goToMine();
                else
                    mine(currentLocation);
            }
        }
        else{
            if (currentLocation.equals(suicideLocation)){
                rc.disintegrate();
                // BOT'S DEAD!
            }
            else if (!rc.canSenseLocation(suicideLocation) || rc.isLocationOccupied(suicideLocation)){
                resetVariables();
                getMiningLocation();
            }
            else{
                desperationIndex++;
                Movement.moveToDest(suicideLocation);
            }
        }
        
        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            RubbleMap.updateRubbleMap();
        }
    }

}