package AFinalsBot;

import battlecode.common.*;

public class BotArchon extends Util{

    // Credits to @IvanGeffner for a major chunk of this code

    enum ArchonBuildUnits { 
        MINER, 
        SOLDIER,
        BUILDER,
        SAGE,  
    }

    public static ArchonBuildUnits[] archonBuildUnits = ArchonBuildUnits.values();
    public static double[] aBUWeights = new double[ArchonBuildUnits.values().length];
    public static double[] currentWeights = new double[ArchonBuildUnits.values().length];
    public static double watchTowerWeight;
    public static int myArchonID; // Each Archon will have their commID
    public static int commID; // Each Archon will have their commID
    public static int startingArchons;
    public static int minerCount;
    public static int builderCount;
    public static int soldierCount;
    public static int sageCount;
    public static int watchTowerCount;
    public static int laboratoryCount;
    public static int turnsWaitingToBuild;
    private static int enemyArchonQueueHead;
    private static int fleeIndex;
    private static int updateNeedToMove;
    private static final int TRANSFORM_AND_MOVE_WAIT_TIME = 70;
    private static final int HIGH_RUBBLE_MOVE_TRIGGER = 40;
    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static RobotInfo[] visibleAllies;
    private static RobotInfo[] inRangeAllies;
    private static boolean isFleeing = false;
    private static boolean needToMove;
    private static MapLocation fleeLocation;
    private static MapLocation selectedEnemyDestination;
    private static MapLocation moveIfNeededTarget;
    private static MapLocation transformAndMoveTarget;
    private static MapLocation vortexMoveIfNeededTarget;
    private static MapLocation transformAndMoveOrigin;
    private static boolean transformAndMove;
    private static boolean goodPlace;
    private static boolean mediumMapFlag;
    private static boolean alreadyMoving;
    private static boolean vortexStartedMoving;
    private static AnomalyScheduleEntry prevAnomaly;
    private static final boolean DEBUG_MODE = false;
    private static int PORTABLE_MODE_TURNS_LIMIT = 200;
    private static int turnsInPortableMode;


    public static MapLocation setEnemyDestination() throws GameActionException{
        if (rc.getRoundNum() > 1){
            MapLocation closestEnemy = Comms.findNearestLocationOfThisType(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (closestEnemy != null) return closestEnemy;
            MapLocation lClosestEnemyArchon = Comms.getClosestEnemyArchonLocation();
            if (lClosestEnemyArchon != null)  return lClosestEnemyArchon;

            for (int i = 0; i < 4; i++){
                if (rememberedEnemyArchonLocations[i] != null && CombatUtil.enemyArchonLocationGuessIsFalse(rememberedEnemyArchonLocations[i]))
                    rememberedEnemyArchonLocations[i] = null;
            }
        }
        if (rememberedEnemyArchonLocations[1] != null)  return rememberedEnemyArchonLocations[1];
        else if (rememberedEnemyArchonLocations[2] != null) return rememberedEnemyArchonLocations[2];
        else if (rememberedEnemyArchonLocations[0] != null) return rememberedEnemyArchonLocations[0];
        else{
            return CENTER_OF_THE_MAP;
        }
    }

    // This will give each Archon which number it is in the queue
    public static void initBotArchon() throws GameActionException {
        int transmitterCount = rc.readSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT);
        myArchonID = transmitterCount % archonCount;
        commID = myArchonID;
        startingArchons = rc.getArchonCount();
        fleeIndex = 0;
        fleeLocation = null;
        enemyArchonQueueHead = 0;
        if (archonCount == 1 && rc.senseRubble(rc.getLocation()) > HIGH_RUBBLE_MOVE_TRIGGER)
        needToMove = true;
        else needToMove = false;
        moveIfNeededTarget = null;
        // if (commID == 0) 
        Anomaly.initAnomaly();
        updateNeedToMove = 2050;
        transformAndMove = false;
        transformAndMoveTarget = null;
        transformAndMoveOrigin = null;
        goodPlace = false;
        if (MAP_HEIGHT * MAP_WIDTH < 901 && archonCount == 1) {
            MEDIUM_MAP = false;
            SMALL_MAP = true;
        }
        mediumMapFlag = MEDIUM_MAP;
        prevAnomaly = null;
        alreadyMoving = false;
        vortexMoveIfNeededTarget = null;
        vortexStartedMoving = false;
        turnsInPortableMode = 0;
        // if (commID == 0)
        // Anomaly.printAllAnomalies();
    }
    
    
    // Update weights of each Archon
    // TODO: Link currentweights with comms counter
    public static void updateArchonBuildUnits(){
        double lTC = turnCount;
        if (SMALL_MAP){
            watchTowerWeight = (watchTowerCount+laboratoryCount)/1.5;
            aBUWeights[ArchonBuildUnits.BUILDER.ordinal()] = Math.min(1.0d, 0.15d + lTC/400.0d);
            aBUWeights[ArchonBuildUnits.MINER.ordinal()] = Math.max(1.8d, 3.2d - (lTC/200.0d) - ((double)minerCount)/20.0d);
            aBUWeights[ArchonBuildUnits.SAGE.ordinal()] = Math.max(2.50d, 4.5d - lTC/100.0d);
            aBUWeights[ArchonBuildUnits.SOLDIER.ordinal()] = Math.min(5.5d, 2.0d + lTC/40.0d - (double)soldierCount/70.0d);
            
        }
        else if (MEDIUM_MAP) {
            watchTowerWeight = (watchTowerCount + laboratoryCount)/2;
            aBUWeights[ArchonBuildUnits.BUILDER.ordinal()] = 0.5d;
            aBUWeights[ArchonBuildUnits.MINER.ordinal()] = 4.5d;
            aBUWeights[ArchonBuildUnits.SAGE.ordinal()] = 9.0d;
            aBUWeights[ArchonBuildUnits.SOLDIER.ordinal()] = Math.max(0.5d, 2.0d - lTC/200.0) ;
        }
        else {
            watchTowerWeight = (watchTowerCount + laboratoryCount)/2;
            aBUWeights[ArchonBuildUnits.BUILDER.ordinal()] = Math.min(1.0d, 0.35d + lTC/400.0d);
            aBUWeights[ArchonBuildUnits.MINER.ordinal()] = Math.max(3.5d, 4.5d - (lTC/100.0d) - ((double)minerCount)/30.0d);
            aBUWeights[ArchonBuildUnits.SAGE.ordinal()] = Math.max(10.0d, 10.0d);
            aBUWeights[ArchonBuildUnits.SOLDIER.ordinal()] = Math.min(4.50d, 2.0d + lTC/30.0d - (double)soldierCount/70.0d);
        }
    }


    private static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(ARCHON_VISION_RADIUS, ENEMY_TEAM);
        inRangeEnemies = rc.senseNearbyRobots(ARCHON_ACTION_RADIUS, ENEMY_TEAM);
        visibleAllies = rc.senseNearbyRobots(ARCHON_VISION_RADIUS, MY_TEAM); 
        inRangeAllies = rc.senseNearbyRobots(ARCHON_ACTION_RADIUS, MY_TEAM);
    }


    public static void buildDivision() throws GameActionException{
        if (!rc.isActionReady()) return;
        else buildUnit();
    }

    public static RobotType giveUnitType(ArchonBuildUnits unitToBuild) throws GameActionException{
        switch(unitToBuild){
            case BUILDER: return RobotType.BUILDER; 
            case MINER:   return RobotType.MINER;   
            case SAGE:    return RobotType.SAGE;    
            case SOLDIER: return RobotType.SOLDIER; 
            default: return RobotType.SOLDIER; 
        }
    }

    // TODO - Analyse this and if fleeindex != 0 make required units
    public static boolean waitQuota(RobotType unitToBuild) throws GameActionException{
        int archonWhoShouldBuild = (rc.getRoundNum() - 1) % archonCount;
        switch(Comms.readArchonMode(archonWhoShouldBuild)){
            case PORTABLE: return false;
            case TURRET: {
                if (Comms.canArchonSeeEnemies(commID) && !Comms.canArchonSeeEnemies(archonWhoShouldBuild) && unitToBuild == RobotType.SAGE) return false;
                return archonWhoShouldBuild > commID && (currentLeadReserves < 95);
            }
            default: System.out.println("Why is this happening oh battlecode god??!!!"); return false;
        }
    }

    static double getValue(MapLocation archonLocation, MapLocation dest, Direction dir) throws GameActionException {
        double dist = 1;
        if (dest != null) dist = archonLocation.add(dir).distanceSquaredTo(dest);
        return dist * (10 + rc.senseRubble(archonLocation.add(dir)));
    }

    public static Direction getBestSpawnDirection(RobotType unitType) throws GameActionException{
        Direction bestSpawnDir = null;
        double bestSpawnValue = Double.MAX_VALUE;
        double SpawnValue = Double.MAX_VALUE;
        MapLocation lCR = currentLocation;

        if (unitType == RobotType.SOLDIER || unitType == RobotType.SAGE){
            for (Direction dir : directions) {
                if (!rc.canBuildRobot(unitType, dir)) continue;
                SpawnValue = getValue(lCR, selectedEnemyDestination, dir); 
                if (bestSpawnDir == null || SpawnValue < bestSpawnValue) {
                    bestSpawnDir = dir;
                    bestSpawnValue = SpawnValue;
                }
            }
            return bestSpawnDir;
        }


        Direction goodAdjDirections[] = new Direction[8];
        int goodAdjCount = 0;
        int bestRubble = Integer.MAX_VALUE;
        for (Direction dir : directions){
            MapLocation loc = lCR.add(dir);
            if (!rc.canBuildRobot(unitType, dir)) continue;
            bestRubble = Math.min(bestRubble, rc.senseRubble(loc));
        }

        for (Direction dir : directions){
            MapLocation loc = lCR.add(dir);
            if (!rc.canBuildRobot(unitType, dir)) continue;
            if (rc.senseRubble(loc) == bestRubble){
                goodAdjDirections[goodAdjCount] = dir;
                goodAdjCount++;
            }
        }
        if (goodAdjCount == 0) return null;
        
        bestSpawnDir = goodAdjDirections[rng.nextInt(goodAdjCount)];
        // rc.setIndicatorString("gACount: " + goodAdjCount + " bs " + bestSpawnDir);
        return bestSpawnDir;

    }

    public static void spawnBot(RobotType unitType, Direction spawnDir, ArchonBuildUnits unitToBuild) throws GameActionException{
        if (spawnDir == null) return;
        rc.buildRobot(unitType, spawnDir);
        turnsWaitingToBuild = 0;
        // System.out.println("Unit to build is " + unitType + " with weight " + currentWeights[unitToBuild.ordinal()]);
        currentWeights[unitToBuild.ordinal()] += 1.0/aBUWeights[unitToBuild.ordinal()];
        // if(ID ==2) System.out.println("Unit built is " + unitType + " Weights are " + Arrays.toString(currentWeights));
    }

    public static ArchonBuildUnits labFirstOrder() throws GameActionException{        
        if (minerCount < 4) return ArchonBuildUnits.MINER;
        else if (builderCount == 0 && rc.getRobotCount() <= archonCount + 4) return ArchonBuildUnits.BUILDER;
        else if (laboratoryCount < 1 && turnsWaitingToBuild < 50) return null;
        else {
            mediumMapFlag = false;
            return null;
        }
    } 

    public static boolean distributeResources(RobotType unitToBuild) throws GameActionException{
        if (unitToBuild == RobotType.BUILDER) {
            if (!Comms.canArchonSeeEnemies(commID)) return false;
            for (int i = archonCount - 1; i > commID; i--){
                if (!Comms.canArchonSeeEnemies(i)) return true;
            }
        }
        else if (unitToBuild == RobotType.SAGE) {
            if (Comms.canArchonSeeEnemies(commID) || rc.getTeamGoldAmount(MY_TEAM) >= 40) return false;
            for (int i = archonCount - 1; i > commID; i--){
                if (Comms.canArchonSeeEnemies(i)) return true;
            }
        }
        return false;
    }

    public static void buildUnit() throws GameActionException{
        try {
            if(!rc.isActionReady()) return;
            ArchonBuildUnits unitToBuild = null;
            if (mediumMapFlag) unitToBuild = labFirstOrder();
            else unitToBuild = standardOrder();
            
            if (unitToBuild == null || waitQuota(giveUnitType(unitToBuild)) || distributeResources(giveUnitType(unitToBuild))) {
                turnsWaitingToBuild++;
                return;
            }
            RobotType unitType = giveUnitType(unitToBuild);
            Direction bestSpawnDir = getBestSpawnDirection(unitType);
            spawnBot(unitType, bestSpawnDir, unitToBuild);

        } catch (Exception e) {
            System.out.println("Archon buildUnit() Exception");
            e.printStackTrace();
        }
    }

    public static ArchonBuildUnits standardOrder() throws GameActionException{
        boolean canBuild[] = new boolean[aBUWeights.length];
        if(shouldBuildBuilder()) canBuild[ArchonBuildUnits.BUILDER.ordinal()] = true;
        if(shouldBuildMiner()) canBuild[ArchonBuildUnits.MINER.ordinal()] = true;
        if(shouldBuildSoldier()) canBuild[ArchonBuildUnits.SOLDIER.ordinal()] = true;
        if(shouldBuildSage()) canBuild[ArchonBuildUnits.SAGE.ordinal()] = true;

        ArchonBuildUnits unitToBuild = null;
        double minWeight = Double.MAX_VALUE;

        for(int i = archonBuildUnits.length; --i >= 0;){
            if(!canBuild[i]) continue;
            if(unitToBuild == null || minWeight > currentWeights[i]){  // If unitToBuild is null, or this unit weight is *lesser*
                minWeight = currentWeights[i];
                unitToBuild = archonBuildUnits[i];
            }
        }
        rc.setIndicatorString("Build1 " + unitToBuild);
        if (watchTowerDebt(minWeight, unitToBuild)) {
                // System.out.println("Building watchtower instead of " + giveUnitType(unitToBuild));
                turnsWaitingToBuild++;
                return null;
        }
        rc.setIndicatorString("Build2 " + unitToBuild + " Gold " + rc.getTeamGoldAmount(MY_TEAM) + " " + commID);
        return unitToBuild;
    }


    public static boolean watchTowerDebt(double minWeight, ArchonBuildUnits unitToBuild) throws GameActionException{
        if (unitToBuild == ArchonBuildUnits.SAGE) return false;
        if (builderCount >= 1 && laboratoryCount == 0 && turnsWaitingToBuild < 75) return true;

        if (MAP_SIZE > 799  && MAP_SIZE <= 1250){
            return minWeight < 100000 && 
            watchTowerWeight < minWeight && 
            builderCount != 0 && 
            currentLeadReserves < giveUnitType(unitToBuild).buildCostLead + RobotType.LABORATORY.buildCostLead && 
            turnsWaitingToBuild < 60 && 
            (minerCount >= 5 || laboratoryCount < 2) && currentLeadReserves > 50;
        }
        else{
        return minWeight < 100000 && 
                watchTowerWeight < minWeight && 
                builderCount != 0 && 
                currentLeadReserves < giveUnitType(unitToBuild).buildCostLead + RobotType.LABORATORY.buildCostLead && 
                turnsWaitingToBuild < 60 && 
                (minerCount >= 10 && currentLeadReserves > 50);
        }
    }


    public static boolean shouldBuildBuilder(){
        // if (currentLeadReserves < RobotType.BUILDER.buildCostLead) return false;
        if (SMALL_MAP) return false;   
        if (builderCount == 0 && rc.getRoundNum() > 120) return true;
        if (turnCount < 30 + commID) return false;
        // if (rc.getRoundNum() < 100 && (builderCount > (laboratoryCount + 1) || currentLeadReserves < 90)) return false; 
        if (builderCount > (laboratoryCount + 1 + archonCount) || currentLeadReserves < 90) return false;
        return true;
    }

    public static boolean shouldBuildMiner(){    
        if (minerCount > Math.max(10 + rc.getRoundNum()/50 * Math.exp(MAP_SIZE- 3600), sageCount/2.0)) return false;
        // if (currentLeadReserves < RobotType.MINER.buildCostLead) return false;
        return currentLeadReserves >= RobotType.MINER.buildCostLead;
    }


    public static boolean shouldBuildSoldier(){
        if (MAP_SIZE > 799 && MAP_SIZE <= 1250 && rc.getRoundNum() > 60 && laboratoryCount != 0) return false;
        // if (currentLeadReserves < RobotType.SOLDIER.buildCostLead) return false;
        if (rc.getTeamGoldAmount(MY_TEAM) > 19) return false;
        if (SMALL_MAP) return turnCount >= 5;
        return (turnCount >= 15);
    }


    public static boolean shouldBuildSage(){
        return rc.getTeamGoldAmount(MY_TEAM) >= 20;
    }


    // Read the unit counts this round
    public static void getCommCounts() throws GameActionException{
        minerCount = rc.readSharedArray(Comms.CHANNEL_MINER_COUNT); 
        builderCount = rc.readSharedArray(Comms.CHANNEL_BUILDER_COUNT); 
        soldierCount = rc.readSharedArray(Comms.CHANNEL_SOLDIER_COUNT); 
        sageCount = rc.readSharedArray(Comms.CHANNEL_SAGE_COUNT);
        watchTowerCount = rc.readSharedArray(Comms.CHANNEL_WATCHTOWER_COUNT); 
        laboratoryCount = (rc.readSharedArray(Comms.CHANNEL_LABORATORY_COUNT) & 0xFF);
    }


    public static void clearCommCounts() throws GameActionException{
        rc.writeSharedArray(Comms.LAB_CHANNEL_MINER_COUNT, minerCount);
        rc.writeSharedArray(Comms.CHANNEL_MINER_COUNT, 0);
        rc.writeSharedArray(Comms.CHANNEL_BUILDER_COUNT, 0);
        rc.writeSharedArray(Comms.CHANNEL_SOLDIER_COUNT, 0);
        rc.writeSharedArray(Comms.LAB_CHANNEL_MINER_COUNT, sageCount);
        rc.writeSharedArray(Comms.CHANNEL_SAGE_COUNT, 0);
        rc.writeSharedArray(Comms.CHANNEL_WATCHTOWER_COUNT, 0);
        Comms.writeLaboratoryCount(laboratoryCount);
        // rc.writeSharedArray(Comms.CHANNEL_LABORATORY_COUNT, 0);
        rc.writeSharedArray(Comms.CHANNEL_LABORATORY_COUNT, (rc.readSharedArray(Comms.CHANNEL_LABORATORY_COUNT) & 0xFF00));
    }


    public static void archonComms() throws GameActionException{
        Comms.updateComms();
        Comms.writeArchonMode(rc.getMode());
        int transmitterCount = rc.readSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT);
        // I'm first Archon (by birth or death of ones before me). I'm number zero transmitter.
        if(transmitterCount > commID) commID = 0;
        else commID = transmitterCount; 
        rc.writeSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT, commID+1); // Clears Transmitter Count by First Archon

        // if (turnCount < 3 || hasMoved)
        Comms.writeSHAFlagMessage(rc.getLocation(), Comms.SHAFlag.ARCHON_LOCATION, Comms.CHANNEL_ARCHON_START + commID*4);        
        
        getCommCounts();

        // If you're the last Archon, clean the counts for this new turn
        if(commID + 1 == archonCount)  clearCommCounts();
        Comms.updateArchonLocations();
    }

    private static void updateHealingQueueComms(){
        int unitsToHealCount = 0;
        int healthDiff = 0;
        for(int i = inRangeAllies.length; --i >=0;){
            RobotInfo ally = inRangeAllies[i];
            healthDiff = ally.getType().getMaxHealth(rc.getLevel()) - ally.getHealth();
            if(healthDiff == 0) continue;
            unitsToHealCount++;
        }
        // rc.setIndicatorString("Heal: " + unitsToHealCount);
        Comms.updateHealingUnitNearby(unitsToHealCount);
    }


    private static void shouldFlee() throws GameActionException{
        // You have more enemies attacking you than friends that could come save you and you are not the main producer Archon
        // if (rc.getMode() == RobotMode.TURRET){
            int enemyMilitaryCount = CombatUtil.militaryCount(inRangeEnemies);
            if(enemyMilitaryCount > 2 && rc.getHealth() < 2.0/3.0 * rc.getType().getMaxHealth(rc.getLevel()) && 
                enemyMilitaryCount > CombatUtil.militaryCount(visibleAllies) && archonCount != 1)
                fleeIndex++;
            else fleeIndex = 0;
    }


    private static void transformAndFlee() throws GameActionException{
        if (fleeIndex > 5 && rc.getMode()!= RobotMode.PORTABLE && rc.isTransformReady()) {
            if (fleeLocation == null) { 
                fleeLocation = getClosestArchonLocation(true);
                if (fleeLocation!= null && !rc.canSenseLocation(fleeLocation)){ 
                    if (DEBUG_MODE) rc.setIndicatorString("transforming to flee");
                    rc.transform();
                }
                else fleeLocation = null;
                isFleeing = true;
            }
        }

        if (rc.getMode() == RobotMode.PORTABLE && isFleeing){ 
            // Enemies contained and transform cooldown gone
            if (rc.isTransformReady() && currentLocation.isWithinDistanceSquared(fleeLocation, ARCHON_ACTION_RADIUS)){
                if (DEBUG_MODE) rc.setIndicatorString("setting itself back into turret after fleeing!");
                rc.transform(); // TODO - Doesn't account for Rubble
                isFleeing = false;
                fleeLocation = null;
            }
            if (fleeLocation != null) {  
                BFS.move(fleeLocation);
            }
        }
    }


    // Sadly, self heal does not look possible.
    // private static void selfHeal() throws GameActionException{
    //     if (rc.isActionReady()) {
    //         RobotInfo unit = null;
    //         int healthDiff = 0;
    //         double robotHealth = 1000;
    //         for(int i = inRangeAllies.length; --i >=0;){
    //             RobotInfo ally = inRangeAllies[i];
    //             healthDiff = ally.getType().getMaxHealth(rc.getLevel()) - ally.getHealth();
    //             if(healthDiff == 0) continue;
    //             if (healthDiff < robotHealth && rc.canRepair(ally.getLocation())) {
    //                 robotHealth = ally.health;
    //                 unit = ally;
    //             }
    //         }
    //         if(unit!=null){
    //             // System.out.println("Repairing");
    //             rc.repair(unit.getLocation());
    //         }
    //     }
    // }

    private static void selfHeal() throws GameActionException{
        if (rc.isActionReady()) {
            RobotInfo unit = null, militaryUnit = null;
            int healthDiff = 0, militaryHealthDiff = 0;
            double robotHealth = Double.MAX_VALUE;
            double militaryRobotHealth = Double.MAX_VALUE;
            for(int i = inRangeAllies.length; --i >=0;){
                RobotInfo ally = inRangeAllies[i];
                if ((MAP_SIZE<=900) && ally.getType() == RobotType.SOLDIER || ally.getType() == RobotType.SAGE)
                {
                    militaryHealthDiff = ally.getType().getMaxHealth(rc.getLevel()) - ally.getHealth();
                    if(militaryHealthDiff == 0) continue;
                    if (militaryHealthDiff < militaryRobotHealth && rc.canRepair(ally.getLocation())) {
                        militaryRobotHealth = ally.health;
                        militaryUnit = ally;
                    }
                }
                else if (militaryUnit == null) {
                    healthDiff = ally.getType().getMaxHealth(rc.getLevel()) - ally.getHealth();
                    if(healthDiff == 0) continue;
                    if (healthDiff < robotHealth && rc.canRepair(ally.getLocation())) {
                        robotHealth = ally.health;
                        unit = ally;
                    }
                }
            }
            if(militaryUnit != null){
                rc.repair(militaryUnit.getLocation());
            }
            else if(unit!=null){
                rc.repair(unit.getLocation());
            }
        }
    }


    private static void incrementHead(){
        enemyArchonQueueHead++;
        enemyArchonQueueHead = (enemyArchonQueueHead % archonCount);
    }


    public static void getEnemyArchonLocations() throws GameActionException{
        if (commID != 0) return;
        MapLocation[] enemyArchonLocations = Comms.findLocationsOfThisTypeAndWipeChannels(Comms.commType.COMBAT, Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION);
        if (enemyArchonLocations.length == 0) return;
        for (MapLocation enemyArchon : enemyArchonLocations){
            Comms.writeSHAFlagMessage(enemyArchon, Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION, Comms.CHANNEL_ARCHON_START + (enemyArchonQueueHead * 4) + 1);
            // rc.writeSharedArray(, ;);
            incrementHead();
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

    private static void updateArchon() throws GameActionException{
        archonComms();
        updateVision();
        if (rc.getRoundNum() > 120 && SMALL_MAP) SMALL_MAP = false;
        selectedEnemyDestination = setEnemyDestination(); // This is for building of soldiers closer to enemy archon guess
        Comms.writeArchonMode(rc.getMode());
        checkIfEnemyArchonInVision();
        getEnemyArchonLocations();
        if (commID == 0) {
            Comms.wipeChannels(Comms.commType.COMBAT);
            Comms.wipeChannels(Comms.commType.MINER);
        }
        updateArchonBuildUnits();
        updateHealingQueueComms();
        shouldTransformAndMove();
        AnomalyScheduleEntry nextAnomaly = Anomaly.getNextAnomaly();
        if (nextAnomaly != null && prevAnomaly == null){
            prevAnomaly = nextAnomaly;
            if (nextAnomaly.anomalyType == AnomalyType.VORTEX){
                updateNeedToMove = nextAnomaly.roundNumber;
            }
            else{
                updateNeedToMove = 2050;
            }
        }

        if (nextAnomaly != null && !nextAnomaly.equals(prevAnomaly)){
            prevAnomaly = nextAnomaly;
            if (nextAnomaly.anomalyType == AnomalyType.VORTEX){
                updateNeedToMove = nextAnomaly.roundNumber;
            }
            else
                updateNeedToMove = 2050;
        }
        if (rc.getMode().equals(RobotMode.PORTABLE)) turnsInPortableMode++;
        if (!isFleeing && rc.getMode().equals(RobotMode.PORTABLE) && turnsInPortableMode > PORTABLE_MODE_TURNS_LIMIT){
            if (rc.isTransformReady()){
                if (DEBUG_MODE) rc.setIndicatorString("You took too long to move. Need to produce too");
                rc.transform();
                if (transformAndMove){
                    transformAndMove = false;
                    transformAndMoveTarget = null;
                    transformAndMoveOrigin = null;
                    Comms.writeArchonMode(rc.getMode());
                    Comms.updateArchonTransformAndMoveTurn();
                    Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
                }
                turnsInPortableMode = 0;
            }
        }

    }


    public static boolean checkIfAnyOtherArchonIsMoving(){
        try{
            for (int i = 0; i < archonCount; ++i){
                if (i == commID) continue;
                if (Comms.readArchonMode(i) == RobotMode.PORTABLE) return true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private static MapLocation findBetterAdjacentLocation(){
        try{
            MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2);
            MapLocation optLoc = rc.getLocation();
            int optRubble = rc.senseRubble(rc.getLocation());
            MapLocation loc;
            for (int i = adjacentLocations.length; --i >= 0; ){
                loc = adjacentLocations[i];
                if (!rc.canSenseLocation(loc)) continue;
                if (rc.canSenseRobotAtLocation(loc)) continue;
                int rubble = rc.senseRubble(loc);
                if (rubble < optRubble){
                    optRubble = rubble;
                    optLoc = loc;
                }
            }
            // if (optLoc.equals(rc.getLocation())){
            //     needToMove = false;
            //     return null;
            // }
            return optLoc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static MapLocation findBetterOverallLocation(){
        try{
            MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), ARCHON_VISION_RADIUS);
            MapLocation optLoc = rc.getLocation();
            int optRubble = rc.senseRubble(rc.getLocation()), optDist = 0;
            MapLocation loc;
            for (int i = adjacentLocations.length; i-- > 0;){
                loc = adjacentLocations[i];
                if (!rc.canSenseLocation(loc)) continue;
                if (rc.canSenseRobotAtLocation(loc)) continue;
                int rubble = rc.senseRubble(loc), dist = rc.getLocation().distanceSquaredTo(loc);
                if (rubble < optRubble){
                    optRubble = rubble;
                    optLoc = loc;
                    optDist = dist;
                }
                else if (rubble == optRubble && dist < optDist){
                    optDist = dist;
                    optLoc = loc;
                }
            }
            return optLoc;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static int transformCooldownCompute() throws GameActionException{
        double cooldown = ((1.0d + ((double)rc.senseRubble(rc.getLocation())) / 10.0d) * ((double)UNIT_TYPE.actionCooldown));
        return (((int)cooldown) / 10);
    }


    private static boolean goodTimeToMove(){
        // if (checkIfAnyOtherArchonIsMoving()) return false;
        if (rc.getRoundNum() > 50) return true;
        // if (turnsWaitingToBuild > 10) return true;

        return false;
    }


    private static void vortexMoveIfNeeded() throws GameActionException{
        int roundsRemaining = (updateNeedToMove - rc.getRoundNum() - transformCooldownCompute()); 
        if (roundsRemaining > 1 && !vortexStartedMoving) return;
        else if (roundsRemaining == 1 && !vortexStartedMoving && rc.getMode().equals(RobotMode.PORTABLE)){
            alreadyMoving = true;
            return;
        }
        else if (roundsRemaining == 1 && !vortexStartedMoving) alreadyMoving = false;
        if (alreadyMoving) return;
        if (rc.getMode().equals(RobotMode.TURRET)){
            if (rc.isTransformReady()) {
                if (DEBUG_MODE) rc.setIndicatorString("transforming to move because of incoming vortex");
                rc.transform();
                vortexStartedMoving = true;
            }
            return;
        }
        if (!rc.isMovementReady()) return;
        if (vortexMoveIfNeededTarget == null){
            vortexMoveIfNeededTarget = findBetterOverallLocation();
        }
        if (vortexMoveIfNeededTarget == null){
            if (rc.isTransformReady()) {
                if (DEBUG_MODE) rc.setIndicatorString("This should never have happened. But just in case transforming back to turret");
                rc.transform();
                vortexStartedMoving = false;
            }
            System.out.println("never happens");
            return;
        }
        if (vortexMoveIfNeededTarget.equals(rc.getLocation())){
            if (rc.isTransformReady()){
                if (DEBUG_MODE) rc.setIndicatorString("at location. transforming back to turret");
                rc.transform();
                vortexMoveIfNeededTarget = null;
                vortexStartedMoving = false;
            }
            return;
        }
        if (rc.canSenseRobotAtLocation(vortexMoveIfNeededTarget)){
            vortexMoveIfNeededTarget = findBetterOverallLocation();
        }
        if (vortexMoveIfNeededTarget.equals(rc.getLocation())){
            if (rc.isTransformReady()){
                if (DEBUG_MODE) rc.setIndicatorString("2: at location. transforming back to turret");
                rc.transform();
                vortexMoveIfNeededTarget = null;
                vortexStartedMoving = false;
            }
            return;
        }
        if (!rc.isMovementReady()) return;
        if (!BFS.move(vortexMoveIfNeededTarget)){
            if (rc.isTransformReady()){
                if (DEBUG_MODE) rc.setIndicatorString("transforming back 'cause I can't move");
                rc.transform();
                vortexStartedMoving = false;
                vortexMoveIfNeededTarget = null;
            }
            return;
        }
    }



    private static void moveIfNeeded(){
        try{
            if (archonCount != 1) return;
            if (!needToMove) return;
            if (!goodTimeToMove()) return;
            if (moveIfNeededTarget != null){
                if (rc.getMode() == RobotMode.PORTABLE){
                    // moveIfNeededTarget = findBetterAdjacentLocation();
                    if (moveIfNeededTarget.equals(rc.getLocation())){
                        if (rc.isTransformReady()){ 
                            rc.transform();
                            rc.setIndicatorString("Started the transformation");
                            needToMove = false;
                            moveIfNeededTarget = null;
                        }
                        else{
                            rc.setIndicatorString("Can't transform for some reason. Wait");
                        }
                        return;
                    }
                    if (!rc.isMovementReady()) {
                        rc.setIndicatorString("Movement cooldown dude. Have patience");
                        return;
                    }
                    BFS.move(moveIfNeededTarget);
                    // Movement.goToDirect(moveIfNeededTarget);
                    return;
                }
                if (moveIfNeededTarget.equals(rc.getLocation())){
                    System.out.println("why oh why does this happen?");
                    needToMove = false;
                    return;
                }
                if (rc.isTransformReady()){ 
                    rc.transform();
                    rc.setIndicatorString("I am gaining the ability to move!");
                }
                return;
            }
            // moveIfNeededTarget = findBetterAdjacentLocation();
            moveIfNeededTarget = findBetterOverallLocation();
            if (moveIfNeededTarget.equals(rc.getLocation())){
                needToMove = false;
                moveIfNeededTarget = null;
                return;
            }
            if (rc.isTransformReady()){ 
                rc.transform();
                rc.setIndicatorString("Imma gonna start moving!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void healArchon() throws GameActionException{
        if (myHealth < MAX_HEALTH && !Comms.isBuilderAssignedToArchon()){
            Comms.updateArchonBuilderNeedFlag(true);
        }
        Comms.builderAssignedToArchon(commID, false);
        // if (!healCommandSent && myHealth < MAX_HEALTH){
        //     Comms.updateArchonBuilderNeedFlag(true);
        //     healCommandSent = true;
        // }
    }


    private static void endOfTurnUpdate(){
        try{
            healArchon();
            Comms.writeArchonMode(rc.getMode());
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void findTransformAndMoveTarget() throws GameActionException{
        if (!transformAndMove) return;
        MapLocation loc = Comms.findNearestLocationOfThisType(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
        // int[] distances = new int[3];
        // for (int i = -1; ++i < 3;){
        //     distances[i] += rc.getLocation().distanceSquaredTo(rememberedEnemyArchonLocations[i]);
        // }
        // int ind = 0, mx = distances[0];
        // if (distances[1] > mx){
        //     ind = 1;
        //     mx = distances[1];
        // }
        // if (distances[2] > mx){
        //     ind = 2;
        //     mx = distances[2];
        // }
        if (loc == null) {
            transformAndMoveTarget = loc;
            return;
        }
        // if (MAP_WIDTH*MAP_HEIGHT < 900)
        // transformAndMoveTarget = translateByStepsCount(rc.getLocation(), loc, 10);
        // else
        transformAndMoveTarget = translateByStepsCount(rc.getLocation(), loc, 20);

    }


    private static void shouldTransformAndMove() throws GameActionException{
        // if (SMALL_MAP) return;
        if (MAP_WIDTH * MAP_HEIGHT < 900 && !SMALL_MAP) return;
        if (archonCount == 1) return;
        if (checkIfAnyOtherArchonIsMoving()){ 
            transformAndMove = false;
            return;
        }
        if (transformAndMove) return;
        if (visibleEnemies.length > 0) return;
        // boolean val = Comms.checkIfMessageOfThisTypeThere(Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);

        transformAndMove = (commID == Comms.getArchonTransformAndMoveTurn() && rc.getRoundNum() >= Comms.getArchonWaitTimeForArchonTransformAndMove());
        findTransformAndMoveTarget();
        transformAndMoveOrigin = rc.getLocation();
        if (transformAndMoveTarget == null){
            transformAndMove = false;
            transformAndMoveOrigin = null;
            return;
        }
        if (visibleAllies.length > 18 || CombatUtil.militaryCount(visibleEnemies) > 0){
            transformAndMove = false;
            transformAndMoveTarget = null;
            transformAndMoveOrigin = null;
            Comms.writeArchonMode(rc.getMode());
            Comms.updateArchonTransformAndMoveTurn();
            Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
            return;
        }
        if (rc.canSenseLocation(transformAndMoveTarget)){
            transformAndMoveTarget = findRubbleFreeSettleLocation();
            // return;
        }
        if (transformAndMoveTarget.distanceSquaredTo(rc.getLocation()) < 15){
            transformAndMove = false;
            transformAndMoveTarget = null;
            transformAndMoveOrigin = null;
            Comms.writeArchonMode(rc.getMode());
            Comms.updateArchonTransformAndMoveTurn();
            Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
            return;
        }
    }


    public static boolean transformAndUpdate() throws GameActionException{
        if (!rc.isTransformReady()) return false;
        if (rc.getMode() == RobotMode.PORTABLE){
            if (DEBUG_MODE) rc.setIndicatorString("transfer and movement done. Settling as turret now");
            rc.transform();
            transformAndMove = false;
            transformAndMoveTarget = null;
            transformAndMoveOrigin = null;
            Comms.writeArchonMode(rc.getMode());
            Comms.updateArchonTransformAndMoveTurn();
            Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
            goodPlace = false;
            return true;
        }
        System.out.println("Should be unreachable piece of code");
        transformAndMove = false;
        transformAndMoveTarget = null;
        transformAndMoveOrigin = null;
        goodPlace = false;
        Comms.writeArchonMode(rc.getMode());
        Comms.updateArchonTransformAndMoveTurn();
        Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
        return true;
    }


    private static MapLocation findRubbleFreeSettleLocation() throws GameActionException{
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), ARCHON_VISION_RADIUS);
        MapLocation optLoc = rc.getLocation(), loc;
        int optRubble = rc.senseRubble(optLoc) + 1, rubble, dist;
        for (int i = nearbyLocations.length; --i >= 0;){
            loc = nearbyLocations[i];
            if ((!rc.canSenseLocation(loc)) || rc.canSenseRobotAtLocation(loc)) continue;
            rubble = rc.senseRubble(loc) + 1;
            dist = rc.getLocation().distanceSquaredTo(loc) / 2;
            rubble = rubble * (dist + 1);
            if (rubble < optRubble){
                optLoc = loc;
                optRubble = rubble;
                // optDist = dist;
            }
            // else if (rubble == optRubble && dist < optDist){
            //     optLoc = loc;
            //     optDist = dist;
            // }
        }
        return optLoc;
    }


    private static MapLocation findRubbleFreeSettleLocationInDirectionOfOrigin() throws GameActionException {
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), ARCHON_VISION_RADIUS);
        MapLocation optLoc = rc.getLocation(), loc;
        int optRubble = rc.senseRubble(optLoc), rubble, optDist = 0, dist;
        if (transformAndMoveOrigin == null){
            System.out.println("Noooooooooooooooooooooooooooooooooooo!!!!!");
            return rc.getLocation();
        }
        int originDist = rc.getLocation().distanceSquaredTo(transformAndMoveOrigin);
        for (int i = nearbyLocations.length; --i >= 0;){
            loc = nearbyLocations[i];
            if ((!rc.canSenseLocation(loc)) || rc.canSenseRobotAtLocation(loc) || transformAndMoveOrigin.distanceSquaredTo(loc) >= originDist) continue;
            rubble = rc.senseRubble(loc);
            dist = rc.getLocation().distanceSquaredTo(loc);
            if (rubble < optRubble){
                optLoc = loc;
                optRubble = rubble;
                optDist = dist;
            }
            else if (rubble == optRubble && dist < optDist){
                optLoc = loc;
                optDist = dist;
            }
        }
        return optLoc;
    }


    public static void transformAndMove(){
        try{
            if (!transformAndMove || isFleeing) return;
            if (checkIfAnyOtherArchonIsMoving()){
                if (rc.getMode() == RobotMode.PORTABLE){
                    if (!rc.canSenseLocation(transformAndMoveTarget)){
                        transformAndMoveTarget = findRubbleFreeSettleLocation();
                        goodPlace = true;
                    }
                    if (rc.getLocation().equals(transformAndMoveTarget)){
                        // if (rc.canTransform()) rc.transform();
                        transformAndUpdate();
                        return;
                    }
                    if (!rc.isMovementReady()) return;
                    if ((!BFS.move(transformAndMoveTarget)) && rc.canSenseRobotAtLocation(transformAndMoveTarget)){
                        // if (rc.canTransform()) rc.transform();
                        // TODO: Improve this.
                        transformAndUpdate();
                    }
                    return;
                }
                transformAndMoveTarget = null;
                transformAndMove = false;
                transformAndMoveOrigin = null;
                Comms.writeArchonMode(rc.getMode());
                Comms.updateArchonTransformAndMoveTurn();
                Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
            }
            // updateTransformAndMoveTarget();
            if (transformAndMoveTarget == null){
                if (rc.getMode().equals(RobotMode.PORTABLE)){
                    System.out.println("NOT. HAPPENING!");
                    // if (rc.isTransformReady()) rc.transform();
                    transformAndUpdate();
                }
                return;
            }
            if (rc.getLocation().equals(transformAndMoveTarget)){
                // System.out.println("Reached!!");
                if (rc.getMode() == RobotMode.PORTABLE){
                    transformAndUpdate();
                }
                else{
                    transformAndMoveTarget = null;
                    transformAndMove = false;
                    transformAndMoveOrigin = null;
                    Comms.writeArchonMode(rc.getMode());
                    Comms.updateArchonTransformAndMoveTurn();
                    Comms.updateWaitTimeForArchonTransformAndMove(rc.getRoundNum() + TRANSFORM_AND_MOVE_WAIT_TIME);
                }
                return;
            }
            if (rc.getMode().equals(RobotMode.TURRET)){
                if (rc.isTransformReady()){ 
                    if (DEBUG_MODE) rc.setIndicatorString("portable; transform and move: " + transformAndMoveTarget);
                    rc.transform();
                }
                return;
            }
            if (rc.canSenseLocation(transformAndMoveTarget)){
                if (!goodPlace){
                    transformAndMoveTarget = findRubbleFreeSettleLocation();
                    goodPlace = true;
                }
                else if (rc.canSenseRobotAtLocation(transformAndMoveTarget)){
                    // TODO: Improve this.
                    transformAndMoveTarget = findRubbleFreeSettleLocation();
                    goodPlace = true;
                    // transformAndUpdate();
                    return;
                }
            }
            if (CombatUtil.militaryCount(visibleEnemies) > 0){
                // TODO: Improve this too
                transformAndMoveTarget = findRubbleFreeSettleLocation();
                // transformAndMoveTarget = findRubbleFreeSettleLocationInDirectionOfOrigin();
                goodPlace = true;
            }
            BFS.move(transformAndMoveTarget);

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void updateArchonCanSeeEnemies() throws GameActionException{
        visibleEnemies = rc.senseNearbyRobots(ARCHON_VISION_RADIUS, ENEMY_TEAM);
        if (CombatUtil.militaryAndArchonCount(visibleEnemies) > 0 && rc.getActionCooldownTurns() < 20)
            Comms.updateArchonCanSeeEnemies(true);
        else Comms.updateArchonCanSeeEnemies(false);
    }


    /**
    * Run a single turn for an Archon.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runArchon(RobotController rc) throws GameActionException {
        updateArchon();
        moveIfNeeded();
        vortexMoveIfNeeded();
        transformAndMove();
        buildDivision();
        shouldFlee();
        transformAndFlee();
        selfHeal();
        updateArchonCanSeeEnemies();
        BotMiner.sendCombatLocation(visibleEnemies);
        endOfTurnUpdate();
    }
}