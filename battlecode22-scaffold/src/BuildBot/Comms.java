package BuildBot;

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
    public static final int COMM_CHANNEL_START = channelArchonStop;
    public static int COMM_CHANNEL_HEAD;
    public static final int COMM_CHANNEL_STOP = 64;
    
    /// Message Info
    /*
        We have three types of messages:
        1) First 32 indexes to fill rubbleMap
        2) (x,y) << 4 | (SHAFlag) messages that deal with different type of locations
        3) Non SHAFlag messages (NOT_A_LOCATION_MESSAGE) that with unit_counts, mapsymmetry etc 
    */
    enum SHAFlag { 
        EMPTY_MESSAGE,
        ARCHON_LOCATION, // 0x1
        POTENTIAL_ENEMY_ARCHON_LOCATION, //0x2
        CONFIRMED_ENEMY_ARCHON_LOCATION, //0x3
        VIABLE_LEAD_LOCATION, //0x4
        REQUEST_FOR_LEAD_LOCATION, //0x5
        TAKEN_LEAD_LOCATION, //0x6 // Note that this is only set by the miners who are travelling to their miningLocation
        IDEAL_WATCHTOWER_BUILD_LOCATION, //0x7
        ENEMY_WATCHTOWER_BUILD_LOCATION,
        ENEMY_NEAR_ARCHON_LOCATION,
        NOT_A_LOCATION_MESSAGE,  //0xF
    }

    public static SHAFlag[] SHAFlags = SHAFlag.values();


    public static void initComms(){
        COMM_CHANNEL_HEAD = channelArchonStop;
    }


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


    // Mostly for non-location type messages
    public static void writeSHAFlagMessage(int message, SHAFlag flag, int channel) throws GameActionException{
        rc.writeSharedArray(channel, (message << 4) | flag.ordinal());
    }


    public static boolean checkIfLeadLocationTaken(MapLocation leadLocation) throws GameActionException{
        for(int i = COMM_CHANNEL_START; i < COMM_CHANNEL_STOP; ++i){
            int message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == SHAFlag.TAKEN_LEAD_LOCATION && leadLocation.equals(readLocationFromMessage(message))){
                return true;
            }
        }
        return false;
    }


    public static int getCommChannel(){
        int channel = COMM_CHANNEL_HEAD++;
        if (COMM_CHANNEL_HEAD == COMM_CHANNEL_STOP)
            COMM_CHANNEL_HEAD = COMM_CHANNEL_START;
        return channel;
    }


    public static int writeCommMessage(int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannel();
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    public static void wipeChannel(int channel) throws GameActionException{
        rc.writeSharedArray(channel, 0);
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

    public static void updateChannelValueBy1(int channel) throws GameActionException{
        rc.writeSharedArray(channel, rc.readSharedArray(channel) + 1);
    }
}
