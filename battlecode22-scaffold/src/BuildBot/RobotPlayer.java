package BuildBot;

import battlecode.common.*;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Globals.initGlobals(rc);
        switch (rc.getType()) {
            case MINER:      BotMiner.initBotMiner();           break;
            case BUILDER:    BotBuilder.initBotBuilder();       break;
            case SOLDIER:    BotSoldier.initBotSoldier();       break;
            case ARCHON:     BotArchon.initBotArchon();         break;
            case WATCHTOWER: BotWatchTower.initBotWatchTower(); break;
        }
        while (true) {

            Globals.updateGlobals();
            try {
                switch (rc.getType()) {
                    case ARCHON:     BotArchon.runArchon(rc);         break;
                    case MINER:      BotMiner.runMiner(rc);           break;
                    case SOLDIER:    BotSoldier.runSoldier(rc);       break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: BotWatchTower.runWatchTower(rc); break;
                    case BUILDER:    BotBuilder.runBuilder(rc);       break;
                    case SAGE:       break;
                }
                // Bytecode Testing Area
                // Util.byteCodeTest();
                // if(rc.getRoundNum() > 4) return;
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                if (Clock.getBytecodesLeft() < 500) System.out.println("Warning, bytecodes left this turn is: " + Clock.getBytecodesLeft());
                Clock.yield();
            }
        }
    }

}
