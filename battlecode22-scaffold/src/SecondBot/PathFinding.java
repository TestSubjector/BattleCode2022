package SecondBot;

import battlecode.common.*;

public class PathFinding extends BotMiner{

    // TODO: Hyperparameter, needs to be tuned
    public static final int numberIterations = 10;
    public static BFPair[][] paths;

    public static void initPathFinding(){
        paths = new BFPair[MAP_WIDTH][MAP_HEIGHT];
    }

    public static void reinitPathFinding(){
        for(int i = 0; i < MAP_WIDTH; ++i)
            for (int j = 0; j < MAP_HEIGHT; ++j){
                paths[i][j].smallestCost = -1.0f;
                paths[i][j].path = CENTER;
            }
    }

    public static Direction findPath(MapLocation src, MapLocation dest){
        boolean flag = false;
        
        if (paths[dest.x][dest.y].path == CENTER && paths[src.x][src.y].path != CENTER)
            return (paths[src.x][src.y].path);
        
        if (paths[dest.x][dest.y].path != CENTER)
            reinitPathFinding();
        
        paths[dest.x][dest.y].smallestCost = rubbleMap[dest.x][dest.y];

        for (int iter = 0; iter < numberIterations; ++iter){
            flag = false;
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
                                flag = true;
                                
                            }
                            else if (paths[adjacent.x][adjacent.y].smallestCost > newCost){
                                paths[adjacent.x][adjacent.y].smallestCost = newCost;
                                paths[adjacent.x][adjacent.y].path = dir.opposite();
                                flag = true;
                            }
                            if (paths[src.x][src.y].path != CENTER)
                                return paths[src.x][src.y].path;
    
                        } catch (Exception e){
                            continue;
                        }
                    }
                }
            }
            if (!flag)
                break;
        }
        return paths[src.x][src.y].path;
        
    }
}