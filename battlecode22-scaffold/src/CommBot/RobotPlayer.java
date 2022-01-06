package CommBot;

import battlecode.common.*;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Globals.initGlobals(rc);
        // PathFinder.initPathFinder();
        Globals.updateEnemyArchonLocation();
        switch (rc.getType()) {
            case MINER:      BotMiner.initBotMiner();       break;
            case BUILDER:    BotBuilder.initBotBuilder();   break;
            case SOLDIER:    BotSoldier.initBotSoldier();   break;
        }
        while (true) {
            Globals.updateGlobals();
            try {
                switch (rc.getType()) {
                    case ARCHON:     BotArchon.runArchon(rc);   break;
                    case MINER:      BotMiner.runMiner(rc);     break;
                    case SOLDIER:    BotSoldier.runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:    BotBuilder.runBuilder(rc); break;
                    case SAGE:       break;
                }
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                if (Clock.getBytecodesLeft() < 1000) System.out.println("Warning, bytecodes left this turn is: " + Clock.getBytecodesLeft());
                // if(rc.getRoundNum() > 39) return;
                Clock.yield();
            }
        }
    }
}
