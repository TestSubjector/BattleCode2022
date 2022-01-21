package OSageActionBot;

import battlecode.common.*;

public class Bot extends Util{


    // Might be needed if watchtowers are buffed:
    // public static boolean inEnemyTowerOrHQRange(MapLocation loc, MapLocation[] enemyTowers) {
    //     if (loc.distanceSquaredTo(theirHQ) <= 52) {
    //         switch (enemyTowers.length) {
    //             case 6:
    //             case 5:
    //                 // enemy HQ has range of 35 and splash
    //                 if (loc.add(loc.directionTo(theirHQ)).distanceSquaredTo(theirHQ) <= 35) return true;
    //                 break;

    //             case 4:
    //             case 3:
    //             case 2:
    //                 // enemy HQ has range of 35 and no splash
    //                 if (loc.distanceSquaredTo(theirHQ) <= 35) return true;
    //                 break;

    //             case 1:
    //             case 0:
    //             default:
    //                 // enemyHQ has range of 24;
    //                 if (loc.distanceSquaredTo(theirHQ) <= 24) return true;
    //                 break;
    //         }
    //     }

    //     for (MapLocation tower : enemyTowers) {
    //         if (loc.distanceSquaredTo(tower) <= RobotType.TOWER.attackRadiusSquared) return true;
    //     }

    //     return false;
    // }
}