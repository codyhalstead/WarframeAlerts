package com.warframealertapp.data_nodes;

import java.util.ArrayList;

/**
 * Created by Cody on 9/21/2017.
 */

public class alertNode extends missionNode {
    private String faction;
    private String credits;
    private String levelMin;
    private String levelMax;
    private String missionType;
    private ArrayList<String> rewards;
    private ArrayList<Integer> amounts;
    private Long endTime;
    private Long startTime;
    private String location;


    public alertNode( String faction, String credits, String minLvl, String maxLvl, String missionType, ArrayList<String> rewards, ArrayList<Integer> amounts, long startTime, long endTime, String id, String location){
        super(id, startTime);
        this.faction = faction;
        this.credits = credits;
        this.levelMin = minLvl;
        this.levelMax = maxLvl;
        this.missionType = missionType;
        this.rewards = rewards;
        this.amounts = amounts;
        this.endTime = endTime;
        this.startTime = startTime;
        this.location = location;
    }

    public boolean isExpired(){
        return System.currentTimeMillis() > endTime;
    }

    public boolean needsRemoved(){
        //checks if node is no longer available
        //the added time is to give the warframe json time to update itself so we don't recreate the data and possibly set an incorrect notification
        return System.currentTimeMillis()  - 360000 > endTime;
    }

    //Getters and setters
    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getLevelMin() {
        return levelMin;
    }

    public void setLevelMin(String levelMin) {
        this.levelMin = levelMin;
    }

    public String getLevelMax() {
        return levelMax;
    }

    public void setLevelMax(String levelMax) {
        this.levelMax = levelMax;
    }

    public String getMissionType() {
        return missionType;
    }

    public void setMissionType(String missionType) {
        this.missionType = missionType;
    }

    public ArrayList<String> getRewards() {
        return rewards;
    }

    public void setRewards(ArrayList<String> rewards) {
        this.rewards = rewards;
    }

    public ArrayList<Integer> getAmounts() {
        return amounts;
    }

    public void setAmounts(ArrayList<Integer> amounts) {
        this.amounts = amounts;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}