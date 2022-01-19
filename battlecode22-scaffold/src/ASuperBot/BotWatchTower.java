package ASuperBot;

import battlecode.common.*;

public class BotWatchTower extends Util{

    //TODO: Watchtower will be in prototype form/portable form for a while. Make sure you dont call methods like attack not possible then.

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;

    public static void initBotWatchTower(){
        
    }

    public static void watchTowerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_WATCHTOWER_COUNT);
        Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
        Comms.updateComms(); 
    }

    private static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(WATCHTOWER_VISION_RADIUS, ENEMY_TEAM);
        inRangeEnemies = rc.senseNearbyRobots(WATCHTOWER_ACTION_RADIUS, ENEMY_TEAM);
    }

    // TODO: Different priority for watchTower using the todo in next line? 
    // TODO Sense rubble at their location and factor that in to find more dangerous unit
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
			return 220.0 * enemyType.getDamage(enemyUnit.getLevel()) / (enemyHealth * (10.0+rubbleAtLocation));
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

    private static void turretTower() throws GameActionException{
        if (inRangeEnemies.length > 0 && rc.isActionReady()) {
            chooseTargetAndAttack(inRangeEnemies);
        }
    }

    private static void portableTower() throws GameActionException{

    }

    static void runWatchTower(RobotController rc) throws GameActionException {
        watchTowerComms();
        updateVision();
        // if (!rc.isActionReady() && (!rc.isMovementReady() || !rc.canTransform())) return;
        if (rc.getMode() == RobotMode.PROTOTYPE) return;
        else if (rc.getMode() == RobotMode.TURRET) turretTower();
        else if (rc.getMode() == RobotMode.PORTABLE) portableTower();
    }
}
