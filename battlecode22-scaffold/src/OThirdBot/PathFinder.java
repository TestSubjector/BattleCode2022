package OThirdBot;

import battlecode.common.*;

public class PathFinder extends BotMiner{

    // TODO: Hyperparameter, needs to be tuned
    public static final int numberIterations = 10;
    public static BFPair[][] paths;
    public static boolean updateFlag;

    public static void initPathFinder(){
        paths = new BFPair[MAP_WIDTH][MAP_HEIGHT];
        for (int i = 0; i < MAP_WIDTH; ++i)
            for(int j = 0; j < MAP_HEIGHT; ++j)
                paths[i][j] = new BFPair();
    }

    public static void reinitPathFinder(){
        for(int i = 0; i < MAP_WIDTH; ++i)
            for (int j = 0; j < MAP_HEIGHT; ++j){
                paths[i][j].smallestCost = -1.0f;
                paths[i][j].path = CENTER;
            }
    }


    public static Direction linearIteration(MapLocation src, MapLocation dest){
        for (int i = 0; i < MAP_WIDTH; ++i){
            for (int j = 0; j < MAP_HEIGHT; ++j){
                for(Direction dir : directions){
                    if (paths[i][j].smallestCost == -1.0f)
                        continue;
                    MapLocation current = mapLocationFromCoords(i, j);
                    try{
                        MapLocation adjacent = current.add(dir);
                        float newCost = paths[i][j].smallestCost + rubbleMap[adjacent.x][adjacent.y];
                        if (paths[adjacent.x][adjacent.y].smallestCost == -1.0f){
                            paths[adjacent.x][adjacent.y].smallestCost = newCost;
                            paths[adjacent.x][adjacent.y].path = dir.opposite();
                            updateFlag = true;
                            
                        }
                        else if (paths[adjacent.x][adjacent.y].smallestCost > newCost){
                            paths[adjacent.x][adjacent.y].smallestCost = newCost;
                            paths[adjacent.x][adjacent.y].path = dir.opposite();
                            updateFlag = true;
                        }
                        if (paths[src.x][src.y].path != CENTER)
                            return paths[src.x][src.y].path;

                    } catch (Exception e){
                        continue;
                    }
                }
            }
        }
        return CENTER;
    }


    // public static Direction spiralIteration(MapLocation src, MapLocation dest){
    //     int i = dest.x, j = dest.y;
    //     int radius = 1;
    //     // maxradiux is the max(dest.x, dest.y, MAP_HEIGHT - dest.y, MAP_WIDTH - dest.x)
    //     int maxradius = StrictMath.max(dest.x, StrictMath.max(dest.y, StrictMath.max(MAP_HEIGHT - dest.y, MAP_WIDTH - dest.x)));
    //     System.out.println("1: " + String.valueOf(Clock.getBytecodeNum()));
    //     System.out.println("maxradius: " + maxradius);
    //     MapLocation current;
    //     while (radius <= maxradius){
    //         i = i - 1;
    //         System.out.println("infinite loop?");

    //         while (dest.y - j < radius){
    //             current = mapLocationFromCoords(i, j);
    //             if (!isValidMapLocation(current)) break;
    //             relaxTile(current);
    //             --j;
    //         }
    //         j = dest.y - radius;
    //         while(i - dest.x < radius){
    //             current = mapLocationFromCoords(i, j);
    //             if (!isValidMapLocation(current)) break;
    //             relaxTile(current);
    //             ++i;
    //         }
    //         i = radius + dest.x;
    //         while(j - dest.y < radius){
    //             current = mapLocationFromCoords(i, j);
    //             if (!isValidMapLocation(current)) break;
    //             relaxTile(current);
    //             ++j;
    //         }
    //         j = radius + dest.y;
    //         while(dest.x - i < radius){
    //             current = mapLocationFromCoords(i, j);
    //             if (!isValidMapLocation(current)) break;
    //             relaxTile(current);
    //             --i;
    //         }
    //         i = dest.x - radius;
    //         current = mapLocationFromCoords(i, j);
    //         if (isValidMapLocation(current))
    //             relaxTile(current);
    //         radius++;
    //         System.out.println("1b: " + String.valueOf(Clock.getBytecodeNum()));
    //     }
    //     System.out.println("2: " + String.valueOf(Clock.getBytecodeNum()));
    //     // if (paths[src.x][src.y].path != CENTER)
    //     return paths[src.x][src.y].path;
    // }


    public static Direction findPath(MapLocation src, MapLocation dest){
        
        if (paths[dest.x][dest.y].path == CENTER && paths[src.x][src.y].path != CENTER)
            return (paths[src.x][src.y].path);
        
        if (paths[dest.x][dest.y].path != CENTER)
            reinitPathFinder();
        
        paths[dest.x][dest.y].smallestCost = rubbleMap[dest.x][dest.y];

        for (int iter = 0; iter < numberIterations; ++iter){
            updateFlag = false;

            Direction result = linearIteration(src, dest);
            if (result != CENTER)
                return result;
            
            if (!updateFlag)
                break;
        }
        return paths[src.x][src.y].path;
        
    }
}