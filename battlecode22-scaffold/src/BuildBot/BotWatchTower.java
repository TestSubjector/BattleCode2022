package BuildBot;

import battlecode.common.*;

public class BotWatchTower extends Util{

    public static void initBotWatchTower(){
        if (isRubbleMapEnabled) RubbleMap.initRubbleMap();
    }


    public static void watchTowerComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_WATCHTOWER_COUNT);
        Comms.channelArchonStop = Comms.CHANNEL_ARCHON_START + 4*archonCount;
        Comms.commChannelStart = Comms.channelArchonStop; 
    }

    static void runWatchTower(RobotController rc) throws GameActionException {
        watchTowerComms();
        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            RubbleMap.updateRubbleMap();
        }
    }
}
