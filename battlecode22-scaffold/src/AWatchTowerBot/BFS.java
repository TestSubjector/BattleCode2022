package AWatchTowerBot;

import battlecode.common.*;

public class BFS extends Globals{

    static final int BYTECODE_REMAINING = 4500; // Go big or go home
    
    public static MapTracker mapTracker = new MapTracker();

    // int turnsGreedy = 0;

    public static MapLocation currentTarget = null;

    public static void reset(){
        mapTracker.reset();
    }

    public static void update(MapLocation target){
        if (currentTarget == null || target.distanceSquaredTo(currentTarget) > 0){
            reset();
        }
        currentTarget = target;
        mapTracker.add(rc.getLocation());
    }

    public static boolean move(MapLocation dest) throws GameActionException{
        if (dest == null) return false;
        if (!rc.isMovementReady()) return false;
        if (rc.getLocation().distanceSquaredTo(dest) < 2) 
            return Movement.goToDirect(dest);

        if (rc.getRoundNum() - BIRTH_ROUND > 2 || Clock.getBytecodesLeft() > BYTECODE_REMAINING){
            update(dest);
            Direction dir = BFSDroid.getBestDir(dest);
            if (dir != null && !mapTracker.check(rc.getLocation().add(dir))){
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    currentLocation = rc.getLocation();
                    return true;
                }
            }   
        }
        return Movement.goToDirect(dest);
    }
}
