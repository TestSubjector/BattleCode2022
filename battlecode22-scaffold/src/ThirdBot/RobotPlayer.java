package ThirdBot;

import battlecode.common.*;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Globals.initGlobals(rc);
        PathFinder.initPathFinder();
        Globals.updateEnemyArchonLocation();
        switch (rc.getType()) {
            // case ARCHON:     runArchon(rc);  break;
            case MINER:      BotMiner.initBotMiner();   break;
            // case SOLDIER:    runSoldier(rc); break;
            // case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
            // case WATCHTOWER: // You might want to give them a try!
            // case BUILDER:
            // case SAGE:       break;
        }
        // System.out.println("2: " + String.valueOf(Clock.getBytecodeNum()));
        while (true) {
            // System.out.println("Age: " + Globals.TURN_COUNT + "; Location: " + rc.getLocation());
            Globals.updateGlobals();
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
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
                // if (Clock.getBytecodesLeft() < 1000) System.out.println("Warning, bytecodes left this turn is: " + Clock.getBytecodesLeft());
                Clock.yield();
            }
        }
    }
}
