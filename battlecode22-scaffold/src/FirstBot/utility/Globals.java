package utility;

import battlecode.common.*;

public class Globals {

    public static RobotController rc;
    public static int turnCount = rc.getRoundNum();
    public static RobotType unitType = rc.getType();
    // public static MapLocation here;
	// public static Team us;
	// public static Team them;
	// public static int myID;
	// public static RobotType myType;
    // public static int temp = 
   


	//// ---------------------Droids Globals---------------------
	// Miner Globals
	public static int minerActionRadius = RobotType.MINER.actionRadiusSquared;
	public static int minerVisionRadius = RobotType.MINER.visionRadiusSquared;

	// Builder Globals
	public static int builderActionRadius = RobotType.BUILDER.actionRadiusSquared;
	public static int builderVisionRadius = RobotType.BUILDER.visionRadiusSquared;

	// Soldier Globals
	public static int soldierActionRadius = RobotType.SOLDIER.actionRadiusSquared;
	public static int soldierVisionRadius = RobotType.SOLDIER.visionRadiusSquared;

	// Sage Globals
	public static int sageActionRadius = RobotType.SAGE.actionRadiusSquared;
	public static int sageVisionRadius = RobotType.SAGE.visionRadiusSquared;


	//// ---------------------Buildings Globals---------------------
	// Archon Globals
	public static int archonActionRadius = RobotType.ARCHON.actionRadiusSquared;
	public static int archonVisionRadius = RobotType.ARCHON.visionRadiusSquared;

	// Laboratory Globals
	// Action Radius Not Applicable
	public static int laboratoryVisionRadius = RobotType.LABORATORY.visionRadiusSquared;

	// Watchtower Globals
	public static int watchtowerActionRadius = RobotType.WATCHTOWER.actionRadiusSquared;
	public static int watchtowerVisionRadius = RobotType.WATCHTOWER.visionRadiusSquared;
    // public static RobotInfo[] visibleHostiles = null;
	// public static RobotInfo[] visibleEnemies = null;
	// public static RobotInfo[] visibleZombies = null;
	// public static RobotInfo[] visibleAllies = null;
	// public static RobotInfo[] attackableHostiles = null;

    // public static void init(RobotController theRC) {
	// 	rc = theRC;
	// 	us = rc.getTeam();
	// 	them = us.opponent();
	// 	myID = rc.getID();
	// 	myType = rc.getType();
	// 	here = rc.getLocation();
	// }

    // public static void update() {
	// 	here = rc.getLocation();
	// 	roundNum = rc.getRoundNum();
	// }

    // public static void updateRobotInfos() {
		// visibleAllies = rc.senseNearbyRobots(mySensorRadiusSquared, us);
		// visibleEnemies =  rc.senseNearbyRobots(mySensorRadiusSquared, them);
		// visibleHostiles = rc.senseHostileRobots(here, mySensorRadiusSquared);
		// attackableHostiles = rc.senseHostileRobots(here, myAttackRadiusSquared);
	// }
}