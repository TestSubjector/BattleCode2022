package CommBot;

import java.util.Random;
import battlecode.common.*;

public class Globals {

    public static RobotController rc;
    public static final Random rng = new Random(6147);
    public static int turnCount;
    public static int BIRTH_ROUND;

    public static RobotType UNIT_TYPE;

    public static Team MY_TEAM;

    public static Team ENEMY_TEAM;

    public static int archonCount;
    public static int ID;
    public static int robotLevel;
    public static int myRobotCount;
    public static int myHealth;
    public static MapLocation START_LOCATION;
    public static MapLocation currentLocation;
    public static MapLocation currentDestination = null;
    public static MapLocation rememberedArchonLocation = null;
    public static MapLocation rememberedEnemyArchonLocation[];

    public static boolean botFreeze;

    public static boolean underAttack;
    public static boolean isSafe=false;
    public static boolean hasMoved=false;
	
    //// ---------------------Droids Globals---------------------
	// Miner Globals
	public static final int MINER_ACTION_RADIUS = RobotType.MINER.actionRadiusSquared;
	public static final int MINER_VISION_RADIUS = RobotType.MINER.visionRadiusSquared;

	// Builder Globals
	public static final int BUILDER_ACTION_RADIUS = RobotType.BUILDER.actionRadiusSquared;
	public static final int BUILDER_VISION_RADIUS = RobotType.BUILDER.visionRadiusSquared;

	// Soldier Globals
	public static final int SOLDIER_ACTION_RADIUS = RobotType.SOLDIER.actionRadiusSquared;
	public static final int SOLDIER_VISION_RADIUS = RobotType.SOLDIER.visionRadiusSquared;

	// Sage Globals
	public static final int SAGE_ACTION_RADIUS = RobotType.SAGE.actionRadiusSquared;
	public static final int SAGE_VISION_RADIUS = RobotType.SAGE.visionRadiusSquared;


	//// ---------------------Buildings Globals---------------------
	// Archon Globals
	public static final int ARCHON_ACTION_RADIUS = RobotType.ARCHON.actionRadiusSquared;
	public static final int ARCHON_VISION_RADIUS = RobotType.ARCHON.visionRadiusSquared;

	// Laboratory Globals
	// Action Radius Not Applicable
	public static final int LABORATORY_VISION_RADIUS = RobotType.LABORATORY.visionRadiusSquared;

	// Watchtower Globals
	public static final int WATCHTOWER_ACTION_RADIUS = RobotType.WATCHTOWER.actionRadiusSquared;
	public static final int WATCHTOWER_VISION_RADIUS = RobotType.WATCHTOWER.visionRadiusSquared;

    //// ---------------------Vision Globals---------------------
	public static RobotInfo[] visibleEnemies = null;
	public static RobotInfo[] visibleAllies = null;


    // public static void updateRobotInfos() {
		// visibleAllies = rc.senseNearbyRobots(mySensorRadiusSquared, us);
		// visibleEnemies =  rc.senseNearbyRobots(mySensorRadiusSquared, them);
		// visibleHostiles = rc.senseHostileRobots(here, mySensorRadiusSquared);
		// attackableHostiles = rc.senseHostileRobots(here, myAttackRadiusSquared);
	// }

        //// ---------------------Game Constants---------------------
    // Map Constants
    public static final int MAP_MIN_HEIGHT = GameConstants.MAP_MIN_HEIGHT;

    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = GameConstants.MAP_MAX_HEIGHT;

    /** The minimum possible map width. */
    public static final int MAP_MIN_WIDTH = GameConstants.MAP_MIN_WIDTH;

    /** The maximum possible map width. */
    public static final int MAP_MAX_WIDTH = GameConstants.MAP_MAX_WIDTH;

    /** The minimum number of starting Archons per team. */
    public static final int MIN_STARTING_ARCHONS = GameConstants.MIN_STARTING_ARCHONS;

    /** The maximum number of starting Archons per team. */
    public static final int MAX_STARTING_ARCHONS = GameConstants.MAX_STARTING_ARCHONS;

    /** The minimum amount of rubble per square. */
    public static final int MIN_RUBBLE = GameConstants.MIN_RUBBLE;

    /** The maximum amount of rubble per square. */
    public static final int MAX_RUBBLE = GameConstants.MAX_RUBBLE;

    public static int MAP_WIDTH;
    public static int MAP_HEIGHT;
    public static int MAP_SIZE;
    public static boolean isMapSquare = true;
    public static int mapSymmetry = 0; // 0 = vertical symmetry, 1 = horizontal symmetry, 2 = rotational symmetry
    public static int currentLeadReserves;
    // TODO: Flag for shape of map (square, rectangle, very rectangle(thin))

    // Rubble Parameters

    public static float[][] rubbleMap; 
    public static boolean[][] isRubbleLocRead; // Defaults to false
    public static int[][] convolutionKernel;
    public static int RubbleTransmissionIndex;

    // Game Parameters
    
    public static final int INDICATOR_STRING_MAX_LENGTH = GameConstants.INDICATOR_STRING_MAX_LENGTH;

    /** The length of each team's shared communication array. */
    public static final int SHARED_ARRAY_LENGTH = GameConstants.SHARED_ARRAY_LENGTH;

    /** The maximum value in shared communication arrays. */
    public static final int MAX_SHARED_ARRAY_VALUE = Short.MAX_VALUE;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = GameConstants.EXCEPTION_BYTECODE_PENALTY;

    /** The initial amount of lead each team starts with. */
    public static final int INITIAL_LEAD_AMOUNT = GameConstants.INITIAL_LEAD_AMOUNT;

    /** The initial amount of gold each team starts with. */
    public static final int INITIAL_GOLD_AMOUNT = GameConstants.INITIAL_GOLD_AMOUNT;

    /** The amount of lead each team gains per turn. */
    public static final int PASSIVE_LEAD_INCREASE = GameConstants.PASSIVE_LEAD_INCREASE;

    /** The number of rounds between adding lead resources to the map. */
    public static final int ADD_LEAD_EVERY_ROUNDS = GameConstants.ADD_LEAD_EVERY_ROUNDS;

    /** The amount of lead to add each round that lead is added. */
    public static final int ADD_LEAD = GameConstants.ADD_LEAD;

    // COOLDOWN CONSTANTS

    /** If the amount of cooldown is at least this value, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = GameConstants.COOLDOWN_LIMIT;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = GameConstants.COOLDOWNS_PER_TURN;

    /** The number of cooldown turns per transformation. */
    public static final int TRANSFORM_COOLDOWN = GameConstants.TRANSFORM_COOLDOWN;

    /** The number of cooldown turns per mutation. */
    public static final int MUTATE_COOLDOWN = GameConstants.MUTATE_COOLDOWN;

    // GAME MECHANICS CONSTANTS

    /** A blueprint building's health, as a multiplier of max health. */
    public static final float PROTOTYPE_HP_PERCENTAGE = GameConstants.PROTOTYPE_HP_PERCENTAGE;

    /** The multiplier for reclaiming a building's cost. */
    public static final float RECLAIM_COST_MULTIPLIER = GameConstants.RECLAIM_COST_MULTIPLIER;

    /** The maximum level a building can be. */
    public static final int MAX_LEVEL = GameConstants.MAX_LEVEL;

    /** Constants for alchemists converting lead to gold. */
    public static final double ALCHEMIST_LONELINESS_A = GameConstants.ALCHEMIST_LONELINESS_A;
    public static final double ALCHEMIST_LONELINESS_B = GameConstants.ALCHEMIST_LONELINESS_B;
    public static final double ALCHEMIST_LONELINESS_K = GameConstants.ALCHEMIST_LONELINESS_K;

    public static final Direction NORTH = Direction.NORTH;
    public static final Direction NORTHEAST = Direction.NORTHEAST;
    public static final Direction EAST = Direction.EAST;
    public static final Direction SOUTHEAST = Direction.SOUTHEAST;
    public static final Direction SOUTH = Direction.SOUTH;
    public static final Direction SOUTHWEST = Direction.SOUTHWEST;
    public static final Direction WEST = Direction.WEST;
    public static final Direction NORTHWEST = Direction.NORTHWEST;
    public static final Direction CENTER = Direction.CENTER;

    /** Array containing all the possible movement directions. */
    public static final Direction[] directions = {
        NORTH,
        NORTHEAST,
        EAST,
        SOUTHEAST,
        SOUTH,
        SOUTHWEST,
        WEST,
        NORTHWEST,
    };

    public static void initGlobals(RobotController rc1){
        rc = rc1;
        BIRTH_ROUND = rc.getRoundNum();
        turnCount = BIRTH_ROUND;
        UNIT_TYPE = rc.getType();
        MAP_WIDTH = rc.getMapWidth();
        MAP_HEIGHT = rc.getMapHeight();
        MAP_SIZE = MAP_WIDTH * MAP_HEIGHT;
        MY_TEAM = rc.getTeam();
        ENEMY_TEAM = MY_TEAM.opponent();
        archonCount = rc.getArchonCount(); // 20 bytecodes
        ID = rc.getID();
        robotLevel = rc.getLevel();
        myRobotCount = rc.getRobotCount(); // 20 bytecodes
        START_LOCATION = rc.getLocation();
        currentLocation = START_LOCATION;
        botFreeze = false;
        myHealth = rc.getHealth();
        underAttack = false;
        visibleAllies = rc.senseNearbyRobots(-1, MY_TEAM);
        
        if (UNIT_TYPE == RobotType.ARCHON) rememberedArchonLocation = currentLocation;
        for (RobotInfo ally : visibleAllies) {
            if (ally.getType() == RobotType.ARCHON) {
                rememberedArchonLocation = ally.getLocation();
            }
        }
        isMapSquare = (MAP_WIDTH == MAP_HEIGHT);
        // mapSymmetry = (BIRTH_ROUND % 2 == 0) ? 0 : 2;
        // if(mapSymmetry == 1 && !isMapSquare) mapSymmetry = 2; // TODO: Junk code to make bot run, change later
        rememberedEnemyArchonLocation = new MapLocation[4];
        updateEnemyArchonLocation();
        if(BIRTH_ROUND % 2 == 0) {
            currentDestination = ratioPointBetweenTwoMapLocations(rememberedArchonLocation, rememberedEnemyArchonLocation[0], 0.3);
        } else {
            currentDestination = ratioPointBetweenTwoMapLocations(rememberedArchonLocation, rememberedEnemyArchonLocation[1], 0.3);
        }
    }

    public static void updateGlobals() {
        // TODO: Check if unit moved last turn
		currentLocation = rc.getLocation();
        
        int curRound = rc.getRoundNum();
        //TODO : Reduce computation if bot is freezing
        if (curRound != turnCount + 1 && curRound != BIRTH_ROUND) {
            botFreeze = true;
            System.out.println("Birth round " + BIRTH_ROUND + " Current Round "+ curRound + " Turn Count " + turnCount + " Bot Froze"); 
        }
        turnCount = curRound;

        //TODO: Warn if unit is under attack and cannot see enemy
        int curHealth = rc.getHealth();
        if (curHealth < myHealth) underAttack = true;
        myHealth = curHealth;

        currentLeadReserves = rc.getTeamLeadAmount(MY_TEAM);
	}

    // Heavy bytecode method
    public static void updateSurroundings() {
        visibleEnemies = rc.senseNearbyRobots(-1, ENEMY_TEAM); // 100 bytecodes
        visibleAllies = rc.senseNearbyRobots(-1, MY_TEAM); // 100 bytecodes
    }

    public static void updateEnemyArchonLocation() {
        rememberedEnemyArchonLocation[0] = new MapLocation(MAP_WIDTH - rememberedArchonLocation.x, rememberedArchonLocation.y);
        rememberedEnemyArchonLocation[1] = new MapLocation(rememberedArchonLocation.x, MAP_HEIGHT - rememberedArchonLocation.y);
        rememberedEnemyArchonLocation[2] = new MapLocation(MAP_WIDTH - rememberedArchonLocation.x, MAP_HEIGHT - rememberedArchonLocation.y);
    }

    public static MapLocation ratioPointBetweenTwoMapLocations(MapLocation src, MapLocation dest, double ratio){
        int x1 = src.x;
        int y1 = src.y;
        int x2 = dest.x;
        int y2 = dest.y;
        int x = (int)((double) ratio * x1 + (1-ratio) * x2);
        int y = (int)((double) ratio * y1 + (1-ratio) * y2);
        return new MapLocation(x, y);
    }
}