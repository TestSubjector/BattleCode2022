package AMapBot;

import battlecode.common.*;

public class BotSage extends CombatUtil{

    //TODO: Watchtower will be in prototype form/portable form for a while. Make sure you dont call methods like attack not possible then.

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static boolean inHealingState;
    private static MapLocation finalDestination = null; 

    public static void initBotSage() throws GameActionException{
        System.out.println("Hello, this is a sage.");
        inHealingState = false;
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
			return 1;
        case BUILDER:
		case MINER:
			return 0.22 /(enemyHealth * (10.0+rubbleAtLocation));
		case WATCHTOWER:
		case SOLDIER:
		case SAGE:
			return 220.0 * enemyType.getDamage(enemyUnit.getLevel()) / (Math.abs(enemyHealth + Math.max(enemyHealth, 45) - 90.00001) * (10.0+rubbleAtLocation));
		default:
			return 0.0001;
		}
	}

    // Copy of BotSoldier.chooseTargetAndAttack()
    private static void chooseTargetAndAttack(RobotInfo[] targets) throws GameActionException {
		RobotInfo bestTarget = null;
		double bestValue = -1;
        double value = 0;
		for (int i = targets.length; --i >= 0;) {
			value = getEnemyScore(targets[i]);
			if (value > bestValue) {
				bestValue = value;
				bestTarget = targets[i];
			}
		}
		if (bestTarget != null) {
			rc.attack(bestTarget.location);
		}
	}

    public static Direction mostEnemyDroidsAdjacentLocation(RobotInfo[] visibleHostiles) throws GameActionException {
        Direction mostCrowdedDirection = null;
        int maxCrowd = 0;
        for (Direction dir : directions){
            int crowd = 0;
            MapLocation adjLoc = rc.getLocation().add(dir);
            if (!rc.canMove(dir)) continue;
            for (RobotInfo hostile : visibleHostiles){
                if (hostile.type == RobotType.MINER || hostile.type == RobotType.BUILDER) continue;
                if (adjLoc.distanceSquaredTo(hostile.location) <= RobotType.SAGE.actionRadiusSquared){
                    crowd++;
                }
            }
            if (crowd > maxCrowd){
                maxCrowd = crowd;
                mostCrowdedDirection = dir;
            }
        }
        if (maxCrowd > 3){
            return mostCrowdedDirection;
        }
        else
            return null;
    }

    private static void manageHealingState() {
        if (rc.getHealth() < rc.getType().getMaxHealth(rc.getLevel()) / 8.0) {
            rc.setIndicatorString("healing");
            inHealingState = true;
        }
        else if (rc.getHealth() > 2.0/3.0 * rc.getType().getMaxHealth(rc.getLevel())) {
            inHealingState = false;
        }
    }

    private static boolean tryToHealAtArchon() throws GameActionException {
		if (!rc.isMovementReady()) return false;
		
		MapLocation closestArchon = getLowestHealingArchonLocation();
		
		if (closestArchon == null)
            return false;
        else if (rc.getLocation().distanceSquaredTo(closestArchon) <= ARCHON_ACTION_RADIUS) {
            Movement.tryMoveInDirection(closestArchon);
        }
		else if (finalDestination == null)
            BFS.move(closestArchon); // TODO: Avoid enemies
		return true;
	}

    private static void tryMicro() throws GameActionException{
        if (rc.isActionReady()){
            if (!inHealingState && rc.isMovementReady() && visibleEnemies.length >=4 && inRangeEnemies.length < 4){
                Direction crowdedDirection = mostEnemyDroidsAdjacentLocation(visibleEnemies);
                if (crowdedDirection != null) {
                    rc.move(crowdedDirection);
                    updateVision();
                    // rc.setIndicatorString("Charging");
                }
            }
            if (inRangeEnemies.length > 0) {
                if (rc.canEnvision(AnomalyType.CHARGE) && inRangeEnemies.length >= 4){
                    // rc.setIndicatorString("Envisioning");
                    rc.envision(AnomalyType.CHARGE);
                }
                else{
                    chooseTargetAndAttack(inRangeEnemies);
                }
            }
        }
    }

    private static boolean sendCombatLocation(RobotInfo[] visibleHostiles) throws GameActionException{
        if (visibleHostiles.length != 0 && Clock.getBytecodesLeft() > 600){
			RobotInfo closestHostile = getClosestUnitWithCombatPriority(visibleHostiles);
            if (closestHostile == null) return false;
            else
                currentDestination = closestHostile.location;
				Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, closestHostile.getLocation(), Comms.SHAFlag.COMBAT_LOCATION);
            return true;
        }
        return false;
    }

    static void runSage(RobotController rc) throws GameActionException {
        sageComms();
        updateVision();
        sendCombatLocation(visibleEnemies);
        manageHealingState();
        tryMicro();
        
        if (inHealingState && tryToHealAtArchon()){
            // return;
        } 

        if (!rc.isActionReady() && rc.isMovementReady()){
            if (CombatUtil.militaryCount(inRangeEnemies) > 0){
                MapLocation retreatTarget = rc.getLocation();
		        for (int i = inRangeEnemies.length; --i >= 0;) {
                    RobotInfo hostile = inRangeEnemies[i];
		        	if (!hostile.type.canAttack()) continue;			
		        	retreatTarget = retreatTarget.add(hostile.location.directionTo(rc.getLocation()));
		        }
		        if (!rc.getLocation().equals(retreatTarget)) {
		        	if (Movement.tryMoveInDirection(retreatTarget));
                    else BFS.move(getClosestArchonLocation());
                    // rc.setIndicatorString("retreating");
		        }
                updateVision();
            }
        }
        if (currentDestination == null || visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= SAGE_VISION_RADIUS)
        {
            currentDestination = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
        }

        if (visibleEnemies.length == 0 && rc.isMovementReady()) {
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
