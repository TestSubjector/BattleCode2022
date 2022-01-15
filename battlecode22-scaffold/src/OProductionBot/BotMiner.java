package OProductionBot;

import battlecode.common.*;

public class BotMiner extends Explore{

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
    private static boolean tooCrowded;
    private static final int CROWD_LIMIT = 3;
    private static boolean depleteMine;
    private static int DEPLETE_MINE_RADIUS_LIMIT;
    private static final boolean DEPLETE_FROM_ENEMY_LOCATIONS = true;
    private static final int LOW_HEALTH_STRAT_TRIGGER = (int)((MAX_HEALTH*3.0d)/10.0d);
    private static final int LOW_HEALTH_STRAT_RELEASER = (int)((MAX_HEALTH*8.0d)/10.0d);
    public static boolean lowHealthStratInPlay = false;
    public static MapLocation lowHealthStratArchon;
    

    public static boolean areMiningLocationsAbundant(){
        try{
            return (rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS).length > 30);
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static void setDepleteMineRadius(){
        // TODO: Tune this fraction
        DEPLETE_MINE_RADIUS_LIMIT = (int)(((double)Math.min(MAP_WIDTH, MAP_HEIGHT) * 5.0d)/13.0d);
        DEPLETE_MINE_RADIUS_LIMIT *= DEPLETE_MINE_RADIUS_LIMIT;
    }


    public static void initBotMiner() throws GameActionException{
        prolificMiningLocationsAtBirth = areMiningLocationsAbundant();
        resetVariables();
        setDepleteMineRadius();
        lowHealthStratInPlay = false;
        lowHealthStratArchon = null;
        // System.out.println("Low Health Strategy Trigger value: " + LOW_HEALTH_STRAT_TRIGGER);
        // System.out.println("Low Health Strategy Trigger releaser: " + LOW_HEALTH_STRAT_RELEASER);
        // adjacentLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, MINER_VISION_RADIUS);
        // explore();
    }


    public static boolean isLocationBeingMined(MapLocation loc) throws GameActionException{
        MapLocation[] locAdjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, MINER_ACTION_RADIUS);
        for (MapLocation curLoc : locAdjacentLocations){
            if (!rc.canSenseLocation(curLoc)) continue;
            RobotInfo bot = rc.senseRobotAtLocation(curLoc);
            if (bot != null && bot.team == MY_TEAM && bot.type == RobotType.MINER) return true;
        }
        return false;
    }


    public static void updateMiner() throws GameActionException{
        isMinedThisTurn = false;
        moveOut = true;
        tooCrowded = false; 
        isFleeing = false;
        minerComms();
        updateVision();
        // depleteMine = checkIfEnemyArchonInVision();
        depleteMine = (checkIfEnemyArchonInVision() || checkIfToDepleteMine());
        // depleteMine = (checkIfToDepleteMine() || checkIfEnemyArchonInVision());
        if (!isSafeToMine(currentLocation)){
            isFleeing = true;
            isFleeing = BotSoldier.tryToBackUpToMaintainMaxRange(visibleEnemies);
        }
    }


    public static boolean checkIfEnemyArchonInVision() throws GameActionException{
        for (RobotInfo bot : visibleEnemies){
            if (bot.type == RobotType.ARCHON){
                Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, bot.getLocation(), Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION);
                // Comms.writeCommMessageUsingQueueWithoutRedundancy(Comms.commType.COMBAT, bot.getLocation(), Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION);
                return true;
            }
        }
        return false;
    }


    public static boolean checkIfToDepleteMine() throws GameActionException{
        int add;
        if (DEPLETE_FROM_ENEMY_LOCATIONS) add = 1;
        else add = 0;
        for (int i = Comms.CHANNEL_ARCHON_START + add; i < Comms.channelArchonStop; i += 4){
            MapLocation loc = Comms.readLocationFromMessage(rc.readSharedArray(i));
            if (loc.distanceSquaredTo(currentLocation) <= DEPLETE_MINE_RADIUS_LIMIT) return DEPLETE_FROM_ENEMY_LOCATIONS;
        }
        return !DEPLETE_FROM_ENEMY_LOCATIONS;
        // for (int i = Comms.CHANNEL_ARCHON_START; i < Comms.channelArchonStop; i += 4){
        //     MapLocation loc = Comms.readLocationFromMessage(rc.readSharedArray(i));
        //     if (loc.distanceSquaredTo(currentLocation) <= DEPLETE_MINE_RADIUS_LIMIT) return false;
        // }
        // return true;
    }


    private static void resetVariables(){
        miningLocation = null;
        inPlaceForMining = false;
        commitSuicide = false;
        suicideLocation = null;
        desperationIndex = 0;
        moveOut = true; 
        isFleeing = false;
        depleteMine = false;
    }

    public static void mine() throws GameActionException{
        if (!rc.isActionReady()) return;
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, 2);
        for (MapLocation loc : adjacentLocations){
            while(rc.canMineGold(loc)){
                isMinedThisTurn = true;
                rc.mineGold(loc);
            }
            int leadCount = rc.senseLead(loc);
            while (rc.canMineLead(loc)){
                if (!depleteMine && leadCount == 1)
                    break;
                isMinedThisTurn = true;
                rc.mineLead(loc);
                leadCount--;
            }
        }
    }



    public static void minerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_MINER_COUNT);
        Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
        Comms.updateComms();
    }

    public static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(MINER_VISION_RADIUS, ENEMY_TEAM);
    }


    public static MapLocation findOptimalLocationForMiningGold(MapLocation[] locations) throws GameActionException{
        if (locations.length == 1) return locations[0];
        MapLocation opt = null;
        double value = 0;
        for (MapLocation loc : locations){
            double curValue;
            if (searchByDistance) curValue = -currentLocation.distanceSquaredTo(loc);
            else curValue = ((double)rc.senseGold(loc)) / ((double)rc.senseRubble(loc));
            if (opt == null || curValue > value){
                opt = loc;
                value = curValue;
            }
        }
        return opt;
    }


    public static MapLocation findOptimalLocationForMiningLead(MapLocation[] locations) throws GameActionException{
        if (locations.length == 1) return locations[0];
        MapLocation opt = null;
        double value = 0;
        for (MapLocation loc : locations){
            double curValue;
            if (searchByDistance) curValue = -currentLocation.distanceSquaredTo(loc);
            else curValue = ((double)rc.senseGold(loc)) / ((double)rc.senseRubble(loc));;
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


    public static MapLocation findOpenMiningLocationNearby() throws GameActionException{
        if (prolificMiningLocationsAtBirth){
            return Movement.moveToLattice(2, 0);
        }
        if (rc.senseGold(currentLocation) > 0 || rc.senseLead(currentLocation) > 20){
            return currentLocation;
        }
        if (countOfMinersInVicinity() > CROWD_LIMIT){ 
            tooCrowded = true;
            return null;
        }
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithGold();
        if (potentialMiningLocations.length > 0) return findOptimalLocationForMiningGold(potentialMiningLocations);
        if (!depleteMine) 
        potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 5);
        else potentialMiningLocations = rc.senseNearbyLocationsWithLead();
        if (potentialMiningLocations.length > 0) return findOptimalLocationForMiningLead(potentialMiningLocations);
        return null;
    }


    public static void goToMine() throws GameActionException{
        moveOut = false;
        if (miningLocation == null) return;
        // miningLocation is not null from now on:
        if (inPlaceForMining){
            mine();
            return;
        }
        // inPlaceForMining = false from now on: 
        int curDist = currentLocation.distanceSquaredTo(miningLocation);
        if (curDist <= 2) { // Reached location
            inPlaceForMining = true;
            mine();
            return;
        }
        // If outside of vision or Location is not occupied:
        if (curDist > MINER_VISION_RADIUS || !rc.isLocationOccupied(miningLocation)){
        // if (curDist > MINER_VISION_RADIUS || !isLocationBeingMined(miningLocation)){
            if (!BFS.move(miningLocation)) desperationIndex++;
            // if (!Movement.goToDirect(miningLocation)) desperationIndex++;
            return;
        }
        // miningLocation is inside vision range and is occupied now:
        miningLocation = null;
        inPlaceForMining = false;
        if (Clock.getBytecodesLeft() < 3000) return;
        getMiningLocation();
        if (!rc.isMovementReady()) return;
        
        // goToMine(); // Be careful of recursive calls.
        if (miningLocation!= null && !BFS.move(miningLocation)) desperationIndex++;
        // if (miningLocation!= null && !Movement.goToDirect(miningLocation)) desperationIndex++;
    }


    public static MapLocation findGoodPlaceToDie() throws GameActionException{
        return Movement.moveToLattice(MIN_SUICIDE_DIST, 0);
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
        if (!tooCrowded) 
            miningLocation = Comms.findNearestLocationOfThisTypeAndWipeChannel(currentLocation, Comms.commType.LEAD, Comms.SHAFlag.LEAD_LOCATION);
        else
            miningLocation = Comms.findNearestLocationOfThisTypeOutOfVisionAndWipeChannel(currentLocation, Comms.commType.LEAD, Comms.SHAFlag.LEAD_LOCATION);
        if (miningLocation != null){
            // desperationIndex--;
            desperationIndex = 0;
            // exploreDir = CENTER;
            return true;
        }
        return false;
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


    public static boolean foundMiningLocationFromVision() throws GameActionException{
        if (prolificMiningLocationsAtBirth){
            miningLocation = Movement.moveToLattice(10, 0);
            return (miningLocation != null);
        }
        miningLocation = findOpenMiningLocationNearby();
        if (miningLocation != null){
            inPlaceForMining = (currentLocation.distanceSquaredTo(miningLocation) <= 2);
            return true;
        }
        return false;
    }


    public static Direction biasedRandomDirectionGenerator(Direction bias){
        return directions[biasedRandomNumberGenerator(0, 8, bias.ordinal(), 50)]; // tune the biasPercentage
    }


    public static Direction findBiasDirection(){
        return currentLocation.directionTo(CENTER_OF_THE_MAP);
    }


    public static int biasedRandomNumberGenerator(int start, int end, int bias, int biasPercentage){
        int excessCount = ((end - start) * biasPercentage)/100;
        int size = excessCount + end - start + 1;
        int rnd = (int)(Math.random()*size) + start;
        if (rnd < bias) return rnd;
        else if (rnd >= bias && rnd <= bias + excessCount) return bias;
        else return rnd - excessCount;
    }


    public static void getExploreDir(){
        MapLocation closestArchon = getClosestArchonLocation();
        if (rc.canSenseLocation(closestArchon)) 
            assignExplore3Dir(closestArchon.directionTo(currentLocation));
        // else assignExplore3Dir(directions[(int)(Math.random()*8)]);
        else assignExplore3Dir(directions[Globals.rng.nextInt(8)]);
        // else{
        //     System.out.println("Biased random direction being used");
        //     assignExplore3Dir(biasedRandomDirectionGenerator(findBiasDirection()));
        // }
    }



    public static MapLocation explore() throws GameActionException{
        if (exploreDir == CENTER)
            getExploreDir();
        return getExplore3Target();

        // int cx = currentLocation.x, cy = currentLocation.y, px = closestArchon.x, py = closestArchon.y;
        // int newX = Math.min(Math.max(2*cx - px, 0), MAP_WIDTH - 1), newY = Math.min(Math.max(2*cy - py, 0), MAP_HEIGHT-1);
        // return (new MapLocation(newX, newY));
    }


    public static void getMiningLocation() throws GameActionException{
        moveOut = false;
        if (miningLocation != null) System.out.println("Location already found?? Something's wrong.");
        if(foundMiningLocationFromVision()) return;
        
        // TODO - Tune this weight
        // if (turnCount > 900 && foundMiningLocationFromComms()) return;
        // if (foundMiningLocationFromComms()) return;
        // Explore now how?
        // Head away from parent Archon to explore. Might get us new mining locations
        
        if (!BFS.move(explore())) desperationIndex++;
        // if (!Movement.goToDirect(explore())) desperationIndex++;
        // if (!Movement.goToDirect(currentLocation.add(persistingRandomMovement()))) desperationIndex++;
        
        // The Final Option:
        // if (goAheadAndDie()) return;
        // Now what??

        // System.out.println("This code is never run, right?!");
        // commitSuicide = false;
        // desperationIndex++;
        // Now what to do?
    }


    public static boolean goodMiningSpot(MapLocation loc) throws GameActionException{
        return (rc.senseLead(loc) > 40 || rc.senseGold(loc) > 0);
    }


    public static void surveyForOpenMiningLocationsNearby() throws GameActionException{
        MapLocation[] potentialMiningLocations = rc.senseNearbyLocationsWithLead(UNIT_TYPE.visionRadiusSquared);
        for (MapLocation loc : potentialMiningLocations){  // Team bias
            if (Clock.getBytecodesLeft() < 1200)
                break;
            if (currentLocation.distanceSquaredTo(loc) > 2 && goodMiningSpot(loc)){
                Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.LEAD, loc, Comms.SHAFlag.LEAD_LOCATION);
            }
        }
    }
    

    public static boolean isSafeToMine(MapLocation loc){
        for (RobotInfo enemy : visibleEnemies) {
            switch (enemy.type) {
                case SOLDIER:
                case WATCHTOWER:
                case SAGE:
                    if (enemy.type.actionRadiusSquared >= loc.distanceSquaredTo(enemy.location))
                    return false;

                default:
                    return true;
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
        double bestCost = Double.MAX_VALUE;
        Direction bestDir = null;
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                MapLocation loc = currentLocation.add(dir);
                int rubbleValue = rc.senseRubble(loc);
                
                if (bestCost >  rc.senseRubble(loc)){
                    bestCost = rubbleValue;
                    bestDir = dir;
                }
            }
        }
        if (bestDir != null) {
            rc.move(bestDir);
            exploreDir = bestDir;
            assignExplore3Dir(exploreDir);
        }
    }


    public static void opportunisticMining() throws GameActionException{
        if (miningLocation == null || rc.canSenseLocation(miningLocation)) return;
        MapLocation[] nearbyLocations = rc.senseNearbyLocationsWithGold();
        if (nearbyLocations.length > 0){ 
            miningLocation = findOptimalLocationForMiningGold(nearbyLocations);
            inPlaceForMining = (currentLocation.distanceSquaredTo(miningLocation) <= 2);
            return;
        }
        if (!depleteMine) nearbyLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 20);
        else nearbyLocations = rc.senseNearbyLocationsWithLead();
        if (nearbyLocations.length > 0){ 
            miningLocation = findOptimalLocationForMiningLead(nearbyLocations);
            if (miningLocation != null) inPlaceForMining = (currentLocation.distanceSquaredTo(miningLocation) <= 2);
            else inPlaceForMining = false;
            return;
        }
    }


    public static void moveIfNeeded() throws GameActionException{
        // int leadVal = -1;
        int rubbleVal = Integer.MAX_VALUE;
        int homeRubbleVal = rc.senseRubble(currentLocation);
        MapLocation selectedLoc = null;
        MapLocation[] mineAdjacentLocations = rc.getAllLocationsWithinRadiusSquared(miningLocation, MINER_ACTION_RADIUS);
        for(MapLocation loc : mineAdjacentLocations){
            int curRubbleVal = rc.senseRubble(loc);
            if (homeRubbleVal > curRubbleVal && rubbleVal >= curRubbleVal){
                // if (rubbleVal == curRubbleVal){
                selectedLoc = loc;
                // }
                // leadVal = curLeadVal;
                
            }
        }
        Direction dir = null;
        if (selectedLoc != null){
            dir = currentLocation.directionTo(selectedLoc);
            if (rc.canMove(dir)){
                rc.move(dir);
                // miningLocation = selectedLoc;
                // inPlaceForMining = true;
            }
        }
        // if (rc.canMove(selectedLoc)) rc.move(dir);
    }


    public static void doMining() throws GameActionException{
        if (isFleeing) return;
        opportunisticMining();
        if (inPlaceForMining){
            // if (isSafeToMine(currentLocation))
            moveIfNeeded();
            mine();
            // else runAway();
            // else BotSoldier.tryToBackUpToMaintainMaxRange(visibleEnemies);
        }
        else if (miningLocation != null){
            goToMine();
            mine();
        }
        else{
            if (lowHealthStratInPlay) return;
            getMiningLocation();
            goToMine();
            mine();
        }
    }


    public static void goMoveOut() throws GameActionException{
        inPlaceForMining = false;
        miningLocation = null;
        if (Clock.getBytecodesLeft() < 500) return;
        getMiningLocation();
        if (!rc.isMovementReady()) return;
        if (miningLocation != null) goToMine();
        else {
            BFS.move(explore());
        }
    }


    public static void toDieOrNotToDie(){
        if (desperationIndex > 50){
            System.out.println("Forced to take the final option");
            rc.disintegrate(); // Bye Bye
        }
    }


    public static void lowHealthStrategy() throws GameActionException{
        if (lowHealthStratInPlay && rc.getHealth() > LOW_HEALTH_STRAT_RELEASER){
            lowHealthStratInPlay = false;
            return;
        }
        if (!lowHealthStratInPlay && rc.getHealth() < LOW_HEALTH_STRAT_TRIGGER){ 
            // System.out.println("Current Health: " + rc.getHealth());
            // System.out.println("Low health strat trigger: " + LOW_HEALTH_STRAT_TRIGGER);
            lowHealthStratInPlay = true;
            lowHealthStratArchon = getClosestArchonLocation();
        }
        if (!lowHealthStratInPlay) return;
        if (lowHealthStratArchon == null){
            System.out.println("Should never happen");
            lowHealthStratArchon = getClosestArchonLocation();
        }
        if (lowHealthStratArchon == null){
            System.out.println("Still happening?!!");
            return;
        }
        if (currentLocation.distanceSquaredTo(lowHealthStratArchon) > ARCHON_ACTION_RADIUS){
            BFS.move(lowHealthStratArchon);
            return;
        }
    }



    /**
    * Run a single turn for a Miner.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runMiner(RobotController rc) throws GameActionException{
        mine();
        updateMiner();
        lowHealthStrategy();
        // toDieOrNotToDie();

        doMining();
        // if (Clock.getBytecodesLeft() < 1000) return;
        if (moveOut) goMoveOut();
        mine();
        // if (Clock.getBytecodesLeft() < 2000) return;
        BotSoldier.sendCombatLocation(visibleEnemies);
        // if (Clock.getBytecodesLeft() < 2000) return;
        // surveyForOpenMiningLocationsNearby();
        
    }

}