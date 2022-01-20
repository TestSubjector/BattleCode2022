package OLabBot;

import battlecode.common.*;

// import java.util.PriorityQueue;

// TODO: Try out Beam Search
// TODO: Switch Cases for A*?????

public class PathFinder extends Util {
        // Currently searches for the adjacent location with least rubble acc. to senseRubble
    public static MapLocation findOptimumAdjacentLocation(MapLocation dest) throws GameActionException{
        int x = dest.x, y = dest.y;
        float minVal = 10;
        int minX = -1, minY = -1;
        MapLocation adjacentLocation = null;

        for (int dx = -2; dx++ < 1;){
            for (int dy = -2; dy++ < 1;){
                int curX = x + dx, curY = y + dy;
                adjacentLocation = new MapLocation(curX, curY);
                if (!rc.canSenseLocation(adjacentLocation)) continue;
                float curRubble = rc.senseRubble(adjacentLocation);
                if (curRubble < minVal){
                    minVal = curRubble;
                    minX = curX;
                    minY = curY;
                }
            }
        }
        if (minVal != 10)
            return new MapLocation(minX, minY);
        else return null;
    }
}