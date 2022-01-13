package ANewFrontierBot;

import ANewFrontierBot.Comms.SHAFlag;
import battlecode.common.*;

public class BotMiner extends explore{

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


    public static boolean areMiningLocationsAbundant(){
        try{
            return (rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS).length > 30);
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static void initBotMiner() throws GameActionException{
        prolificMiningLocationsAtBirth = areMiningLocationsAbundant();
        resetVariables();
        // explore();
    }


    public static void updateMiner() throws GameActionException{
        isMinedThisTurn = false;
        moveOut = true;
        tooCrowded = false; 
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

    public static void mine() throws GameActionException{
        if (!rc.isActionReady()) return;
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(currentLocation, 2);
        for (MapLocation loc : adjacentLocations){
            while(rc.canMineGold(loc)){
                isMinedThisTurn = true;
                rc.mineGold(loc);
            }
            while (rc.canMineLead(loc)){
                if (rc.senseLead(loc) == 1)
                    break;
                isMinedThisTurn = true;
                rc.mineLead(loc);
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
        potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 5);
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
            miningLocation = Comms.findNearestLocationOfThisTypeAndWipeChannel(currentLocation, Comms.commType.LEAD, SHAFlag.LEAD_LOCATION);
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
            miningLocation = Movement.moveToLattice(2, 0);
            return (miningLocation != null);
        }
        miningLocation = findOpenMiningLocationNearby();
        if (miningLocation != null){
            inPlaceForMining = (currentLocation.distanceSquaredTo(miningLocation) <= 2);
            return true;
        }
        return false;
    }


    public static void getExploreDir(){
        MapLocation closestArchon = getClosestArchonLocation();
        if (rc.canSenseLocation(closestArchon)) 
            assignExplore3Dir(closestArchon.directionTo(currentLocation));
        else assignExplore3Dir(directions[(int)(Math.random()*8)]);
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
        RobotInfo[] potentialAttackers = rc.senseNearbyRobots(loc, 24, ENEMY_TEAM);
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
            miningLocation = findOptimalLocationForMiningGold(nearbyLocations);
            inPlaceForMining = (currentLocation.distanceSquaredTo(miningLocation) <= 2);
            return;
        }
        nearbyLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 20);
        if (nearbyLocations.length > 0){ 
            miningLocation = findOptimalLocationForMiningLead(nearbyLocations);
            if (miningLocation != null) inPlaceForMining = (currentLocation.distanceSquaredTo(miningLocation) <= 2);
            else inPlaceForMining = false;
            return;
        }
    }


    public static void doMining() throws GameActionException{
        opportunisticMining();
        if (inPlaceForMining){
            // if (isSafeToMine(currentLocation))
            mine();
            // else runAway();
        }
        else if (miningLocation != null){
            goToMine();
        }
        else{
            getMiningLocation();
            goToMine();
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


    /**
    * Run a single turn for a Miner.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runMiner(RobotController rc) throws GameActionException{
        updateMiner();

        // toDieOrNotToDie();

        doMining();

        if (moveOut) goMoveOut();
        // if (Clock.getBytecodesLeft() < 2000) return;
        BotSoldier.sendCombatLocation(visibleEnemies);
        // if (Clock.getBytecodesLeft() < 2000) return;
        surveyForOpenMiningLocationsNearby();
        
    }

}