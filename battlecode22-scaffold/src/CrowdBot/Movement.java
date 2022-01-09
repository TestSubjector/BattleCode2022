package CrowdBot;

import battlecode.common.*;

public class Movement extends Util{
    public static boolean tryMoveInDirection(Direction dir) throws GameActionException {
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}
		Direction left = dir.rotateLeft();
		if (rc.canMove(left)) {
			rc.move(left);
			return true;
		}
		Direction right = dir.rotateRight();
		if (rc.canMove(right)) {
			rc.move(right);
			return true;
		}
		return false;
	}

    // Takes around 400 bytecodes to run 
    public static boolean goToDirect(MapLocation dest) throws GameActionException {
    	if (currentLocation.equals(dest)) return false;
        Direction forward = currentLocation.directionTo(dest);
    	if (currentLocation.isAdjacentTo(dest)) {
    		if (rc.canMove(forward)) {
    			rc.move(forward);
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
    		return true;
    	}
    	return false;
    }


	public static boolean moveToDest(MapLocation dest) throws GameActionException{
		if (usingOnlyBugNav){
			return goToDirect(dest);
		}
		else{
			Direction dir = PathFinder.findPathAStar(currentLocation, dest);
			if (rc.canMove(dir)){
				rc.move(dir);
				return true;
			}
			else return false;
		}
	}

	
    private static boolean preferLeft(MapLocation dest) {
        Direction toDest = currentLocation.directionTo(dest);
        MapLocation leftLoc = currentLocation.add(toDest.rotateLeft());
        MapLocation rightLoc = currentLocation.add(toDest.rotateRight());
        return (dest.distanceSquaredTo(leftLoc) < dest.distanceSquaredTo(rightLoc));
    }

    public static void moveRandomly() throws GameActionException {
        Direction dir = Globals.directions[Globals.rng.nextInt(Globals.directions.length)];
        if (rc.canMove(dir)) rc.move(dir);
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

            for (int i = droidVisionDirs.length; i-- > 0; ) {
                if (Clock.getBytecodesLeft() < 2000) return bestLoc;
                lCurrentLocation = lCurrentLocation.add(droidVisionDirs[i]);
                if ((lCurrentLocation.x + lCurrentLocation.y) % 2 != congruence) continue;
                if (!rc.onTheMap(lCurrentLocation)) continue;
                if (rc.isLocationOccupied(lCurrentLocation)) continue;

                int estimatedDistance = lCurrentLocation.distanceSquaredTo(lArchonLocation);

                if (estimatedDistance < minLatticeDist + weights) continue;

                if (bestLoc == null  || estimatedDistance < bestDist){
                    bestLoc = lCurrentLocation;
                    bestDist = estimatedDistance;
                    // byteCodeSaver++;
                }
                // if(byteCodeSaver == 5){
                //     return bestLoc;
                // }
            }
            if (bestLoc != null){
                return bestLoc;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}