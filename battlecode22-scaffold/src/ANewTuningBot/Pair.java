package ANewTuningBot;

import java.util.Objects;

public class Pair<A, B>
{
    private A first;
    private B second;

    public Pair(A first, B second){
        this.first = first;
        this.second = second;
    }

    public A getFirst(){
        return first;
    }

    public B getSecond(){
        return second;
    }
    
    public void setBoth(A firstValue, B secondValue){
        this.first = firstValue;
        this.second = secondValue;
    }

    public void setFirst(A value){
        this.first = value;
    }

    public void setSecond(B value){
        this.second = value;
    }

    @Override
    public boolean equals(Object oth){
        if (this == oth){
            return true;
        }
        if (oth == null || !(getClass().isInstance(oth))){
            return false;
        }
        Pair<A, B> other = getClass().cast(oth);
        return ((first == null) ? (other.first == null) : (first.equals(other.first)) &&
                (second == null ? other.second == null : second.equals(other.second)));
    }

    @Override
    public int hashCode(){
        return Objects.hash(first, second);
    }
}