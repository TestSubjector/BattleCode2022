package BuildBot;

import battlecode.common.*;

public class BotWatchTower extends Util{

    public static void initBotBuilder(){
        RubbleMap.initRubbleMap();
    }


    public static void watchTowerComms() throws GameActionException {
        Comms.updateChannelValueBy1(Comms.CHANNEL_WATCHTOWER_COUNT);
    }


    static void runBuilder(RobotController rc) throws GameActionException {
        watchTowerComms();

        if(turnCount != BIRTH_ROUND) RubbleMap.rubbleMapFormation(rc);
        if(turnCount != BIRTH_ROUND) RubbleMap.updateRubbleMap();
    }
}
