package OBFSTestBot;

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
        // System.out.println("The dest is " + dest);
        if (!rc.isMovementReady()) return false;
        if (rc.getLocation().distanceSquaredTo(dest) == 0) return false;

        if (rc.getRoundNum() - BIRTH_ROUND > 3 || Clock.getBytecodesLeft() > BYTECODE_REMAINING){
            update(dest);
            // System.out.println(" Byte code A: " + Clock.getBytecodesLeft());
            Direction dir = BFSDroid.getBestDir(dest);
            // System.out.println(" Byte code B: " + Clock.getBytecodesLeft());
            if (dir != null && !mapTracker.check(rc.getLocation().add(dir))){
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    return true;
                }
            }   
        }
        return Movement.goToDirect(dest);
    }
}
