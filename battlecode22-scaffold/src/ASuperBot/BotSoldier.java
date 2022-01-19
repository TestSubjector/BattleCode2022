package ASuperBot;

import battlecode.common.*;

public class BotSoldier extends CombatUtil{

    // Credits to @TheDuck314 for a major chunk of this code

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static RobotInfo attackTarget;
    private static boolean inHealingState;

    public static void initBotSoldier() throws GameActionException{
        inHealingState = false;
        currentDestination = Comms.getClosestEnemyArchonLocation();
        if (currentDestination == null){
            for (int i = 0; i < 4; i++){
                if (rememberedEnemyArchonLocations[i] != null && CombatUtil.enemyArchonLocationGuessIsFalse(rememberedEnemyArchonLocations[i]))
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

    public static void soldierComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_SOLDIER_COUNT);
        Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
        Comms.updateComms();
    }

    private static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(SOLDIER_VISION_RADIUS, ENEMY_TEAM);
        inRangeEnemies = rc.senseNearbyRobots(SOLDIER_ACTION_RADIUS, ENEMY_TEAM);
    }

    // private static void detectIfAttackTargetIsGone() throws GameActionException {
	// 	if (attackTarget != null) {
	// 		if (currentLocation.distanceSquaredTo(attackTarget) <= 10) {
	// 		    RobotInfo targetInfo = rc.senseRobotAtLocation(attackTarget);
	// 			if (targetInfo.ID != attackTarget) {
	// 				attackTarget = null;
	// 			}
	// 		}
	// 	}
	// }

    // TODO: Sense rubble at their location and factor that in to find more dangerous unit
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


    private static boolean tryMoveToHelpAlly(RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		MapLocation closestHostileLocation = closestHostile.location;
		
		boolean allyIsFighting = false;
		RobotInfo[] alliesAroundHostile = rc.senseNearbyRobots(closestHostileLocation, SOLDIER_ACTION_RADIUS, MY_TEAM);
		for (int i = alliesAroundHostile.length; --i >= 0;) {
			if (alliesAroundHostile[i].type.canAttack()) {
				if (alliesAroundHostile[i].location.distanceSquaredTo(closestHostileLocation) <= alliesAroundHostile[i].type.actionRadiusSquared) {
					allyIsFighting = true;
					break;
				}
			}
		}
		if (allyIsFighting) 
			if (Movement.tryMoveInDirection(closestHostileLocation)) 
				return true;
		return false;
	}

    private static boolean tryMoveToAttackProductionUnit(RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		if (closestHostile.type.canAttack()) 
            return false;
	    if (Clock.getBytecodesLeft() > 3500 && BFS.move(closestHostile.location)) 
            return true;
        else if (Movement.tryMoveInDirection(closestHostile.location)) 
            return true;
		return false;
	}

    private static boolean tryMoveToEngageOutnumberedEnemy(RobotInfo[] visibleHostiles, RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		MapLocation closestHostileLocation = closestHostile.location;
        int numNearbyHostiles = 0;
		for (int i = visibleHostiles.length; --i >= 0;) {
			if (visibleHostiles[i].type.canAttack()) {
				if (visibleHostiles[i].location.distanceSquaredTo(closestHostileLocation) <= SOLDIER_ACTION_RADIUS) {
					numNearbyHostiles += 1;
				}
			}
		}
		
		int numNearbyAllies = 1; // Counts ourself
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(closestHostileLocation, SOLDIER_ACTION_RADIUS, MY_TEAM);
		for (int i = nearbyAllies.length; --i >= 0;) {
			if (nearbyAllies[i].type.canAttack() && nearbyAllies[i].health >= nearbyAllies[i].type.getMaxHealth(nearbyAllies[i].getLevel())/2.0) {
				numNearbyAllies += 1;
			}
		}
		
		if (numNearbyAllies > numNearbyHostiles || (numNearbyHostiles == 1 && rc.getHealth() > closestHostile.health)) {
			return Movement.tryMoveInDirection(closestHostile.location);
		}
		return false;
	}

    private static boolean retreatIfOutnumbered(RobotInfo[] visibleHostiles) throws GameActionException {
		RobotInfo closestHostileThatAttacksUs = null;
		int closestDistSq = Integer.MAX_VALUE;
		int numHostilesThatAttackUs = 0;
		for (int i = visibleHostiles.length; --i >= 0;) {
            RobotInfo hostile = visibleHostiles[i];
			if (hostile.type.canAttack()) {
				int distSq = hostile.location.distanceSquaredTo(rc.getLocation());
				if (distSq <= hostile.type.actionRadiusSquared) {
					if (distSq < closestDistSq) {
						closestDistSq = distSq;
						closestHostileThatAttacksUs = hostile;
					}
					numHostilesThatAttackUs += 1;
				}
			}
		}
		
		if (numHostilesThatAttackUs == 0) {
			return false;
		}
		
		int numAlliesAttackingClosestHostile = 0;
		if (rc.getLocation().distanceSquaredTo(closestHostileThatAttacksUs.location) <= SOLDIER_ACTION_RADIUS) {
			numAlliesAttackingClosestHostile += 1;
		}

        // TODO: Vision, action or 13?
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(closestHostileThatAttacksUs.location, SOLDIER_VISION_RADIUS, MY_TEAM);
		for (int i = nearbyAllies.length; --i >= 0;) {
            RobotInfo ally = nearbyAllies[i];
			if (ally.type.canAttack()) {
				if (ally.location.distanceSquaredTo(closestHostileThatAttacksUs.location)
						<= ally.type.actionRadiusSquared) {
					numAlliesAttackingClosestHostile += 1;
				}
			}
		}
		
		if (numAlliesAttackingClosestHostile > numHostilesThatAttackUs) {
			return false;
		} 
		if (numAlliesAttackingClosestHostile == numHostilesThatAttackUs) {
			if (numHostilesThatAttackUs == 1) {
				if (rc.getHealth() >= closestHostileThatAttacksUs.health) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		MapLocation retreatTarget = rc.getLocation();
		for (int i = visibleHostiles.length; --i >= 0;) {
            RobotInfo hostile = visibleHostiles[i];
			if (!hostile.type.canAttack()) continue;			
			retreatTarget = retreatTarget.add(hostile.location.directionTo(rc.getLocation()));
		}
		if (!rc.getLocation().equals(retreatTarget)) {
			Direction retreatDir = rc.getLocation().directionTo(retreatTarget);
			return tryHardMoveInDirection(retreatDir);
		}
		return false;
	}
    
    public static boolean tryHardMoveInDirection(Direction dir) throws GameActionException {
        // TODO: Pick minimum rubble one?
        int curRubble = rc.senseRubble(rc.getLocation());
		if (rc.canMove(dir) && rc.senseRubble(rc.getLocation().add(dir)) < curRubble) {
			rc.move(dir);
			return true;
		}
		Direction left = dir.rotateLeft();
		if (rc.canMove(left) && rc.senseRubble(rc.getLocation().add(left)) < curRubble) {
			rc.move(left);
			return true;
		}
		Direction right = dir.rotateRight();
		if (rc.canMove(right) && rc.senseRubble(rc.getLocation().add(right)) < curRubble) {
			rc.move(right);
			return true;
		}
		Direction leftLeft = left.rotateLeft();
		if (rc.canMove(leftLeft) && rc.senseRubble(rc.getLocation().add(leftLeft)) < curRubble) {
			rc.move(leftLeft);
			return true;
		}
		Direction rightRight = right.rotateRight();
		if (rc.canMove(rightRight) && rc.senseRubble(rc.getLocation().add(rightRight)) < curRubble) {
			rc.move(rightRight);
			return true;
		}
		return false;
	}

    private static boolean tryToMicro() throws GameActionException {
        if (visibleEnemies.length == 0) { // TODO: Either wait or get out of any possible Watchtower range. Skip if charging
            return false;
        }

        if (rc.isMovementReady()){
            if(retreatIfOutnumbered(visibleEnemies)) return true;
            // if(retreatFromEnemyWatchTowerRange()) return true;
        }

        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
                chooseTargetAndAttack(inRangeEnemies);
            }
            else if (rc.isMovementReady()){
                RobotInfo closestHostile = getClosestUnitWithCombatPriority(visibleEnemies);
                if(tryMoveToHelpAlly(closestHostile)) return true;
                if(tryMoveToAttackProductionUnit(closestHostile)) return true;
            }
        }
        if (rc.isMovementReady()){
            // Most important function
            if (inRangeEnemies.length > 0 && CombatUtil.tryToBackUpToMaintainMaxRangeSoldier(visibleEnemies)) return true; // Cant attack, try to move out
            RobotInfo closestHostile = getClosestUnitWithCombatPriority(visibleEnemies);
            if (!inHealingState && tryMoveToHelpAlly(closestHostile)) return true; // Maybe add how many turns of attack cooldown here and how much damage being taken?
            if (!inHealingState && tryMoveToEngageOutnumberedEnemy(visibleEnemies, closestHostile)) return true;
            if (!inHealingState && tryMoveToAttackProductionUnit(closestHostile)) return true;
        }
        return false;
    }

    public static boolean sendCombatLocation(RobotInfo[] visibleHostiles) throws GameActionException{
        if (visibleHostiles.length != 0 && Clock.getBytecodesLeft() > 600){
			RobotInfo closestHostile = getClosestUnitWithCombatPriority(visibleHostiles);
            if (closestHostile == null) return false;
			currentDestination = closestHostile.location;
            if (closestHostile != null)
				Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, closestHostile.getLocation(), Comms.SHAFlag.COMBAT_LOCATION);
            return true;
        }
        return false;
    }

    // public static boolean sendCombatLocation(RobotInfo[] visibleHostiles) throws GameActionException{
    //     if (visibleHostiles.length != 0 && Clock.getBytecodesLeft() > 600){
	// 		RobotInfo closestHostile = getClosestUnitWithCombatPriority(visibleHostiles), closestSoldier = getClosestMilitaryUnit(visibleHostiles);
	// 		if (closestSoldier != null)
    //         currentDestination = closestSoldier.location;
    //         else
    //         currentDestination = closestHostile.location;
    //         if (closestHostile != null){
	// 			if (isMilitaryUnit(closestHostile.type))
    //             Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, closestHostile.getLocation(), Comms.SHAFlag.COMBAT_LOCATION);
    //             else{
    //                 Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, closestHostile.getLocation(), Comms.SHAFlag.ENEMY_MINER_LOCATION);
    //                 if (closestSoldier != null)
    //                 Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, closestSoldier.getLocation(), Comms.SHAFlag.COMBAT_LOCATION);
    //             }
    //         }
    //         return true;
    //     }
    //     return false;
    // }

    // If our current destination has no enemies left, move to the nearest new location with combat
    public static boolean findNewCombatLocation() throws GameActionException{
        if (currentDestination == null || (visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= SOLDIER_VISION_RADIUS)){
            MapLocation combatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (combatLocation != null) currentDestination = combatLocation;
            // else combatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.ENEMY_MINER_LOCATION);
            // if (combatLocation != null) currentDestination = combatLocation;
            return true;
        }
        return false;
    }

    private static void simpleAttack() throws GameActionException{
        if (inRangeEnemies.length > 10 && rc.isActionReady()){
            chooseTargetAndAttack(inRangeEnemies);
        }
    }
    
    private static void manageHealingState() {
        if (rc.getHealth() < rc.getType().getMaxHealth(rc.getLevel()) / 3.0) {
            inHealingState = true;
        }
        else if (rc.getHealth() > 2.0/3.0 * rc.getType().getMaxHealth(rc.getLevel())) {
            inHealingState = false;
        }
    }

    private static boolean tryToHealAtArchon() throws GameActionException {
		if (!rc.isMovementReady()) return false;
		
		MapLocation closestArchon = getClosestArchonLocation();
		
		if (closestArchon == null || rc.getLocation().distanceSquaredTo(closestArchon) <= ARCHON_ACTION_RADIUS) 
            Movement.tryMoveInDirection(closestArchon);
		else
            BFS.move(closestArchon); // TODO: Avoid enemies
		return true;
	}

    // private static boolean checkIfEnemyArchonInVision() throws GameActionException{
    //     for (RobotInfo bot : visibleEnemies){
    //         if (bot.type == RobotType.ARCHON){
    //             Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, bot.getLocation(), Comms.SHAFlag.CONFIRMED_ENEMY_ARCHON_LOCATION);
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    /**
    * This will try to update the destination of the soldier so as to not make it go away from fights to a predetermined location.
    */
    private static void opportunisticCombatDestination() throws GameActionException{
        MapLocation nearestCombatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
        if (nearestCombatLocation != null){ 
            if (currentDestination == null) currentDestination = nearestCombatLocation;
            else if (!rc.canSenseLocation(currentDestination) && rc.getLocation().distanceSquaredTo(currentDestination) > rc.getLocation().distanceSquaredTo(nearestCombatLocation)){
                currentDestination = nearestCombatLocation;
            }
        }
    }

    /**
    * Run a single turn for a Soldier.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runSoldier(RobotController rc) throws GameActionException {
        soldierComms(); // 300 Bytecodes
        updateVision();
        opportunisticCombatDestination();

        // checkIfEnemyArchonInVision();
        // simpleAttack();
        manageHealingState();
        // detectIfAttackTargetIsGone();
        tryToMicro();
        updateVision();
        // TODO: Turret avoidance Comms code

        if (inHealingState && tryToHealAtArchon()){
            return;
        } 
        if (visibleEnemies.length == 0) {
            BFS.move(currentDestination); // 2700 Bytecodes
        }
        updateVision();
        if(sendCombatLocation(visibleEnemies)){    
        }
        else {
            findNewCombatLocation();
        }
        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
                chooseTargetAndAttack(inRangeEnemies);
            }
        }
    }
}
