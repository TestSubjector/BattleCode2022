package AFinalsBot;

import battlecode.common.*;

public class Util extends Globals {
    //TODO: Make separate versions of each method for different units to avoid if cases
    //TODO: All util functions should be try-catch when used by some other classes

    public static int intFromMapLocation(MapLocation loc){
        return (loc.x << 6) | loc.y; 
    }

    public static int intFromMapLocation(int x, int y){
        return (x << 6) | y;
    }

    public static MapLocation mapLocationFromInt(int loc){
        return new MapLocation(loc >> 6, loc & 0x3F);
    }

    public static MapLocation mapLocationFromCoords(int x, int y){
        return new MapLocation(x, y);
    }

    public static int intFromMapLocation(MapLocation loc, int shift){
        return (loc.x << shift) | loc.y; 
    }


    public static int manhattanDistance(MapLocation first, MapLocation second){
        return (Math.abs(first.x - second.x) + Math.abs(first.y - second.y));
    }

    public static int findPointByRatio(MapLocation src, MapLocation dest, int dist){
        int tot = src.distanceSquaredTo(dest);
        int diff = tot - dist;
        int x1 = src.x;
        int y1 = src.y;
        int x2 = dest.x;
        int y2 = dest.y;
        int x = (int)(((float)(diff * x1 + dist * x2)) / ((float) tot));
        int y = (int)(((float)(diff * y1 + dist * y2)) / ((float) tot));
        return intFromMapLocation(x, y);
    }

    public static final int getTurnFlag(){
        return (turnCount % 2);
    }

    public static final int flipTurnFlag(){
        return ((turnCount + 1) % 2);
    }


    // Set kth bit of the input number to 1
    public static final int setKthBit(int num, int k){
        return ((1 << k) | num);
    }

    // Set kth bit of the input number to 0
    public static final int unsetKthBit(int num, int k){
        return (num & ~(1 << k));
    }

    public static final int setKthBitByInput(int num, int k, int val){
        if (val == 1)
            return setKthBit(num, k);
        return unsetKthBit(num, k);
    }

    public static boolean isOnEdge(MapLocation loc){
        return (loc.x == 0 || loc.x == MAP_WIDTH - 1 || loc.y == 0 || loc.y == MAP_HEIGHT - 1);
    }

    // Very expensive computations (around 15-30 bytecodes)
    public static boolean isValidMapLocation(int x, int y){
        MapLocation loc = new MapLocation(x,y);
        return isValidMapLocation(loc);
    }


    // Bytecode Cost: 20-25
    public static boolean isValidMapLocation(MapLocation loc){ 
        return loc.x < rc.getMapWidth() && loc.x >= 0 && loc.y < rc.getMapHeight() && loc.y >= 0;
    }


    // Bytecode Cost: 50-60
    public static MapLocation getClosestArchonLocation(){
        int minDistance = Integer.MAX_VALUE;
        int curDistance = 0;
        MapLocation closest = null;
        for(int i = 0; i < archonCount; i++){
            curDistance = rc.getLocation().distanceSquaredTo(archonLocations[i]);
            if (curDistance < minDistance){
                minDistance = curDistance;
                closest = archonLocations[i];
            }
        }
        return closest;
    }

    public static MapLocation getLowestHealingArchonLocation(){
        int minHealingUnits = Integer.MAX_VALUE;
        int unitsBeingHealedByArchon = 0;
        MapLocation closest = null;
        for(int i = 0; i < archonCount; i++){
            unitsBeingHealedByArchon = Comms.readHealingUnitsNearby(i);
            if (unitsBeingHealedByArchon < minHealingUnits){
                minHealingUnits = unitsBeingHealedByArchon;
                closest = archonLocations[i];
            }
            else if(unitsBeingHealedByArchon == minHealingUnits && 
            rc.getLocation().distanceSquaredTo(archonLocations[i]) < rc.getLocation().distanceSquaredTo(closest)){
                    closest = archonLocations[i];
            }
        }
        return closest;
    }

    public static boolean isOverCrowdedArchon(){
        int unitsBeingHealedByArchon = 0;
        MapLocation closest = archonLocations[0];
        for(int i = 0; i < archonCount; i++){
            if(closest!=null && rc.getLocation().distanceSquaredTo(archonLocations[i]) <= rc.getLocation().distanceSquaredTo(closest)){
                closest = archonLocations[i];
                unitsBeingHealedByArchon = Comms.readHealingUnitsNearby(i);
            }
        }
        return unitsBeingHealedByArchon > 5;
    }

    public static MapLocation getClosestArchonLocation(boolean isArchon){
        int minDistance = Integer.MAX_VALUE;
        int curDistance = 0;
        MapLocation closest = null;
        for(int i = 0; i < archonCount; i++){
            curDistance = rc.getLocation().distanceSquaredTo(archonLocations[i]);
            if(curDistance == 0 && isArchon) continue; // I don't want to flee to my own location
            if (curDistance < minDistance){
                minDistance = curDistance;
                closest = archonLocations[i];
            }
        }
        return closest;
    }

    public static RobotInfo getClosestUnit(RobotInfo[] units) {
		RobotInfo closestUnit = null;
		int minDistSq = Integer.MAX_VALUE;
        int distSq = 0;
        MapLocation lCR = rc.getLocation();
		for (int i = units.length; i --> 0; ) {
			distSq = lCR.distanceSquaredTo(units[i].location);
			if (distSq < minDistSq) {
				minDistSq = distSq;
				closestUnit = units[i];
			}
		}
		return closestUnit;
	}

    // Very important function
    public static boolean closerToCenter(MapLocation firstLoc, MapLocation secondLoc){
        return (firstLoc.distanceSquaredTo(CENTER_OF_THE_MAP) < secondLoc.distanceSquaredTo(CENTER_OF_THE_MAP));
    }

    public static Direction directionAwayFromAllRobots(){
        RobotInfo[] senseRobots = rc.senseNearbyRobots();
        MapLocation currentTarget = rc.getLocation();
        for (int i = senseRobots.length; --i >= 0;) {
            RobotInfo aRobot = senseRobots[i];			
        	currentTarget = currentTarget.add(aRobot.location.directionTo(rc.getLocation()));
        }
        if (!rc.getLocation().equals(currentTarget)) {
        	return rc.getLocation().directionTo(currentTarget);
        }
        return null;
    }

    public static Direction directionAwayFromGivenRobots(RobotInfo[] givenRobots){
        MapLocation currentTarget = rc.getLocation();
        for (int i = givenRobots.length; --i >= 0;) {
            RobotInfo aRobot = givenRobots[i];			
        	currentTarget = currentTarget.add(aRobot.location.directionTo(rc.getLocation()));
        }
        if (!rc.getLocation().equals(currentTarget)) {
        	return rc.getLocation().directionTo(currentTarget);
        }
        return null;
    }

    public static MapLocation getClosestNonLeadLocation(MapLocation givenLocation) throws GameActionException{
        MapLocation targetLocations[] = rc.getAllLocationsWithinRadiusSquared(givenLocation, 13);
        MapLocation selectedLocation = null;
        int distToLoc = Integer.MAX_VALUE;
        for (int i = targetLocations.length; i --> 0; ){
            if (rc.canSenseLocation(targetLocations[i]) && rc.senseLead(targetLocations[i]) == 0 && !rc.canSenseRobotAtLocation(targetLocations[i]) && 
                targetLocations[i].distanceSquaredTo(rc.getLocation()) < distToLoc){
                selectedLocation =  targetLocations[i];
                distToLoc = selectedLocation.distanceSquaredTo(rc.getLocation());
            }
        }
        return selectedLocation;
    }

    // public static int isInQuadrant(int centerX, int centerY){
    //     int dx = max(abs(px - x) - width / 2, 0);
    //     int dy = max(abs(py - y) - height / 2, 0);
    //     return dx * dx + dy * dy; //Returns zero if inside Quad(Rectangle)
    // }


    public static MapLocation findClosestCorner(MapLocation curLoc){
        int optDist = Integer.MAX_VALUE, dist;
        MapLocation loc = null;
        for (int i = -1; ++i < 4;){
            dist = MAP_CORNERS[i].distanceSquaredTo(curLoc);
            if (loc == null || dist < optDist){
                loc = MAP_CORNERS[i];
                optDist = dist;
            }
        }
        return loc;
    }


    public static void byteCodeTest(){
        int temp = 1;
        int index_i = 0, index_j =0;
        int holder_1[] = new int[10];
        int holder_2[][] = new int[10][10];
        StringBuilder sb = new StringBuilder("111111111111111111111111111111111111111111111111111111111111");
        StringBuilder s = sb.delete(20, 60); // 12 bytecodes irrespective of length of string
        System.out.println("Bytecodes left before testing area: " + Clock.getBytecodesLeft()); // 7 bytecodes
        // StringBuilder s = sb.delete(20, 60); // 12 bytecodes irrespective of length of string
        // s.setCharAt(1, '2'); // 10 bytecodes (2/3 in reality)
        // s.setCharAt(2, '2');
        // s.setCharAt(3, '2');
        // s.replace(1,3,"2");
        // int holder_1[]; // 7 bytecodes (actual is 0 or 1)
        // holder_1 = new int[10]; //19 bytecodes (actual is 12/13) 
        // temp = holder_1[index_i]; // 11 bytecodes (actual is 4/5)
        // holder_1[index_i] = temp; // 11 bytecodes (actual is 4/5)
        // temp = holder_2[index_i][index_j]; // 13 bytecodes (actual is 6/7)
        // holder_2[index_i][index_j] = temp; // 13 bytecodes (actual is 6/7)	
        System.out.println("Bytecodes left after testing area: " + Clock.getBytecodesLeft());
    }


    public static MapLocation translateByStepsCount(MapLocation src, MapLocation dest, int count) throws GameActionException{
        double angle = Math.atan2(dest.y-src.y, dest.x - src.x);
        double x = src.x, y = src.y;
        x += Math.cos(angle)*count;
        y += Math.sin(angle)*count;
        return new MapLocation((int)x, (int)y);
    }


    public static MapLocation translateByStepsCountUsingDir(MapLocation src, MapLocation dest, int count) throws GameActionException{
        Direction dir = src.directionTo(dest);
        MapLocation intermediateStep;
        for (int i = -1; ++i < count;){
            intermediateStep = src.add(dir);
            if (!isValidMapLocation(intermediateStep)) return src;
            src = intermediateStep;
        }
        return src;
    }


    // String functions

    // Return string of length 


}
