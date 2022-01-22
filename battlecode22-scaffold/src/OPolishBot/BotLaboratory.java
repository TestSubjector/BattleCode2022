package OPolishBot;

import battlecode.common.*;


public class BotLaboratory extends Util {
    public static boolean shouldDoAlchemy;
    public static int transmutationRate;
    public static MapLocation relocationTarget;
    public static boolean needForRelocation;

    public static void initBotLaboratory(){
        shouldDoAlchemy = false;
        transmutationRate = rc.getTransmutationRate();
        relocationTarget = null;
        needForRelocation = true;
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


    private static void touchOfMidas() throws GameActionException{
        if (!shouldDoAlchemy || !rc.canTransmute()) return;
        rc.transmute();
    }


    private static void moveIfNeeded(){
        try{
            if (!needForRelocation) return;
            if (relocationTarget != null && rc.getLocation().equals(relocationTarget)){
                if (rc.getMode().equals(RobotMode.PORTABLE)){
                    if (rc.canTransform()){
                        rc.transform();
                        relocationTarget = null;
                    }
                    
                }
                else{
                    System.out.println("Enough is enough dude");
                }
                return;
            }
            else if (relocationTarget != null){
                if (rc.getMode().equals(RobotMode.TURRET)){
                    if (rc.canTransform()) rc.transform();
                    return;
                }
                if (Movement.goToDirect(relocationTarget)){
                    if (relocationTarget.equals(rc.getLocation()) && rc.canTransform()) rc.transform();
                }
                return;
            }
            MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
            MapLocation optLoc = rc.getLocation();
            int optRubble = rc.senseRubble(rc.getLocation()), optDist = 0;
            MapLocation loc;
            for (int i = adjacentLocations.length; --i >= 0; ){
                loc = adjacentLocations[i];
                if (!rc.canSenseLocation(loc)) continue;
                if (rc.canSenseRobotAtLocation(loc)) continue;
                int rubble = rc.senseRubble(loc), dist = rc.getLocation().distanceSquaredTo(loc);
                if (rubble < optRubble){
                    optRubble = rubble;
                    optLoc = loc;
                    optDist = dist;
                }
                else if (rubble == optRubble && dist < optDist){
                    optLoc = loc;
                    optDist = dist;
                }
            }
            if (optLoc.equals(rc.getLocation())){
                needForRelocation = false;
                return;
            }
            relocationTarget = optLoc;
            if (rc.canTransform()) rc.transform();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void runLaboratory(RobotController rc) throws GameActionException{
        updateLaboratory();
        moveIfNeeded();
        touchOfMidas();
    }
}