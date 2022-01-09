package ASoldierBot;

import battlecode.common.*;

public class BotSoldier extends Util{

    private static RobotInfo[] visibleEnemies;
    private static RobotInfo[] inRangeEnemies;
    private static MapLocation attackTarget;

    public static void initBotSoldier(){
        if (isRubbleMapEnabled) RubbleMap.initRubbleMap();
    }

    public static void soldierComms() throws GameActionException {
        Comms.updateArchonLocations();
        Comms.updateChannelValueBy1(Comms.CHANNEL_SOLDIER_COUNT);
        Comms.channelArchonStop = Comms.CHANNEL_ARCHON_START + 4*archonCount;
        Comms.commChannelStart = Comms.channelArchonStop; 
    }

    public static void updateVision() throws GameActionException {
        visibleEnemies = rc.senseNearbyRobots(SOLDIER_VISION_RADIUS, ENEMY_TEAM);
        inRangeEnemies = rc.senseNearbyRobots(SOLDIER_ACTION_RADIUS, ENEMY_TEAM);
    }

    private static void detectIfAttackTargetIsGone() throws GameActionException {
		if (attackTarget != null) {
			if (currentLocation.distanceSquaredTo(attackTarget) <= 10) {
			    RobotInfo targetInfo = rc.senseRobotAtLocation(attackTarget);
				if (targetInfo == null || targetInfo.team == MY_TEAM) {
					attackTarget = null;
				}
			}
		}
	}

    // TODO: Sense rubble at their location and factor that in to find more dangerous unit
    private static double getEnemyScore(RobotType type, int health) {
		switch(type) {
		case ARCHON:
			return 0.0001;
		case LABORATORY:
			return 0.00001;
		case MINER:
			return 0.1; // Low priority
		case WATCHTOWER:
			return 0.5 * RobotType.WATCHTOWER.damage / (health); // * RobotType.WATCHTOWER attack cooldown;
		case SOLDIER:
			return RobotType.SOLDIER.damage / (health); //  SOLDIER attack cooldown;
		case SAGE:
			return 10 / (health * 1);
		default:
			return type.damage / (health); // Cooldown due to rubble ;
		}
	}

    private static void chooseTargetAndAttack(RobotInfo[] targets) throws GameActionException {
		RobotInfo bestTarget = null;
		double bestValue = -1;
        double value = 0;
		for (RobotInfo target : targets) {
			value = getEnemyScore(target.getType(), target.getHealth());
			if (value > bestValue) {
				bestValue = value;
				bestTarget = target;
			}
		}
		if (bestTarget != null) {
			rc.attack(bestTarget.location);
		}
	}

    private static void tryToMicro() throws GameActionException {
        if (visibleEnemies.length == 0) { // TODO: Either wait or get out of any possible Watchtower range. Skip if charging
            return;
        }

        if (rc.isActionReady() && rc.isMovementReady()){
            if (inRangeEnemies.length > 0) {
                chooseTargetAndAttack(inRangeEnemies);
            }
        }
    }

    // Try to attack someone - 114 bytecodes
    public static void attackRandomly() throws GameActionException {
        if (inRangeEnemies.length > 0) {
            MapLocation toAttack = inRangeEnemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
    }
    

    /**
    * Run a single turn for a Soldier.
    * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
    */
    static void runSoldier(RobotController rc) throws GameActionException {
        soldierComms(); // 300 Bytecodes
        
        updateVision();
        detectIfAttackTargetIsGone();
        tryToMicro();
        // TODO: Turret Comms code

        Movement.goToDirect(Globals.currentDestination); // 370 Bytecodes

        if (isRubbleMapEnabled && turnCount != BIRTH_ROUND){
            RubbleMap.rubbleMapFormation(rc);
            // RubbleMap.updateRubbleMap();
        }
    }
}
