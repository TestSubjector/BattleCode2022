package ATuningBot;

import battlecode.common.*;

////// Implementation of Min-Heap:

//TODO: Implement MinHeap using only one integer per node
// class node extends Util{
//     public int cost, distance;
//     public MapLocation loc;
//     public node shortestPathParent;

//     public node(){
//         this.cost = 4000;
//         distance = 0;
//         this.shortestPathParent = null;
//         this.loc = null;
//     }

//     public node(MapLocation givenLoc, int cost, int givenDistance){
//         this.cost = cost;
//         this.loc = givenLoc;
//         // this.shortestPathParent = givenParent;
//         this.distance = givenDistance;
//     }
    
//     // TODO: Required anymore?
//     public void copyValues(node other){
//         this.cost = other.cost;
//         this.loc = other.loc;
//         // this.shortestPathParent = other.shortestPathParent;
//     }

//     @Override
//     public String toString(){
//         return ("Node: Location = " + loc + ", Distance = " + distance + ", Cost = " + cost);// + ", shortestPathParent: " + shortestPathParent);
//     }
// }


public class Heap extends node {
    public static node[] minHeap;
    // TODO: Decide on length value
    public static final int length = 800;
    private static int curLength;


    public static void initHeap(){
        minHeap = new node[length];
        curLength = 0;
    }

    public static void printHeap(){
        for (int i = 0; i < curLength; ++i){
            System.out.print(minHeap[i] + ", ");
        }
        System.out.print("\n");
    }


    public static int parentIndex(int i){
        return (i-1)/2;
    }


    public static int leftChildIndex(int i){
        return (2*i + 1);
    }


    public static int rightChildIndex(int i){
        return (2*i + 2);
    }

    public static boolean isEmpty(){
        return (curLength == 0);
    }


    public static void swapNodes(node first, node second){
        node temp;
        temp = first;
        first = second;
        second = temp;
    }


    public static void swapNodes(int first, int second){
        node temp = minHeap[first];
        minHeap[first] = minHeap[second];
        minHeap[second] = temp;
    }


    public static void heapify(int curIndex){
        node current = minHeap[curIndex];
        int leftIndex = leftChildIndex(curIndex), rightIndex = rightChildIndex(curIndex);
        if (leftIndex >= curLength)
            return;
        node leftChild = minHeap[leftIndex];
        if (rightIndex >= curLength){
            if (current.cost > leftChild.cost){
                minHeap[curIndex] = minHeap[leftIndex];
                minHeap[leftIndex] = current;
            }
                // swapNodes(curIndex, leftIndex);
            return;
        }
        
        node rightChild = minHeap[rightIndex];
        int curCost = current.cost, leftCost = leftChild.cost, rightCost = rightChild.cost;
        if (curCost <= leftCost && curCost <= rightCost)
            return;
        if (leftCost <= rightCost){
            // swapNodes(leftIndex, curIndex);
            minHeap[curIndex] = minHeap[leftIndex];
            minHeap[leftIndex] = current;
            heapify(leftIndex);
        }
        else{
            // swapNodes(curIndex, rightIndex);
            minHeap[curIndex] = minHeap[rightIndex];
            minHeap[rightIndex] = current;
            heapify(rightIndex);
        }
    }


    public static node insertIntoHeap(MapLocation loc, int cost, int distance){
        if (curLength >= length) return null;
        minHeap[curLength] = new node(loc, cost, distance);
        int curIndex = curLength;
        curLength++;
        int parentNodeIndex = parentIndex(curIndex);
        node currentNode = minHeap[curIndex], parentNode = minHeap[parentNodeIndex];
        node curNode = currentNode;
        int count = 0;
        while (curIndex != 0 && parentNode.cost > currentNode.cost){
            // swapNodes(currentNode, parentNode);
            minHeap[curIndex] = parentNode;
            // System.out.println("1 : " + minHeap[curIndex]);
            minHeap[parentNodeIndex] = currentNode;
            // System.out.println("2 : " + minHeap[parentNodeIndex] + " " + parentNodeIndex );
            curIndex = parentNodeIndex;
            // System.out.println("3 : " + curIndex);
            if (curIndex == 0) break;
            // currentNode = parentNode;
            parentNodeIndex = parentIndex(curIndex);
            parentNode = minHeap[parentNodeIndex];

            // System.out.println("4 : " + parentNode);
            // count++;
            // if (count > 200){
            //     System.out.println("PROBLEM2222222222222222222222222222");
            // //     // printHeap();
            //     break;
            // }
            
        }
        return curNode;
        // return true;
    }

    // public static boolean insertIntoHeap(node currentNode){
    //     if (curLength >= length) return false;
    //     minHeap[curLength] = currentNode;
    //     int curIndex = curLength++;
    //     int parentNodeIndex = parentIndex(curIndex);
    //     node parentNode = minHeap[parentNodeIndex];
    //     while (curIndex != 0 && parentNode.cost > currentNode.cost){
    //         swapNodes(currentNode, parentNode);
    //         if (curIndex == 0) break;
    //         curIndex = parentNodeIndex;
    //         currentNode = parentNode;
    //         parentNode = minHeap[parentIndex(curIndex)];
    //     }
    //     return true;
    // }


    public static node extractMin(){
        if (curLength <= 0) return null;
        if (curLength == 1){
            curLength--;
            return minHeap[0];
        }
        node root = minHeap[0];
        
        minHeap[0] = minHeap[curLength - 1];
        curLength--;
        heapify(0);
        return root;
    }
}