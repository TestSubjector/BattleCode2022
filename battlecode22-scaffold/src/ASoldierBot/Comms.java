package ASoldierBot;

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
    // Archon Channels Format: 
    // Channel 1: Our Archon's Location
    // Channel 2: Enemy's Archon Location
    // Channel 3: Move away command from our archon to be written here when too crowded
    // Channel 4: ?????????????
    public static int channelArchonStop = CHANNEL_ARCHON_START + 16; // Can change depending on number of Archons alive, updated in BotComms()
    public static int commChannelStart = channelArchonStop; // Can change depending on number of Archons alive, updated in BotComms()
    public static int commChannelHead;
    public static final int COMM_CHANNEL_STOP = 64;
    
    /// Message Info
    /*
        We have three types of messages:
        1) First 32 indexes to fill rubbleMap
        2) (x,y) << 4 | (SHAFlag) messages that deal with different type of locations
        3) Non SHAFlag messages (NOT_A_LOCATION_MESSAGE) that with unit_counts, mapsymmetry etc 
    */


    // SHAFlags written in increasing order of priority. Unsure of NOT_A_LOCATION_MESSAGE placement in the priority order.
    enum SHAFlag { 
        EMPTY_MESSAGE, // 0x0
        NOT_A_LOCATION_MESSAGE, // 0x1
        REQUEST_FOR_LEAD_LOCATION, // 0x2
        LEAD_LOCATION, // 0x3
        IDEAL_WATCHTOWER_BUILD_LOCATION, // 0x4
        ENEMY_WATCHTOWER_BUILD_LOCATION, // 0x5
        POTENTIAL_ENEMY_ARCHON_LOCATION, // 0x6
        ENEMY_NEAR_ARCHON_LOCATION, // 0x7
        CONFIRMED_ENEMY_ARCHON_LOCATION, // 0x8
        ARCHON_LOCATION; // 0x9

        public boolean higherPriority(SHAFlag flag){
            return (this.ordinal() > flag.ordinal());
        }

        public boolean lesserPriority(SHAFlag flag){
            return (this.ordinal() < flag.ordinal());
        }
    }

    public static SHAFlag[] SHAFlags = SHAFlag.values();


    public static void initComms(){
        commChannelHead = commChannelStart;
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


    public static MapLocation checkIfCommMessagePresent(SHAFlag flag) throws GameActionException{
        for(int i = commChannelStart; i < COMM_CHANNEL_STOP; ++i){
            int message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == flag){
                return readLocationFromMessage(message);
            }
        }
        return null;
    }


    public static int getCommChannel() throws GameActionException{
        for (int i = commChannelStart; i < COMM_CHANNEL_STOP; ++i){
            if (readSHAFlagFromMessage(rc.readSharedArray(i)) == SHAFlag.EMPTY_MESSAGE)
                return i;
        }
        return -1;
    }


    public static int getCommChannelOfLesserPriority(SHAFlag flag) throws GameActionException{
        for (int i = commChannelStart; i < COMM_CHANNEL_STOP; ++i){
            if (readSHAFlagFromMessage(rc.readSharedArray(i)).lesserPriority(flag))
                return i;
        }
        return -1;
    }


    public static int getCommChannelFromHead() throws GameActionException{
        int failing_channel = -1;
        try{
            int start = commChannelHead;
            int message = rc.readSharedArray(commChannelHead);
            boolean wrapAround = false;
            while (readSHAFlagFromMessage(message) != SHAFlag.EMPTY_MESSAGE){
                if (commChannelHead == COMM_CHANNEL_STOP - 1)
                    commChannelHead = commChannelStart;
                else
                    commChannelHead++;
                if (commChannelHead == start){
                    wrapAround = true;
                    break;
                }
                failing_channel = commChannelHead;
                message = rc.readSharedArray(commChannelHead);
            }
            if (wrapAround) return -1;
            int channel = commChannelHead++;
            if (commChannelHead == COMM_CHANNEL_STOP)
                commChannelHead = commChannelStart;
            return channel;
        } catch (Exception e){
            System.out.println("Failing channel: " + failing_channel);
            System.out.println("commChannelStart: " + commChannelStart);
            e.printStackTrace();
        }
        return -1;
    }


    public static int getCommChannelOfLesserPriorityFromHead(SHAFlag flag) throws GameActionException{
        int start = commChannelHead;
        int message = rc.readSharedArray(commChannelHead);
        boolean wrapAround = false;
        while (readSHAFlagFromMessage(message).higherPriority(flag)){
            if (commChannelHead == COMM_CHANNEL_STOP - 1)
                commChannelHead = commChannelStart;
            else
                commChannelHead++;
            if (commChannelHead == start){
                wrapAround = true;
                break;
            }
            message = rc.readSharedArray(commChannelHead);
        }
        if (wrapAround) return -1;
        int channel = commChannelHead++;
        if (commChannelHead == COMM_CHANNEL_STOP)
            commChannelHead = commChannelStart;
        return channel;
    }


    // This consumes over 2100 bytecodes sometimes!!!!
    public static int writeCommMessage(int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannel();
        if (channel == -1) channel = getCommChannelOfLesserPriority(flag); // find a channel of lesser priority that can be overwritten
        if (channel == -1) return channel; // No available channels of lesser priority
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    public static int writeCommMessage(MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannel();
        if (channel == -1) channel = getCommChannelOfLesserPriority(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(loc, flag, channel);
        return channel;
    }


    // This still consumes ~1250 bytecodes at maximum!!! Trying out Comms channel head concept now.
    // Experiment Conclusion: Success!!! Look at the functions below
    public static int writeCommMessageOverrwriteLesserPriorityMessage(int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriority(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    public static int writeCommMessageOverrwriteLesserPriorityMessage(MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriority(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(loc, flag, channel);
        return channel;
    }


    // Takes ~1650 bytecodes at max.
    public static int writeCommMessageToHead(int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelFromHead();
        if (channel == -1) channel = getCommChannelOfLesserPriorityFromHead(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    public static int writeCommMessageToHead(MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelFromHead();
        if (channel == -1) channel = getCommChannelOfLesserPriorityFromHead(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(intFromMapLocation(loc), flag, channel);
        return channel;
    }


    // Takes ~200 bytecodes at max
    public static int writeCommMessageOverrwriteLesserPriorityMessageToHead(int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriorityFromHead(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    public static int writeCommMessageOverrwriteLesserPriorityMessageToHead(MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriorityFromHead(flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(intFromMapLocation(loc), flag, channel);
        return channel;
    }


    public static void wipeChannel(int channel) throws GameActionException{
        rc.writeSharedArray(channel, 0);
    }

    
    // Bytecode Cost: ~200
    public static void wipeChannelUpdateHead(int channel) throws GameActionException{
        if (commChannelHead == commChannelStart)
            commChannelHead = COMM_CHANNEL_STOP - 1;
        else
            commChannelHead--;
        rc.writeSharedArray(channel, rc.readSharedArray(commChannelHead));
        rc.writeSharedArray(commChannelHead, 0); //Important that this be here I think.
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
