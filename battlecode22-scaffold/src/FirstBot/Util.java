package FirstBot;

import battlecode.common.MapLocation;
// import utility.Globals;

public class Util extends Globals {
    //TODO:  Make separate versions of each method for different units to avoid if cases
    //TODO: All util functions should be try-catch when used by some other classes

    public static int intFromMapLocation(MapLocation loc){
        return (loc.x << 6) | loc.y; 
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

    // mask needs to be changed to reflect shift value
    // public static MapLocation mapLocationFromInt(int loc, int shift){
    //     return new MapLocation(loc >> shift, loc & mask);
    // }

    public static boolean isValidMapLocation(int x, int y){
        return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT;
    }

    public static boolean isValidMapLocation(MapLocation loc){
        return isValidMapLocation(loc.x, loc.y);
    }

}
