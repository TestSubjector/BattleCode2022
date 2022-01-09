package OFourthBot;

import battlecode.common.*;


// TODO: Add storage of type of robot count
public class Comms extends Util{

    public static final int CHANNEL_RUBBLE_START = 0;
    public static final int CHANNEL_RUBBLE_STOP = 32;
    public static final int CHANNEL_RUBBLE_TRANSMITTER_COUNT = 32;
    
    
    // 0 <= type < 16
    public static void writeLocation(MapLocation loc, int type, int index) throws GameActionException{
        int value = intFromMapLocation(loc);
        value = ((value << 4) | type);
        rc.writeSharedArray(index, value);
    }


    public static void writeLocation(int x, int y, int type, int index) throws GameActionException{
        int value = intFromMapLocation(x, y);
        value = ((value << 4) | type);
        rc.writeSharedArray(index, value);
    }


    public static MapLocation readLocation(int index) throws GameActionException{
        return mapLocationFromInt((rc.readSharedArray(index) >> 4));
    }


    public static MapLocation readLocationFromMessage(int message){
        return mapLocationFromInt((message >> 4));
    }


    public static int createRubbleMessage(int locationValue, int rubbleValue, int turnFlag){
        return ( (turnFlag << 15) | locationValue << 3 | rubbleValue);
    }

    
    public static int getRubbleValueFromRubbleMessage(int num){
        return (num & 0x0007);
    }

    
    public static MapLocation getLocFromRubbleMessage(int loc) throws GameActionException{
        return mapLocationFromInt((loc & (0x7FF8))>>>3);
    }
}
