package AFinalSprintBot;

import battlecode.common.*;

// TODO: Add storage of type of robot count
/** <pre> 
 * 'Using the Communcation System:
 * Parameter Description is given in the javadocs of the relevant functions.
 * 
 * Writing into the comms channels:
 *  Use one of the four following write functions to write a message into the comms channels:'
 *      1. writeCommMessage(commType type, int message/MapLocation loc, SHAFlag flag):
 *          'Use this if you do not want to use the channel queue to read and write into the communications channels.
 *          Recommendation (upon BytecodeCost): Do not use. Use writeCommMessageOverrwriteLesserPriorityMessage if channel queue must be avoided.'
 *      2. writeCommMessageOverrwriteLesserPriorityMessage(commType type, int message/MapLocation loc, SHAFlag flag):
 *          'Same as writeCommMessage but writes into the first channel found with lesser priority.
 *          Recommendation (upon BytecodeCost): Should be used if total comms channels number is low (like in LEAD channels).'
 *      3. writeCommMessageToHead(commType type, int message/MapLocation loc, SHAFlag flag):
 *          'Use this if you wish to use channel queue to read and write into the comms channels.
 *          Recommendation (upon BytecodeCost): Do not use. Use writeCommMessageOverrwriteLesserPriorityMessageToHead instead.'
 *      4. writeCommMessageOverrwriteLesserPriorityMessageToHead(commType type, int message/MapLocation loc, SHAFlag flag):
 *          'Same as writeCommMessageToHead but writes into the first channel it finds (using the queue head pointer) with lesser priority.
 *          Recommendation (upon BytecodeCost): Should be used if total comms channels number is high (like in COMBAT channels).'
 * 
 * 'Reading from the channels:
 *  Use of one the following six read functions to find a relevant message from the communication channels:'
 *      1. findLocationOfThisType(commType type, SHAFlag flag):
 *          'Searches the comms channels for a location that has the given SHAFlag. Returns first one it encounters'
 *      2. findNearestLocationOfThisType(MapLocation loc, commType type, SHAFlag flag):
 *          'Searches for a location with the given SHAFlag in the comms channels that is nearest to the given MapLocation loc.'
 *      3. findMessageOfThisType(commType type, SHAFlag flag):
 *          'Identical to' findLocationOfThisType. 'Just returns the int message (without SHAFlag; message>>4) instead of MapLocation'.
 *      4. findLocationOfThisTypeUsingChannelQueue(commType type, SHAFlag flag):
 *          'Identical to' findLocationOfThisType 'except for iterating order. Written to optimize average bytecode consumption. Needs testing if it succeeds at that.'
 *      5. findNearestLocationOfThisTypeUsingChannelQueue(MapLocation loc, commType type, SHAFLag flag):
 *          'Function name is self-explanatory.'
 *      6. findMessageOfThisTypeUsingChannelQueue(commType type, SHAFlag flag):
 *          'Function name is self-explanatory.' 
 * </pre>
 */
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
    // All Archons will have minimum 4 channels until they die
    public static final int CHANNEL_ARCHON_START = CHANNEL_SAGE_COUNT + 1;
    
    /// Archon Channels Format:
    /* 
        Channel 1: Our Archon's Location
        Channel 2: Enemy's Archon Location
        Channel 3: Move away command from our archon to be written here when too crowded
        Channel 4: Count of units build by this Archon
    */
    
    public static int channelArchonStop = CHANNEL_ARCHON_START + 16; // Can change depending on number of Archons alive, updated in BotComms()
    
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
        TAKEN_LEAD_LOCATION, // 0x2
        REQUEST_FOR_LEAD_LOCATION, // 0x3
        LEAD_LOCATION, // 0x4
        IDEAL_WATCHTOWER_BUILD_LOCATION, // 0x5
        ENEMY_WATCHTOWER_BUILD_LOCATION, // 0x6
        COMBAT_LOCATION, // 0x7
        POTENTIAL_ENEMY_ARCHON_LOCATION, // 0x8
        ENEMY_NEAR_ARCHON_LOCATION, // 0x9
        CONFIRMED_ENEMY_ARCHON_LOCATION, // 0xA
        ARCHON_LOCATION; // 0xB

        public boolean higherPriority(SHAFlag flag){
            return (this.ordinal() > flag.ordinal());
        }

        public boolean lesserPriority(SHAFlag flag){
            return (this.ordinal() < flag.ordinal());
        }
    }


    public static enum commType{
        LEAD, // 0x0
        COMBAT; // 0x1

        public int commChannelStart;
        public int commChannelHead;
        public int commChannelStop;
        public int commChannelHeadChannel;
    }


    public static SHAFlag[] SHAFlags = SHAFlag.values();


    public static void initComms(){
        channelArchonStop = CHANNEL_ARCHON_START + 4 * archonCount;
        commType.LEAD.commChannelStart = channelArchonStop;
        commType.LEAD.commChannelHead = commType.LEAD.commChannelStart;
        commType.LEAD.commChannelStop = (64 + 3 * commType.LEAD.commChannelStart) / 4;
        commType.LEAD.commChannelHeadChannel = commType.LEAD.commChannelStop;
        commType.COMBAT.commChannelStart = commType.LEAD.commChannelHeadChannel + 1;
        commType.COMBAT.commChannelHead = commType.COMBAT.commChannelStart;
        commType.COMBAT.commChannelStop = 63;
        commType.COMBAT.commChannelHeadChannel = commType.COMBAT.commChannelStop;
    }


    public static void updateComms() throws GameActionException{
        channelArchonStop = CHANNEL_ARCHON_START + 4 * archonCount;
        // commLeadChannelStart = channelArchonStop;
        commType.LEAD.commChannelStart = channelArchonStop;
        // commLeadChannelStop = (64 + 3 * commLeadChannelStart) / 4 ;
        commType.LEAD.commChannelStop = (64 + 3 * commType.LEAD.commChannelStart) / 4;
        rc.writeSharedArray(commType.LEAD.commChannelStop, rc.readSharedArray(commType.LEAD.commChannelHeadChannel));
        commType.LEAD.commChannelHeadChannel = commType.LEAD.commChannelStop;
        // commCombatChannelStart = commLeadChannelStop;
        commType.COMBAT.commChannelStart = commType.LEAD.commChannelStop;
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


    /**
     * Finds and returns the location from the first message found in the communication channels that has the given SHAFlag.
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return the first MapLocation found of the correct SHAFlag type or null if none found.
     * @throws GameActionException
     */
    public static MapLocation findLocationOfThisType(commType type, SHAFlag flag) throws GameActionException{
        for (int i = type.commChannelStart; i < type.commChannelStop; ++i){
            int message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == flag){
                return readLocationFromMessage(message);
            }
        }
        return null;
    }


    /**
     * Finds and returns the nearest location (to the reference location) that has the given SHAFlag from the communication channels.
     * @param loc  : The reference location.
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return the first MapLocation found of the correct SHAFlag type or null if none found.
     * @throws GameActionException
     */
    public static MapLocation findNearestLocationOfThisType(MapLocation loc, commType type, SHAFlag flag) throws GameActionException{
        MapLocation nearestLoc = null;
        for (int i = type.commChannelStart; i < type.commChannelStop; ++i){
            int message = rc.readSharedArray(i);
            MapLocation newLoc = readLocationFromMessage(message);
            if (nearestLoc == null || loc.distanceSquaredTo(nearestLoc) > loc.distanceSquaredTo(newLoc)) nearestLoc = newLoc;
        }
        return nearestLoc;
    }


    /**
     * Finds and returns the nearest location (to the reference location) that has the given SHAFlag from the communication channels and wipes the channel after reading.
     * @param loc  : The reference location.
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return the first MapLocation found of the correct SHAFlag type or null if none found.
     * @throws GameActionException
     */
    public static MapLocation findNearestLocationOfThisTypeAndWipeChannel(MapLocation loc, commType type, SHAFlag flag) throws GameActionException{
        MapLocation nearestLoc = null;
        int channel = -1;
        for (int i = type.commChannelStart; i < type.commChannelStop; ++i){
            int message = rc.readSharedArray(i);
            MapLocation newLoc = readLocationFromMessage(message);
            if (nearestLoc == null || loc.distanceSquaredTo(nearestLoc) > loc.distanceSquaredTo(newLoc)){ 
                nearestLoc = newLoc;
                channel = i;
            }
        }
        if (channel != -1)
        wipeChannel(channel);
        return nearestLoc;
    }


    /**
     * Finds and returns the first message found in the communication channels that has the given SHAFlag.
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return the first message (with the SHAFlag removed from it; message >> 4) found of the correct SHAFlag type or -1 if none found.
     * @throws GameActionException
     */
    public static int findMessageOfThisType(commType type, SHAFlag flag) throws GameActionException{
        for (int i = type.commChannelStart; i < type.commChannelStop; ++i){
            int message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == flag){
                return (message >> 4);
            }
        }
        return -1;
    }


    /**
     * Using the head pointer of channel queue, finds and returns the first location from the communication channels that has the given SHAFlag. Written to somehow improve average bytecode consumption. Needs testing
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return MapLocation if found or null if not.
     * @throws GameActionException
     */
    public static MapLocation findLocationOfThisTypeUsingChannelQueue(commType type, SHAFlag flag) throws GameActionException{
        type.commChannelHead = rc.readSharedArray(type.commChannelHeadChannel);
        int i;
        if (type.commChannelHead == type.commChannelStart)
            i = type.commChannelStop - 1;
        else i = type.commChannelHead - 1;
        int message = rc.readSharedArray(type.commChannelHead);
        if (readSHAFlagFromMessage(message) == flag) return readLocationFromMessage(message);

        while(i != type.commChannelHead){
            message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == flag)
                return readLocationFromMessage(message);
            if (i == type.commChannelStart)
                i = type.commChannelStop;
            else i--;
        }
        return null;
    }


    /**
     * Using the head pointer of channel queue, finds and returns the nearest location (to the reference location) that has the given SHAFlag from the comm channels. Written to somehow improve average bytecode consumption. Needs testing
     * @param loc  : The reference location.
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return MapLocation if found or null if not.
     * @throws GameActionException
     */
    public static MapLocation findNearestLocationOfThisTypeUsingChannelQueue(MapLocation loc, commType type, SHAFlag flag) throws GameActionException{
        type.commChannelHead = rc.readSharedArray(type.commChannelHeadChannel);
        int i;
        if (type.commChannelHead == type.commChannelStart)
            i = type.commChannelStop - 1;
        else i = type.commChannelHead - 1;
        MapLocation nearestLoc;
        int message = rc.readSharedArray(type.commChannelHead);
        if (readSHAFlagFromMessage(message) == flag) nearestLoc = readLocationFromMessage(message);
        else nearestLoc = null;

        while(i != type.commChannelHead){
            message = rc.readSharedArray(i);
            MapLocation newLoc = readLocationFromMessage(message);
            if (readSHAFlagFromMessage(message) == flag){
                if (nearestLoc == null || loc.distanceSquaredTo(nearestLoc) > loc.distanceSquaredTo(newLoc)) nearestLoc = newLoc;
            }
            if (i == type.commChannelStart)
                i = type.commChannelStop;
            else i--;
        }
        return nearestLoc;
    }


    /**
     * Using the head pointer of channel queue, finds and returns the first message from the communication channels that has the given SHAFlag. Written to somehow improve average bytecode consumption. Needs testing
     * @param type : Search for the message in this type's channels (LEAD or COMBAT);
     * @param flag : the SHAFlag that is being searched for in the comms channels.
     * @return the first message (with the SHAFlag removed from it; message >> 4) found of the correct SHAFlag type or -1 if none found.
     * @throws GameActionException
     */
    public static int findMessageOfThisTypeUsingChannelQueue(commType type, SHAFlag flag) throws GameActionException{
        type.commChannelHead = rc.readSharedArray(type.commChannelHeadChannel);
        int i;
        if (type.commChannelHead == type.commChannelStart)
            i = type.commChannelStop - 1;
        else i = type.commChannelHead - 1;
        int message = rc.readSharedArray(type.commChannelHead);
        if (readSHAFlagFromMessage(message) == flag) return (message >> 4);

        while(i != type.commChannelHead){
            message = rc.readSharedArray(i);
            if (readSHAFlagFromMessage(message) == flag)
                return (message >> 4);
            if (i == type.commChannelStart)
                i = type.commChannelStop;
            else i--;
        }
        return -1;
    }


    public static int getCommChannel(commType type) throws GameActionException{
        for (int i = type.commChannelStart; i < type.commChannelStop; ++i){
            if (rc.readSharedArray(i) == 0)
                return i;
        }
        return -1;
    }


    public static int getCommChannelOfLesserPriority(commType type, SHAFlag flag) throws GameActionException{
        for (int i = type.commChannelStart; i < type.commChannelStop; ++i){
            if (readSHAFlagFromMessage(rc.readSharedArray(i)).lesserPriority(flag))
                return i;
        }
        return -1;
    }


    public static int getCommChannelFromHead(commType type) throws GameActionException{
        type.commChannelHead = rc.readSharedArray(type.commChannelHeadChannel);
        int start = type.commChannelHead;
        int message = rc.readSharedArray(type.commChannelHead);
        boolean wrapAround = false;
        while (message != 0){
            if (type.commChannelHead == type.commChannelStop - 1)
                type.commChannelHead = type.commChannelStart;
            else
                type.commChannelHead++;
            if (type.commChannelHead == start){
                wrapAround = true;
                break;
            }
            message = rc.readSharedArray(type.commChannelHead);
        }
        if (wrapAround) return -1;
        int channel = type.commChannelHead++;
        if (type.commChannelHead == type.commChannelStop)
            type.commChannelHead = type.commChannelStart;
        rc.writeSharedArray(type.commChannelHeadChannel, type.commChannelHead);
        return channel;
    }


    public static int getCommChannelOfLesserPriorityFromHead(commType type, SHAFlag flag) throws GameActionException{
        type.commChannelHead = rc.readSharedArray(type.commChannelHeadChannel);
        int start = type.commChannelHead;
        int message = rc.readSharedArray(type.commChannelHead);
        boolean wrapAround = false;
        while (readSHAFlagFromMessage(message).higherPriority(flag)){
            if (type.commChannelHead == type.commChannelStop - 1) 
                type.commChannelHead = type.commChannelStart;
            else type.commChannelHead++;

            if (type.commChannelHead == start){
                wrapAround = true;
                break;
            }
            message = rc.readSharedArray(type.commChannelHead);
        }
        
        if (wrapAround) return -1;
        
        int channel = type.commChannelHead++;

        if (type.commChannelHead == type.commChannelStop) 
            type.commChannelHead = type.commChannelStart;
        
        rc.writeSharedArray(type.commChannelHeadChannel, type.commChannelHead);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). This searches for an empty channel in which it can write the message. If it can't find any empty channel, it then searches for a channel with a lesser priority than the current message (which it gets from the SHAFlag) to write the message.
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param message : the int message that you want to write to the channel. In most cases it's going to be int form of a MapLocation. There's another overloaded function with the same name that accepts MapLocation as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~600 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * @throws GameActionException
     * **/
    public static int writeCommMessage(commType type, int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannel(type);
        if (channel == -1) channel = getCommChannelOfLesserPriority(type, flag); // find a channel of lesser priority that can be overwritten
        if (channel == -1) return channel; // No available channels of lesser priority
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). This searches for an empty channel in which it can write the message. If it can't find any empty channel, it then searches for a channel with a lesser priority than the current message (which it gets from the SHAFlag) to write the message.
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param loc : the location that you want to write into a communication channel. If you want to send an int message there's another overloaded function with the same name that accepts int as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~600 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessage(commType type, MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannel(type);
        if (channel == -1) channel = getCommChannelOfLesserPriority(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(loc, flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). This directly searches for a channel whose information holds less priority than the current message (priority decided by SHAFlag).
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param message : the int message that you want to write to the channel. In most cases it's going to be int form of a MapLocation. There's another overloaded function with the same name that accepts MapLocation as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~440 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessageOverrwriteLesserPriorityMessage(commType type, int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriority(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). This directly searches for a channel whose information holds less priority than the current message (priority decided by SHAFlag).
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param loc : the location that you want to write into a communication channel. If you want to send an int message there's another overloaded function with the same name that accepts int as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~440 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessageOverrwriteLesserPriorityMessage(commType type, MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriority(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(loc, flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). Uses a queue and a head pointer (that points to the queue head) to search for an empty channel in which it can write the message. If it can't find any empty channel, it then searches for a channel with a lesser priority than the current message (which it gets from the SHAFlag) to write the message.
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param message : the int message that you want to write to the channel. In most cases it's going to be int form of a MapLocation. There's another overloaded function with the same name that accepts MapLocation as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~840 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessageToHead(commType type, int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelFromHead(type);
        if (channel == -1) channel = getCommChannelOfLesserPriorityFromHead(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). Uses a queue and a head pointer (that points to the queue head) to search for an empty channel in which it can write the message. If it can't find any empty channel, it then searches for a channel with a lesser priority than the current message (which it gets from the SHAFlag) to write the message.
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param loc : the location that you want to write into a communication channel. If you want to send an int message there's another overloaded function with the same name that accepts int as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~840 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessageToHead(commType type, MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelFromHead(type);
        if (channel == -1) channel = getCommChannelOfLesserPriorityFromHead(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(intFromMapLocation(loc), flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). Uses a queue and a head pointer (that points to the queue head) to directly search for a channel with a lesser priority information than the current message (priority decided by SHAFlag) to write the message.
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param message : the int message that you want to write to the channel. In most cases it's going to be int form of a MapLocation. There's another overloaded function with the same name that accepts MapLocation as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~350 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessageOverrwriteLesserPriorityMessageToHead(commType type, int message, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriorityFromHead(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(message, flag, channel);
        return channel;
    }


    /** Writes to the communication channels (not Rubble or archon or various bot count channels). Uses a queue and a head pointer (that points to the queue head) to directly search for a channel with a lesser priority information than the current message (priority decided by SHAFlag) to write the message.
     * @param type : either Comms.commType.LEAD or Comms.commType.COMBAT depending on whose (LEAD or COMBAT) channels need to be written into.
     * @param loc : the location that you want to write into a communication channel. If you want to send an int message there's another overloaded function with the same name that accepts int as input.
     * @param flag : the SHAFlag flag that denotes the type of the message: LEAD_LOCATION, CONFIRMED_ENEMY_ARCHON_LOCATION, etc.
     * @return the channel index to which the message was written. -1 is returned if it couldn't find a channel to write the message into.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~350 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     * **/
    public static int writeCommMessageOverrwriteLesserPriorityMessageToHead(commType type, MapLocation loc, SHAFlag flag) throws GameActionException{
        int channel = getCommChannelOfLesserPriorityFromHead(type, flag);
        if (channel == -1) return channel;
        writeSHAFlagMessage(intFromMapLocation(loc), flag, channel);
        return channel;
    }


    /** Writes 0 to the given channel.
     * @param channel : the input channel to be set to 0.
     * @BytecodeCost ~110 at max.
     */
    public static void wipeChannel(int channel) throws GameActionException{
        rc.writeSharedArray(channel, 0);
    }

    
    /** Writes 0 to the given channel and, compacts the channel queue to remove the new empty space created.
     * @param type: the type (LEAD OR COMBAT) of the input channel that is about to set as 0.
     * @param channel : the input channel to be set to 0.
     * @BytecodeCost<pre>
     *      When commType is LEAD   : ~340 at max
     *When commType is COMBAT : ??? at max (will be different because of more num of channels in COMBAT)
     * </pre>
     */
    public static void wipeChannelUpdateHead(commType type, int channel) throws GameActionException{
        type.commChannelHead = rc.readSharedArray(type.commChannelHeadChannel);
        if (type.commChannelHead == type.commChannelStart)
            type.commChannelHead = type.commChannelStop - 1;
        else type.commChannelHead--;

        rc.writeSharedArray(channel, rc.readSharedArray(type.commChannelHead));
        rc.writeSharedArray(type.commChannelHead, 0); //Important that this be here I think.
        rc.writeSharedArray(type.commChannelHeadChannel, type.commChannelHead);
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
