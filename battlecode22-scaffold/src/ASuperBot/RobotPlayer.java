package ASuperBot;

import battlecode.common.*;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // if (rc.getType() == RobotType.SOLDIER) 
            // System.out.println(" Byte code A: " + Clock.getBytecodesLeft());
        Globals.initGlobals(rc);
        // if (rc.getType() == RobotType.MINER) 
            // System.out.println(" Byte code B: " + Clock.getBytecodesLeft());
        switch (rc.getType()) {
            case MINER:      BotMiner.initBotMiner();           break;
            case BUILDER:    BotBuilder.initBotBuilder();       break;
            case SOLDIER:    BotSoldier.initBotSoldier();       break;
            case ARCHON:     BotArchon.initBotArchon();         break;
            case WATCHTOWER: BotWatchTower.initBotWatchTower(); break;
            case LABORATORY: BotLaboratory.initBotLaboratory(); break;
            case SAGE:       BotSage.initBotSage();             break;
        }
        // if (rc.getType() == RobotType.MINER) 
            // System.out.println(" Byte code C: " + Clock.getBytecodesLeft());
        while (true) {
            // if(rc.getRoundNum() > 23) return;
            Globals.updateGlobals();
            try {
                switch (rc.getType()) {
                    case ARCHON:     BotArchon.runArchon(rc);         break;
                    case MINER:      BotMiner.runMiner(rc);           break;
                    case SOLDIER:    BotSoldier.runSoldier(rc);       break;
                    case LABORATORY: BotLaboratory.runLaboratory(rc); break;
                    case WATCHTOWER: BotWatchTower.runWatchTower(rc); break;
                    case BUILDER:    BotBuilder.runBuilder(rc);       break;
                    case SAGE:       BotSage.runSage(rc);             break;
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
                // For bytecode comparison of functions:
                // if (rc.getRoundNum() == 2000 && Globals.UNIT_TYPE == RobotType.MINER){
                //     System.out.println("Max bytecode diff: " + Globals.bytecodediff);
                // }
                if (Clock.getBytecodesLeft() < 500) System.out.println("Warning, bytecodes left this turn is: " + Clock.getBytecodesLeft());
                Clock.yield();
            }
        }
    }

}
