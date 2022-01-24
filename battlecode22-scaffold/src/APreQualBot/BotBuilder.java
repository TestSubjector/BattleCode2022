package APreQualBot;

import battlecode.common.*;

public class BotBuilder extends Util{

    private static boolean repairMode;
    private static boolean TAG_ALONG_ENABLED = true;
    private static MapLocation buildLocation;
    private static MapLocation healArchon;
    private static MapLocation[] visibleLocations, inRangeLocations;
    private static RobotInfo[] visibleAllies, visibleEnemies;
    private static boolean isFleeing;
    private static boolean archonHealingDuty;
    private static final boolean DEBUG_MODE = true;
    private static int healArchonCommID;

    public static void initBotBuilder(){
        buildLocation = null;
        repairMode = false;
        isFleeing = false;
        archonHealingDuty = false;
        healArchon = null;
    }


    public static boolean isSafeToBuild(MapLocation loc){
        for (int i = visibleEnemies.length; --i >= 0;) {
            RobotInfo enemy = visibleEnemies[i];
            switch (enemy.type) {
                case SOLDIER:
                case WATCHTOWER:
                case SAGE:
                    if(SMALL_MAP && enemy.type.actionRadiusSquared == loc.distanceSquaredTo(enemy.location))
                        return false;
                    if (enemy.type.actionRadiusSquared > loc.distanceSquaredTo(enemy.location))
                        return false;
                default: break;
            }
        }
        return true;
    }


    private static void updateBuilder(){
        try{
            builderComms();
            repairMode = false;
            visibleLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), BUILDER_VISION_RADIUS);
            inRangeLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), BUILDER_ACTION_RADIUS);
            visibleAllies = rc.senseNearbyRobots(BUILDER_VISION_RADIUS, MY_TEAM);
            visibleEnemies = rc.senseNearbyRobots(BUILDER_VISION_RADIUS, ENEMY_TEAM);
            if (!isSafeToBuild(rc.getLocation())){
                isFleeing = true;
                buildLocation = null;
                if (DEBUG_MODE)
                rc.setIndicatorString("Fleeing!");
                isFleeing = CombatUtil.tryToBackUpToMaintainMaxRangeMiner(visibleEnemies);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    
    private static void builderComms(){
        try{
            Comms.updateArchonLocations();
            Comms.updateChannelValueBy1(Comms.CHANNEL_BUILDER_COUNT);
            Comms.updateChannelValueBy1(Comms.CHANNEL_TRANSMITTER_COUNT);
            Comms.updateComms(); 
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void repair(MapLocation loc) throws GameActionException{
        if (rc.canRepair(loc))
            rc.repair(loc);
    }


    private static MapLocation findNearestBuildingThatCanBeRepaired(){
        try{
            if (visibleAllies.length == 0) return null;
            RobotInfo bot;
            for (int i = visibleAllies.length; --i >= 0;){
                bot = visibleAllies[i];
                if (bot == null || bot.team == ENEMY_TEAM || !bot.type.isBuilding() || !CombatUtil.isBotInjured(bot)) continue;
                return bot.location;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static MapLocation findNearestBuildingThatCanBeMutated(){
        try{
            if (visibleAllies.length == 0) return null;
            RobotInfo bot;
            for (int i = visibleAllies.length; --i >= 0;){
                bot = visibleAllies[i];
                if (bot == null || bot.team == ENEMY_TEAM || !rc.canMutate(bot.location)) continue;
                return bot.location;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static boolean repairMode(){
        try{
            if (!repairMode) return false;
            MapLocation repairTarget = findNearestBuildingThatCanBeRepaired();
            if (repairTarget == null) {
                if (DEBUG_MODE)
                rc.setIndicatorString("can't finding anything to repair");
                return false;
            }
            if (rc.getLocation().distanceSquaredTo(repairTarget) > BUILDER_ACTION_RADIUS){ 
                Movement.goToDirect(repairTarget);
                if (DEBUG_MODE)
                rc.setIndicatorString("Moving to repairTarget: " + repairTarget);
                return true;
            }
            if (rc.canRepair(repairTarget) && CombatUtil.isBotInjured(rc.senseRobotAtLocation(repairTarget))){ 
                if (DEBUG_MODE)
                rc.setIndicatorString("repairing: " + repairTarget);
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
                if (!rc.canSenseLocation(loc)) continue;
                // if (loc.distanceSquaredTo(closestArchon) <= BUILDER_ACTION_RADIUS) continue;
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


    private static void findLaboratoryBuildLocation(){
        buildLocation = findClosestCorner(rc.getLocation());
        return;
    }


    private static void findBuildLocation(RobotType buildType){
        try{
            if (buildLocation != null && !buildType.equals(RobotType.LABORATORY)) return;
            if (buildLocation == null && buildType.equals(RobotType.LABORATORY)){
                findLaboratoryBuildLocation();
            }
            if (buildLocation != null){
                if (rc.getLocation().distanceSquaredTo(buildLocation) > BUILDER_VISION_RADIUS) return;
            }
            MapLocation closestArchon = getClosestArchonLocation();
            // if (closestArchon != null && closestArchon.distanceSquaredTo(buildLocation) <= BUILDER_ACTION_RADIUS){
            //     Movement.moveAwayFromLocation(closestArchon);
            //     return;
            // }
            // buildLocation = Movement.moveToLattice(MIN_LATTICE_DIST, 0);
            buildLocation = findRubbleFreeBuildLocation(closestArchon);
            if (buildLocation == null) System.out.println("Seriously?!!!");
        } catch (Exception e){
            e.printStackTrace();
        }
        return;
    }


    private static RobotType getUnitTypeToBuild(){
        int laboratoryCount = Comms.getLaboratoryCount();
        if (laboratoryCount < MAX_LABORATORY_COUNT) return RobotType.LABORATORY;
        return RobotType.WATCHTOWER;
    }


    private static Direction findBuildDirection(RobotType buildType){
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
            if (archonHealingDuty){
                repairMode = true;
                return false;
            }
            if (prioritizeAdjacentPrototypeRepair()) return true;
            RobotType buildType = getUnitTypeToBuild();
            if (buildType == null) return false;
            findBuildLocation(buildType);
            if (buildLocation == null) {
                if (DEBUG_MODE)
                rc.setIndicatorString("Moving away from archon it seems. Check.");
                return false;
            }
            if (!rc.getLocation().equals(buildLocation)){ 
                // Movement.goToDirect(buildLocation);
                BFS.move(buildLocation);
                if (DEBUG_MODE)
                // rc.setIndicatorString("Moving to buildLocation : " + buildLocation);
                return true;
            }
            int buildCost = buildType.buildCostLead;
            if (rc.getTeamLeadAmount(MY_TEAM) < buildCost){
                repairMode = true;
                return false;
            }
            if (!rc.isActionReady()) return false;
            Direction targetDir = findBuildDirection(buildType);
            if (targetDir == null){
                if (DEBUG_MODE)
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


    private static boolean prioritizeAdjacentPrototypeRepair(){
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
            if (archonHealingDuty) return;
            RobotInfo bot;
            for (int i = visibleAllies.length; --i >= 0;){
                bot = visibleAllies[i];
                if (bot.type.equals(RobotType.WATCHTOWER)){
                    if (rc.getLocation().distanceSquaredTo(bot.location) > BUILDER_ACTION_RADIUS)
                        BFS.move(bot.location);
                    if (DEBUG_MODE)
                    rc.setIndicatorString("Moving towards watchtower");
                    buildLocation = null;
                    return;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void assignClosestUnHealedArchon() throws GameActionException{
        int optDist = Integer.MAX_VALUE;
        int index = -1;
        // if (rc.getRoundNum() > 200)
        // System.out.println("trying to assign healLocation");
        for (int i = 0; i < archonCount; ++i){
            if (Comms.checkIfArchonNeedsBuilder(i)){
                MapLocation loc = Comms.getArchonLocation(i);
                if (loc == null) continue;
                if (!Comms.checkIfArchonNeedsBuilder(i)) continue;
                int dist = rc.getLocation().distanceSquaredTo(loc);
                if (healArchon == null || optDist < dist){
                    dist = optDist;
                    healArchon = loc;
                    index = i;
                }
            }
        }
        if (index != -1){
            System.out.println("wiping out need for healing flag");
            Comms.updateArchonBuilderNeedFlag(index, false);
            Comms.builderAssignedToArchon(index, true);
            healArchonCommID = index;
        }
    }


    public static boolean checkIfAnyArchonNeedsHealing() throws GameActionException{
        for (int i = 0; i < archonCount; ++i){
            if (Comms.checkIfArchonNeedsBuilder(i)) return true;
        }
        return false;
    }


    private static void healArchon() throws GameActionException{
        if (!archonHealingDuty && !checkIfAnyArchonNeedsHealing()) return;
        else if (!archonHealingDuty) archonHealingDuty = true;
        if (healArchon == null) assignClosestUnHealedArchon();
        if (healArchon == null) return;
        Comms.builderAssignedToArchon(healArchonCommID, true);
        if (rc.getLocation().distanceSquaredTo(healArchon) > BUILDER_ACTION_RADIUS){
            if (DEBUG_MODE)
            rc.setIndicatorString("heading out to heal archon");
            BFS.move(healArchon);
        }
    }


    private static boolean mutateMode() throws GameActionException{
        // if (rc.getTeamLeadAmount(MY_TEAM) < 150 && rc.getTeamGoldAmount(MY_TEAM) < 30) return false;
        if (rc.getTeamLeadAmount(MY_TEAM) < 150) return false;
        if (visibleAllies.length == 0) return false;
        MapLocation mutateTarget = findNearestBuildingThatCanBeMutated();
        if (mutateTarget == null) {
            if (DEBUG_MODE)
            rc.setIndicatorString("can't finding anything to mutate");
            return false;
        }
        if (rc.getLocation().distanceSquaredTo(mutateTarget) > 2){ 
            Movement.goToDirect(mutateTarget);
            if (DEBUG_MODE)
            rc.setIndicatorString("Moving to mutateTarget: " + mutateTarget);
            return true;
        }
        if (rc.canMutate(mutateTarget)){ 
            if (DEBUG_MODE)
            rc.setIndicatorString("mutating: " + mutateTarget);
            System.out.println("Mutating!");
            rc.mutate(mutateTarget);
            return true;
        }
        return false;
    }


    public static void runBuilder(RobotController rc){
        try{
            updateBuilder();
            healArchon();
            BotMiner.surveyForOpenMiningLocationsNearby();
            if (isFleeing) return;
            if (buildMode() || mutateMode() || repairMode()) return;
            else{
                if (TAG_ALONG_ENABLED) moveTowardsWatchTower();
                else Movement.goToDirect(BotMiner.explore());
                buildLocation = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
