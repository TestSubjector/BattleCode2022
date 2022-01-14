package OTempBot;

import battlecode.common.*;


public class BotLaboratory extends Util {
    public static boolean shouldDoAlchemy;
    public static int transmutationRate;

    public static void initBotLaboratory(){
        shouldDoAlchemy = false;
        transmutationRate = rc.getTransmutationRate();
    }


    public static boolean becomeMidas(){
        // TODO: Figure out a criteria to turn on conversion of Lead -> Gold
        return false;
    }


    public static void updateLaboratory(){
        shouldDoAlchemy = becomeMidas();
        // TODO: Don't update transmutationRate in every turn. Update only when the laboratory moves.
        transmutationRate = rc.getTransmutationRate(); // Bytecode Cost: 20
    }


    public static void alchemy() throws GameActionException{
        if (!shouldDoAlchemy || !rc.canTransmute()) return;
        rc.transmute();
    }


    public static void runLaboratory(RobotController rc) throws GameActionException{
        updateLaboratory();
        alchemy();
    }
}