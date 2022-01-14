package OTempBot;

import battlecode.common.MapLocation;
import battlecode.common.Clock;
import battlecode.common.RobotInfo;
// import utility.Globals;

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
        int minDistance = MAX_DISTANCE;
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

    public static MapLocation getClosestArchonLocation(boolean isArchon){
        int minDistance = MAX_DISTANCE;
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
        MapLocation lCR = currentLocation;
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

    // public static int isInQuadrant(int centerX, int centerY){
    //     int dx = max(abs(px - x) - width / 2, 0);
    //     int dy = max(abs(py - y) - height / 2, 0);
    //     return dx * dx + dy * dy; //Returns zero if inside Quad(Rectangle)
    // }

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

    // String functions

    // Return string of length 
}
