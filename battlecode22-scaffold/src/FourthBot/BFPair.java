package FourthBot;

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
}