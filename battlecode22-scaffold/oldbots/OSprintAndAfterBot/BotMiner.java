package OSprintAndAfterBot;

import OSprintAndAfterBot.Comms.SHAFlag;
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
    private static boolean moveOut;
    private static RobotInfo[] visibleEnemies;
    private static boolean isFleeing;

    public static boolean areMiningLocationsAbundant(){
        try{
            return (rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS).length > 30);
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
        // byteCodeTest();
    }


    private static void resetVariables(){
        miningLocation = null;
        inPlaceForMining = false;
        commitSuicide = false;
        suicideLocation = null;
        desperationIndex = 0;
        moveOut = false; 
        isFleeing = false;
    }


    public static void mineAdjacentLocations() throws GameActionException {
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, 2);
        for (MapLocation loc : adjacentLocations){
            mine(loc);
            if(!rc.isActionReady()) // We can mine more one time so this is placed after mine()
                return;
        }
    }

    
    public static void mine(MapLocation loc) throws GameActionException{
        if (!rc.isActionReady())
            return;
        while(rc.canMineGold(loc)){
            isMinedThisTurn = true;
            desperationIndex = 0;
            rc.mineGold(loc);
        }
        int leadAmount = rc.senseLead(loc);
        if (leadAmount == 0) {  // No more lead on location for some reason
            miningLocation = null;
            inPlaceForMining = false;
            moveOut = true;
            desperationIndex++;
        }
        while (rc.canMineLead(loc)) {
            if (leadAmount == 1){
                moveOut = true;
                desperationIndex++;
                break;
            }
            moveOut = false;
            isMinedThisTurn = true;
            desperationIndex = 0;
            // desperationIndex = Math.max(0, desperationIndex - 2);
            rc.mineLead(loc);
            leadAmount--;
        }
    }


    public static void minerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_MINER_COUNT);
        Comms.updateComms();
    }

    public static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(MINER_VISION_RADIUS, ENEMY_TEAM);
    }

    public static MapLocation findOpenMiningLocationNearby() throws GameActionException{
        if (prolificMiningLocationsAtBirth){
            return Movement.moveToLattice(2, 0);
        }
        
        if (rc.senseLead(currentLocation) > 5 && parentArchonLocation.distanceSquaredTo(currentLocation) > 2){
            return currentLocation;
        }
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS);
        
        for (MapLocation loc : potentialMiningLocations){   
            if(!rc.isLocationOccupied(loc) && parentArchonLocation.distanceSquaredTo(loc) > 2 && goodMiningSpot(loc) ){
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
                    if (prolificMiningLocationsAtBirth) mineAdjacentLocations();
                    else mine(miningLocation);
                }
                // If outside of vision radius and the location has lead and is unoccupied
                else if (curDist > MINER_VISION_RADIUS || (rc.senseLead(miningLocation) != 0 && !rc.isLocationOccupied(miningLocation))){
                    if (!Movement.moveToDest(miningLocation)) desperationIndex++;
                    // else desperationIndex = Math.max(0, desperationIndex - 2);
                    else desperationIndex = 0;
                }
                else{ // Location is occupied or no lead // TODO: Check if occupied by miner/enemy miner, mine adjacent to deplete it
                    miningLocation = null;
                    getMiningLocation(); // Get new mining location
                    inPlaceForMining = false;
                }
            }
            else{
                if (prolificMiningLocationsAtBirth) mineAdjacentLocations();
                else mine(miningLocation);
            }
        }
    }


    public static MapLocation checkCommsForMiningLocation() throws GameActionException{
        MapLocation loc = null;
        int selectedChannel = -1;
        for(int i = Comms.commType.LEAD.commChannelStart; i < Comms.commType.LEAD.commChannelStop; ++i){
            int message = rc.readSharedArray(i);
            
            // TODO: Go for the nearest mine or the fattest mine? 

            // If Nearest:
            if (Comms.readSHAFlagFromMessage(message) == SHAFlag.LEAD_LOCATION){
                MapLocation tempLoc = Comms.readLocationFromMessage(message);
                if (loc == null || tempLoc.distanceSquaredTo(currentLocation) < loc.distanceSquaredTo(currentLocation)){
                    loc = tempLoc;
                    selectedChannel = i;
                }
            }
        }
        if (selectedChannel != -1){
            // int bytecodeC = Clock.getBytecodesLeft();
            Comms.wipeChannel(selectedChannel);
            // Comms.wipeChannelUpdateHead(Comms.commType.LEAD, selectedChannel);
            // bytecodediff = Math.max(bytecodeC - Clock.getBytecodesLeft(), bytecodediff);

        }
        return loc;
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


    public static boolean foundMiningLocationFromComms() throws GameActionException{
        // miningLocation = checkCommsForMiningLocation();
        miningLocation = Comms.findNearestLocationOfThisTypeAndWipeChannel(currentLocation, Comms.commType.LEAD, SHAFlag.LEAD_LOCATION);
        if (miningLocation != null){
            // desperationIndex--;
            desperationIndex = 0;
            return true;
        }
        return false;
    }


    public static boolean goAheadAndDie() throws GameActionException{
        commitSuicide = true;
        suicideLocation = findGoodPlaceToDie();
        if (suicideLocation != null){
            return true;
        }
        return false;
    }


    public static void getMiningLocation() throws GameActionException{
        MapLocation dest = null;
        if (miningLocation == null) dest = findOpenMiningLocationNearby();
        if (dest != null){ 
            miningLocation = dest;
            // desperationIndex--;
            desperationIndex = 0;
            // System.out.println("miningLocation Found");
        }
        else{
            if (foundMiningLocationFromComms()){
                // System.out.println("Mining Location found from Comms");
                return;
            }
            
            if (goAheadAndDie()){
                // System.out.println("Suicide seems successfull: " + suicideLocation);
                return;
            }
            System.out.println("Suicide Attempt Failed!");
            commitSuicide = false;
            desperationIndex++;
            Movement.moveToDest(parentArchonLocation);
        }
    }


    public static boolean goodMiningSpot(MapLocation loc) throws GameActionException{
        return (rc.senseLead(loc) > 20 || rc.senseGold(loc) > 0);
    }


    public static void surveyForOpenMiningLocationsNearby() throws GameActionException{
        // System.out.println("A: Bytecode remaining: " + Clock.getBytecodesLeft());
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS);
        // System.out.println("B: Bytecode remaining: " + Clock.getBytecodesLeft());
        for (MapLocation loc : potentialMiningLocations){
            // int bytecodeC = Clock.getBytecodesLeft();
            // System.out.println("C: Bytecode remaining: " + Clock.getBytecodesLeft());
            if (Clock.getBytecodesLeft() < 1500)
                break;
            if (!rc.isLocationOccupied(loc) && goodMiningSpot(loc)){
                // Takes ~350 bytecodes at max. With wipeChannelUpdateHead() = ~690 at max
                // Comms.writeCommMessageOverrwriteLesserPriorityMessageToHead(Comms.commType.LEAD, loc, SHAFlag.LEAD_LOCATION);

                // Takes ~840 bytecodes at max. With wipeChannelUpdateHead() = ~1180 at max
                // Comms.writeCommMessageToHead(Comms.commType.LEAD, loc, SHAFlag.LEAD_LOCATION);

                // For lead, it turns out this overall consumes the least bytecodes in the worst case:
                // Takes ~440 bytecodes at max. With wipeChannel() = ~550 bytecodes at max.
                Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);
                
                // Takes ~600 bytecodes at max. With wipeChannel() = ~710 bytecodes at max.
                // Comms.writeCommMessage(Comms.commType.LEAD, intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);
            }
            // bytecodediff = Math.max(bytecodeC - Clock.getBytecodesLeft(), bytecodediff);
            // System.out.println("D: Bytecode remaining: " + Clock.getBytecodesLeft());
        }
    }
    
    // public static void surveyForOpenMiningLocationsNearby() throws GameActionException{
    //     turnBroadcastCount = 0; // Assuming no writes have happened till now

    //     MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS);
    //     MapLocation best1 = null, best2 = null;
    //     int leadCount1 = 0, leadCount2 = 0;
    //     for (MapLocation loc : potentialMiningLocations){
    //         if (turnBroadcastCount > TURN_BROADCAST_LIMIT || Clock.getBytecodesLeft() < 2000)
    //             break;
    //         if (rc.isLocationOccupied(loc)) continue;
    //         if (rc.senseGold(loc) > 0){
    //             Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);
    //             turnBroadcastCount++;
    //             continue;
    //         }
    //         if (rc.senseLead(loc) > 100){
    //             Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);
    //             turnBroadcastCount++;
    //             continue;
    //         }
    //         int curLeadCount = rc.senseLead(loc);
    //         if (curLeadCount <= leadCount2)
    //             continue;

    //         // TODO: Use switch statements on turnBroadcastCount to update only relevant counts of bests.
    //         if (!rc.isLocationOccupied(loc)){
    //             if (best1 == null){
    //                 best1 = loc;
    //                 leadCount1 = curLeadCount;
    //                 continue;
    //             }
    //             if (best2 == null){
    //                 best2 = loc;
    //                 leadCount2 = curLeadCount;
    //                 continue;
    //             }
    //             if (curLeadCount <= leadCount1){
    //                 best2 = loc;
    //                 leadCount2 = curLeadCount;
    //                 continue;
    //             }
    //             best2 = best1;
    //             leadCount2 = leadCount1;
    //             best1 = loc;
    //             leadCount1 = curLeadCount;
    //         }
    //     }
    //     if (Clock.getBytecodesLeft() < 2000) return;
    //     switch(turnBroadcastCount){
    //         case 0: return; // break commands are missing in the following lines on purpose. Taking advantage of switch fall through.
    //         case 2: if (best2 != null) Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(best2), SHAFlag.LEAD_LOCATION);
    //         case 1: if (best1 != null) Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(best1), SHAFlag.LEAD_LOCATION);
    //     }
    // }


    /**
    * Run a single turn for a Miner.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        //TODO: Move away from Archon to make space for it in first turn
        // TODO: Check for Archon's move away command.
        turnBroadcastCount = 0;
        minerComms();
        updateVision();
        // if(Movement.retreatIfNecessary(visibleAllies, visibleEnemies)){
        //     isFleeing = true;
        //     moveOut = true;
        //     mineAdjacentLocations();
        // }
        // else isFleeing = false;
        isMinedThisTurn = false;
        if (desperationIndex > 30){
            Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(currentLocation), SHAFlag.LEAD_LOCATION);
            rc.disintegrate();
            // You killed me! :(
        }
        
        if (!commitSuicide && !isFleeing){
            if (miningLocation == null){
                getMiningLocation();
                goToMine();
            }
            else{
                if (!inPlaceForMining)
                    goToMine();
                else{
                    if (prolificMiningLocationsAtBirth) mineAdjacentLocations();
                    else mine(currentLocation);
                }
            }
        }
        if (commitSuicide && !isFleeing){
            if (currentLocation.equals(suicideLocation)){
                Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(currentLocation), SHAFlag.LEAD_LOCATION);
                rc.disintegrate();
                // Adios!
            }
            else if (rc.canSenseLocation(suicideLocation) && rc.isLocationOccupied(suicideLocation)){ // Reset only if location in vision and occupied
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
        if (moveOut){
            inPlaceForMining = false;
            miningLocation = null;
        }
        // int bytecodeC = Clock.getBytecodesLeft();
        BotSoldier.sendCombatLocation(visibleEnemies);
        surveyForOpenMiningLocationsNearby();
        // bytecodediff = Math.max(bytecodeC - Clock.getBytecodesLeft(), bytecodediff);
    }

}