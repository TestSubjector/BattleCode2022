package APreQualBot;

import battlecode.common.*;

public class BotSoldier extends CombatUtil{

    // Credits to @TheDuck314 for a major chunk of this code

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static boolean inHealingState;
    private static MapLocation finalDestination = null; 
    private static boolean standOff = false;
    private static MapLocation closestHealingArchon = null;

    public static void initBotSoldier() throws GameActionException{
        inHealingState = false;
        currentDestination = Comms.getClosestEnemyArchonLocation();
        if (currentDestination == null){
            for (int i = 0; i < 4; i++){
                if (rememberedEnemyArchonLocations[i] != null && CombatUtil.enemyArchonLocationAreaIsFalse(rememberedEnemyArchonLocations[i]))
                    rememberedEnemyArchonLocations[i] = null;
            }
            if (rememberedEnemyArchonLocations[2] != null)  
                currentDestination = rememberedEnemyArchonLocations[2];
            else if (rememberedEnemyArchonLocations[0] != null) 
                currentDestination = rememberedEnemyArchonLocations[0];
            else if (rememberedEnemyArchonLocations[1] != null) 
                currentDestination = rememberedEnemyArchonLocations[1];
            else{
                findNewCombatLocation();
                if (currentDestination == null) currentDestination = CENTER_OF_THE_MAP;
            }
        }
    }

    public static void updateSoldier(){
        standOff = false;
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

    private static double getEnemyScore(RobotInfo enemyUnit) throws GameActionException{
        RobotType enemyType = enemyUnit.type;
        int enemyHealth = enemyUnit.getHealth();
        if (enemyHealth <= 3) return 100000; // Instakill
        int rubbleAtLocation = rc.senseRubble(enemyUnit.getLocation());
		switch(enemyType) {
		case ARCHON:
			return 0.00001;
		case LABORATORY:
			return 1;
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
			if (Movement.tryMoveInDirection(closestHostileLocation)) {
				rc.setIndicatorString("Trying to help");
                return true;
            }
		return false;
	}

    private static boolean tryMoveToAttackProductionUnit(RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
		if (closestHostile.type.canAttack()) 
            return false;
	    if (BFS.move(closestHostile.location)){
            return true;
        } 
        else if (Movement.tryMoveInDirection(closestHostile.location)) {
            rc.setIndicatorString("Trying to attack production unit");
            return true;
        }
		return false;
	}

    private static boolean tryMoveToEngageOutnumberedEnemy(RobotInfo[] visibleHostiles, RobotInfo closestHostile) throws GameActionException {
        if(closestHostile == null) return false;
        MapLocation closestHostileLocation = closestHostile.location;
        int numNearbyHostiles = 0;
		for (int i = visibleHostiles.length; --i >= 0;) {
			if (visibleHostiles[i].type.canAttack()) {
				// if (!SMALL_MAP && visibleHostiles[i].location.distanceSquaredTo(closestHostileLocation) <= SOLDIER_ACTION_RADIUS) {
					numNearbyHostiles += 1;
				// }
			}
		}
		RobotInfo[] visibleAllies = rc.senseNearbyRobots(SOLDIER_VISION_RADIUS, MY_TEAM);
		int numNearbyAllies = 1; // Counts ourself
		for (int i = visibleAllies.length; --i >= 0;) {
			if (visibleAllies[i].type.canAttack() && visibleAllies[i].health >= visibleAllies[i].type.getMaxHealth(visibleAllies[i].getLevel())/2.0) {
				numNearbyAllies += 1;
			}
		}
		
		if (numNearbyAllies >= numNearbyHostiles || (numNearbyHostiles == 1 && rc.getHealth() > closestHostile.health)) {
			if (Movement.tryMoveInDirection(closestHostile.location))
                return true;
            else if(numNearbyAllies >= 1.5 * numNearbyHostiles && Movement.tryForcedMoveInDirection(closestHostile.location)){
                return true;
            }
            else {
                standOff = true;
                return false;
            }
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
			return CombatUtil.tryHardMoveInDirection(retreatDir);
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
            else if (rc.isMovementReady() && visibleEnemies.length > 0) {
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
			if (!standOff) currentDestination = closestHostile.location;
            if (closestHostile != null)
                if (!standOff)
				    Comms.writeCommMessageOverrwriteLesserPriorityMessageUsingQueue(Comms.commType.COMBAT, closestHostile.getLocation(), Comms.SHAFlag.COMBAT_LOCATION);
            return true;
        }
        return false;
    }

    // If our current destination has no enemies left, move to the nearest new location with combat
    private static boolean findNewCombatLocation() throws GameActionException{
        if (currentDestination == null || (visibleEnemies.length == 0 && rc.getLocation().distanceSquaredTo(currentDestination) <= SOLDIER_VISION_RADIUS)){
            MapLocation combatLocation = Comms.findNearestLocationOfThisTypeOutOfVision(rc.getLocation(), Comms.commType.COMBAT, Comms.SHAFlag.COMBAT_LOCATION);
            if (combatLocation != null) currentDestination = combatLocation;
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
        if (rc.getHealth() < rc.getType().getMaxHealth(rc.getLevel()) * 3.0/10.0) {
            inHealingState = true;
        }
        else if (rc.getHealth() > 3.0/4.0 * rc.getType().getMaxHealth(rc.getLevel())) {
            inHealingState = false;
        }
    }

    private static boolean tryToHealAtArchon() throws GameActionException {
		if (!rc.isMovementReady()) return false;
		
		closestHealingArchon = getLowestHealingArchonLocation();
		
		if (closestHealingArchon == null)
            return false;
            else if (rc.getLocation().distanceSquaredTo(closestHealingArchon) <= ARCHON_ACTION_RADIUS) {
                if (rc.getHealth() <= 5 && visibleEnemies.length == 0 && isOverCrowdedArchon()) {
                    rc.disintegrate();
                }
                Movement.tryMoveInDirection(closestHealingArchon);
            }
        else if (finalDestination == null)
            BFS.move(closestHealingArchon);
		return true;
	}

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
        updateSoldier();
        opportunisticCombatDestination();

        // checkIfEnemyArchonInVision();
        // simpleAttack();
        manageHealingState();
        // detectIfAttackTargetIsGone();
        tryToMicro();
        updateVision();
        // TODO: Turret avoidance Comms code

        if (inHealingState && tryToHealAtArchon()){
            updateVision();
        } 
        if(sendCombatLocation(visibleEnemies));
        else {
            findNewCombatLocation();
        }
        if (visibleEnemies.length == 0 && rc.isMovementReady()) {
            BFS.move(currentDestination); // 2700 Bytecodes
            updateVision();
        }
        if (rc.isActionReady()){
            if (inRangeEnemies.length > 0) {
                chooseTargetAndAttack(inRangeEnemies);
            }
        }
        BotMiner.surveyForOpenMiningLocationsNearby();
    }
}
