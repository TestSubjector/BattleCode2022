package OFourthBot;

import battlecode.common.*;

////// Implementation of Min-Heap:

//TODO: Implement MinHeap using only one integer per node
class node extends Util{
    public int cost;
    public MapLocation loc;
    public node shortestPathParent;

    public node(){
        this.cost = 4000;
        this.shortestPathParent = null;
        this.loc = null;
    }

    public node(MapLocation givenLoc, int cost, node givenParent){
        this.cost = cost;
        this.loc = givenLoc;
        this.shortestPathParent = givenParent;
    }

    
    // TODO: Required anymore?
    public void copyValues(node other){
        this.cost = other.cost;
        this.loc = other.loc;
        this.shortestPathParent = other.shortestPathParent;
    }
}


public class Heap extends node {
    public static node[] minHeap;
    // TODO: Decide on length value
    public static final int length = 500;
    public static int curLength;


    public void initHeap(){
        minHeap = new node[length];
        curLength = 0;
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


    public static void swapNodes(node first, node second){
        node temp;
        temp = first;
        first = second;
        second = temp;
    }


    public static void heapify(int curIndex){
        node current = minHeap[curIndex];
        int leftIndex = leftChildIndex(curIndex), rightIndex = rightChildIndex(curIndex);
        if (leftIndex >= curLength)
            return;
        node leftChild = minHeap[leftIndex];
        if (rightIndex >= curLength){
            if (current.cost > leftChild.cost)
                swapNodes(current, leftChild);
            return;
        }
        
        node rightChild = minHeap[rightIndex];
        int curCost = current.cost, leftCost = leftChild.cost, rightCost = rightChild.cost;
        if (curCost <= leftCost && curCost <= rightCost)
            return;
        if (leftCost <= rightCost){
            swapNodes(leftChild, current);
            heapify(leftIndex);
        }
        else{
            swapNodes(current, rightChild);
            heapify(rightIndex);
        }
    }


    public boolean insertIntoHeap(MapLocation loc, int cost){
        if (curLength >= length) return false;
        minHeap[curLength] = new node();
        int curIndex = curLength++;
        int parentNodeIndex = parentIndex(curIndex);
        node currentNode = minHeap[curIndex], parentNode = minHeap[parentNodeIndex];
        while (curIndex != 0 && parentNode.cost > currentNode.cost){
            swapNodes(currentNode, parentNode);
            if (curIndex == 0) break;
            curIndex = parentNodeIndex;
            currentNode = parentNode;
            parentNode = minHeap[parentIndex(curIndex)];
        }
        return true;
    }


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