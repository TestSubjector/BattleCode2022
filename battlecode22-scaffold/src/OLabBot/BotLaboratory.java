package OLabBot;

import battlecode.common.*;


public class BotLaboratory extends Util {
    public static boolean shouldDoAlchemy;
    public static int transmutationRate;
    

    public static void initBotLaboratory(){
        shouldDoAlchemy = false;
        transmutationRate = rc.getTransmutationRate();
    }


    private static boolean shouldTransmute(){
        // TODO: Figure out a criteria to turn on conversion of Lead -> Gold
        return true;
    }


    private static void laboratoryComms(){
        try{
            Comms.updateArchonLocations();
            Comms.updateChannelValueBy1(Comms.CHANNEL_LABORATORY_COUNT);
            Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
            Comms.updateComms();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void updateLaboratory(){
        laboratoryComms();
        shouldDoAlchemy = shouldTransmute();
        // TODO: Don't update transmutationRate in every turn. Update only when the laboratory moves.
        transmutationRate = rc.getTransmutationRate(); // Bytecode Cost: 20
    }


    private static void theTouchOfMidas() throws GameActionException{
        if (!shouldDoAlchemy || !rc.canTransmute()) return;
        rc.transmute();
    }


    public static void runLaboratory(RobotController rc) throws GameActionException{
        updateLaboratory();
        theTouchOfMidas();
    }
}