package com.warframealertapp.data_managers;

import android.util.Log;

import com.warframealertapp.activities.MainActivity;
import com.warframealertapp.data_nodes.alertNode;
import com.warframealertapp.data_nodes.invasionNode;
import com.warframealertapp.data_nodes.messageNode;
import com.warframealertapp.data_nodes.missionNode;
import com.warframealertapp.other_utilities.NotificationLauncher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Cody on 9/21/2017.
 */

public class DataParserAndManager {
    private JSONObject theJSONData;
    private String versionURL;
    private ArrayList<missionNode> alertArray;
    private ArrayList<missionNode> invasionArray;
    private ArrayList<missionNode> messageArray;
    private DatabaseHandler databaseHandler;
    private boolean doesDBNeedRefreshed;
    private NotificationLauncher notificationLauncher;
    private boolean notificationsActive;

    public DataParserAndManager(String versionURL, DatabaseHandler databaseHandler, NotificationLauncher notificationLauncher) {
        this.versionURL = versionURL;
        this.theJSONData = null;
        this.alertArray = new ArrayList<>();
        this.invasionArray = new ArrayList<>();
        this.messageArray = new ArrayList<>();
        this.databaseHandler = databaseHandler;
        this.doesDBNeedRefreshed = false;
        this.notificationLauncher = notificationLauncher;
        this.notificationsActive = true;
    }

    //Creates/updates all data arrays from web
    public void initializeOrRefreshData() {
        loadJSONData(versionURL);
        databaseHandler.open();
        if (theJSONData != null) {
            try {
                createOrUpdateAlertArray();
                createOrUpdateMessageArray();
                createOrUpdateInvasionArray();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        databaseHandler.close();
    }

    //loads json from web
    //saves in class variable "theJSONData"
    private void loadJSONData(String versionUrl) {
        OkHttpClient httpClient;
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(versionUrl)
                .build();

        Response response;
        try {
            response = httpClient.newCall(request).execute();
            String theText = response.body().string();
            theJSONData = new JSONObject(theText);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Creates/updates alert data from web
    //Does not open database on its own
    public void createOrUpdateAlertArray() throws JSONException {
        if (theJSONData.has("Alerts")) {
            JSONArray JSONAlertArray = theJSONData.getJSONArray("Alerts");
            //Clear expired data from alertNode array
            removeExpiredAlerts();
            //Getting All Data For Node And Creating
            for (int i = 0; i < JSONAlertArray.length(); i++) {
                JSONObject JSONObject = JSONAlertArray.getJSONObject(i);
                //If no ID, skip
                if (!JSONObject.getJSONObject("_id").has("$oid")) {
                    continue;
                }

                String id = JSONObject.getJSONObject("_id").getString("$oid");

                //Check if data already exists within array
                //Do nothing if so
                if (!isDuplicate(id, alertArray)) {
                    //Create lists for possible multiple rewards
                    ArrayList<String> rewards = new ArrayList<>();
                    ArrayList<Integer> rewardAmount = new ArrayList<>();

                    //Sub jsons
                    JSONObject JSONMissionInfo = JSONObject.getJSONObject("MissionInfo");
                    JSONObject JSONMissionReward = JSONMissionInfo.optJSONObject("missionReward");

                    //Getting data
                    String creditReward = "0";
                    if (JSONMissionReward.has("credits")) {
                        creditReward = JSONMissionReward.getString("credits");
                    }

                    String missionType = "";
                    if (JSONMissionInfo.has("missionType")) {
                        missionType = clean(JSONMissionInfo.getString("missionType"), DatabaseHandler.MISSION_TYPES_TABLE);
                    }

                    String minLvl = "";
                    if (JSONMissionInfo.has("minEnemyLevel")) {
                        minLvl = JSONMissionInfo.getString("minEnemyLevel");
                    }

                    String maxLvl = "";
                    if (JSONMissionInfo.has("maxEnemyLevel")) {
                        maxLvl = JSONMissionInfo.getString("maxEnemyLevel");
                    }

                    String faction = "";
                    if (JSONMissionInfo.has("faction")) {
                        faction = clean(JSONMissionInfo.getString("faction"), DatabaseHandler.FACTIONS_TABLE);
                    }

                    Long endTime = 0L;
                    if (JSONObject.getJSONObject("Expiry").getJSONObject("$date").has("$numberLong")) {
                        endTime = JSONObject.getJSONObject("Expiry").getJSONObject("$date").getLong("$numberLong");
                    }

                    Long startTime = 0L;
                    if (JSONObject.getJSONObject("Activation").getJSONObject("$date").has("$numberLong")) {
                        startTime = JSONObject.getJSONObject("Activation").getJSONObject("$date").getLong("$numberLong");
                    }

                    String location = "";
                    if (JSONMissionInfo.has("location")) {
                        clean(JSONMissionInfo.getString("location"), DatabaseHandler.LOCATIONS_TABLE);
                    }

                    //The warframe rewards json contains either "countedItems" or "items" depending on the situation
                    //So we must check for both
                    //There might be multiple
                    if (JSONMissionReward.has("countedItems")) {
                        for (int x = 0; x < JSONMissionReward.getJSONArray("countedItems").length(); x++) {
                            String reward = clean(JSONMissionReward.getJSONArray("countedItems").getJSONObject(x).getString("ItemType"), DatabaseHandler.ITEMS_TABLE);
                            rewards.add(reward);
                            rewardAmount.add(JSONMissionReward.getJSONArray("countedItems").getJSONObject(x).getInt("ItemCount"));
                            //Check if notification for item is set, and create notification if so
                            if (notificationsActive) {
                                if (databaseHandler.isAlarmSet(reward) && endTime > System.currentTimeMillis()) {
                                    notificationLauncher.launchAlertNotification(reward);
                                }
                            }
                        }
                    }
                    if (JSONMissionReward.has("items")) {
                        for (int x = 0; x < JSONMissionReward.getJSONArray("items").length(); x++) {
                            String reward = clean(JSONMissionReward.getJSONArray("items").getString(x), DatabaseHandler.ITEMS_TABLE);
                            rewards.add(reward);
                            rewardAmount.add(1);
                            //Check if notification for item is set, and create notification if so
                            if (notificationsActive) {
                                if (databaseHandler.isAlarmSet(reward) && endTime > System.currentTimeMillis()) {
                                    notificationLauncher.launchAlertNotification(reward);
                                }
                            }
                        }
                    }

                    //Create alertNode and add to alertArray
                    alertNode alertNode = new alertNode(faction, creditReward, minLvl, maxLvl, missionType, rewards, rewardAmount, startTime, endTime, id, location);
                    this.alertArray.add(alertNode);
                }
            }
        }
    }


    public ArrayList<missionNode> getAlertArray() {
        return this.alertArray;
    }

    //Creates/updates message data from web
    //Does not open database on its own
    public void createOrUpdateMessageArray() throws JSONException {
        //Getting all info for messageNode and creating
        if (theJSONData.has("Events")) {
            JSONArray JSONEventsArray = theJSONData.getJSONArray("Events");
            for (int i = 0; i < JSONEventsArray.length(); i++) {
                JSONObject JSONMessageObject = JSONEventsArray.getJSONObject(i);
                //If no ID, skip
                if (!JSONMessageObject.getJSONObject("_id").has("$oid")) {
                    continue;
                }

                String id = JSONMessageObject.getJSONObject("_id").getString("$oid");
                //Check if data already exists within array
                //Do nothing if so
                if (!isDuplicate(id, messageArray)) {
                    JSONArray JSONMessageArray = JSONMessageObject.getJSONArray("Messages");
                    for (int y = 0; y < JSONMessageArray.length(); y++) {
                        //check if message contains language code
                        if (JSONMessageArray.getJSONObject(y).has("LanguageCode")) {
                            //check if language code matches users language code
                            if (JSONMessageArray.getJSONObject(y).getString("LanguageCode").equals(Locale.getDefault().getLanguage())) {
                                //getting Node info
                                String messageToAdd = "";
                                if (JSONMessageArray.getJSONObject(y).has("Message")) {
                                    messageToAdd = JSONMessageArray.getJSONObject(y).getString("Message");
                                }

                                long date = 0L;
                                if (JSONMessageObject.getJSONObject("Date").getJSONObject("$date").has("$numberLong")) {
                                    date = JSONMessageObject.getJSONObject("Date").getJSONObject("$date").getLong("$numberLong");
                                }

                                long timeSincePost = 0L;
                                if (date != 0L) {
                                    timeSincePost = System.currentTimeMillis() - date;
                                }

                                //create messageNode and add to messageArray
                                messageNode messageNode = new messageNode(messageToAdd, id, date, timeSincePost);
                                this.messageArray.add(messageNode);
                                //ignore other language codes and prevent duplicates, which for some reason the warframe json sometimes has
                                break;
                            }
                        }
                    }
                }
            }
        }
        //update times since post and sort by newest post
        resetAllTimesSincePostForMessageArray();
        Collections.sort(messageArray);
    }

    //updates time since post to reflect current time
    public void resetAllTimesSincePostForMessageArray() {
        for (int i = 0; i < messageArray.size(); i++) {
            messageNode messageNode = (messageNode) messageArray.get(i);
            messageNode.updateTimeSincePost();
        }
    }

    public ArrayList<missionNode> getMessageArray() {
        return this.messageArray;
    }


    public void createOrUpdateInvasionArray() throws JSONException {
        //get current data
        ArrayList<missionNode> currentInvasions = getCurrentInvasionData(this.theJSONData);
        //Iterate through current data
        for (int i = 0; i < currentInvasions.size(); i++) {
            invasionNode upToDateNode = (invasionNode) currentInvasions.get(i);
            missionNode theDuplicate = getInvasionDuplicate(upToDateNode, this.invasionArray);
            //if there is a duplicate already in invasionArray
            if (theDuplicate != null) {
                //if invasion is now completed, remove
                if (upToDateNode.isCompleted()) {
                    this.invasionArray.remove(theDuplicate);
                } else {
                    //if still active, update data
                    updateInvasionNodeData(upToDateNode, theDuplicate);
                }
            } else {
                //invasion is not in invasionArray
                //if new invasion is not completed, add to invasionArray
                if (!upToDateNode.isCompleted()) {
                    this.invasionArray.add(upToDateNode);
                    ArrayList<String> attackerRewards = upToDateNode.getAttackerRewards();
                    ArrayList<String> defenderRewards = upToDateNode.getDefenderRewards();
                    //cycle through attacker and defender rewards to see if notification needs to be made for items
                    for (int x = 0; x < attackerRewards.size(); x++) {
                        String attackerReward = attackerRewards.get(x);
                        if (notificationsActive) {
                            if (databaseHandler.isAlarmSet(attackerReward)) {
                                notificationLauncher.launchInvasionNotification(attackerReward);
                            }
                        }
                    }
                    for (int x = 0; x < defenderRewards.size(); x++) {
                        String defenderReward = defenderRewards.get(x);
                        if (notificationsActive) {
                            if (databaseHandler.isAlarmSet(defenderReward)) {
                                notificationLauncher.launchInvasionNotification(defenderReward);
                            }
                        }
                    }
                }
                //Do nothing if new invasion is completed already
                //They tend to remain in the warframe json for awhile after completion, so we must check
            }
        }
    }

    //update old invasion node data with new current data
    public void updateInvasionNodeData(missionNode nodeWithCurrentData, missionNode nodeToUpdate) {
        invasionNode currentNode = (invasionNode) nodeWithCurrentData;
        invasionNode oldNode = (invasionNode) nodeToUpdate;
        oldNode.setCurrentAmount(currentNode.getCurrentAmount());
        oldNode.setCompleted(currentNode.isCompleted());
        oldNode.setCurrentPercent();
    }


    //Creates invasion data from web
    //Does not open database on its own
    public ArrayList<missionNode> getCurrentInvasionData(JSONObject JSONData) throws JSONException {
        ArrayList<missionNode> currentInvasions = new ArrayList<>();
        if (theJSONData.has("Invasions")) {
            JSONArray JSONInvasionArray = JSONData.getJSONArray("Invasions");
            //Iterate through all current invasions
            for (int i = 0; i < JSONInvasionArray.length(); i++) {
                JSONObject JSONObject = JSONInvasionArray.getJSONObject(i);
                //skip if no ID
                if (!JSONObject.getJSONObject("_id").has("$oid")) {
                    continue;
                }
                String id = JSONObject.getJSONObject("_id").getString("$oid");

                //create lists for multiple rewards
                ArrayList<String> attackerRewards = new ArrayList<>();
                ArrayList<Integer> attackerRewardAmount = new ArrayList<>();
                ArrayList<String> defenderRewards = new ArrayList<>();
                ArrayList<Integer> defenderRewardAmount = new ArrayList<>();


                boolean isCompleted = JSONObject.getBoolean("Completed");

                //Get data for bar color if user modified faction name
                String attackerFactionActual = "";
                if (JSONObject.getJSONObject("AttackerMissionInfo").has("faction")) {
                    attackerFactionActual = JSONObject.getJSONObject("AttackerMissionInfo").getString("faction");
                }
                String attackerFaction = clean(attackerFactionActual, DatabaseHandler.FACTIONS_TABLE);

                //Get data for bar color if user modified faction name
                String defenderFactionActual = "";
                if (JSONObject.getJSONObject("DefenderMissionInfo").has("faction")) {
                    defenderFactionActual = JSONObject.getJSONObject("DefenderMissionInfo").getString("faction");
                }
                String defenderFaction = clean(defenderFactionActual, DatabaseHandler.FACTIONS_TABLE);


                String location = "";
                if (JSONObject.has("Node")) {
                    location = clean(JSONObject.getString("Node"), DatabaseHandler.LOCATIONS_TABLE);
                }

                Long startTime = 0L;
                if (JSONObject.getJSONObject("Activation").getJSONObject("$date").has("$numberLong")) {
                    startTime = JSONObject.getJSONObject("Activation").getJSONObject("$date").getLong("$numberLong");
                }

                int goal = 0;
                if (JSONObject.has("Goal")) {
                    goal = JSONObject.getInt("Goal");
                }

                int currentAmount = 0;
                if (JSONObject.has("Count")) {
                    currentAmount = JSONObject.getInt("Count");
                }

                String locType = "";
                if (JSONObject.has("LocTag")) {
                    locType = JSONObject.getString("LocTag");
                }

                //The warframe rewards json contains either "countedItems" or "items" depending on the situation
                //So we must check for both
                //There might be multiple
                //invasions can have rewards for attackers and defenders
                //The invasion rewards specifically can be of type JSONObject if it has rewards, or JSONArray if empty, so we must check type
                Object JSONTypeChecker = JSONObject.get("AttackerReward");
                if (JSONTypeChecker instanceof JSONObject) {
                    JSONObject JSONAttackerReward = JSONObject.getJSONObject("AttackerReward");
                    if (JSONAttackerReward.has("countedItems")) {
                        JSONArray JSONCountedItemsArray = JSONAttackerReward.getJSONArray("countedItems");
                        for (int x = 0; x < JSONCountedItemsArray.length(); x++) {
                            String attackerReward = clean(JSONCountedItemsArray.getJSONObject(x).getString("ItemType"), DatabaseHandler.ITEMS_TABLE);
                            attackerRewards.add(attackerReward);
                            attackerRewardAmount.add(JSONCountedItemsArray.getJSONObject(x).getInt("ItemCount"));
                        }
                    } else if (JSONAttackerReward.has("items")) {
                        JSONArray JSONItemsArray = JSONAttackerReward.getJSONArray("items");
                        for (int x = 0; x < JSONItemsArray.length(); x++) {
                            String attackerReward = clean(JSONItemsArray.getString(x), DatabaseHandler.ITEMS_TABLE);
                            attackerRewards.add(attackerReward);
                            attackerRewardAmount.add(1);
                        }
                    }
                }
                JSONTypeChecker = JSONObject.get("DefenderReward");
                if (JSONTypeChecker instanceof JSONObject) {
                    JSONObject JSONDefenderReward = JSONObject.optJSONObject("DefenderReward");
                    if (JSONDefenderReward.has("countedItems")) {
                        JSONArray JSONCountedItemsArray = JSONDefenderReward.getJSONArray("countedItems");
                        for (int x = 0; x < JSONCountedItemsArray.length(); x++) {
                            String defenderReward = clean(JSONCountedItemsArray.getJSONObject(x).getString("ItemType"), DatabaseHandler.ITEMS_TABLE);
                            defenderRewards.add(defenderReward);
                            defenderRewardAmount.add(JSONCountedItemsArray.getJSONObject(x).getInt("ItemCount"));
                        }
                    } else if (JSONDefenderReward.has("items")) {
                        JSONArray JSONItemsArray = JSONDefenderReward.getJSONArray("items");
                        for (int x = 0; x < JSONItemsArray.length(); x++) {
                            String defenderReward = clean(JSONItemsArray.getString(x), DatabaseHandler.ITEMS_TABLE);
                            defenderRewards.add(defenderReward);
                            defenderRewardAmount.add(1);
                        }
                    }
                }
                //create invasionNode and add to array
                invasionNode invasionNode = new invasionNode(id, attackerFaction, attackerFactionActual, attackerRewards, attackerRewardAmount, defenderFaction, defenderFactionActual, defenderRewards, defenderRewardAmount,
                        location, startTime, goal, currentAmount, locType, isCompleted);
                currentInvasions.add(invasionNode);
            }
        }
        return currentInvasions;
    }

    //check if node has a duplicate within array (matched by id)
    public missionNode getInvasionDuplicate(missionNode node, ArrayList<missionNode> arrayToSearch) {
        String id = node.getId();
        //will be new if arrayToSearch is empty
        if (arrayToSearch.isEmpty()) {
            return null;
        }
        //iterate through arrayToSearch comparing ids until we get a match or run out of objects
        for (int i = 0; i < arrayToSearch.size(); i++) {
            if (arrayToSearch.get(i).getId().equals(id)) {
                return arrayToSearch.get(i);
            }
        }
        //confirms that node is in fact new and not currently within arrayToSearch
        return null;
    }

    public ArrayList<missionNode> getInvasionArray() {
        return invasionArray;
    }

    //Check if given url matches the DataManagers url, clearing all node data if changed
    public boolean didPlatformChange(String url) {
        if (url.equals(this.versionURL)) {
            return false;
            //Do nothing
        } else {
            this.versionURL = url;
            clearData();
            return true;
        }
    }

    //tells DataParserAndManager it needs to refresh data
    public void setDBToRefresh() {
        this.doesDBNeedRefreshed = true;
    }

    //checks if DataParserAndManager needs refreshed
    public boolean doesDBNeedRefreshed() {
        return doesDBNeedRefreshed;
    }

    //clears all node data
    public void clearData() {
        this.alertArray.clear();
        this.messageArray.clear();
        this.invasionArray.clear();
    }

    //checks if id is within nodeList, meaning its a duplicate if so. works for all missionNodes
    public boolean isDuplicate(String id, ArrayList<missionNode> nodeList) {
        if (nodeList.isEmpty()) {
            return false;
        }
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    //removes old alerts from alertNode array
    public void removeExpiredAlerts() {
        for (int i = 0; i < alertArray.size(); i++) {
            if (((alertNode) alertArray.get(i)).needsRemoved()) {
                alertArray.remove(i);
            }
        }
    }

    //converts base actual name (file name) to simple readable name, and handles the database operations for new data
    public String clean(String actualFileName, String dataType) {
        if (actualFileName.equals("")) {
            return "";
        } else {
            return databaseHandler.getOrCreateReadableEntry(actualFileName, dataType);
        }
    }

    public void setNotificationsActive(boolean bool) {
        this.notificationsActive = bool;
    }
}
