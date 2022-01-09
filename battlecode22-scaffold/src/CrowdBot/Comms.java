package CrowdBot;

import battlecode.common.*;

// TODO: Add storage of type of robot count
public class Comms extends Util{

    // Fuck Rubble Transmission
    public static final int CHANNEL_RUBBLE_START = 0;
    public static final int CHANNEL_RUBBLE_STOP = 0;
    public static final int CHANNEL_TRANSMITTER_COUNT = CHANNEL_RUBBLE_STOP;
    public static final int CHANNEL_MINER_COUNT = CHANNEL_TRANSMITTER_COUNT + 1;
    public static final int CHANNEL_SOLDIER_COUNT = CHANNEL_MINER_COUNT + 1;
    public static final int CHANNEL_BUILDER_COUNT = CHANNEL_SOLDIER_COUNT + 1;
    public static final int CHANNEL_WATCHTOWER_COUNT = CHANNEL_BUILDER_COUNT + 1;
    public static final int CHANNEL_LABORATORY_COUNT = CHANNEL_WATCHTOWER_COUNT + 1;
    public static final int CHANNEL_SAGE_COUNT = CHANNEL_LABORATORY_COUNT + 1;
    // All Archons will have minimum 4 channels from 37-52 until they die
    public static final int CHANNEL_ARCHON_START = CHANNEL_SAGE_COUNT + 1;
    public static int channelArchonStop = CHANNEL_ARCHON_START + 16; // Can change depending on number of Archons alive, updated in BotComms()
    public static int commChannelStart = channelArchonStop; // Can change depending on number of Archons alive, updated in BotComms()
    // public static int commChannelHead;
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
        LEAD_LOCATION, //0x4
        REQUEST_FOR_LEAD_LOCATION, //0x5
        IDEAL_WATCHTOWER_BUILD_LOCATION, //0x6
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


    // Mostly for non-location type messages
    public static void writeSHAFlagMessage(int message, SHAFlag flag, int channel) throws GameActionException{
        rc.writeSharedArray(channel, (message << 4) | flag.ordinal());
    }


    public static MapLocation checkIfCommMessagePresent(SHAFlag flag) throws GameActionException{
        for(int i = commChannelStart; i < COMM_CHANNEL_STOP; ++i){
            int message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == flag){
                return readLocationFromMessage(message);
            }
        }
        return null;
    }


    public static int getCommChannel(){
        try{
            for (int i = commChannelStart; i < COMM_CHANNEL_STOP; ++i){
                if (readSHAFlagFromMessage(rc.readSharedArray(i)) == SHAFlag.EMPTY_MESSAGE)
                    return i;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }


    public static int writeCommMessage(int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannel();
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    public static void wipeChannel(int channel) throws GameActionException{
        rc.writeSharedArray(channel, 0);
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


    public static void updateArchonLocations() throws GameActionException{
        int count = 0;
        for(int i = CHANNEL_ARCHON_START; i < channelArchonStop; i+=4){
            int message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == SHAFlag.ARCHON_LOCATION){
                archonLocations[count] = readLocationFromMessage(message);
                count++;
            }
        }
    }
}
