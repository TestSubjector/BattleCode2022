// Cleaned BotMiner code: Jan 15
package AFoolsGoldBot;

import battlecode.common.*;

public class BotMiner2 extends Explore{

    public static boolean isMinedThisTurn, inPlaceForMining, prolificMiningLocationsAtBirth;
    private static MapLocation miningLocation;
    private static RobotInfo[] visibleEnemies;
    private static boolean isFleeing, moveOut, tooCrowded, depleteMine;
    private static final boolean searchByDistance = false;
    private static final int CROWD_LIMIT = 3;
    private static int DEPLETE_MINE_RADIUS_LIMIT;
    public static final boolean DEPLETE_FROM_ENEMY_LOCATIONS = true;
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
        DEPLETE_MINE_RADIUS_LIMIT = (int)((double)Math.min(MAP_WIDTH, MAP_HEIGHT) * 0.22d);
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
        // adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), MINER_VISION_RADIUS);
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
        if (BotArchon.SMALL_MAP)
            depleteMine = checkIfEnemyArchonInVision();
        else
            depleteMine = (checkIfEnemyArchonInVision() || checkIfToDepleteMine());
        // depleteMine = (checkIfToDepleteMine() || checkIfEnemyArchonInVision());
        if (!isSafeToMine(rc.getLocation())){
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
        for (int i = Comms.CHANNEL_ARCHON_START; i < Comms.channelArchonStop; i += 4){
            MapLocation alliedLoc = Comms.readLocationFromMessage(rc.readSharedArray(i));
            for (int j = Comms.CHANNEL_ARCHON_START + 1; j < Comms.channelArchonStop; j += 4){
                MapLocation enemyLoc = Comms.readLocationFromMessage(rc.readSharedArray(j));
                if ((rc.getLocation().distanceSquaredTo(enemyLoc)) >= (int)(((double)rc.getLocation().distanceSquaredTo(alliedLoc)) / 1.30d)) return false;
            }
        }
        return true;
        // int add;
        // if (DEPLETE_FROM_ENEMY_LOCATIONS) add = 1;
        // else add = 0;
        // for (int i = Comms.CHANNEL_ARCHON_START + add; i < Comms.channelArchonStop; i += 4){
        //     MapLocation loc = Comms.readLocationFromMessage(rc.readSharedArray(i));
        //     if (loc.distanceSquaredTo(rc.getLocation()) <= DEPLETE_MINE_RADIUS_LIMIT) return DEPLETE_FROM_ENEMY_LOCATIONS;
        // }
        // return !DEPLETE_FROM_ENEMY_LOCATIONS;
    }


    private static void resetVariables(){
        miningLocation = null;
        inPlaceForMining = false;
        // moveOut = true; 
        isFleeing = false;
        depleteMine = false;
    }

    public static void mine() throws GameActionException{
        if (!rc.isActionReady()) return;
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2);
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
            if (searchByDistance) curValue = -rc.getLocation().distanceSquaredTo(loc);
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
            if (searchByDistance) curValue = -rc.getLocation().distanceSquaredTo(loc);
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
        if (rc.senseGold(rc.getLocation()) > 0 || rc.senseLead(rc.getLocation()) > 20){
            return rc.getLocation();
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
        int curDist = rc.getLocation().distanceSquaredTo(miningLocation);
        if (curDist <= 2) { // Reached location
            inPlaceForMining = true;
            mine();
            return;
        }
        // If outside of vision or Location is not occupied:
        if (curDist > MINER_VISION_RADIUS || !rc.canSenseRobotAtLocation(miningLocation)){
        // if (curDist > MINER_VISION_RADIUS || !isLocationBeingMined(miningLocation)){
            BFS.move(miningLocation);
            return;
        }
        // miningLocation is inside vision range and is occupied now:
        moveOut = true;
        return;
        // miningLocation = null;
        // inPlaceForMining = false;
        // if (Clock.getBytecodesLeft() < 3000) return;
        // getMiningLocation();
        // if (!rc.isMovementReady()) return;
        
        // // goToMine(); // Be careful of recursive calls.
        // if (miningLocation != null) BFS.move(miningLocation);
        // if (miningLocation!= null && !BFS.move(miningLocation)) desperationIndex++;
    }


    public static int getCountOfAdjacentBots(MapLocation location) throws GameActionException{
        int count = 0;
        for (Direction dir: directions){
            MapLocation loc = location.add(dir);
            if (rc.canSenseLocation(loc) && rc.canSenseRobotAtLocation(loc))
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
            miningLocation = Comms.findNearestLocationOfThisTypeAndWipeChannel(rc.getLocation(), Comms.commType.LEAD, Comms.SHAFlag.LEAD_LOCATION);
        else
            miningLocation = Comms.findNearestLocationOfThisTypeOutOfVisionAndWipeChannel(rc.getLocation(), Comms.commType.LEAD, Comms.SHAFlag.LEAD_LOCATION);
        if (miningLocation != null) return true;
        return false;
    }


    public static boolean foundMiningLocationFromVision() throws GameActionException{
        if (prolificMiningLocationsAtBirth){
            miningLocation = Movement.moveToLattice(10, 0);
            return (miningLocation != null);
        }
        miningLocation = findOpenMiningLocationNearby();
        if (miningLocation != null){
            inPlaceForMining = (rc.getLocation().distanceSquaredTo(miningLocation) <= 2);
            return true;
        }
        return false;
    }


    public static Direction biasedRandomDirectionGenerator(Direction bias){
        return directions[biasedRandomNumberGenerator(0, 7, bias.ordinal(), 50)]; // tune the biasPercentage
    }


    public static Direction findBiasDirection(){
        return rc.getLocation().directionTo(CENTER_OF_THE_MAP);
    }


    public static int biasedRandomNumberGenerator(int start, int end, int bias, int biasPercentage){
        int excessCount = (int) (((double)(biasPercentage *(end - start + 1) - 100)) / ((double)(100.0d - biasPercentage)));
        int size = excessCount + end - start + 1;
        int rnd = (Globals.rng.nextInt(size)) + start;
        if (rnd < bias) return rnd;
        else if (rnd >= bias && rnd <= bias + excessCount) return bias;
        else return rnd - excessCount;
    }


    public static void getExploreDir(){
        MapLocation closestArchon = getClosestArchonLocation();
        if (rc.canSenseLocation(closestArchon)){
            // if(BIRTH_ROUND % 8 == 0) {
            //     assignExplore3Dir(Direction.NORTHEAST);
            //     // currentDestination = ratioPointBetweenTwoMapLocations(parentArchonLocation, rememberedEnemyArchonLocations[0], 0.15);
            // }
            // else if (BIRTH_ROUND % 8 == 1){
            //     assignExplore3Dir(Direction.SOUTHEAST);
            //     // currentDestination = ratioPointBetweenTwoMapLocations(parentArchonLocation, rememberedEnemyArchonLocations[1], 0.15);
            // }
            // else if (BIRTH_ROUND % 8 == 2){
            //     assignExplore3Dir(Direction.NORTHWEST);
            //     // currentDestination = ratioPointBetweenTwoMapLocations(parentArchonLocation, rememberedEnemyArchonLocations[2], 0.15);
            // }
            // else if (BIRTH_ROUND % 8 == 3){
            //     assignExplore3Dir(Direction.SOUTHWEST);
            // }
            // else if (BIRTH_ROUND % 8 == 4){
            //     assignExplore3Dir(Direction.NORTH);
            // }
            // else if (BIRTH_ROUND % 8 == 5){
            //     assignExplore3Dir(Direction.WEST);
            // }
            // else if (BIRTH_ROUND % 8 == 6){
            //     assignExplore3Dir(Direction.SOUTH);
            // }
            // else{
            //     assignExplore3Dir(Direction.EAST);
            // }
            if (BIRTH_ROUND % 3 == 0)
                assignExplore3Dir(rc.getLocation().directionTo(CENTER_OF_THE_MAP));
            else
                assignExplore3Dir(closestArchon.directionTo(rc.getLocation()));
        }
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

        // int cx = rc.getLocation().x, cy = rc.getLocation().y, px = closestArchon.x, py = closestArchon.y;
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
        miningLocation = null;
        inPlaceForMining = false;
        BFS.move(explore());
        moveOut = true;
        // if (!BFS.move(explore())) desperationIndex++;
        // if (!Movement.goToDirect(explore())) desperationIndex++;
        // if (!Movement.goToDirect(rc.getLocation().add(persistingRandomMovement()))) desperationIndex++;
        
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
            if (rc.getLocation().distanceSquaredTo(loc) > 2 && goodMiningSpot(loc)){
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
                int distSq = rc.getLocation().distanceSquaredTo(enemy.location);
                if (distSq < smallestDistSq) {
                    smallestDistSq = distSq;
                    nearestEnemy = enemy;
                }
            }
        }
        if (nearestEnemy == null) return;

        Direction away = nearestEnemy.location.directionTo(rc.getLocation());
        Direction[] dirs = new Direction[] { away, away.rotateLeft(), away.rotateRight(), away.rotateLeft().rotateLeft(), away.rotateRight().rotateRight() };
        double bestCost = Double.MAX_VALUE;
        Direction bestDir = null;
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                MapLocation loc = rc.getLocation().add(dir);
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
            inPlaceForMining = (rc.getLocation().distanceSquaredTo(miningLocation) <= 2);
            moveOut = false; // new
            return;
        }
        if (!depleteMine) nearbyLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 20);
        else nearbyLocations = rc.senseNearbyLocationsWithLead();
        if (nearbyLocations.length > 0){ 
            miningLocation = findOptimalLocationForMiningLead(nearbyLocations);
            if (miningLocation != null){ 
                inPlaceForMining = (rc.getLocation().distanceSquaredTo(miningLocation) <= 2);
                moveOut = false;
            }
            else inPlaceForMining = false;
            return;
        }
    }


    public static void moveIfNeeded() throws GameActionException{
        int rubbleVal = Integer.MAX_VALUE;
        int homeRubbleVal = rc.senseRubble(rc.getLocation());
        MapLocation selectedLoc = null;
        MapLocation[] mineAdjacentLocations = rc.getAllLocationsWithinRadiusSquared(miningLocation, MINER_ACTION_RADIUS);
        for(MapLocation loc : mineAdjacentLocations){
            if (!rc.canSenseLocation(loc)) continue;
            int curRubbleVal = rc.senseRubble(loc);
            if (homeRubbleVal > curRubbleVal && rubbleVal >= curRubbleVal)
                selectedLoc = loc;
        }
        Direction dir = null;
        if (selectedLoc != null){
            dir = rc.getLocation().directionTo(selectedLoc);
            if (rc.canMove(dir)) rc.move(dir);
        }
    }


    public static void doMining() throws GameActionException{
        if (isFleeing) return;
        opportunisticMining();
        if (inPlaceForMining) moveIfNeeded();
        else if (miningLocation != null) goToMine();
        else{
            if (lowHealthStratInPlay) return;
            getMiningLocation();
            goToMine();
        }
    }


    public static void goMoveOut() throws GameActionException{
        if (!moveOut) return;
        inPlaceForMining = false;
        miningLocation = null;
        if (Clock.getBytecodesLeft() < 500) return;
        getMiningLocation();
        if (!rc.isMovementReady()) return;
        if (miningLocation != null) goToMine();
        else BFS.move(explore());
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
            System.out.println("Warning, Should never happen");
            lowHealthStratArchon = getClosestArchonLocation();
        }
        if (lowHealthStratArchon == null){
            System.out.println("Warning, Still happening?!!");
            return;
        }
        if (rc.getLocation().distanceSquaredTo(lowHealthStratArchon) > ARCHON_ACTION_RADIUS){
            if (!isFleeing) BFS.move(lowHealthStratArchon);
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
        doMining();
        mine();
        goMoveOut();
        mine();
        BotSoldier.sendCombatLocation(visibleEnemies);
    }

}


