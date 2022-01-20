package ALabBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static MapLocation destAdjacent;
    private static MapLocation buildLocation;
    // private static RobotType buildType;
    private static int desperationIndex; // TODO: Can possibly create lead farms by builder sacrifice.
    private static boolean repairMode;
    private static MapLocation healLocation;
    private static boolean TAG_ALONG_ENABLED = true;
    private static MapLocation[] visibleLocations, inRangeLocations;
    private static RobotInfo[] visibleAllies;
    // private static final int MIN_LATTICE_DIST = 15;

    public static void initBotBuilder(){
        destAdjacent = null;
        buildLocation = null;
        // healMode = false;
        healLocation = null;
        repairMode = false;
    }


    public static void updateBuilder(){
        try{
            builderComms();
            repairMode = false;
            visibleLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), BUILDER_VISION_RADIUS);
            inRangeLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), BUILDER_ACTION_RADIUS);
            visibleAllies = rc.senseNearbyRobots();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static boolean moveToLocationForBuilding(MapLocation dest) throws GameActionException{
        // TODO: Write helper function to see if moving to optimum destAdjacent location is better than building from right here
        if (rc.getLocation().distanceSquaredTo(dest) == 1){
            destAdjacent = currentLocation;
            return true;
        }
         // TODO: Reorganise to sprint
        if (destAdjacent == null && rc.canSenseLocation(dest)) destAdjacent = PathFinder.findOptimumAdjacentLocation(dest);
        if (destAdjacent == null){
            if(!rc.getLocation().equals(dest)){
                Movement.moveToDest(dest);
                return rc.getLocation().equals(dest);
            }
        }
        else if(!rc.getLocation().equals(destAdjacent)){
            Movement.moveToDest(destAdjacent);
            return rc.getLocation().equals(destAdjacent);
        }
        return false;
    }


    public static boolean buildRobot(RobotType type, Direction dir) throws GameActionException{
        if (rc.canBuildRobot(type, dir)){
            rc.buildRobot(type, dir);
            desperationIndex = 0;
            // healMode = true;
            healLocation = currentLocation.add(dir);
            return true;
        }
        return false;
    }


    // public static boolean createBuildingInRandomDirection(RobotType type) throws GameActionException{
    //     for (Direction dir : directions) {
    //         if (buildRobot(type, dir))
    //             return true;
    //     }
    //     return false;
    // }


    private static boolean makeBuilding(MapLocation dest, RobotType type) throws GameActionException{
        Direction dir = currentLocation.directionTo(dest);
        destAdjacent = null;
        return buildRobot(type, dir);
        // if (!buildRobot(type, dir))
        //     return createBuildingInRandomDirection(type);
        // return true;
    }


    public static MapLocation mutableBuildingNearby(){
        try{
            
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    
    public static void builderComms(){
        try{
            Comms.updateArchonLocations();
            Comms.updateChannelValueBy1(Comms.CHANNEL_BUILDER_COUNT);
            Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
            Comms.updateComms(); 
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void repair(MapLocation loc) throws GameActionException{
        if (rc.canRepair(loc))
            rc.repair(loc);
    }


    public static void healBuilding() throws GameActionException{
        // TODO: Add code for actual healing of injured buildings; Currently it only repairs prototypes
        RobotInfo target = rc.senseRobotAtLocation(healLocation);
        if (target != null && target.getMode() == RobotMode.PROTOTYPE){
            repair(healLocation);
        }
        else{
            healLocation = null;
            // healMode = false;
        }
    }


    public static MapLocation findNearestBuildingThatCanBeRepaired(){
        try{
            RobotInfo bot;
            for (int i = visibleAllies.length; --i >= 0;){
                bot = visibleAllies[i];
                if (bot == null || bot.team == ENEMY_TEAM || !bot.type.isBuilding() || !CombatUtil.isBotInjured(bot)) continue;
                return bot.location;
            }

            // int ct = CombatUtil.militaryCount(visibleAllies);
            // if (ct == 0) return null;
            // if (ct < 10){
            //     MapLocation loc;
            //     MapLocation optLoc = null;
            //     int optDist = Integer.MAX_VALUE;
            //     for (int i = visibleLocations.length; --i >= 0;){
            //         loc = visibleLocations[i];
            //         RobotInfo bot = rc.senseRobotAtLocation(loc);
            //         if (bot == null) continue;
            //         if (bot.team == ENEMY_TEAM) continue;
            //         if (!bot.type.isBuilding()) continue;
            //         if (!CombatUtil.isBotInjured(bot)) continue;
            //         int dist = rc.getLocation().distanceSquaredTo(loc);
            //         if (dist < optDist){
            //             optLoc = loc;
            //             optDist = dist;
            //         }
            //     }
            //     return optLoc;
            // }
            // else{
            //     MapLocation curLoc = rc.getLocation();
            //     for (int i = droidVisionDirs.length - 1; --i >= 0;){
            //         Direction dir = droidVisionDirs[i];
            //         curLoc = curLoc.add(dir);
            //         if (rc.getLocation().distanceSquaredTo(curLoc) > BUILDER_VISION_RADIUS) break;
            //         if (!rc.canSenseLocation(curLoc)) continue;
            //         RobotInfo bot = rc.senseRobotAtLocation(curLoc);
            //         if (bot == null || bot.team == ENEMY_TEAM || !bot.type.isBuilding() || !CombatUtil.isBotInjured(bot)) continue;
            //         return bot.location;
            //     }
            // }
            
            // MapLocation loc;
            // MapLocation optLoc = null;
            // int optDist = Integer.MAX_VALUE;
            // for (int i = visibleAllies.length; --i >= 0;){
            //     loc = visibleAllies[i];
            //     RobotInfo bot = rc.senseRobotAtLocation(loc);
            //     if (bot == null) continue;
            //     if (bot.team == ENEMY_TEAM) continue;
            //     if (!bot.type.isBuilding()) continue;
            //     if (!CombatUtil.isBotInjured(bot)) continue;
            //     int dist = rc.getLocation().distanceSquaredTo(loc);
            //     if (dist < optDist){
            //         optLoc = loc;
            //         optDist = dist;
            //     }
            // }
            // return optLoc;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static boolean repairMode(){
        try{
            if (!repairMode) return false;
            MapLocation repairTarget = findNearestBuildingThatCanBeRepaired();
            if (repairTarget == null) return false;
            if (rc.getLocation().distanceSquaredTo(repairTarget) > BUILDER_ACTION_RADIUS){ 
                Movement.goToDirect(repairTarget);
                rc.setIndicatorString("Moving to repairTarget");
                return true;
            }
            if (rc.canRepair(repairTarget) && CombatUtil.isBotInjured(rc.senseRobotAtLocation(repairTarget))){ 
                rc.repair(repairTarget);
                return true;
            }
            return false;
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private static MapLocation findRubbleFreeBuildLocation(MapLocation closestArchon){
        try{
            // MapLocation[] possibleLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 20); // TODO: Decrease if too much bytecode cost
            MapLocation optLoc = rc.getLocation();
            int optRubble = rc.senseRubble(optLoc);
            int optDist = 0;
            for (int i = visibleAllies.length; --i >= 0; ){
                MapLocation loc = visibleLocations[i];
                if (loc.distanceSquaredTo(closestArchon) <= BUILDER_ACTION_RADIUS) continue;
                // if (rc.senseLead(loc) > 0 || rc.senseGold(loc) > 0) continue;
                int rubble = rc.senseRubble(loc), dist = rc.getLocation().distanceSquaredTo(loc);

                if (rubble < optRubble){
                    optLoc = loc;
                    optRubble = rubble;
                    optDist = dist;
                }
                else if (rubble == optRubble && dist < optDist){
                    optLoc = loc;
                    optDist = dist;
                }
            }
            return optLoc;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static void findBuildLocation(){
        try{
            if (buildLocation != null) return;
            MapLocation closestArchon = getClosestArchonLocation();
            if (closestArchon.distanceSquaredTo(rc.getLocation()) <= BUILDER_ACTION_RADIUS){
                Movement.moveAwayFromLocation(closestArchon);
                return;
            }
            // buildLocation = Movement.moveToLattice(MIN_LATTICE_DIST, 0);
            buildLocation = findRubbleFreeBuildLocation(closestArchon);
            if (buildLocation == null) System.out.println("Seriously?!!!");
        } catch (Exception e){
            e.printStackTrace();
        }
        return;
    }


    private static RobotType getUnitTypeToBuild(){
        // TODO: Include labs in this.
        return RobotType.WATCHTOWER;
    }


    public static Direction findBuildDirection(RobotType buildType){
        try{
            Direction dir;
            Direction optDir = null;
            int optRubble = MAX_RUBBLE + 1;
            for (int i = directions.length; --i >= 0; ){
                dir = directions[i];
                if (!rc.canBuildRobot(buildType, dir)) continue;
                int rubble = rc.senseRubble(rc.getLocation().add(dir));
                if (rubble < optRubble){
                    optDir = dir;
                    optRubble = rubble;
                }
            }
            return optDir;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static boolean buildMode(){
        try{
            if (prioritizeAdjacentPrototypeRepair()) return true;
            RobotType buildType = getUnitTypeToBuild();
            findBuildLocation();
            if (buildLocation == null) {
                rc.setIndicatorString("Moving away from archon it seems. Check.");
                return false;
            }
            int buildCost = buildType.buildCostLead;
            if (rc.getTeamLeadAmount(MY_TEAM) < buildCost){
                repairMode = true;
                return false;
            }
            if (!rc.getLocation().equals(buildLocation)){ 
                Movement.goToDirect(buildLocation);
                rc.setIndicatorString("Moving to buildLocation : " + buildLocation);
                return true;
            }
            if (!rc.isActionReady()) return false;
            Direction targetDir = findBuildDirection(buildType);
            if (targetDir == null){
                rc.setIndicatorString("Must be surrounded by bots in all directions. Check.");
                return false;
            }
            rc.buildRobot(buildType, targetDir);
            buildLocation = null;
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static boolean prioritizeAdjacentPrototypeRepair(){
        try{
            // MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), BUILDER_ACTION_RADIUS);
            for (MapLocation loc : inRangeLocations){
                if (!rc.canSenseRobotAtLocation(loc)) continue;
                RobotInfo bot = rc.senseRobotAtLocation(loc);
                if (bot.mode.equals(RobotMode.PROTOTYPE) || (bot.type.isBuilding() && CombatUtil.isBotInjured(bot))){
                    if (rc.canRepair(loc)) rc.repair(loc);
                    return true;
                }
                // if (rc.canMutate(loc)){
                //     rc.mutate(loc);
                // }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private static void moveTowardsWatchTower(){
        try{
            RobotInfo bot;
            for (int i = visibleAllies.length; --i >= 0;){
                bot = visibleAllies[i];
                if (bot.type.equals(RobotType.WATCHTOWER)){
                    if (rc.getLocation().distanceSquaredTo(bot.location) > BUILDER_ACTION_RADIUS)
                        Movement.goToDirect(bot.location);
                    rc.setIndicatorString("Moving towards watchtower");
                    buildLocation = null;
                    return;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    static void runBuilder(RobotController rc){
        try{
            // System.out.println("A: Bytecodes left: " + Clock.getBytecodesLeft());
            updateBuilder();
            // System.out.println("B: Bytecodes left: " + Clock.getBytecodesLeft());
            if (buildMode() || repairMode()) return;
            else{
                // System.out.println("C: Bytecodes left: " + Clock.getBytecodesLeft());
                // TODO: Do What?
                if (TAG_ALONG_ENABLED) moveTowardsWatchTower();
                else Movement.goToDirect(BotMiner.explore());
                buildLocation = null;
            }
            // System.out.println("D: Bytecodes left: " + Clock.getBytecodesLeft());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

