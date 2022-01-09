package OThirdBot;

import battlecode.common.*;

public class RubbleMap extends Util{
    public static int computeGradedRubbleValue(int rubbleValue) throws GameActionException{
        // TODO: Extra case required?
        float temp = ((float)rubbleValue/(MAX_RUBBLE - MIN_RUBBLE)) * 7.0f;
        return (int)(temp);
    }

    // 600 bytecodes
    public static int computeRubbleValue(MapLocation loc) throws GameActionException{
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, 1);
        int rubbleValue = 0, locationCount = 0, rubbleValueSum = 0;
        for(MapLocation adjLoc : adjacentLocations){
            if (rc.canSenseLocation(adjLoc)){
                int adjX = adjLoc.x;
                int adjY = adjLoc.y; 
                int i = adjX - loc.x + 1;
                int j = adjY - loc.y + 1;
                rubbleValue = rc.senseRubble(adjLoc);
                rubbleMap[adjX][adjY] = rubbleValue/(MAX_RUBBLE - MIN_RUBBLE) * 7.0f;
                isRubbleLocRead[adjX][adjY] = true;   
                rubbleValueSum += rubbleValue * convolutionKernel[i][j];
                locationCount += convolutionKernel[i][j];
            }
        }
        return computeGradedRubbleValue(rubbleValueSum/locationCount);
    }

    public static void updateRubbleMapByReadValue(int readValue) throws GameActionException{
        if ((readValue>>15) == flipTurnFlag()){
            MapLocation loc = Comms.getLocFromRubbleMessage(readValue);
            if(rc.canSenseLocation(loc)) return;
            int x = loc.x, y = loc.y;
            
            if(isRubbleLocRead[x][y]) return;
                else isRubbleLocRead[x][y] = true;
            
            int rubbleLayer = Comms.getRubbleValueFromRubbleMessage(readValue);
            // if (!isValidMapLocation(x+dx,y+dy)) continue; // RubbleMapFormation Line 4 removes the need for this check
            // System.out.println("1: " + Clock.getBytecodesLeft());
            updateRubbleMapLayer(rubbleLayer, x, y); // Averages out the map, will always lead to better information of the map
            // System.out.println("2: " + Clock.getBytecodesLeft());
        }
    }

    public static void updateRubbleMapLayer(int rubbleLayer, int x, int y) throws GameActionException{
        int a = x-1, b = x+1, c = y-1, d = y+1;
        rubbleMap[x][y] = (rubbleMap[x][y] + rubbleLayer) / 2.0f;
        rubbleMap[x][c] = (rubbleMap[x][c] + rubbleLayer) / 2.0f;
        rubbleMap[x][d] = (rubbleMap[x][d] + rubbleLayer) / 2.0f;
        rubbleMap[a][c] = (rubbleMap[a][c] + rubbleLayer) / 2.0f;
        rubbleMap[a][y] = (rubbleMap[a][y] + rubbleLayer) / 2.0f;
        rubbleMap[a][d] = (rubbleMap[a][d] + rubbleLayer) / 2.0f;
        rubbleMap[b][c] = (rubbleMap[b][c] + rubbleLayer) / 2.0f;
        rubbleMap[b][y] = (rubbleMap[b][y] + rubbleLayer) / 2.0f;
        rubbleMap[b][d] = (rubbleMap[b][d] + rubbleLayer) / 2.0f;
    }

    public static void updateRubbleMap() throws GameActionException{
        for (int i = 0; i < Comms.CHANNEL_RUBBLE_STOP; ++i){
            int curVal = rc.readSharedArray(i);
            if (curVal == 0 || Clock.getBytecodesLeft() < 400) continue;
            updateRubbleMapByReadValue(curVal);
        }
    }
}
