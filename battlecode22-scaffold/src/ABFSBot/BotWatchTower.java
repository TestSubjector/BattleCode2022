package ABFSBot;

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
    private static double getEnemyScore(RobotType type, int health) {
        switch(type) {
        case ARCHON:
            return 0.0001;
        case LABORATORY:
            return 0.00001;
        case MINER:
            return 0.1/(health); // Low priority
        case WATCHTOWER:
            return 0.5 * RobotType.WATCHTOWER.damage / (health); // RobotType.WATCHTOWER attack cooldown;
        case SOLDIER:
            return RobotType.SOLDIER.damage / (health); //  SOLDIER attack cooldown;
        case SAGE:
            return 10 / (health * 1);
        default:
            return (type.damage+0.00001) / (health); // Cooldown due to rubble ;
        }
    }

    // Copy of BotSoldier.chooseTargetAndAttack()
    private static void chooseTargetAndAttack(RobotInfo[] targets) throws GameActionException {
		RobotInfo bestTarget = null;
		double bestValue = -1;
        double value = 0;
		for (RobotInfo target : targets) {
			value = getEnemyScore(target.getType(), target.getHealth());
			if (value > bestValue) {
				bestValue = value;
				bestTarget = target;
			}
		}
		if (bestTarget != null) {
			rc.attack(bestTarget.location);
		}
	}

    static void runWatchTower(RobotController rc) throws GameActionException {
        watchTowerComms();
        updateVision();
        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
            chooseTargetAndAttack(inRangeEnemies);
            }
        }
    }
}
