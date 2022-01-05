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
            int rubbleLayer = Comms.getRubbleValueFromRubbleMessage(readValue);
            // if (!isValidMapLocation(x+dx,y+dy)) continue; // RubbleMapFormation Line 4 removes the need for this check
            // Averages out the map, will always lead to better information of the map
            // System.out.println("1: " + Clock.getBytecodesLeft());
            updateRubbleMapLayer(rubbleLayer, x, y);
            // System.out.println("2: " + Clock.getBytecodesLeft());
        }
    }

    public static void updateRubbleMapLayer(int rubbleLayer, int x, int y) throws GameActionException{
        int a = x-1, b = x+1, c = y-1, d = y+1;
        rubbleMap[x][y] = (rubbleMap[x][y] + rubbleLayer) / 2.0f;
        rubbleMap[a][c] = (rubbleMap[a][c] + rubbleLayer) / 2.0f;
        rubbleMap[a][y] = (rubbleMap[a][y] + rubbleLayer) / 2.0f;
        rubbleMap[a][d] = (rubbleMap[a][d] + rubbleLayer) / 2.0f;
        rubbleMap[x][c] = (rubbleMap[x][c] + rubbleLayer) / 2.0f;
        rubbleMap[x][d] = (rubbleMap[x][d] + rubbleLayer) / 2.0f;
        rubbleMap[b][c] = (rubbleMap[b][c] + rubbleLayer) / 2.0f;
        rubbleMap[b][y] = (rubbleMap[b][y] + rubbleLayer) / 2.0f;
        rubbleMap[b][d] = (rubbleMap[b][d] + rubbleLayer) / 2.0f;
    }

    public static void updateRubbleMap() throws GameActionException{
        for (int i = 0; i < SHARED_ARRAY_LENGTH; ++i){
            int curVal = rc.readSharedArray(i);
            if (curVal == 0) continue;
            updateRubbleMapByReadValue(curVal);
        }
    }
}
