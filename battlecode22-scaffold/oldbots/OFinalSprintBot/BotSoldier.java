package OFinalSprintBot;

import battlecode.common.*;

public class BotSoldier extends Util{

    // Credits to @TheDuck314 for a major chunk of this code

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static MapLocation attackTarget;

    public static void initBotSoldier(){
        if (isRubbleMapEnabled) RubbleMap.initRubbleMap();
    }

    public static void soldierComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_SOLDIER_COUNT);
        Comms.updateComms();
    }

    private static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(SOLDIER_VISION_RADIUS, ENEMY_TEAM);
        inRangeEnemies = rc.senseNearbyRobots(SOLDIER_ACTION_RADIUS, ENEMY_TEAM);
    }

    private static void detectIfAttackTargetIsGone() throws GameActionException {
		if (attackTarget != null) {
			if (currentLocation.distanceSquaredTo(attackTarget) <= 10) {
			    RobotInfo targetInfo = rc.senseRobotAtLocation(attackTarget);
				if (targetInfo == null || targetInfo.team == MY_TEAM) {
					attackTarget = null;
				}
			}
		}
	}

    // TODO: Sense rubble at their location and factor that in to find more dangerous unit
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
			return (type.damage+0.0001) / (health); // Cooldown due to rubble ;
		}
	}

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

    // TODO: Add check for Rubble
    private static boolean tryToBackUpToMaintainMaxRange(RobotInfo[] visibleHostiles) throws GameActionException {
		int closestHostileDistSq = Integer.MAX_VALUE;
        MapLocation lCR = currentLocation;
        for (RobotInfo hostile : visibleHostiles) {
			if (!hostile.type.canAttack()) continue;
			int distSq = lCR.distanceSquaredTo(hostile.location);
			if (distSq < closestHostileDistSq) {
				closestHostileDistSq = distSq;
			}
		}
		
		if (closestHostileDistSq > SOLDIER_ACTION_RADIUS) return false; // We dont want to get out of our max range
		
		Direction bestRetreatDir = null;
		int bestDistSq = closestHostileDistSq;
		// boolean foundOrthogonalRetreatDir = false;
		for (Direction dir : directions) {
			if (!rc.canMove(dir)) continue;
			MapLocation dirLoc = lCR.add(dir);			
			int smallestDistSq = Integer.MAX_VALUE;
			for (RobotInfo hostile : visibleHostiles) {
				if (!hostile.type.canAttack()) continue;
				int distSq = hostile.location.distanceSquaredTo(dirLoc);
				if (distSq < smallestDistSq) {
					smallestDistSq = distSq;
				}
			}
			if (smallestDistSq > bestDistSq) {
				bestDistSq = smallestDistSq;
				bestRetreatDir = dir;
			}
		}
		if (bestRetreatDir != null) {
			rc.move(bestRetreatDir);
            currentLocation = rc.getLocation();
			return true;
		}
		return false;
	}

    private static boolean tryMoveToHelpAlly(RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		MapLocation closestHostileLocation = closestHostile.location;
		
		boolean allyIsFighting = false;
		RobotInfo[] alliesAroundHostile = rc.senseNearbyRobots(closestHostileLocation, SOLDIER_ACTION_RADIUS, MY_TEAM);
		for (RobotInfo ally : alliesAroundHostile) {
			if (ally.type.canAttack()) {
				if (ally.location.distanceSquaredTo(closestHostileLocation) <= ally.type.actionRadiusSquared) {
					allyIsFighting = true;
					break;
				}
			}
		}
		if (allyIsFighting) 
			if (Movement.tryMoveInDirection(currentLocation.directionTo(closestHostileLocation))) 
				return true;

		return false;
	}

    private static boolean tryMoveToAttackProductionUnit(RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		if (closestHostile.type.canAttack()) 
            return false;
	    if (Movement.tryMoveInDirection(currentLocation.directionTo(closestHostile.location))) 
            return true;
		return false;
	}

    private static boolean tryMoveToEngageOutnumberedEnemy(RobotInfo[] visibleHostiles, RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		MapLocation closestHostileLocation = closestHostile.location;
        int numNearbyHostiles = 0;
		for (RobotInfo hostile : visibleHostiles) {
			if (hostile.type.canAttack()) {
				if (hostile.location.distanceSquaredTo(closestHostileLocation) <= SOLDIER_ACTION_RADIUS) {
					numNearbyHostiles += 1;
				}
			}
		}
		
		int numNearbyAllies = 1; // Counts ourself
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(closestHostileLocation, SOLDIER_ACTION_RADIUS, MY_TEAM);
		for (RobotInfo ally : nearbyAllies) {
			if (ally.type.canAttack() && ally.health >= ally.type.getMaxHealth(1)/2) {
				numNearbyAllies += 1;
			}
		}
		
		if (numNearbyAllies > numNearbyHostiles || (numNearbyHostiles == 1 && rc.getHealth() > closestHostile.health)) {
			return Movement.tryMoveInDirection(currentLocation.directionTo(closestHostile.location));
		}
		return false;
	}

    private static boolean tryToMicro() throws GameActionException {
        if (visibleEnemies.length == 0) { // TODO: Either wait or get out of any possible Watchtower range. Skip if charging
            return false;
        }

        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
                chooseTargetAndAttack(inRangeEnemies);
            }
            else if (rc.isMovementReady()){
                RobotInfo closestHostile = getClosestUnit(visibleEnemies);
                if(tryMoveToHelpAlly(closestHostile)) return tryToMicro();
                if(tryMoveToAttackProductionUnit(closestHostile)) return tryToMicro();
            }
        }
        if (rc.isMovementReady()){
            // Most important function
            if (inRangeEnemies.length > 0 && tryToBackUpToMaintainMaxRange(visibleEnemies)) return true; // Cant attack, try to move out
            RobotInfo closestHostile = getClosestUnit(visibleEnemies);
            if (tryMoveToHelpAlly(closestHostile)) return true; // Maybe add how many turns of attack cooldown here?
            if (tryMoveToEngageOutnumberedEnemy(visibleEnemies, closestHostile)) return true;
            if (tryMoveToAttackProductionUnit(closestHostile)) return true;
        }
        return false;
    }

    // Try to attack someone - 114 bytecodes
    public static void attackRandomly() throws GameActionException {
        if (inRangeEnemies.length > 0) {
            MapLocation toAttack = inRangeEnemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
    }

    public static void sendCombatLocation(RobotInfo[] visibleHostiles) throws GameActionException{
        if (visibleHostiles.length != 0){
            RobotInfo closestHostile = getClosestUnit(visibleHostiles);
            if (closestHostile != null)
                Comms.writeCommMessageOverrwriteLesserPriorityMessageToHead(Comms.commType.COMBAT, closestHostile.getLocation(), Comms.SHAFlag.COMBAT_LOCATION);
        }
    }


    // If our current destination has no enemies left, move to the nearest new location with combat
    public static void findNewCombatLocation() throws GameActionException{
        if (visibleEnemies.length == 0 && rc.canSenseLocation(currentDestination)){
            MapLocation combatLocation = Comms.findNearestLocationOfThisType(currentLocation, Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (combatLocation != null){
                currentDestination = combatLocation;
            }
        }
    }
    
    /**
    * Run a single turn for a Soldier.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runSoldier(RobotController rc) throws GameActionException {
        soldierComms(); // 300 Bytecodes
        
        // TODO: Combat simulator for soldiers, sense all rubble in vision for 1v1 or 1vMany combat

        updateVision();
        detectIfAttackTargetIsGone();
        tryToMicro();
        // TODO: Turret avoidance Comms code

        findNewCombatLocation();
        if (visibleEnemies.length == 0) { // Do normal pathfinding only when no enemy units around
            Movement.goToDirect(currentDestination); // 370 Bytecodes
        }

        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            // RubbleMap.updateRubbleMap();
        }
        sendCombatLocation(visibleEnemies);
        BotMiner.surveyForOpenMiningLocationsNearby(); // TODO: Reorganise to sprint
    }
}
