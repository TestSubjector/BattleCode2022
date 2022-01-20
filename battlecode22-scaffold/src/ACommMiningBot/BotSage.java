package ACommMiningBot;

import battlecode.common.*;

public class BotSage extends Util{

    //TODO: Watchtower will be in prototype form/portable form for a while. Make sure you dont call methods like attack not possible then.

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;

    public static void initBotSage() throws GameActionException{
        System.out.println("Hello, this is a sage.");
        currentDestination = Comms.getClosestEnemyArchonLocation();
        if (currentDestination == null){
            for (int i = 0; i < 4; i++){
                if (rememberedEnemyArchonLocations[i] != null && CombatUtil.enemyArchonLocationAreaIsFalse(rememberedEnemyArchonLocations[i]))
                    rememberedEnemyArchonLocations[i] = null;
            }
            int token = BIRTH_ROUND % 3;
            if (rememberedEnemyArchonLocations[(token+1)%3] != null)  
                currentDestination = rememberedEnemyArchonLocations[(token+1)%3];
            else if (rememberedEnemyArchonLocations[(token+2)%3] != null) 
                currentDestination = rememberedEnemyArchonLocations[(token+2)%3];
            else if (rememberedEnemyArchonLocations[(token)%3] != null) 
                currentDestination = rememberedEnemyArchonLocations[(token)%3];
            else{
                findNewCombatLocation();
                if (currentDestination == null) currentDestination = CENTER_OF_THE_MAP;
            }
        }
    }

    public static void sageComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_SAGE_COUNT);
        Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
        Comms.updateComms(); 
    }

    private static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(SAGE_VISION_RADIUS, ENEMY_TEAM);
        inRangeEnemies = rc.senseNearbyRobots(SAGE_ACTION_RADIUS, ENEMY_TEAM);
    }

    private static boolean findNewCombatLocation() throws GameActionException{
        if (currentDestination == null || (visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= SAGE_VISION_RADIUS)){
            MapLocation combatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (combatLocation != null) currentDestination = combatLocation;
            return true;
        }
        return false;
    }

    private static double getEnemyScore(RobotInfo enemyUnit) throws GameActionException{
        RobotType enemyType = enemyUnit.type;
        int enemyHealth = enemyUnit.getHealth();
        int rubbleAtLocation = rc.senseRubble(enemyUnit.getLocation());
		switch(enemyType) {
		case ARCHON:
			return 0.00001;
		case LABORATORY:
			return 0.000001;
        case BUILDER:
		case MINER:
			return 0.22 /(enemyHealth * (10.0+rubbleAtLocation)); // Max= 0.22, Min = 0.005 Low priority
		case WATCHTOWER:
		case SOLDIER:
		case SAGE:
			return 220.0 * enemyType.getDamage(enemyUnit.getLevel()) / (Math.abs(enemyHealth - 45.00001) * (10.0+rubbleAtLocation));
		default:
			return 0.0001;
		}
	}

    // Copy of BotSoldier.chooseTargetAndAttack()
    private static void chooseTargetAndAttack(RobotInfo[] targets) throws GameActionException {
		RobotInfo bestTarget = null;
		double bestValue = -1;
        double value = 0;
		for (RobotInfo target : targets) {
			value = getEnemyScore(target);
			if (value > bestValue) {
				bestValue = value;
				bestTarget = target;
			}
		}
		if (bestTarget != null) {
			rc.attack(bestTarget.location);
		}
	}

    static void runSage(RobotController rc) throws GameActionException {
        sageComms();
        updateVision();
        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
                
                chooseTargetAndAttack(inRangeEnemies);
            }
        }
        if (!rc.isActionReady()){
            MapLocation closestArchon = getClosestArchonLocation();
            if (inRangeEnemies.length> 0 && closestArchon != null){
                BFS.move(closestArchon);
                updateVision();
            }
        }

        if (currentDestination == null || visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= SAGE_VISION_RADIUS)
        {
            currentDestination = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
        }

        if (visibleEnemies.length == 0) {
            BFS.move(currentDestination); 
            updateVision();
        }

        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
                chooseTargetAndAttack(inRangeEnemies);
            }
        }
    }
}
