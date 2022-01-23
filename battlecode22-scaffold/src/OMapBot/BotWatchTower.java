package OMapBot;

import battlecode.common.*;

public class BotWatchTower extends Util{

    //TODO: Watchtower will be in prototype form/portable form for a while. Make sure you dont call methods like attack not possible then.

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static RobotInfo[] visibleAllies;
    private static final int PACK_DELAY = 10;
	private static int packCountdown;

    public static void initBotWatchTower(){
        packCountdown = PACK_DELAY;
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
        visibleAllies = rc.senseNearbyRobots(WATCHTOWER_VISION_RADIUS, MY_TEAM);
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

    private static boolean chooseTargetAndAttack(RobotInfo[] targets) throws GameActionException {
		RobotInfo bestTarget = null;
		double bestValue = -1;
        double value = 0;
		for (int i = targets.length; --i >= 0;) {
            RobotInfo target = targets[i];
			value = getEnemyScore(target);
			if (value > bestValue) {
				bestValue = value;
				bestTarget = target;
			}
		}
		if (bestTarget != null) {
			rc.attack(bestTarget.location);
            return true;
		}
        return false;
	}

    private static void turretTower() throws GameActionException{
        if (inRangeEnemies.length > 0 && rc.isActionReady() && chooseTargetAndAttack(inRangeEnemies)) {
            BotMiner.sendCombatLocation(visibleEnemies);
            packCountdown = PACK_DELAY;
			return;
		}

		if (inRangeEnemies.length == 0) {
			--packCountdown;
			if (packCountdown == 0 && rc.canTransform()) {
				rc.transform();
				return;
			}
		}		
        else if (visibleEnemies.length != 0) {
			packCountdown = Math.min(10, packCountdown + 1);
		}	
    }

    private static MapLocation goodLocationToSettle() throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2);
        
        int optVal = rc.senseRubble(rc.getLocation());
        MapLocation optLoc = rc.getLocation();
        for(int i = adjacentLocations.length; --i >= 0;){
            MapLocation loc = adjacentLocations[i];
            if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc)) continue;
            int rubble = rc.senseRubble(loc);
            if (rubble < optVal){
                optVal = rubble;
                optLoc = loc;
            }
        }
        return optLoc;
    }

    private static void layRoots() throws GameActionException{
        if (visibleEnemies.length > 0 && currentDestination != null && rc.getLocation().distanceSquaredTo(currentDestination) <= 2) {
            currentDestination = goodLocationToSettle();
        }
        if (inRangeEnemies.length > 0){
            if(rc.canTransform()) {
			    rc.transform();
                currentDestination = null;
			    packCountdown = PACK_DELAY;
			    return;
            }
		}
    }

    private static void portableTower() throws GameActionException{
        layRoots();
        moveToCombatLocation();
        layRoots();
    }

    private static boolean findNewCombatLocation() throws GameActionException{
        if (currentDestination == null || (visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= WATCHTOWER_VISION_RADIUS)){
            MapLocation combatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (combatLocation != null) currentDestination = combatLocation;
            return true;
        }
        return false;
    }

    private static void moveToCombatLocation() throws GameActionException {
        findNewCombatLocation();
		if (currentDestination != null && rc.isMovementReady()) BFS.move(currentDestination);
		// if (tryGoToCenterOfMass()) {
		// 	return;
		// }
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
