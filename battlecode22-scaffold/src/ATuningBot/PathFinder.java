package ATuningBot;

import battlecode.common.*;

// import java.util.PriorityQueue;

// TODO: Try out Beam Search
// TODO: Switch Cases for A*?????

public class PathFinder extends Util {

    // private static Heap openList, closedList;
    private static boolean[][] visited;
    private static int[][] parent;
    private static int maxSquaredDist = 100;
    // private static PriorityQueue<node> pq;


    private static void initPathFinder(){
        Heap.initHeap();
        // closedList.initHeap();
        // System.out.println("3: " + Clock.getBytecodesLeft() + " TurnCount: " + rc.getRoundNum());
        // System.out.println()
        visited = new boolean[MAP_WIDTH][MAP_HEIGHT];
        // pq = new PriorityQueue<node>();
        // System.out.println("4: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
        // parent = new int[MAP_WIDTH][MAP_HEIGHT];
        
    }

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


    // Currently searches for the adjacent location with least rubble acc. to RubbleMap
    // public static MapLocation findOptimumAdjacentLocation(MapLocation dest){
    //     int x = dest.x, y = dest.y;
    //     float minVal = 10;
    //     int minX = -1, minY = -1;
    //     for (int dx = -2; dx++ < 1;){
    //         for (int dy = -2; dy++ < 1;){
    //             int curX = x + dx, curY = y + dy;
    //             if (!isValidMapLocation(curX, curY)) continue;
    //             float curRubble = rubbleMap[curX][curY];
    //             if (curRubble < minVal){
    //                 minVal = curRubble;
    //                 minX = curX;
    //                 minY = curY;
    //             }
    //         }
    //     }
    //     if (minVal != 10)
    //         return new MapLocation(minX, minY);
    //     else return null;
    // }

    // public static MapLocation findAnAdjacentLocation(MapLocation dest){
    //     int x = dest.x, y = dest.y;
    //     float minVal = 10;
    //     int minX = -1, minY = -1;
    //     for (int dx = -2; dx++ < 1;){
    //         for (int dy = -2; dy++ < 1;){
    //             int curX = x + dx, curY = y + dy;
    //             if (!isValidMapLocation(curX, curY)) continue;
    //             float curRubble = rubbleMap[curX][curY];
    //             if (curRubble < minVal){
    //                 minVal = curRubble;
    //                 minX = curX;
    //                 minY = curY;
    //             }
    //         }
    //     }
    //     if (minVal != 10)
    //         return new MapLocation(minX, minY);
    //     else return null;
    // }

    private static Direction traceback(MapLocation src, MapLocation dest){
        int srcInt = intFromMapLocation(src), curInt = intFromMapLocation(dest);
        int nextInt = parent[(curInt >> 6)][(curInt & 0x3F)];
        // int nextX = parents[(curInt >> 6)], nextY = (curInt & 0x3F);
        int count = 0;
        while (nextInt != srcInt){
            curInt = nextInt;
            nextInt = parent[(curInt >> 6)][(curInt & 0x3F)];
            count++;
            if (count > Heap.length){
                System.out.println("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
                break;
            }
        }
        // if (nextInt == srcInt){
        //     System.out.println("YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
        // }
        MapLocation loc = mapLocationFromInt(curInt);
        return src.directionTo(loc);
    }

    
    private static Direction traceback(MapLocation src, node dest){
        int count = 0;
        while(dest.shortestPathParent.loc != src){
            dest = dest.shortestPathParent;
            count++;
            if (count > Heap.length){
                System.out.println("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
                break;
            }
        }
        // if (dest.shortestPathParent.loc == src){
        //     System.out.println("YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
        // }
        return src.directionTo(dest.loc);
    }


    public static Direction findPathAStar(MapLocation src, MapLocation dest){
        int mDest = src.distanceSquaredTo(dest);
        if (mDest > maxSquaredDist){
            // System.out.println("src: " + src + "; original dest: " + dest);
            int newDest = findPointByRatio(src, dest, maxSquaredDist);
            dest = mapLocationFromInt(newDest);
            // System.out.println("; new dest: " + dest);
        }

        // System.out.println("1: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
        // System.out.println("1: " + turnCount);
        initPathFinder();
        // System.out.println("2: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
        // System.out.println("2: " + turnCount);


        node minNode = Heap.insertIntoHeap(src, manhattanDistance(src, dest), 0);
        
        // pq.add(new node(src, -1*manhattanDistance(src, dest), 0));
        // node minNode = null;

        int itercount = 0;


        // while(pq.size() != 0){
        while(!Heap.isEmpty()){
            // System.out.println("1: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
            itercount++;
            
            minNode = Heap.extractMin();

            // minNode = pq.poll();
            



            // System.out.println("minNode: " + minNode);
            // closedList.insertIntoHeap(minNode);
            MapLocation loc = minNode.loc;
            if (loc.equals(dest))
                break;
            
            int x = loc.x, y = loc.y;
            visited[x][y] = true;
            for (int dx = -1; dx <= 1; dx++){
                for (int dy = -1; dy <= 1; dy++){
                    int curX = dx + x, curY = dy + y;
                    if (isValidMapLocation(curX, curY) && (!visited[curX][curY])){
                        MapLocation curLoc = mapLocationFromCoords(curX, curY);
                        int mDist = manhattanDistance(curLoc, dest);//, curInt = intFromMapLocation(curX, curY);
                        System.out.println("3: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
                        node curNode = Heap.insertIntoHeap(curLoc, mDist + minNode.distance + 1, minNode.distance + 1);
                        // System.out.println(Heap.minHeap);
                        // Heap.printHeap();
                        
                        // node curNode = new node(curLoc, -mDist + minNode.distance - 1, minNode.distance - 1); 
                        // pq.add(curNode);
                        
                        System.out.println("4: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
                        // parent[curX][curY] = intFromMapLocation(x, y);
                        if (curNode != null)
                            curNode.shortestPathParent = minNode;
                        else{
                            break;
                        }
                    }
                }
            }
            // System.out.println("2: " + Clock.getBytecodesLeft()+ " TurnCount: " + rc.getRoundNum());
        }
        Direction dir = null;
        // Direction dir = traceback(src, dest);
        if (minNode == null){
            System.out.println("PROBLEM!!!!!!!!!!!!!!!!!!!!");
        }
        else
            dir = traceback(src, minNode);
        System.out.println("itercount: " + itercount + "; direction received: " + dir);
        return dir;
    }
    
}