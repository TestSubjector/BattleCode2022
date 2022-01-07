package CommBot;

import battlecode.common.*;


// TODO: Add storage of type of robot count
public class Comms extends Util{

    public static final int CHANNEL_RUBBLE_START = 0;
    public static final int CHANNEL_RUBBLE_STOP = 32;
    public static final int CHANNEL_TRANSMITTER_COUNT = 32;
    public static final int CHANNEL_MINER_COUNT = 33;
    public static final int CHANNEL_SOLDIER_COUNT = 34;
    public static final int CHANNEL_BUILDER_COUNT = 35;
    public static final int CHANNEL_WATCHTOWER_COUNT = 36;
    // All Archons will have minimum 4 channels from 37-52 until they die
    public static final int CHANNEL_ARCHON_START = 37;
    public static final int channelArchonStop = 53; 
    
    /// Message Info
    /*
        We have three types of messages:
        1) First 32 indexes to fill rubbleMap
        2) (x,y) << 4 | (SHAFlag) messages that deal with different type of locations
        3) Non SHAFlag messages (NOT_A_LOCATION_MESSAGE) that with unit_counts, mapsymmetry etc 
    */
    enum SHAFlag { 
        ARCHON_LOCATION, // 0x0
        POTENTIAL_ENEMY_ARCHON_LOCATION, //0x1
        CONFIRMED_ENEMY_ARCHON_LOCATION, //0x2
        LEAD_LOCATION, //0x3
        REQUEST_FOR_LEAD_LOCATION, //0x4
        IDEAL_WATCHTOWER_BUILD_LOCATION, //0x5
        ENEMY_WATCHTOWER_BUILD_LOCATION,
        ENEMY_NEAR_ARCHON_LOCATION,
        NOT_A_LOCATION_MESSAGE,  //0xF
    }

    public static SHAFlag[] SHAFlags = SHAFlag.values();

    public static MapLocation readLocationFromMessage(int message){
        return mapLocationFromInt((message >> 4));
    }

    
    public static SHAFlag readSHAFlagFromMessage(int message){
        return SHAFlags[message & 0xF];
    }


    public static void writeSHAFlagMessage(MapLocation loc, SHAFlag flag, int channel) throws GameActionException{
        int value = intFromMapLocation(loc);
        rc.writeSharedArray(channel, (value << 4) | flag.ordinal());
    }

    // 0 <= type < 16
    public static void writeLocation(MapLocation loc, int type, int channel) throws GameActionException{
        int value = intFromMapLocation(loc);
        value = ((value << 4) | type);
        rc.writeSharedArray(channel, value);
    }


    public static void writeLocation(int x, int y, int type, int channel) throws GameActionException{
        int value = intFromMapLocation(x, y);
        value = ((value << 4) | type);
        rc.writeSharedArray(channel, value);
    }


    public static MapLocation readLocation(int channel) throws GameActionException{
        return mapLocationFromInt((rc.readSharedArray(channel) >> 4));
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
