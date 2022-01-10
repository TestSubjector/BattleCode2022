package ASprintAndAfterBot;

import battlecode.common.*;

public class RubbleMap extends Util {

    public static void initRubbleMap() {
        // rubbleMap = new float[MAP_WIDTH][MAP_HEIGHT];
        // isRubbleLocRead = new boolean[MAP_WIDTH][MAP_HEIGHT];
        // convolutionKernel = new int[3][3];
        // RubbleTransmissionIndex = -1;

        // for (int i = 0; i < 3; ++i)
        //     for (int j = 0; j < 3; ++j)
        //         convolutionKernel[i][j] = 1;
    }

    public static int computeGradedRubbleValue(int rubbleValue) throws GameActionException {
        return (int) (((float) rubbleValue / (MAX_RUBBLE - MIN_RUBBLE)) * 7.0f);
    }

    // 600 bytecodes
    public static int computeRubbleValue(MapLocation loc) throws GameActionException {
        MapLocation[] adjacentLocations = rc.getAllLocationsWithinRadiusSquared(loc, 1);
        int rubbleValue = 0, locationCount = 0, rubbleValueSum = 0;
        for (MapLocation adjLoc : adjacentLocations) {
            if (rc.canSenseLocation(adjLoc)) {
                int adjX = adjLoc.x;
                int adjY = adjLoc.y;
                int i = adjX - loc.x + 1;
                int j = adjY - loc.y + 1;
                rubbleValue = rc.senseRubble(adjLoc);
                rubbleMap[adjX][adjY] = rubbleValue / (MAX_RUBBLE - MIN_RUBBLE) * 7.0f;
                isRubbleLocRead[adjX][adjY] = true;
                rubbleValueSum += rubbleValue * convolutionKernel[i][j];
                locationCount += convolutionKernel[i][j];
            }
        }
        return computeGradedRubbleValue(rubbleValueSum / locationCount);
    }

    public static void updateRubbleMapByReadValue(int readValue) throws GameActionException {
        if ((readValue >> 15) == flipTurnFlag()) {
            MapLocation loc = Comms.getLocFromRubbleMessage(readValue);
            if (rc.canSenseLocation(loc))
                return;
            int x = loc.x, y = loc.y;

            if (isRubbleLocRead[x][y])
                return;
            else
                isRubbleLocRead[x][y] = true;

            int rubbleLayer = Comms.getRubbleValueFromRubbleMessage(readValue);
            // System.out.println("Reading rubble layer: " + rubbleLayer);
            // if (!isValidMapLocation(x+dx,y+dy)) continue; // RubbleMapFormation Line 4
            // removes the need for this check
            // System.out.println("1: " + Clock.getBytecodesLeft());
            updateRubbleMapLayer(rubbleLayer, x, y); // Averages out the map, will always lead to better information of
                                                     // the map
            // System.out.println("2: " + Clock.getBytecodesLeft());
        }
    }

    public static void updateRubbleMapLayer(int rubbleLayer, int x, int y) throws GameActionException {
        int a = x - 1, b = x + 1, c = y - 1, d = y + 1;
        rubbleMap[x][y] = (rubbleMap[x][y] + rubbleLayer) / 2;
        rubbleMap[x][c] = (rubbleMap[x][c] + rubbleLayer) / 2;
        rubbleMap[x][d] = (rubbleMap[x][d] + rubbleLayer) / 2;
        rubbleMap[a][c] = (rubbleMap[a][c] + rubbleLayer) / 2;
        rubbleMap[a][y] = (rubbleMap[a][y] + rubbleLayer) / 2;
        rubbleMap[a][d] = (rubbleMap[a][d] + rubbleLayer) / 2;
        rubbleMap[b][c] = (rubbleMap[b][c] + rubbleLayer) / 2;
        rubbleMap[b][y] = (rubbleMap[b][y] + rubbleLayer) / 2;
        rubbleMap[b][d] = (rubbleMap[b][d] + rubbleLayer) / 2;
    }

    public static void updateRubbleMap() throws GameActionException {
        for (int i = 0; i < Comms.CHANNEL_RUBBLE_STOP; ++i) {
            if (Clock.getBytecodesLeft() < 400) {
                // System.out.println("Hitting bytecode barrier");
                return;
            }
            int curVal = rc.readSharedArray(i);
            if (curVal == 0)
                continue;
            updateRubbleMapByReadValue(curVal);
        }
    }

    public static void rubbleMapFormation(RobotController rc) throws GameActionException {
        int transmitterCount = rc.readSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT);
        // System.out.println(RubbleTransmitterCount);
        RubbleTransmissionIndex = transmitterCount++ % Comms.CHANNEL_RUBBLE_STOP;
        rc.writeSharedArray(Comms.CHANNEL_TRANSMITTER_COUNT, transmitterCount);

        // if (isOnEdge(currentLocation))
            // return; // Do not send information when on edge of Map
        // System.out.println("1: " + String.valueOf(Clock.getBytecodesLeft()));
        // int rubbleValue = RubbleMap.computeRubbleValue(currentLocation);
        // System.out.println("2: " + String.valueOf(Clock.getBytecodesLeft()));
        // int turnFlag = (turnCount % 2);
        // TODO: If rubbleValue == 0, then some other bot's vision's rubble info should
        // be used
        // int intMapLoc = intFromMapLocation(currentLocation);
        // rc.writeSharedArray(RubbleTransmissionIndex, Comms.createRubbleMessage(intMapLoc, rubbleValue, turnFlag));
        // if (turnCount == 500 && UNIT_TYPE == RobotType.MINER) {
        //     System.out.println("Printing rubbleMap:");
        //     System.out.println(Arrays.deepToString(rubbleMap));
        // }
    }
}
