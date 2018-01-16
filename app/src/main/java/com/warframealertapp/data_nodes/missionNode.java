package com.warframealertapp.data_nodes;

import android.support.annotation.NonNull;

/**
 * Created by Cody on 9/21/2017.
 */

public class missionNode implements Comparable<missionNode>{
    private String id;
    private long dateToCompare;

    public missionNode(String id, long dateToCompare) {
        this.id = id;
        this.dateToCompare = dateToCompare;
    }

    //used to compare and sort nodes based on given long
    //can be start date, end date, etc. Whatever Long value was given upon node creation
    @Override
    public int compareTo(@NonNull missionNode o) {
        missionNode otherNode = (missionNode) o;
        long long1 = this.dateToCompare;
        long long2 = otherNode.dateToCompare;
        if (long1 < long2)
            return -1;
        if (long1 == long2)
            return 0;
        return 1;
    }

    //getters and setters
    public String getId(){
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    public long getDateToCompare() {
        return dateToCompare;
    }

    public void setDateToCompare(long dateToCompare) {
        this.dateToCompare = dateToCompare;
    }
}
