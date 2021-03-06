package OSageActionBot;

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
    private static final int LOW_HEALTH_STRAT_TRIGGER = (int)((MAX_HEALTH*3.0d)/10.0d);
    private static final int LOW_HEALTH_STRAT_RELEASER = (int)((MAX_HEALTH*8.0d)/10.0d);
    public static boolean lowHealthStratInPlay = false;
    public static MapLocation lowHealthStratArchon;
    public static int fleeCount;
    private static MapLocation finalDestination = null; 

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
        lowHealthStratInPlay = false;
        lowHealthStratArchon = null;
        fleeCount = 0;
    }


    public static boolean isLocationBeingMined(MapLocation loc) throws GameActionException{
        MapLocation[] locAdjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, MINER_ACTION_RADIUS);
        for (int i = locAdjacentLocations.length; i-->0;){
            MapLocation curLoc = locAdjacentLocations[i];
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
        fleeCount = Math.max(0, fleeCount - 1);
        if (fleeCount == 0) isFleeing = false;
        else isFleeing = true;
        minerComms();
        updateVision();
        if (BotArchon.SMALL_MAP)
            depleteMine = checkIfEnemyArchonInVision();
        else
            depleteMine = (checkIfEnemyArchonInVision() || checkIfToDepleteMine());
        // depleteMine = (checkIfToDepleteMine() || checkIfEnemyArchonInVision());
        if (!isSafeToMine(rc.getLocation())){
            isFleeing = true;
            miningLocation = null;
            inPlaceForMining = false;
            isFleeing = CombatUtil.tryToBackUpToMaintainMaxRangeMiner(visibleEnemies);
            fleeCount = 5;
        }
    }


    private static boolean checkIfEnemyArchonInVision() throws GameActionException{
        for (int i = visibleEnemies.length; i-->0;){
            RobotInfo bot = visibleEnemies[i];
            if (bot.type == RobotType.ARCHON){
                Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, bot.getLocation(), Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION);
                return true;
            }
        }
        return false;
    }


    public static boolean checkIfToDepleteMine() throws GameActionException{
        boolean checkedOnce = false;
        for (int j = Comms.CHANNEL_ARCHON_START + 1; j < Comms.channelArchonStop; j += 4){
            int message = rc.readSharedArray(j);
            MapLocation enemyLoc = Comms.readLocationFromMessage(message);
            if (Comms.readSHAFlagFromMessage(message) != Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION) continue;
            checkedOnce = true;
            for (int i = Comms.CHANNEL_ARCHON_START; i < Comms.channelArchonStop; i += 4){
                MapLocation alliedLoc = Comms.readLocationFromMessage(rc.readSharedArray(i));
                if ((rc.getLocation().distanceSquaredTo(enemyLoc)) >= (int)(((double)rc.getLocation().distanceSquaredTo(alliedLoc)) / 1.20d)) return false;
            }
        }
        if (checkedOnce) return true;
        else return false;
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
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2);
        for (int i = adjacentLocations.length; i-->0;){
            MapLocation loc = adjacentLocations[i];
            if (!rc.isActionReady()) return;
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
        for (int i = locations.length; i-->0;){
            MapLocation loc = locations[i];
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
        for (int i = locations.length; i-->0;){
            MapLocation loc = locations[i];
            double curValue;
            if (searchByDistance) curValue = -rc.getLocation().distanceSquaredTo(loc);
            else curValue = ((double)rc.senseLead(loc) + 1) / ((double)rc.senseRubble(loc) + 1);;
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
        for (int i = visibleAllies.length; i-->0;){
            if (visibleAllies[i].type == RobotType.MINER) count++;
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
            potentialMiningLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 8);
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
        if (curDist > MINER_VISION_RADIUS || !rc.isLocationOccupied(miningLocation)){
        // if (curDist > MINER_VISION_RADIUS || !isLocationBeingMined(miningLocation)){
            if (!BFS.move(miningLocation)) desperationIndex++;
            // if (!Movement.goToDirect(miningLocation)) desperationIndex++;
            return;
        }
        // miningLocation is inside vision range and is occupied now:
        miningLocation = null;
        inPlaceForMining = false;
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
        // if (!tooCrowded) 
        //     miningLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.MINER, Comms.SHAFlag.LEAD_LOCATION);
        //     // miningLocation = Comms.findNearestLocationOfThisTypeAndWipeChannel(rc.getLocation(), Comms.commType.MINER, Comms.SHAFlag.LEAD_LOCATION);
        // else
        miningLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.MINER, Comms.SHAFlag.LEAD_LOCATION);
            // miningLocation = Comms.findNearestLocationOfThisTypeOutOfVisionAndWipeChannel(rc.getLocation(), Comms.commType.MINER, Comms.SHAFlag.LEAD_LOCATION);
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
            inPlaceForMining = (rc.getLocation().distanceSquaredTo(miningLocation) <= 2);
            return true;
        }
        return false;
    }


    public static Direction biasedRandomDirectionGenerator(Direction bias){
        return directions[biasedRandomNumberGenerator(0, 8, bias.ordinal(), 50)]; // tune the biasPercentage
    }


    public static Direction findBiasDirection(){
        return rc.getLocation().directionTo(CENTER_OF_THE_MAP);
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
            assignExplore3Dir(closestArchon.directionTo(rc.getLocation()));
        else
            assignExplore3Dir(directions[Globals.rng.nextInt(8)]);
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

        if(foundMiningLocationFromVision()){ 
            rc.setIndicatorString("vision location found: " + miningLocation);
            return;
        }
        
        // TODO - Tune this weight
        // if (turnCount > 900 && foundMiningLocationFromComms()) return;
        if (foundMiningLocationFromComms()) return;
        // Explore now how?
        // Head away from parent Archon to explore. Might get us new mining locations
        
        if (!BFS.move(explore())) desperationIndex++;
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
        MapLocation[] potentialMiningLocations;
        // potentialMiningLocations = rc.senseNearbyLocationsWithGold();
        // if (potentialMiningLocations.length != 0){
        //     MapLocation loc;
        //     for (int i = potentialMiningLocations.length; --i >= 0;){
        //         loc = potentialMiningLocations[i];
        //         if (Clock.getBytecodesLeft() < 1000) return;
        //         Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.MINER, loc, Comms.SHAFlag.LEAD_LOCATION);
        //     }
        // }
        potentialMiningLocations = rc.senseNearbyLocationsWithLead(UNIT_TYPE.visionRadiusSquared, 6);
        if (potentialMiningLocations.length < 15) return;
        Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.MINER, rc.getLocation(), Comms.SHAFlag.LEAD_LOCATION);
        // for (MapLocation loc : potentialMiningLocations){  // Team bias
        //     if (Clock.getBytecodesLeft() < 1200)
        //         break;
        //     if (rc.getLocation().distanceSquaredTo(loc) > 2 && goodMiningSpot(loc)){
        //         Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.LEAD, loc, Comms.SHAFlag.LEAD_LOCATION);
        //     }
        // }
    }
    

    public static boolean isSafeToMine(MapLocation loc){
        for (int i = visibleEnemies.length; --i >= 0;) {
            RobotInfo enemy = visibleEnemies[i];
            switch (enemy.type) {
                case SOLDIER:
                case WATCHTOWER:
                case SAGE:
                    if(SMALL_MAP && enemy.type.actionRadiusSquared == loc.distanceSquaredTo(enemy.location))
                        return false;
                    if (enemy.type.actionRadiusSquared > loc.distanceSquaredTo(enemy.location))
                        return false;

                default:
                    break;
            }
        }
        return true;
    }

    public static void opportunisticMining() throws GameActionException{
        if (miningLocation == null || rc.canSenseLocation(miningLocation)) return;
        MapLocation[] nearbyLocations = rc.senseNearbyLocationsWithGold();
        if (nearbyLocations.length > 0){ 
            miningLocation = findOptimalLocationForMiningGold(nearbyLocations);
            inPlaceForMining = (rc.getLocation().distanceSquaredTo(miningLocation) <= 2);
            return;
        }
        if (!depleteMine) nearbyLocations = rc.senseNearbyLocationsWithLead(MINER_VISION_RADIUS, 20);
        else nearbyLocations = rc.senseNearbyLocationsWithLead();
        if (nearbyLocations.length > 0){ 
            miningLocation = findOptimalLocationForMiningLead(nearbyLocations);
            if (miningLocation != null) inPlaceForMining = (rc.getLocation().distanceSquaredTo(miningLocation) <= 2);
            else inPlaceForMining = false;
            return;
        }
    }


    public static void moveIfNeeded() throws GameActionException{
        // int leadVal = -1;
        int rubbleVal = Integer.MAX_VALUE;
        int homeRubbleVal = rc.senseRubble(rc.getLocation());
        MapLocation selectedLoc = null;
        MapLocation[] mineAdjacentLocations = rc.getAllLocationsWithinRadiusSquared(miningLocation, MINER_ACTION_RADIUS);
        for (int i = mineAdjacentLocations.length; i-- > 0;){
            MapLocation loc = mineAdjacentLocations[i];
            if (!rc.canSenseLocation(loc)) continue;
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
            dir = rc.getLocation().directionTo(selectedLoc);
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
        if (isMinedThisTurn){
            moveOut = false;
            return;
        }
        opportunisticMining();
        if (inPlaceForMining){
            // if (isSafeToMine(rc.getLocation()))
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
            rc.setIndicatorString("lowHealthStrategy triggered");
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
        if (finalDestination == null && rc.getLocation().distanceSquaredTo(lowHealthStratArchon) > ARCHON_ACTION_RADIUS){
            BFS.move(lowHealthStratArchon);
            return;
        } 
        else if (rc.getHealth() < 8 && visibleEnemies.length == 0) {
            finalDestination = getClosestNonLeadLocation(lowHealthStratArchon);
            if (finalDestination == null) rc.disintegrate();
            if (rc.canSenseRobotAtLocation(finalDestination)) rc.disintegrate();
            if (!BFS.move(finalDestination)) rc.disintegrate();
        }
    }



    /**
    * Run a single turn for a Miner.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runMiner(RobotController rc) throws GameActionException{
        updateMiner();
        mine();
        lowHealthStrategy();
        // toDieOrNotToDie();

        doMining();
        if (moveOut) goMoveOut();
        mine();
        BotSoldier.sendCombatLocation(visibleEnemies);
        surveyForOpenMiningLocationsNearby();
    }
}
