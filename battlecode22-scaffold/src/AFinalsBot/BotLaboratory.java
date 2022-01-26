package AFinalsBot;

import battlecode.common.*;


public class BotLaboratory extends Util {
    public static boolean shouldDoAlchemy;
    public static int transmutationRate;
    public static MapLocation relocationTarget;
    private static MapLocation vortexMoveIfNeededTarget;
    public static boolean needForRelocation;
    private static boolean alreadyMoving;
    private static boolean vortexStartedMoving;
    private static AnomalyScheduleEntry prevAnomaly;
    private static int updateNeedToMove;

    public static void initBotLaboratory(){
        shouldDoAlchemy = false;
        transmutationRate = rc.getTransmutationRate();
        relocationTarget = null;
        needForRelocation = true;
        prevAnomaly = null;
        alreadyMoving = false;
        vortexMoveIfNeededTarget = null;
        vortexStartedMoving = false;
        updateNeedToMove = 2050;
        Anomaly.initAnomaly();
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
        AnomalyScheduleEntry nextAnomaly = Anomaly.getNextAnomaly();
        if (nextAnomaly != null && prevAnomaly == null){
            prevAnomaly = nextAnomaly;
            if (nextAnomaly.anomalyType == AnomalyType.VORTEX){
                updateNeedToMove = nextAnomaly.roundNumber;
            }
            else{
                updateNeedToMove = 2050;
            }
            // System.out.println("This anomaly: " + nextAnomaly.anomalyType);
            // System.out.println("This anomaly round num: " + nextAnomaly.roundNumber);
        }

        if (nextAnomaly != null && !nextAnomaly.equals(prevAnomaly)){
            prevAnomaly = nextAnomaly;
            if (nextAnomaly.anomalyType == AnomalyType.VORTEX){
                updateNeedToMove = nextAnomaly.roundNumber;
            }
            else
                updateNeedToMove = 2050;
            // System.out.println("This anomaly: " + nextAnomaly.anomalyType);
            // System.out.println("This anomaly round num: " + nextAnomaly.roundNumber);
        }
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


    private static void vortexMoveIfNeeded() throws GameActionException{
        if (needForRelocation) return;
        int roundsRemaining = (updateNeedToMove - rc.getRoundNum() - BotArchon.transformCooldownCompute()); 
        if (roundsRemaining > 1 && !vortexStartedMoving) return;
        else if (roundsRemaining == 1 && !vortexStartedMoving && rc.getMode().equals(RobotMode.PORTABLE)){
            alreadyMoving = true;
            return;
        }
        else if (roundsRemaining == 1 && !vortexStartedMoving) alreadyMoving = false;
        if (alreadyMoving) return;
        if (rc.getMode().equals(RobotMode.TURRET)){
            if (rc.isTransformReady()) {
                rc.transform();
                vortexStartedMoving = true;
            }
            return;
        }
        if (!rc.isMovementReady()) return;
        if (vortexMoveIfNeededTarget == null){
            vortexMoveIfNeededTarget = findRubbleFreeLocation();
        }
        if (vortexMoveIfNeededTarget == null){
            if (rc.isTransformReady()) {
                rc.transform();
                vortexStartedMoving = false;
            }
            System.out.println("never happens");
            return;
        }
        if (vortexMoveIfNeededTarget.equals(rc.getLocation())){
            if (rc.isTransformReady()){
                rc.transform();
                vortexMoveIfNeededTarget = null;
                vortexStartedMoving = false;
            }
            return;
        }
        if (rc.canSenseRobotAtLocation(vortexMoveIfNeededTarget)){
            vortexMoveIfNeededTarget = findRubbleFreeLocation();
        }
        if (vortexMoveIfNeededTarget.equals(rc.getLocation())){
            if (rc.isTransformReady()){
                rc.transform();
                vortexMoveIfNeededTarget = null;
                vortexStartedMoving = false;
            }
            return;
        }
        if (!rc.isMovementReady()) return;
        if (!BFS.move(vortexMoveIfNeededTarget)){
            if (rc.isTransformReady()){
                rc.transform();
                vortexStartedMoving = false;
                vortexMoveIfNeededTarget = null;
            }
            return;
        }
    }


    public static void runLaboratory(RobotController rc) throws GameActionException{
        updateLaboratory();
        moveIfNeeded();
        vortexMoveIfNeeded();
        touchOfMidas();
        BotMiner.surveyForOpenMiningLocationsNearby();
    }
}