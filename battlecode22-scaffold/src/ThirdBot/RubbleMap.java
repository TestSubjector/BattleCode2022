package ThirdBot;

import battlecode.common.*;

public class RubbleMap extends Util{
    public static int computeGradedRubbleValue(int rubbleValue) throws GameActionException{
        // TODO: Extra case required?
        float temp = ((float)rubbleValue/(MAX_RUBBLE - MIN_RUBBLE)) * 7.0f;
        return (int)(temp);
    }

    public static int computeRubbleValue(MapLocation loc) throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, 1);
        int rubbleValue = 0, locationCount = 0;
        // rubbleValue = rc.senseRubble(loc);
        for(MapLocation adjLoc : adjacentLocations){
            if (rc.canSenseLocation(adjLoc)){
                int i = adjLoc.x - loc.x + 1;
                int j = adjLoc.y - loc.y + 1;
                rubbleValue += rc.senseRubble(adjLoc) * convolutionKernel[i][j];
                locationCount += convolutionKernel[i][j];
            }
        }
        return computeGradedRubbleValue(rubbleValue/locationCount);
    }

    public static void updateRubbleMapByReadValue(int readValue) throws GameActionException{
        if ((readValue>>15) == flipTurnFlag()){
            MapLocation loc = Comms.getLocFromRubbleMessage(readValue);
            int x = loc.x, y = loc.y;

            rubbleMap[x][y] = Comms.getRubbleValueFromRubbleMessage(readValue);
            for (int dx = -1; dx <= 1; ++dx){
                for (int dy = -1; dy <= 1; ++dy){
                    if (!isValidMapLocation(x+dx,y+dy)) continue;
                    // TODO: Add comment
                    rubbleMap[x+dx][y + dy] = (rubbleMap[x+dx][y + dy] + rubbleMap[x][y]) / 2.0f;
                }
            }
        }
    }

    public static void updateRubbleMap() throws GameActionException{
        for (int i = 0; i < SHARED_ARRAY_LENGTH; ++i){
            int curVal = rc.readSharedArray(i);
            if (curVal == 0) continue;
            updateRubbleMapByReadValue(curVal);
        }
    }
}
