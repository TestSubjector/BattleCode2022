package BuildBot;

import battlecode.common.*;

import java.util.Objects;

public class node extends Util implements Comparable<node>{
    public Integer cost, distance;
    public MapLocation loc;
    public node shortestPathParent;

    public node(){
        this.cost = 4000;
        distance = 0;
        this.shortestPathParent = null;
        this.loc = null;
    }

    public node(MapLocation givenLoc, int givencost, int givenDistance){
        this.cost = givencost;
        this.loc = givenLoc;
        // this.shortestPathParent = givenParent;
        this.distance = givenDistance;
    }
    
    // TODO: Required anymore?
    public void copyValues(node other){
        this.cost = other.cost;
        this.loc = other.loc;
        // this.shortestPathParent = other.shortestPathParent;
    }

    @Override
    public String toString(){
        return ("Node: Location = " + loc + ", Distance = " + distance + ", Cost = " + cost);// + ", shortestPathParent: " + shortestPathParent);
    }

    // @Override
    // public boolean equals(Object oth)
    // {
    //     if (this == oth)
    //     {
    //         return true;
    //     }
    //     if (oth == null || !(getClass().isInstance(oth)))
    //     {
    //         return false;
    //     }
    //     node other = getClass().cast(oth);

    //     // return ((loc == null) ? (other.loc == null) : (loc.equals(other.loc)) &&
    //     //         (shortestPathParent == null ? other.shortestPathParent == null : ((cost == other.cost) ? ((distance == other.distance)):  shortestPathParent.equals(other.shortestPathParent))));
    // }

    @Override
    public int hashCode()
    {
        // return Objects.hash
        return Objects.hash(cost, distance, loc, shortestPathParent);
    }

    @Override
    public int compareTo(node o)
    {

        // return this.cost < o.cost;
        return o.cost.compareTo(this.cost);
    }

}