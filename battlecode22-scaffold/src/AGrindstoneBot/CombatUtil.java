package AGrindstoneBot;

import battlecode.common.*;

public class CombatUtil extends Util{

    public static boolean isMilitaryUnit(RobotType type){
        switch (type){
            case SOLDIER:
            case WATCHTOWER:
            case ARCHON:
            case SAGE:
                return true;
            default:
                return false;
        }
    }

    public static int militaryCount(RobotInfo[] visibleRobots){
        int count = 0;
        for (int i = visibleRobots.length; --i >= 0;) {
            if (isMilitaryUnit(visibleRobots[i].type)) count++;
        }
        return count;
    }

    public static RobotInfo getClosestUnitWithCombatPriority(RobotInfo[] units) {
		RobotInfo closestUnit = null;
        RobotInfo closestCombatUnit = null;
		int minDistSq = Integer.MAX_VALUE;
        int minCombatDistSq = Integer.MAX_VALUE;
        int distSq = 0;
        MapLocation lCR = currentLocation;
		for (int i = units.length; --i >= 0; ) {
			distSq = lCR.distanceSquaredTo(units[i].location);
			if (distSq < minDistSq) {
				minDistSq = distSq;
				closestUnit = units[i];
			}
            if (isMilitaryUnit(units[i].getType()) && distSq < minCombatDistSq) {
				minCombatDistSq = distSq;
				closestCombatUnit = units[i];
			}
		}
        if (closestCombatUnit != null) return closestCombatUnit;
		else return closestUnit;
	}

}
