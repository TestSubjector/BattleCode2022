package AFinalsBot;

import battlecode.common.*;

public class CombatUtil extends Util{

    public static boolean isMilitaryUnit(RobotType type){
        switch (type){
            case SOLDIER:
            case WATCHTOWER:
            case ARCHON:
            case SAGE:
                return true;
            default:
                return false;
        }
    }

	public static boolean isMaxHealth(RobotInfo bot){
		return (bot.getHealth() == bot.type.getMaxHealth(bot.level));
	}

	public static boolean isBotInjured(RobotInfo bot){
		double frac = 1.0d;
		return ((double)bot.health < frac * ((double)bot.type.getMaxHealth(bot.level)));
	}

    public static int militaryCount(RobotInfo[] visibleRobots){
        int count = 0;
        for (int i = visibleRobots.length; --i >= 0;) {
            if (isMilitaryUnit(visibleRobots[i].type)) count++;
        }
        return count;
    }

    public static int militaryAndArchonCount(RobotInfo[] visibleRobots){
        int count = 0;
        for (int i = visibleRobots.length; --i >= 0;) {
            if (isMilitaryUnit(visibleRobots[i].type) || visibleRobots[i].type == RobotType.ARCHON) count++;
        }
        return count;
    }

    public static RobotInfo getClosestUnitWithCombatPriority(RobotInfo[] units) {
        if (units.length == 0) return null;
		RobotInfo closestUnit = null;
        RobotInfo closestCombatUnit = null;
		int minDistSq = Integer.MAX_VALUE;
        int minCombatDistSq = Integer.MAX_VALUE;
        int distSq = 0;
        MapLocation lCR = rc.getLocation();
		for (int i = units.length; --i >= 0;) {
			distSq = lCR.distanceSquaredTo(units[i].location);
			if (distSq < minDistSq) {
				minDistSq = distSq;
				closestUnit = units[i];
			}
            if (isMilitaryUnit(units[i].getType()) && distSq < minCombatDistSq) {
				minCombatDistSq = distSq;
				closestCombatUnit = units[i];
			}
		}
        if (closestCombatUnit != null) return closestCombatUnit;
		else return closestUnit;
	}


    public static RobotInfo getClosestMilitaryUnit(RobotInfo[] units) {
        RobotInfo closestCombatUnit = null;
        int minCombatDistSq = Integer.MAX_VALUE;
        int distSq = 0;
        MapLocation lCR = rc.getLocation();
		for (int i = units.length; --i >= 0; ) {
            if (!isMilitaryUnit(units[i].getType())) continue;
			distSq = lCR.distanceSquaredTo(units[i].location);
            if (distSq < minCombatDistSq) {
				minCombatDistSq = distSq;
				closestCombatUnit = units[i];
			}
		}
        return closestCombatUnit;
	}

    public static boolean enemyArchonLocationGuessIsFalse(MapLocation enemyArchonGuess) throws GameActionException{
        for (int j = 0; j < archonCount; j++){
            if (archonLocations[j] != null && enemyArchonGuess.equals(archonLocations[j])) return true;
        }
        return false;
    }

    public static boolean enemyArchonLocationAreaIsFalse(MapLocation enemyArchonGuess) throws GameActionException{
        for (int j = 0; j < archonCount; j++){
            if (archonLocations[j] != null && enemyArchonGuess.distanceSquaredTo(archonLocations[j]) <= ARCHON_VISION_RADIUS) return true;
        }
        return false;
    }

    public static boolean tryToBackUpToMaintainMaxRangeMiner(RobotInfo[] visibleHostiles) throws GameActionException {
		int closestHostileDistSq = Integer.MAX_VALUE;
        MapLocation lCR = rc.getLocation();
        for (RobotInfo hostile : visibleHostiles) {
			if (!hostile.type.canAttack()) continue;
			int distSq = lCR.distanceSquaredTo(hostile.location);
			if (distSq < closestHostileDistSq) {
				closestHostileDistSq = distSq;
			}
		}
		
		Direction bestRetreatDir = null;
		int bestDistSq = closestHostileDistSq;
        int bestRubble = rc.senseRubble(rc.getLocation());

		for (Direction dir : directions) {
			if (!rc.canMove(dir)) continue;
			MapLocation dirLoc = lCR.add(dir);
            int dirLocRubble = rc.senseRubble(dirLoc);
            if (dirLocRubble > bestRubble) continue; // Don't move to even more rubble

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
                // bestRubble = dirLocRubble;
			}
		}
		if (bestRetreatDir != null) {
			rc.move(bestRetreatDir);
            currentLocation = rc.getLocation();
			if (UNIT_TYPE.equals(RobotType.MINER)){ 
                rc.setIndicatorString("Backing: " + bestRetreatDir);
				Explore.exploreDir = bestRetreatDir;
				Explore.assignExplore3Dir(exploreDir);
			}
			return true;
		}
		return false;
	}

    public static boolean tryToBackUpToMaintainMaxRangeSoldier(RobotInfo[] visibleHostiles) throws GameActionException {
		int closestHostileDistSq = Integer.MAX_VALUE;
        // int sageCount = 0;
        // int otherEnemies = 0;
        MapLocation lCR = rc.getLocation();
        for (RobotInfo hostile : visibleHostiles) {
			if (!hostile.type.canAttack() && hostile.type != RobotType.ARCHON) continue;
            // if (hostile.type == RobotType.SAGE) sageCount += 1;
            // else if (hostile.type == RobotType.ARCHON) otherEnemies += 0;
            // else otherEnemies += 1;
			int distSq = lCR.distanceSquaredTo(hostile.location);
			if (distSq < closestHostileDistSq) {
				closestHostileDistSq = distSq;
			}
		}
		
        // if (otherEnemies == 0 && sageCount > 0) return false;
		if (closestHostileDistSq > SOLDIER_ACTION_RADIUS) return false; // We dont want to get out of our max range
		
		Direction bestRetreatDir = null;
		int bestDistSq = closestHostileDistSq;
        int bestRubble = rc.senseRubble(rc.getLocation());

		for (Direction dir : directions) {
			if (!rc.canMove(dir)) continue;
			MapLocation dirLoc = lCR.add(dir);
            int dirLocRubble = rc.senseRubble(dirLoc);
            if (dirLocRubble > bestRubble) continue; // Don't move to even more rubble

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
                // bestRubble = dirLocRubble;
			}
		}
		if (bestRetreatDir != null) {
            rc.setIndicatorString("Backing: " + bestRetreatDir);
			rc.move(bestRetreatDir);
            currentLocation = rc.getLocation();
			return true;
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

}
