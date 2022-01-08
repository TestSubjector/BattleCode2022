package BuildBot;

import battlecode.common.*;

public class BotWatchTower extends Util{

    public static void initBotWatchTower(){
        RubbleMap.initRubbleMap();
    }


    public static void watchTowerComms() throws GameActionException {
        Comms.updateChannelValueBy1(Comms.CHANNEL_WATCHTOWER_COUNT);
    }


    static void runWatchTower(RobotController rc) throws GameActionException {
        watchTowerComms();

        if(turnCount != BIRTH_ROUND) RubbleMap.rubbleMapFormation(rc);
        if(turnCount != BIRTH_ROUND) RubbleMap.updateRubbleMap();
    }
}
