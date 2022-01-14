package AFleeingBot;

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
    public static int myArchonID; // Each Archon will have their commID
    public static int commID; // Each Archon will have their commID
    public static int startingArchons;
    public static int minerCount;
    public static int builderCount;
    public static int soldierCount;
    public static int sageCount;
    public static int watchTowerCount;
    public static int watchTowerWeight;
    public static int turnsWaitingToBuild;
    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static RobotInfo[] visibleAllies;
    private static RobotInfo[] inRangeAllies;
    private static int fleeIndex;
    private static MapLocation fleeLocation;

    // This will give each Archon which number it is in the queue
    public static void initBotArchon() throws GameActionException {    
        int transmitterCount = rc.readSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT);
        myArchonID = transmitterCount % archonCount;
        commID = myArchonID;
        startingArchons = rc.getArchonCount();
        fleeIndex = 0;
        fleeLocation = null;
    }
    
    // Update weights of each Archon
    // TODO: Link currentweights with comms counter
    public static void updateArchonBuildUnits(){
        int lTC = turnCount;
        watchTowerWeight = watchTowerCount;
        aBUWeights[ArchonBuildUnits.BUILDER.ordinal()] = Math.min(1, 0.15 + lTC/400);
        aBUWeights[ArchonBuildUnits.MINER.ordinal()] = Math.max(1, 4.5 - lTC/50);
        aBUWeights[ArchonBuildUnits.SAGE.ordinal()] = Math.max(2.5, 4.5 - lTC/100);
        aBUWeights[ArchonBuildUnits.SOLDIER.ordinal()] = Math.min(4.5, 2 + lTC/50);
    }

    private static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(ARCHON_VISION_RADIUS, ENEMY_TEAM); // TODO - Is apparently better?
        inRangeEnemies = rc.senseNearbyRobots(ARCHON_ACTION_RADIUS, ENEMY_TEAM);
        visibleAllies = rc.senseNearbyRobots(ARCHON_VISION_RADIUS, MY_TEAM); 
        inRangeAllies = rc.senseNearbyRobots(ARCHON_ACTION_RADIUS, MY_TEAM);
    }

    public static void buildDivision() throws GameActionException{
        if (!rc.isActionReady() || currentLeadReserves < RobotType.MINER.buildCostLead) return;
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
    public static boolean waitQuota(){
        return (rc.getRoundNum() - 0) % archonCount != commID && currentLeadReserves < 95 && fleeIndex == 0;
    }

    public static void buildUnit() throws GameActionException{
        try {
            ArchonBuildUnits unitToBuild = standardOrder();
            if (unitToBuild == null || waitQuota()) {
                turnsWaitingToBuild++;
                return;
            }
            Direction bestSpawnDir = null;
            double bestSpawnValue = 0;
            double SpawnValue = 0;
            MapLocation lCR = currentLocation; 

            RobotType unitType=giveUnitType(unitToBuild);
            boolean crowded = true;

            for (Direction dir : directions) {
                if (!rc.canBuildRobot(unitType, dir)){
                    if (rc.onTheMap(lCR.add(dir)) && !rc.isLocationOccupied(lCR.add(dir)))
                        crowded = false;
                    continue;
                }
                crowded = false;
                SpawnValue = getValue(lCR, currentDestination, dir); // TODO: Change destination
                if (bestSpawnDir == null || SpawnValue < bestSpawnValue) {
                    bestSpawnDir = dir;
                    bestSpawnValue = SpawnValue;
                }
            }

            if (bestSpawnDir != null){
                rc.buildRobot(unitType, bestSpawnDir);
                turnsWaitingToBuild = 0;
                // System.out.println("Unit to build is " + unitType + " with weight " + currentWeights[unitToBuild.ordinal()]);
                currentWeights[unitToBuild.ordinal()] += 1.0/aBUWeights[unitToBuild.ordinal()];
                // if(ID ==2) System.out.println("Unit built is " + unitType + " Weights are " + Arrays.toString(currentWeights));
            }

            // TODO: Use crowded flag to send a panic message to comms that the bots should get out of the way.
            // if (crowded)

        } catch (Exception e) {
            System.out.println("Archon buildUnit() Exception");
            e.printStackTrace();
        }
    }

    static double getValue(MapLocation archonLocation, MapLocation dest, Direction dir) throws GameActionException {
        double dist = 1;
        if (dest != null){
            dist = archonLocation.add(dir).distanceSquaredTo(dest);
        }
        return dist * (10 + rc.senseRubble(archonLocation.add(dir)));
    }

    public static ArchonBuildUnits standardOrder() throws GameActionException{
        boolean canBuild[] = new boolean[aBUWeights.length];
        if(shouldBuildBuilder()) canBuild[ArchonBuildUnits.BUILDER.ordinal()] = true;
        if(shouldBuildMiner()) canBuild[ArchonBuildUnits.MINER.ordinal()] = true;
        if(shouldBuildSoldier()) canBuild[ArchonBuildUnits.SOLDIER.ordinal()] = true;
        if(shouldBuildSage()) canBuild[ArchonBuildUnits.SAGE.ordinal()] = true;

        ArchonBuildUnits unitToBuild = null;
        double minWeight = 100000;

        // TODO : Loop unrolling
        for(int i = 0; i < archonBuildUnits.length; i++){
            if(!canBuild[i]) continue;
            if(unitToBuild == null || minWeight > currentWeights[i]){  // If unitToBuild is null, or this unit weight is *lesser*
                minWeight = currentWeights[i];
                unitToBuild = archonBuildUnits[i];
            }
        }

        if (watchTowerDebt(minWeight, unitToBuild)) {
                // System.out.println("Building watchtower instead of " + giveUnitType(unitToBuild));
                turnsWaitingToBuild++;
                return null;
        }
        // System.out.println("Unit to build is " + unitToBuild + " with weight " + minWeight);
        return unitToBuild;
    }

     // TODO: Reorganise after sprint
    public static boolean watchTowerDebt(double minWeight, ArchonBuildUnits unitToBuild) throws GameActionException{
        return minWeight < 100000 && 
                watchTowerWeight < minWeight && 
                builderCount != 0 && 
                currentLeadReserves < giveUnitType(unitToBuild).buildCostLead + RobotType.WATCHTOWER.buildCostLead && 
                turnsWaitingToBuild < 100 && 
                (BotMiner.areMiningLocationsAbundant() || currentLeadReserves > 500);
    }

    public static boolean shouldBuildBuilder(){
        if (turnCount < 30) return false;
        if (builderCount > archonCount || currentLeadReserves < 200) return false;
        return true;
    }

    public static boolean shouldBuildMiner(){
        // TODO: Add turnsWaitingToBuild because of less lead here
        // if (minerCount > 30 * archonCount) return false;
        return currentLeadReserves >= RobotType.MINER.buildCostLead;
    }

    public static boolean shouldBuildSoldier(){
        if (turnCount < 15) return false;
        return true;
    }

    public static boolean shouldBuildSage(){
        return false;
    }

    // Read the unit counts this round
    public static void getCommCounts() throws GameActionException{
        minerCount = rc.readSharedArray(Comms.CHANNEL_MINER_COUNT); 
        builderCount = rc.readSharedArray(Comms.CHANNEL_BUILDER_COUNT); 
        soldierCount = rc.readSharedArray(Comms.CHANNEL_SOLDIER_COUNT); 
        watchTowerCount = rc.readSharedArray(Comms.CHANNEL_WATCHTOWER_COUNT); 
    }

    public static void clearCommCounts() throws GameActionException{
        rc.writeSharedArray(Comms.CHANNEL_MINER_COUNT, 0);
        rc.writeSharedArray(Comms.CHANNEL_BUILDER_COUNT, 0);
        rc.writeSharedArray(Comms.CHANNEL_SOLDIER_COUNT, 0);
        // rc.writeSharedArray(Comms._, 0);
        rc.writeSharedArray(Comms.CHANNEL_WATCHTOWER_COUNT, 0);
    }

    public static void archonComms() throws GameActionException{
        Comms.updateComms();
        
        int transmitterCount = rc.readSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT);
        // I'm first Archon (by birth or death of ones before me). I'm number zero transmitter.
        if(transmitterCount > commID) commID = 0;
        else commID = transmitterCount; 
        rc.writeSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT, commID+1); // Clears Transmitter Count by First Archon

        if (turnCount < 3 || hasMoved)
            Comms.writeSHAFlagMessage(currentLocation, Comms.SHAFlag.ARCHON_LOCATION, Comms.CHANNEL_ARCHON_START + commID*4);        
        
        getCommCounts();

        // If you're the last Archon, clean the counts for this new turn
        if(commID + 1 == archonCount)  clearCommCounts();
        Comms.updateArchonLocations();
    }

    private static void shouldFlee(){
        // You have more enemies attacking you than friends that could come save you and you are not the main producer Archon
        if (CombatUtil.militaryCount(inRangeEnemies) > CombatUtil.militaryCount(visibleAllies) && commID > 0 && turnsWaitingToBuild > 0)
            fleeIndex++;
        else fleeIndex = 0;
    }

    private static void transformAndFlee() throws GameActionException{
        if (fleeIndex > 5 && rc.getMode()!= RobotMode.PORTABLE && rc.canTransform()) {
            if (fleeLocation == null) { 
                fleeLocation = getClosestArchonLocation(true);
                if (!rc.canSenseLocation(fleeLocation)) rc.transform();
                else fleeLocation = null;
            }
        }

        if (rc.getMode() == RobotMode.PORTABLE){ 
            // Enemies contained and transform cooldown gone
            if (rc.canTransform() && currentLocation.isWithinDistanceSquared(fleeLocation, ARCHON_ACTION_RADIUS)){
                rc.transform(); // TODO - Doesn't account for Rubble
                fleeLocation = null;
            }
            if (fleeLocation != null) BFS.move(fleeLocation);
        }
    }

    // Sadly, self heal does not look possible.
    private static void selfHeal() throws GameActionException{
        if (rc.isActionReady() && rc.senseRubble(currentLocation) < 10) {
            RobotInfo unit = null;
            double robotHealth = 1000;
            for(int i = inRangeAllies.length; --i >=0;){
                RobotInfo ally = inRangeAllies[i];
                if(ally.getHealth() == ally.getType().getMaxHealth(rc.getLevel())) continue;
                if (ally.getHealth() < robotHealth && rc.canRepair(ally.getLocation())) {
                    robotHealth = ally.health;
                    unit = ally;
                }
            }
            if(unit!=null){
                // System.out.println("Repairing");
                rc.repair(unit.getLocation());
            }
        }
    }

    /**
    * Run a single turn for an Archon.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runArchon(RobotController rc) throws GameActionException {
        archonComms();
        updateVision();
        updateArchonBuildUnits();
        buildDivision();
        shouldFlee();
        transformAndFlee();
        selfHeal();
        BotMiner.surveyForOpenMiningLocationsNearby();
        BotSoldier.sendCombatLocation(visibleEnemies);
    }

}
