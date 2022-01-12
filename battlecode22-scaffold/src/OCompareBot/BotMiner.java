package OCompareBot;

import OCompareBot.Comms.SHAFlag;
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
    private static final boolean searchByDistance = false;
    private static final int randomPersistance = 20;

    public static boolean areMiningLocationsAbundant(){
        try{
            return (rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS).length > 30);
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static void initBotMiner(){
        prolificMiningLocationsAtBirth = areMiningLocationsAbundant();
        resetVariables();
    }


    public static void updateMiner() throws GameActionException{
        isMinedThisTurn = false;
        moveOut = true; 
        // isFleeing = false; ???
        minerComms();
        updateVision();
    }


    private static void resetVariables(){
        miningLocation = null;
        inPlaceForMining = false;
        commitSuicide = false;
        suicideLocation = null;
        desperationIndex = 0;
        moveOut = true; 
        isFleeing = false;
    }


    // public static void mineAdjacentLocations() throws GameActionException {
    //     MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, 2);
    //     for (MapLocation loc : adjacentLocations){ // Team Bias
    //         mine(loc);
    //         if(!rc.isActionReady()) // We can mine more one time so this is placed after mine()
    //             return;
    //     }
    // }


    public static void mine() throws GameActionException{
        if (!rc.isActionReady()) return;
        // System.out.println("Action ready");
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, 2);
        for (MapLocation loc : adjacentLocations){
            while(rc.canMineGold(loc)){
                rc.mineGold(loc);
            }
            while (rc.canMineLead(loc)){
                if (rc.senseLead(loc) <= 1){
                    miningLocation = null;
                    break;
                }
                rc.mineLead(loc);
                // System.out.println("G - Mined lead + " + loc);
            }
        }
    }

    
    // public static void mine(MapLocation loc) throws GameActionException{
    //     if (!rc.isActionReady())
    //         return;
    //     while(rc.canMineGold(loc)){
    //         desperationIndex = 0;
    //         rc.mineGold(loc);
    //     }
    //     int leadAmount = rc.senseLead(loc);
    //     if (leadAmount == 0 && rc.senseGold(loc) == 0) {  // No more lead/gold on location
    //         miningLocation = null;
    //         desperationIndex++;
    //     }
    //     while (rc.canMineLead(loc)) {
    //         if (leadAmount <= 1 && rc.senseGold(loc) == 0){
    //             miningLocation = null;
    //             break;
    //         }
    //         desperationIndex = 0;
    //         rc.mineLead(loc);
    //         leadAmount--;
    //     }
    // }


    public static void minerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_MINER_COUNT);
        Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
        Comms.updateComms();
    }

    public static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(MINER_VISION_RADIUS, ENEMY_TEAM);
    }


    // Finds closest location or one with best gold value (greater than 5)
    public static MapLocation getBestGoldMine(MapLocation[] locations) throws GameActionException{
        if (locations.length == 1) return locations[0];
        MapLocation opt = null;
        int value = 0;
        for (MapLocation loc : locations){
            if (parentArchonLocation.distanceSquaredTo(loc) <= 2) continue;
            int curValue;
            if (searchByDistance) curValue = -currentLocation.distanceSquaredTo(loc);
            else curValue = rc.senseGold(loc);
            if (opt == null || curValue > value){
                opt = loc;
                value = curValue;
            }
        }
        return opt;
    }

    // Finds closest location or one with best lead value (greater than 5)
    public static MapLocation getBestLeadMine(MapLocation[] locations) throws GameActionException{
        if (locations.length == 1) return locations[0];
        MapLocation opt = null;
        int value = 0;
        for (MapLocation loc : locations){
            if (parentArchonLocation.distanceSquaredTo(loc) <= 2) continue; //TODO: Improve this
            int curValue;
            if (searchByDistance) curValue = -currentLocation.distanceSquaredTo(loc);
            else curValue = rc.senseLead(loc);
            if (opt == null || curValue > value){
                opt = loc;
                value = curValue;
            }
        }
        return opt;
    }


    public static int countOfMinersInVicinity() throws GameActionException{
        RobotInfo[] visibleAllies = rc.senseNearbyRobots(MINER_VISION_RADIUS, MY_TEAM);
        int count = 0;
        for (RobotInfo bot : visibleAllies){
            if (bot.type == RobotType.MINER) count++;
        }
        return count;
    }

    public static void goToMine() throws GameActionException{
        moveOut = false;
        if (miningLocation == null) return;
        // miningLocation is not null from now on:
        if (currentLocation.distanceSquaredTo(miningLocation) <= 2){
            mine();
            return;
        }

        int curDist = currentLocation.distanceSquaredTo(miningLocation);

        // If outside of vision, move to location
        if (curDist > MINER_VISION_RADIUS){
            if (!BFS.move(miningLocation)) desperationIndex++;
            return;
        }

        // Mine depleted
        if (rc.canSenseLocation(miningLocation) && rc.senseLead(miningLocation) < 5){ 
            miningLocation = null;
            getMiningLocation();
        }

        // Can't ... move
        if (!rc.isMovementReady() || (miningLocation!= null && !BFS.move(miningLocation))) desperationIndex++;
    }


    // public static MapLocation checkCommsForMiningLocation() throws GameActionException{
    //     MapLocation loc = null;
    //     int selectedChannel = -1;
    //     for(int i = Comms.commType.LEAD.commChannelStart; i < Comms.commType.LEAD.commChannelStop; ++i){
    //         int message = rc.readSharedArray(i);
            
    //         // TODO: Go for the nearest mine or the fattest mine? 

    //         // If Nearest:
    //         if (Comms.readSHAFlagFromMessage(message) == SHAFlag.LEAD_LOCATION){
    //             MapLocation tempLoc = Comms.readLocationFromMessage(message);
    //             if (loc == null || tempLoc.distanceSquaredTo(currentLocation) < loc.distanceSquaredTo(currentLocation)){
    //                 loc = tempLoc;
    //                 selectedChannel = i;
    //             }
    //         }
    //     }
    //     if (selectedChannel != -1){
    //         // int bytecodeC = Clock.getBytecodesLeft();
    //         Comms.wipeChannel(selectedChannel);
    //         // Comms.wipeChannelUpdateHead(Comms.commType.LEAD, selectedChannel);
    //         // bytecodediff = Math.max(bytecodeC - Clock.getBytecodesLeft(), bytecodediff);

    //     }
    //     return loc;
    // }


    public static MapLocation findGoodPlaceToDie() throws GameActionException{
        return Movement.moveToLattice(MIN_SUICIDE_DIST, 0);
    }


    // public static int getCountOfAdjacentBots(MapLocation location) throws GameActionException{
    //     int count = 0;
    //     for (Direction dir: directions){
    //         MapLocation loc = location.add(dir);
    //         if (rc.canSenseLocation(loc) && rc.isLocationOccupied(loc))
    //             count++;
    //     }
    //     return count;
    // }


    // public static boolean goodSpot(MapLocation loc) throws GameActionException{
    //     if (getCountOfAdjacentBots(loc) > 4)
    //         return false;
    //     return true;
    // }


    public static boolean foundMiningLocationFromComms() throws GameActionException{
        miningLocation = Comms.findNearestLocationOfThisTypeOutOfVision(currentLocation, Comms.commType.LEAD, SHAFlag.LEAD_LOCATION);
        
        if (miningLocation != null){
            // desperationIndex--;
            desperationIndex = 0;
            return true;
        }
        return false;
        // miningLocation = checkCommsForMiningLocation();
        // miningLocation = Comms.findLocationOfThisType(Comms.commType.LEAD, SHAFlag.LEAD_LOCATION);
        // miningLocation = Comms.findNearestLocationOfThisTypeAndWipeChannel(currentLocation, Comms.commType.LEAD, SHAFlag.LEAD_LOCATION);
    }


    public static Direction persistingRandomMovement() throws GameActionException{
        return Direction.values()[((BIRTH_ROUND + Globals.rng.nextInt(turnCount)) % randomPersistance) % 9];
    }


    public static boolean goAheadAndDie() throws GameActionException{
        commitSuicide = true;
        suicideLocation = findGoodPlaceToDie();
        if (suicideLocation != null){
            return true;
        }
        return false;
    }

    // Checks lattice, then for gold, then for more than 5 lead
    public static boolean foundMiningLocationFromVision() throws GameActionException{
        // For lots of mining locations nearby
        if (prolificMiningLocationsAtBirth){
            miningLocation = Movement.moveToLattice(2, 0);
            if (miningLocation != null) return true;
        }
        if (parentArchonLocation.distanceSquaredTo(currentLocation) >= 2){
            if (rc.senseGold(currentLocation) > 0 || rc.senseLead(currentLocation) > 20)
            miningLocation = currentLocation;
        }
        if (countOfMinersInVicinity() > 4) return false;
        if (rc.senseGold(currentLocation) > 0 || rc.senseLead(currentLocation) > 20)
            miningLocation = currentLocation;
        // Get locations with gold
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithGold();
        if (potentialMiningLocations.length > 0) { 
            miningLocation = getBestGoldMine(potentialMiningLocations);
            if (miningLocation != null) return true;
        }

        // Get locations with more than 5 lead in entire vision
        potentialMiningLocations = rc.senseNearbyLocationsWithLead(-1, 5);
        if (potentialMiningLocations.length > 0) {
            miningLocation = getBestLeadMine(potentialMiningLocations);
            if (miningLocation != null) return true;
        }

        return false;
    }


    public static MapLocation explore() throws GameActionException{
        if(BIRTH_ROUND % 3 == 0) {
            return ratioPointBetweenTwoMapLocations(parentArchonLocation, rememberedEnemyArchonLocations[0], 0.5);
        } else if (BIRTH_ROUND % 3 == 1){
            return ratioPointBetweenTwoMapLocations(parentArchonLocation, rememberedEnemyArchonLocations[1], 0.5);
        }
        else{
            return ratioPointBetweenTwoMapLocations(parentArchonLocation, rememberedEnemyArchonLocations[2], 0.5);
        }
        // int cx = currentLocation.x, cy = currentLocation.y, px = parentArchonLocation.x, py = parentArchonLocation.y;
        // int newX = Math.min(Math.max(2*cx - px, 0), MAP_WIDTH - 1), newY = Math.min(Math.max(2*cy - py, 0), MAP_HEIGHT-1);
        // return (new MapLocation(0, 0));
    }


    public static void getMiningLocation() throws GameActionException{
        moveOut = false;
        if (miningLocation != null) {
            System.out.println("Location already found?? Something's wrong.");
            return;
        }

        // First we look at locations in vision
        if (foundMiningLocationFromVision()) return;
        
        // We then look at locations in vision
        if (foundMiningLocationFromComms()) return;
        
        // TODO: Find a better exploration function. Perhaps Geffner's?
        if (!BFS.move(explore())) desperationIndex++;

    }


    public static boolean goodMiningSpot(MapLocation loc) throws GameActionException{
        return (rc.senseLead(loc) > 20 || rc.senseGold(loc) > 0);
    }


    public static void surveyForOpenMiningLocationsNearby() throws GameActionException{
        // System.out.println("A: Bytecode remaining: " + Clock.getBytecodesLeft());
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(UNIT_TYPE.visionRadiusSquared);
        // System.out.println("B: Bytecode remaining: " + Clock.getBytecodesLeft());
        for (MapLocation loc : potentialMiningLocations){  // Team bias
            // int bytecodeC = Clock.getBytecodesLeft();
            // System.out.println("C: Bytecode remaining: " + Clock.getBytecodesLeft());
            if (Clock.getBytecodesLeft() < 1200)
                break;
            if (!rc.isLocationOccupied(loc) && goodMiningSpot(loc)){
                // Takes ~350 bytecodes at max. With wipeChannelUpdateHead() = ~690 at max
                // Comms.writeCommMessageOverrwriteLesserPriorityMessageToHead(Comms.commType.LEAD, loc, SHAFlag.LEAD_LOCATION);

                // Takes ~840 bytecodes at max. With wipeChannelUpdateHead() = ~1180 at max
                // Comms.writeCommMessageToHead(Comms.commType.LEAD, loc, SHAFlag.LEAD_LOCATION);

                // For lead, it turns out this overall consumes the least bytecodes in the worst case:
                // Takes ~440 bytecodes at max. With wipeChannel() = ~550 bytecodes at max.
                // Comms.writeCommMessageOverrwriteLesserPriorityMessage(Comms.commType.LEAD, intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);
                
                // Takes ~600 bytecodes at max. With wipeChannel() = ~710 bytecodes at max.
                // Comms.writeCommMessage(Comms.commType.LEAD, intFromMapLocation(loc), SHAFlag.LEAD_LOCATION);

                Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.LEAD, loc, Comms.SHAFlag.LEAD_LOCATION);
            }
            // bytecodediff = Math.max(bytecodeC - Clock.getBytecodesLeft(), bytecodediff);
            // System.out.println("D: Bytecode remaining: " + Clock.getBytecodesLeft());
        }
    }
    

    public static boolean isSafeToMine(MapLocation loc){
        RobotInfo[] potentialAttackers = rc.senseNearbyRobots(loc, -1, ENEMY_TEAM);
        for (RobotInfo enemy : potentialAttackers) {
            switch (enemy.type) {
                case SOLDIER:
                case WATCHTOWER:
                case SAGE:
                    return false;

                case ARCHON:
                case BUILDER:
                case LABORATORY:
                case MINER:
                    if (enemy.type.actionRadiusSquared >= loc.distanceSquaredTo(enemy.location))
                        return false;
                    break;
            }
        }
        return true;
    }


    public static void runAway() throws GameActionException{
        moveOut = false;
        RobotInfo nearestEnemy = null;
        int smallestDistSq = 999999;
        for (RobotInfo enemy : visibleEnemies) {

            if (enemy.type.canAttack()
            //  || enemy.type == RobotType.LAUNCHER // Currently not needed, might be needed after buffing of watchtowers
             ) {
                int distSq = currentLocation.distanceSquaredTo(enemy.location);
                if (distSq < smallestDistSq) {
                    smallestDistSq = distSq;
                    nearestEnemy = enemy;
                }
            }
        }
        if (nearestEnemy == null) return;

        Direction away = nearestEnemy.location.directionTo(currentLocation);
        Direction[] dirs = new Direction[] { away, away.rotateLeft(), away.rotateRight(), away.rotateLeft().rotateLeft(), away.rotateRight().rotateRight() };
        Direction flightDir = null;
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                // if (!inEnemyTowerOrHQRange(currentLocation.add(dir), enemyTowers)) { // this gets checked twice :(
                    if (isSafeToMine(currentLocation.add(dir))) {
                        if (rc.canMove(dir))
                            rc.move(dir);
                        return;
                    } else if (flightDir == null) {
                        flightDir = dir;
                    }
                // }
            }
        }
        if (flightDir != null) {
            rc.move(flightDir);
        }
    }


    public static void opportunisticMining() throws GameActionException{
        if (miningLocation == null) return;
        if (rc.canSenseLocation(miningLocation)) return;
        MapLocation[] nearbyLocations = rc.senseNearbyLocationsWithGold();
        if (nearbyLocations.length > 0){ 
            miningLocation = getBestGoldMine(nearbyLocations);
            return;
        }
        nearbyLocations = rc.senseNearbyLocationsWithLead(-1, 20);
        if (nearbyLocations.length > 0){ 
            miningLocation = getBestLeadMine(nearbyLocations);
            return;
        }
    }

    // public static void doMining() throws GameActionException{
    //         getMiningLocation();
    //         goToMine();
    // }

    public static void goMoveOut() throws GameActionException{
        inPlaceForMining = false;
        miningLocation = null;
        if (Clock.getBytecodesLeft() < 500) return;
        getMiningLocation();
        if (!rc.isMovementReady()) return;
        if (miningLocation != null) goToMine();
        else {
            // TODO: Do What???
        }
    }


    public static void toDieOrNotToDie(){
        if (desperationIndex > 50){
            System.out.println("Forced to take the final option");
            rc.disintegrate(); // Bye Bye
        }
    }


    /**
    * Run a single turn for a Miner.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runMiner(RobotController rc) throws GameActionException{
        updateMiner();
        // toDieOrNotToDie();

        // First, if miningLocation is null, get a new location
        if (miningLocation == null) getMiningLocation();

        // Adjacent to mining location, TODO: Find least rubble location
        if (miningLocation != null){
            if (currentLocation.distanceSquaredTo(miningLocation) <= 2) {
                mine(); // Let's do some mining!
            }
            else {
                opportunisticMining();
                goToMine();
            }
            // As previous else could change miningLocation
            if (miningLocation != null && currentLocation.distanceSquaredTo(miningLocation) <= 2) {
                mine(); // Let's do some mining!
            }
        }

        if (moveOut) goMoveOut();
        // if (Clock.getBytecodesLeft() < 2000) return;
        BotSoldier.sendCombatLocation(visibleEnemies);
        // if (Clock.getBytecodesLeft() < 2000) return;
        surveyForOpenMiningLocationsNearby();
        
    }

}