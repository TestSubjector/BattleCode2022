package FourthBot;

import java.util.Arrays;

import battlecode.common.*;

public class BotArchon extends Util{

    enum ArchonBuildUnits { 
        MINER, 
        SOLDIER,
        BUILDER,
        SAGE,  
    }

    public static ArchonBuildUnits[] archonBuildUnits = ArchonBuildUnits.values();
    public static double[] aBUWeights = new double[ArchonBuildUnits.values().length];
    public static double[] currentWeights = new double[ArchonBuildUnits.values().length];

    public static void updateArchonBuildUnits(){
        int lTC = turnCount;
        aBUWeights[ArchonBuildUnits.BUILDER.ordinal()] = Math.min(2.5, 1.5 + lTC/200);
        aBUWeights[ArchonBuildUnits.MINER.ordinal()] = Math.max(2.5, 4.5 - lTC/100);
        aBUWeights[ArchonBuildUnits.SAGE.ordinal()] = Math.max(2.5, 4.5 - lTC/100);
        aBUWeights[ArchonBuildUnits.SOLDIER.ordinal()] = Math.min(7.5, 1.5 + lTC/50);
    }

    /**
    * Run a single turn for an Archon.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runArchon(RobotController rc) throws GameActionException {
        rc.writeSharedArray(Comms.CHANNEL_RUBBLE_TRANSMITTER_COUNT, 0);
        updateArchonBuildUnits();
        buildDivision();
        // randomBuild();
    }
    
    public static void buildDivision() throws GameActionException{
        if (!rc.isActionReady() || currentLeadReserves < RobotType.MINER.buildCostLead) return;
        else buildUnit();
    }

    public static void buildUnit() throws GameActionException{
        try {
            ArchonBuildUnits unitToBuild = standardOrder();
            if (unitToBuild == null) return;
            Direction bestSpawnDir = null;
            double bestSpawnValue = 0;
            double SpawnValue = 0;
            MapLocation lCR = currentLocation; 

            RobotType unitType=null;
            switch(unitToBuild){
                case BUILDER: unitType = RobotType.BUILDER; break;
                case MINER:   unitType = RobotType.MINER;   break;
                case SAGE:    unitType = RobotType.SAGE;    break;
                case SOLDIER: unitType = RobotType.SOLDIER; break;
            }

            for (Direction dir : directions) {
                if (!rc.canBuildRobot(unitType, dir)) continue;
                SpawnValue = getValue(lCR, rememberedEnemyArchonLocation, dir); // TODO: Change destination
                if (bestSpawnDir == null || SpawnValue < bestSpawnValue) {
                    bestSpawnDir = dir;
                    bestSpawnValue = SpawnValue;
                }
            }

            if (bestSpawnDir != null){
                rc.buildRobot(unitType, bestSpawnDir);
                // System.out.println("Unit to build is " + unitType + " with weight " + currentWeights[unitToBuild.ordinal()]);
                currentWeights[unitToBuild.ordinal()] += 1.0/aBUWeights[unitToBuild.ordinal()];
                // if(ID ==2) System.out.println("Unit built is " + unitType + " Weights are " + Arrays.toString(currentWeights));
            }
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
        return dist/rc.senseRubble(archonLocation.add(dir));
    }

    public static ArchonBuildUnits standardOrder(){
        boolean canBuild[] = new boolean[aBUWeights.length];
        if(shouldBuildBuilder()) canBuild[ArchonBuildUnits.BUILDER.ordinal()] = true;
        if(shouldBuildMiner()) canBuild[ArchonBuildUnits.MINER.ordinal()] = true;
        if(shouldBuildSoldier()) canBuild[ArchonBuildUnits.SOLDIER.ordinal()] = true;
        if(shouldBuildSage()) canBuild[ArchonBuildUnits.SAGE.ordinal()] = true;

        ArchonBuildUnits unitToBuild = null;
        double minWeight = 1000;

        //TODO : Loop unrolling
        for(int i = 0; i < archonBuildUnits.length; i++){
            if(!canBuild[i]) continue;
            if(unitToBuild == null || minWeight > currentWeights[i]){  // If unitToBuild is null, or this unit weight is *lesser*
                minWeight = currentWeights[i];
                unitToBuild = archonBuildUnits[i];
            }
        }
        // System.out.println("Unit to build is " + unitToBuild + " with weight " + minWeight);
        return unitToBuild;
    }

    public static boolean shouldBuildBuilder(){
        if (turnCount < 10) return false;
        return true;
    }

    public static boolean shouldBuildMiner(){
        return currentLeadReserves >= RobotType.MINER.buildCostLead;
    }

    public static boolean shouldBuildSoldier(){
        if (turnCount < 10) return false;
        return true;
    }

    public static boolean shouldBuildSage(){
        return false;
    }

    public static void randomBuild() throws GameActionException{
        Direction dir = Globals.directions[Globals.rng.nextInt(Globals.directions.length)];
        if (Globals.rng.nextBoolean() && rc.canBuildRobot(RobotType.MINER, dir)) 
            rc.buildRobot(RobotType.MINER, dir);
        else if (rc.canBuildRobot(RobotType.SOLDIER, dir)) 
            rc.buildRobot(RobotType.SOLDIER, dir);
    }
}
