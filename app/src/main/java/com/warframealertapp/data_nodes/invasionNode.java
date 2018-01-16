package com.warframealertapp.data_nodes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Cody on 10/12/2017.
 */

public class invasionNode extends missionNode {
    private String attackerFaction;
    private String attackerFactionActual;
    private ArrayList<String> attackerRewards;
    private ArrayList<Integer> attackerAmounts;
    private String defenderFaction;
    private String defenderFactionActual;
    private ArrayList<String> defenderRewards;
    private ArrayList<Integer> defenderAmounts;
    private String location;
    private Long activationTime;
    private int goal;
    private int currentAmount;
    private String locType;
    private boolean isPhorid;
    private boolean isInfestedInvasion;
    private double currentPercent;
    private boolean isCompleted;

    public invasionNode(String id, String attackerFaction, String attackerFactionActual, ArrayList<String> attackerRewards, ArrayList<Integer> attackerAmounts, String defenderFaction,
                        String defenderFactionActual, ArrayList<String> defenderRewards, ArrayList<Integer> defenderAmounts, String location, Long activationTime,
                        int goal, int currentAmount, String locType, boolean isCompleted) {
        super(id, activationTime);
        this.attackerFaction = attackerFaction;
        this.attackerFactionActual = attackerFactionActual;
        this.attackerRewards = attackerRewards;
        this.attackerAmounts = attackerAmounts;
        this.defenderFaction = defenderFaction;
        this.defenderFactionActual = defenderFactionActual;
        this.defenderRewards = defenderRewards;
        this.defenderAmounts = defenderAmounts;
        this.location = location;
        this.activationTime = activationTime;
        this.goal = goal;
        this.currentAmount = currentAmount;
        this.locType = locType;
        isPhorid = false;
        isInfestedInvasion = false;
        this.isCompleted = isCompleted;
        checkInvasionType();
        setCurrentPercent();
    }

    //Used to update progress percent when data is updated
    public void setCurrentPercent() {
        double d = (currentAmount + goal) / (double) (goal * 2) * 100;
        if (isInfestedInvasion) {
            d *= 2;
        }
        this.currentPercent = d;
    }

    //returns current percent using double type
    public double getCurrentPercentDouble() {
        return currentPercent;
    }

    //returns current percent using String type, formatted to contain only two decimal places. Will return "Completed" if either side is maxed
    public String getCurrentPercentString() {
        if (this.currentPercent < 0 || this.currentPercent > 100) {
            return "Completed";
        } else {
            return String.format(Locale.US, "%.2f", this.currentPercent) + "%";
        }
    }

    //determines invasion type (is Phorid boss in mission? does the invasion involve infested?)
    public void checkInvasionType() {
        if (this.locType.equals("/Lotus/Language/Menu/InfestedInvasionBoss")) {
            isPhorid = true;
            isInfestedInvasion = true;
        } else if (this.locType.equals("/Lotus/Language/Menu/InfestedInvasionGeneric")) {
            isInfestedInvasion = true;
        }
    }

    //Getters and setters
    public String getAttackerFaction() {
        return attackerFaction;
    }

    public void setAttackerFaction(String attackerFaction) {
        this.attackerFaction = attackerFaction;
    }

    public ArrayList<String> getAttackerRewards() {
        return attackerRewards;
    }

    public void setAttackerRewards(ArrayList<String> attackerRewards) {
        this.attackerRewards = attackerRewards;
    }

    public ArrayList<Integer> getAttackerAmounts() {
        return attackerAmounts;
    }

    public void setAttackerAmounts(ArrayList<Integer> attackerAmounts) {
        this.attackerAmounts = attackerAmounts;
    }

    public String getDefenderFaction() {
        return defenderFaction;
    }

    public void setDefenderFaction(String defenderFaction) {
        this.defenderFaction = defenderFaction;
    }

    public ArrayList<String> getDefenderRewards() {
        return defenderRewards;
    }

    public void setDefenderRewards(ArrayList<String> defenderRewards) {
        this.defenderRewards = defenderRewards;
    }

    public ArrayList<Integer> getDefenderAmounts() {
        return defenderAmounts;
    }

    public void setDefenderAmounts(ArrayList<Integer> defenderAmounts) {
        this.defenderAmounts = defenderAmounts;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Long activationTime) {
        this.activationTime = activationTime;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public boolean isPhorid() {
        return isPhorid;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getAttackerFactionActual() {
        return attackerFactionActual;
    }

    public void setAttackerFactionActual(String attackerFactionActual) {
        this.attackerFactionActual = attackerFactionActual;
    }

    public String getDefenderFactionActual() {
        return defenderFactionActual;
    }

    public void setDefenderFactionActual(String defenderFactionActual) {
        this.defenderFactionActual = defenderFactionActual;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
