package FourthBot;

import battlecode.common.*;

public class BotArchon extends Util{

    enum ArchonBuildUnits {
        BUILDER, 
        MINER, 
        SAGE, 
        SOLDIER, 
    }

    ArchonBuildUnits[] archonBuildUnits = ArchonBuildUnits.values();
    public static double[] aBUWeights = new double[ArchonBuildUnits.values().length];

    void updateArchonBuildUnits(){
        int lTurnCount = turnCount;
        aBUWeights[ArchonBuildUnits.BUILDER.ordinal()] = Math.min(2.5, 2.0 + lTurnCount/100);
        aBUWeights[ArchonBuildUnits.MINER.ordinal()] = Math.max(3.0, 3.5 - lTurnCount/200);
        aBUWeights[ArchonBuildUnits.SAGE.ordinal()] = Math.min(1.5, 1.5 + lTurnCount/100);
        aBUWeights[ArchonBuildUnits.SOLDIER.ordinal()] = Math.min(3.5, 2.0 + lTurnCount/100);
    }

    /**
    * Run a single turn for an Archon.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    public static void runArchon(RobotController rc) throws GameActionException {
        
        // Pick a direction to build in.
        randomBuild();
    }
    
    public static void buildDivision(){
        if (!rc.isActionReady()) return;

        // standardOrder();

    }

    // public static ArchonBuildUnits standardOrder(){
    //     boolean canBuild[] = new boolean[aBUWeights.length];
        // if(shoudBuildSoldier()) canBuild[ArchonBuildUnits.SOLDIER.ordinal()] = true;
        // if(shoudBuildBuilder()) canBuild[ArchonBuildUnits.BUILDER.ordinal()] = true;
        // if(shoudBuildMiner()) canBuild[ArchonBuildUnits.MINER.ordinal()] = true;
        // if(shoudBuildSage()) canBuild[ArchonBuildUnits.SAGE.ordinal()] = true;

        // ArchonBuildUnits unitToBuild = null;
        // double maxWeight = 0;

        // for(int i = 0; i < aBUWeights.length; ++i){
        //     if(canBuild[i] && aBUWeights[i] > maxWeight){
        //         maxWeight = aBUWeights[i];
        //         unitToBuild = archonBuildUnits[i];
        //     }
        // }

    // }

    public static void randomBuild() throws GameActionException{
        Direction dir = Globals.directions[Globals.rng.nextInt(Globals.directions.length)];
        if (Globals.rng.nextBoolean() && rc.canBuildRobot(RobotType.MINER, dir)) 
            rc.buildRobot(RobotType.MINER, dir);
        else if (rc.canBuildRobot(RobotType.SOLDIER, dir)) 
            rc.buildRobot(RobotType.SOLDIER, dir);
    }
}
