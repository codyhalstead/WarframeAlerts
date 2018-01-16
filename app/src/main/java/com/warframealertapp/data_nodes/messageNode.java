package com.warframealertapp.data_nodes;

/**
 * Created by Cody on 10/12/2017.
 */

public class messageNode extends missionNode {
    private String theMessage;
    private long date;

    public messageNode(String message, String id, long date, long timeSincePost){
        super(id, timeSincePost);
        this.theMessage = message;
        this.date = date;
    }

    //updates times since post
    //super class uses this data to sort (newest first)
    public void updateTimeSincePost(){
        super.setDateToCompare(System.currentTimeMillis() - date);
    }

    //gives time since post in seconds (unix / 1000)
    public long getTimeSincePostInSeconds(){
        return super.getDateToCompare() / 1000;
    }

    //getters and setters
    public String getTheMessage() {
        return theMessage;
    }

    public void setTheMessage(String theMessage) {
        this.theMessage = theMessage;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }


}
