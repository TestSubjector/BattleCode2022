package AGrindstoneBot;

import battlecode.common.*;

public class BugNav extends Util {

    private static final int ACCEPTABLE_RUBBLE = 50;

    private static Direction bugDirection = null;

    static Direction walkTowards(MapLocation target) throws GameActionException {
        bugDirection = null;
        if (!rc.isMovementReady()) {
            return null;
        }

        MapLocation currentLocation = rc.getLocation();
        if (currentLocation.equals(target)) {
            return null;
        }

        Direction d = currentLocation.directionTo(target);
        Direction result = null;
        if (rc.canMove(d) && !isObstacle(rc, d)) {
            rc.move(d);
            bugDirection = null;
        } else {
            if (bugDirection == null) {
                bugDirection = d;
            }
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(bugDirection) && !isObstacle(rc, bugDirection)) {
                    // rc.move(bugDirection);
                    result = bugDirection;
                    bugDirection = bugDirection.rotateLeft();
                    break;
                } else {
                    bugDirection = bugDirection.rotateRight();
                }
            }
        }
        return result;
    }

    /**
     * Checks if the square we reach by moving in direction d is an obstacle.
     */
    private static boolean isObstacle(RobotController rc, Direction d) throws GameActionException {
        MapLocation adjacentLocation = rc.getLocation().add(d);
        int rubbleOnLocation = rc.senseRubble(adjacentLocation);
        return rubbleOnLocation > ACCEPTABLE_RUBBLE;
    }
}