package OThirdBot;

import battlecode.common.*;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Globals.initGlobals(rc);
        // PathFinder.initPathFinder();
        Globals.updateEnemyArchonLocation();
        switch (rc.getType()) {
            case MINER:      BotMiner.initBotMiner();   break;
        }
        while (true) {
            Globals.updateGlobals();
            try {
                switch (rc.getType()) {
                    case ARCHON:     BotArchon.runArchon(rc);  break;
                    case MINER:      BotMiner.runMiner(rc);   break;
                    case SOLDIER:    BotSoldier.runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:
                    case SAGE:       break;
                }
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                if (Clock.getBytecodesLeft() < 500) System.out.println("Warning, bytecodes left this turn is: " + Clock.getBytecodesLeft());
                // if(rc.getRoundNum() > 9) return;
                Clock.yield();
            }
        }
    }
}
