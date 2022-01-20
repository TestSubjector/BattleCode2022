package AnAnomalyBot;

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
        if (currentDestination == null || (visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= SOLDIER_VISION_RADIUS)){
            MapLocation combatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (combatLocation != null) currentDestination = combatLocation;
            return true;
        }
        return false;
    }

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
            if (closestArchon != null){
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
