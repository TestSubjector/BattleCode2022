package OSprintAndAfterBot;

import battlecode.common.*;

public class Movement extends Util{
    public static boolean tryMoveInDirection(Direction dir) throws GameActionException {
		if (rc.canMove(dir)) {
			rc.move(dir);
            currentLocation = rc.getLocation();
			return true;
		}
		Direction left = dir.rotateLeft();
		if (rc.canMove(left)) {
			rc.move(left);
            currentLocation = rc.getLocation();
			return true;
		}
		Direction right = dir.rotateRight();
		if (rc.canMove(right)) {
			rc.move(right);
            currentLocation = rc.getLocation();
			return true;
		}
		return false;
	}

    // Takes around 400 bytecodes to run 
    public static boolean goToDirect(MapLocation dest) throws GameActionException {
        try{
            if(!rc.isMovementReady()) return false;
    	    if (currentLocation.equals(dest)) return false;
            Direction forward = currentLocation.directionTo(dest);
    	    if (currentLocation.isAdjacentTo(dest)) {
    	    	if (rc.canMove(forward)) {
    	    		rc.move(forward);
                    currentLocation = rc.getLocation();
    	    		return true;
    	    	}
    	    }	
            
    	    Direction[] dirs;
    	    if (preferLeft(dest)) {
    	    	dirs = new Direction[] { forward, forward.rotateLeft(), forward.rotateRight(),
    	    			forward.rotateLeft().rotateLeft(), forward.rotateRight().rotateRight()};			
    	    } else {
    	    	dirs = new Direction[] { forward, forward.rotateRight(), forward.rotateLeft(), 
    	    			forward.rotateRight().rotateRight(), forward.rotateLeft().rotateLeft()};
    	    }
        
    	    Direction bestDir = null;
    	    double bestRubble = MAX_RUBBLE+1;
    	    int currentDistSq = currentLocation.distanceSquaredTo(dest);
    	    for (Direction dir : dirs) {
    	    	MapLocation dirLoc = currentLocation.add(dir);
                if (!isValidMapLocation(dirLoc)) continue;
    	    	if (dirLoc.distanceSquaredTo(dest) > currentDistSq) continue;
    	    	double rubble = rc.senseRubble(dirLoc);
                if (rubble < bestRubble && rc.canMove(dir)) {
                    bestRubble = rubble;
                    bestDir = dir;
                }
    	    }
        
    	    if (bestDir != null) {
    	    	rc.move(bestDir);
                currentLocation = rc.getLocation();
    	    	return true;
    	    }
    	    return false;
        }
        catch (Exception e) {
            System.out.println("Exception in goToDirect: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


	public static boolean moveToDest(MapLocation dest) throws GameActionException{
		if (usingOnlyBugNav){
			return goToDirect(dest);
		}
		else{
			Direction dir = PathFinder.findPathAStar(currentLocation, dest);
			if (rc.canMove(dir)){
				rc.move(dir);
                currentLocation = rc.getLocation();
				return true;
			}
			else return false;
		}
	}

	
    public static boolean preferLeft(MapLocation dest) {
        Direction toDest = currentLocation.directionTo(dest);
        MapLocation leftLoc = currentLocation.add(toDest.rotateLeft());
        MapLocation rightLoc = currentLocation.add(toDest.rotateRight());
        return (dest.distanceSquaredTo(leftLoc) < dest.distanceSquaredTo(rightLoc));
    }

    public static void moveRandomly() throws GameActionException {
        Direction dir = Globals.directions[Globals.rng.nextInt(Globals.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            currentLocation = rc.getLocation();
        }
    }

    public static MapLocation moveToLattice(int minLatticeDist, int weights){
        try { 
            MapLocation lCurrentLocation = currentLocation;
            MapLocation lArchonLocation = Util.getClosestArchonLocation();
            MapLocation bestLoc = null;
            MapLocation myLoc = rc.getLocation();

            int bestDist = 0;
            // int byteCodeSaver=0;
            if (lArchonLocation == null) return null;
            int congruence = (lArchonLocation.x + lArchonLocation.y + 1) % 2;

            if ((myLoc.x + myLoc.y)%2 == congruence && myLoc.distanceSquaredTo(lArchonLocation) >= minLatticeDist + weights){
                bestDist = myLoc.distanceSquaredTo(lArchonLocation);
                bestLoc = myLoc;
                // return bestLoc;
            }

            for (int i = droidVisionDirs.length; i-- > 0; ) { //TODO: Add a bytecode check
                if (Clock.getBytecodesLeft() < 2000) return bestLoc;
                lCurrentLocation = lCurrentLocation.add(droidVisionDirs[i]);
                if ((lCurrentLocation.x + lCurrentLocation.y) % 2 != congruence) continue;
                if (!rc.onTheMap(lCurrentLocation)) continue;
                if (rc.isLocationOccupied(lCurrentLocation)) continue;
                if (UNIT_TYPE == RobotType.MINER && !BotMiner.commitSuicide && rc.senseLead(lCurrentLocation) == 0) continue;

                int estimatedDistance = lCurrentLocation.distanceSquaredTo(lArchonLocation);

                if (estimatedDistance < minLatticeDist + weights) continue;

                if (bestLoc == null  || estimatedDistance < bestDist){
                    bestLoc = lCurrentLocation;
                    bestDist = estimatedDistance;
                }
            }
            if (bestLoc != null){
                return bestLoc;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean retreatIfNecessary(RobotInfo[] visibleFriends, RobotInfo[] visibleHostiles) throws GameActionException {
		if (visibleHostiles.length == 0 //|| visibleFriends.length >= visibleHostiles.length
        ) return false;
		
		boolean mustRetreat = false;
		int bestClosestDistSq = Integer.MAX_VALUE;
        MapLocation lCR = currentLocation;
		for (RobotInfo hostile : visibleHostiles) {			
            //Sage killing one miner is worth for sage action cooldown
			if (hostile.type.canAttack() && hostile.type != RobotType.SAGE) {
				int distSq = lCR.distanceSquaredTo(hostile.location);
				if (distSq <= hostile.type.actionRadiusSquared) {
					mustRetreat = true;
					if (distSq < bestClosestDistSq) {
						bestClosestDistSq = distSq;
					}
				}
			}
		}
		if (!mustRetreat) return false;
		
		Direction bestDir = null;
		Direction[] dirs = Direction.values();
		for (int i = dirs.length; i--> 0;) {
			Direction dir = dirs[i];
			if (!rc.canMove(dir)) continue;
			MapLocation dirLoc = lCR.add(dir);
			int dirClosestDistSq = Integer.MAX_VALUE;
			for (RobotInfo hostile : visibleHostiles) {
				if (hostile.type.canAttack()) {
					int distSq = dirLoc.distanceSquaredTo(hostile.location);
					if (distSq < dirClosestDistSq) {
						dirClosestDistSq = distSq;						
						if (dirClosestDistSq <= bestClosestDistSq) break;
					}
				}
			}
			if (dirClosestDistSq > bestClosestDistSq) {
				bestClosestDistSq = dirClosestDistSq;
				bestDir = dir;
			}
		}
		
		if (bestDir != null) {
			rc.move(bestDir);
            currentLocation = rc.getLocation();
			return true;
		}
		return false;
	}
}
