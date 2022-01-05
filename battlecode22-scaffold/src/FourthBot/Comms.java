package FourthBot;

import battlecode.common.*;

public class Comms extends Util{

    public static final int CHANNEL_RUBBLE_START = 0;
    public static final int CHANNEL_RUBBLE_STOP = 32;


    public static int createRubbleMessage(int locationValue, int rubbleValue, int turnFlag) throws GameActionException{
        return ( (turnFlag << 15) | locationValue << 3 | rubbleValue);
    }

    public static int getRubbleValueFromRubbleMessage(int num) throws GameActionException{
        return (num & 0x0007);
    }

    public static MapLocation getLocFromRubbleMessage(int loc) throws GameActionException{
        return mapLocationFromInt((loc & (0x7FF8))>>>3);
    }
}
