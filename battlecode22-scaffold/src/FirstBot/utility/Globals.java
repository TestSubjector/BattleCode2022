package utility;

import examplefuncsplayer.RobotPlayer;

public class Globals {

    // public static RobotController rc;
	// public static MapLocation here;
	// public static Team us;
	// public static Team them;
	// public static int myID;
	// public static RobotType myType;
    public static int roundNum;


	//// ---------------------Droids Globals---------------------
	// Miner Globals
	public static int MinerActionRadius = 2;
	public static int MinerVisionRadius = 20;

	// Builder Globals
	public static int BuilderActionRadius = 5;
	public static int BuilderVisionRadius = 20;

	// Soldier Globals
	public static int SoldierActionRadius = 13;
	public static int SoldierVisionRadius = 20;

	// Sage Globals
	public static int SageActionRadius = 13;
	public static int SageVisionRadius = 20;


	//// ---------------------Buildings Globals---------------------
	// Archon Globals
	public static int ArchonActionRadius = 20;
	public static int ArchonVisionRadius = 34;

	// Laboratory Globals
	// Action Radius Not Applicable
	public static int LaboratoryVisionRadius = 53;

	// Watchtower Globals
	public static int WatchtowerActionRadius = 20;
	public static int WatchtowerVisionRadius = 34;
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