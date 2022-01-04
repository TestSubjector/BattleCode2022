package utility;

import battlecode.common.MapLocation;
import utility.Globals;

public class Util extends Globals {
    //TODO:  Make separate versions of each method for different units to avoid if cases
    //TODO: All util functions should be try-catch when used by some other classes

    public static int intFromMapLocation(MapLocation loc){
        return (loc.x << 6) | loc.y; 
    }

    public static MapLocation mapLocationFromInt(int loc){
        return new MapLocation(loc >> 6, loc & 0x3F);
    }

    public static int intFromMapLocation(MapLocation loc, int shift){
        return (loc.x << shift) | loc.y; 
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
