package SecondBot;

import battlecode.common.*;

public class BFPair extends Util{
    public float smallestCost;
    public Direction path;
    
    public BFPair(){
        smallestCost = -1.0f;
        path = CENTER;
    }


    public boolean isEquals(BFPair other){
        return ((this.smallestCost == other.smallestCost) ? (this.path.equals(other.path)) : false);
    }
    // @Override
    // public boolean equals(Object oth){
    //     if (this == oth)
    //     {
    //         return true;
    //     }
    //     if (oth == null || !(getClass().isInstance(oth)))
    //     {
    //         return false;
    //     }
    //     BFPair other = getClass().cast(oth);
    //     return ((this.smallestCost == other.smallestCost)? (this.path == other.path) : false);
    //     // return ((smallestCost == -1) ? (other.first == null) : (first.equals(other.first)) &&
    //             // (second == null ? other.second == null : second.equals(other.second)));
    // }
}