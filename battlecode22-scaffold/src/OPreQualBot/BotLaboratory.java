package OPreQualBot;

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
        if (rc.getTeamGoldAmount(MY_TEAM) > 50) return false;
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

    private static MapLocation findRubbleFreeLocation() throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), LABORATORY_VISION_RADIUS);
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
                // rc.setIndicatorString("Opt loc" + optLoc);
                optDist = dist;
            }
            else if (rubble == optRubble && dist < optDist){
                optLoc = loc;
                // rc.setIndicatorString("Opt loc" + optLoc);
                optDist = dist;
            }
        }
        if (rc.senseRubble(rc.getLocation()) - optRubble < 10){
            return rc.getLocation();
        }
        return optLoc;
    }


    private static void moveIfNeeded(){
        try{
            // TODO: Handle vortices too.
            if (!needForRelocation) return;
            if (rc.getRoundNum() < BIRTH_ROUND + 60){
                return;
            }
            if (relocationTarget != null && rc.getLocation().equals(relocationTarget)){
                rc.setIndicatorString("RT " + relocationTarget);
                if (rc.getMode().equals(RobotMode.PORTABLE)){
                    if (rc.isTransformReady()){
                        rc.transform();
                        relocationTarget = null;
                        needForRelocation = false;
                    }
                }
                else{
                    System.out.println("Enough is enough dude");
                }
                return;
            }
            else if (relocationTarget != null){
                rc.setIndicatorString("RT2 " + relocationTarget);
                if (rc.getMode().equals(RobotMode.TURRET)){
                    // System.out.println("STOP! No further. This is a problem now!");
                    // needForRelocation = false;
                    if (rc.isTransformReady()) rc.transform();
                    return;
                }
                if (rc.canSenseRobotAtLocation(relocationTarget)) relocationTarget = findRubbleFreeLocation();
                if (relocationTarget.equals(rc.getLocation())){
                    if (rc.isTransformReady()) {
                        rc.transform();
                        needForRelocation = false;
                        relocationTarget = null;
                    }
                    return;
                }
                if (Movement.goToDirect(relocationTarget)){
                    if (relocationTarget.equals(rc.getLocation()) && rc.isTransformReady()){ 
                        rc.transform();
                        needForRelocation = false;
                        relocationTarget = null;
                    }
                }
                return;
            }
            
            relocationTarget = findRubbleFreeLocation();
            if (relocationTarget.equals(rc.getLocation())){
                needForRelocation = false;
                relocationTarget = null;
                return;
            }
            if (rc.isTransformReady()) rc.transform();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void runLaboratory(RobotController rc) throws GameActionException{
        updateLaboratory();
        moveIfNeeded();
        touchOfMidas();
        BotMiner.surveyForOpenMiningLocationsNearby();
    }
}