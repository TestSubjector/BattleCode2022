package SecondBot;

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