package com.warframealertapp.data_managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by Cody on 10/3/2017.
 */

public class DatabaseHandler {
    //Database constants
    public static int DATABASE_VERSION = 1;
    public static String DB_FILE_NAME = "appDB";
    public static final String FACTIONS_TABLE = "factions";
    public static final String FACTIONS_SIMPLE_COLUMN = "factionSimple";
    public static final String FACTIONS_ACTUAL_COLUMN = "factionActual";
    public static final String LOCATIONS_TABLE = "locations";
    public static final String LOCATIONS_SIMPLE_COLUMN = "locationSimple";
    public static final String LOCATIONS_ACTUAL_COLUMN = "locationActual";
    public static final String MISSION_TYPES_TABLE = "missionTypes";
    public static final String MISSION_TYPES_SIMPLE_COLUMN = "typeSimple";
    public static final String MISSION_TYPES_ACTUAL_COLUMN = "typeActual";
    public static final String ITEMS_TABLE = "items";
    public static final String ITEMS_SIMPLE_COLUMN = "itemSimple";
    public static final String ITEMS_ACTUAL_COLUMN = "itemActual";
    public static final String ITEMS_IS_ALARM_SET = "itemIsAlarmSet";
    public static final String ID_FOR_ALL_TABLES = "_id";

    private DataBaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    //Open database
    public DatabaseHandler open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    //Close database
    //Important to close when done
    public void close() {
        dbHelper.close();
    }

    //Return database
    public SQLiteDatabase getDataBase(){
        return this.db;
    }

    //Reset database to default
    public void resetDB(){
        dbHelper.resetDatabaseToDefault(db);
    }

    //editRowByID for String entry
    public void editRowByID(String tableName, String columnName, int id, String newEntry){
        ContentValues cv = new ContentValues();
        cv.put(columnName, newEntry);
        db.update(tableName, cv, ID_FOR_ALL_TABLES + "=" + id, null);
    }

    //editRowByID for int entry
    public void editRowByID(String tableName, String columnName, int id, int newEntry){
        ContentValues cv = new ContentValues();
        cv.put(columnName, newEntry);
        db.update(tableName, cv, ID_FOR_ALL_TABLES + "=" + id, null);
    }

    //Creates new location entry
    public void addNewLocation(String baseLocation, String readableLocation) {
        ContentValues values = new ContentValues();
        values.put(LOCATIONS_SIMPLE_COLUMN, readableLocation);
        values.put(LOCATIONS_ACTUAL_COLUMN, baseLocation);
        this.db.insert(LOCATIONS_TABLE, null, values);
    }

    //Creates new faction entry
    public void addNewFaction(String baseFaction, String readableFaction) {
        ContentValues values = new ContentValues();
        values.put(FACTIONS_SIMPLE_COLUMN, readableFaction);
        values.put(FACTIONS_ACTUAL_COLUMN, baseFaction);
        this.db.insert(FACTIONS_TABLE, null, values);
    }

    //Creates new mission type entry
    public void addNewMissionType(String baseMissionType, String readableMissionType) {
        ContentValues values = new ContentValues();
        values.put(MISSION_TYPES_SIMPLE_COLUMN, readableMissionType);
        values.put(MISSION_TYPES_ACTUAL_COLUMN, baseMissionType);
        this.db.insert(MISSION_TYPES_TABLE, null, values);
    }

    //Creates new item entry
    public void addNewItem(String baseItem, String readableItem) {
        ContentValues values = new ContentValues();
        values.put(ITEMS_SIMPLE_COLUMN, readableItem);
        values.put(ITEMS_ACTUAL_COLUMN, baseItem);
        values.put(ITEMS_IS_ALARM_SET, 0);
        this.db.insert(ITEMS_TABLE, null, values);
    }

    //Gets data from Simple column matching Actual column, returning readable name
    //If new entry, creates entry
    //works for all 4 data tables
    public String getOrCreateReadableEntry(String data, String dataType) {
        String tableName;
        String actualColumnName;
        String simpleColumnName;
        String itemSimple;
        String answer;
        switch (dataType) {
            case FACTIONS_TABLE:
                tableName = FACTIONS_TABLE;
                actualColumnName = FACTIONS_ACTUAL_COLUMN;
                simpleColumnName = FACTIONS_SIMPLE_COLUMN;
                answer = getSimpleColumnEntry(tableName, actualColumnName, simpleColumnName, data);
                //if new faction, add
                if (answer.equals("")) {
                    itemSimple = createSimpleFactionName(data);
                    addNewFaction(data, itemSimple);
                    return itemSimple;
                }
                return answer;
            case MISSION_TYPES_TABLE:
                tableName = MISSION_TYPES_TABLE;
                actualColumnName = MISSION_TYPES_ACTUAL_COLUMN;
                simpleColumnName = MISSION_TYPES_SIMPLE_COLUMN;
                answer = getSimpleColumnEntry(tableName, actualColumnName, simpleColumnName, data);
                //if new mission type, add
                if (answer.equals("")) {
                    itemSimple = createSimpleMissionTypeName(data);
                    addNewMissionType(data, itemSimple);
                    return itemSimple;
                }
                return answer;
            case ITEMS_TABLE:
                tableName = ITEMS_TABLE;
                actualColumnName = ITEMS_ACTUAL_COLUMN;
                simpleColumnName = ITEMS_SIMPLE_COLUMN;
                answer = getSimpleColumnEntry(tableName, actualColumnName, simpleColumnName, data);
                //if new item, add
                if (answer.equals("")) {
                    itemSimple = createSimpleItemName(data);
                    addNewItem(data, itemSimple);
                    return itemSimple;
                }
                return answer;
            case LOCATIONS_TABLE:
                tableName = LOCATIONS_TABLE;
                actualColumnName = LOCATIONS_ACTUAL_COLUMN;
                simpleColumnName = LOCATIONS_SIMPLE_COLUMN;
                answer = getSimpleColumnEntry(tableName, actualColumnName, simpleColumnName, data);
                //if new location, add
                if (answer.equals("")) {
                    itemSimple = createSimpleLocationName(data);
                    addNewLocation(data, itemSimple);
                    return itemSimple;
                }
                return answer;
            default:
                //if unknown data type
                return "Error";
        }
    }

    //Returns readable name from simple column when supplied with table name and Actual(fileName) entry
    //Returns blank string if data not is not in database
    //Limit 1 for efficiency
    public String getSimpleColumnEntry(String tableName, String actualColumnName, String simpleColumnName, String actualFileName) {
        String Query = "Select * from " + tableName + " where " + actualColumnName + " like '%" + actualFileName + "%' LIMIT 1";
        Cursor cursor = db.rawQuery(Query, null);
        String answer = "";
        if (cursor.moveToFirst()) {
            answer = cursor.getString(cursor.getColumnIndex(simpleColumnName));
            cursor.close();
            return answer;
        } else {
            cursor.close();
            return answer;
        }
    }


    //Use regex to create default readable name from actual file name (for new entries)
    //ex: /Lotus/StoreItems/Upgrades/Mods/Melee/DualStat/FocusEnergyMod --> Focus Energy Mod
    public String createSimpleItemName(String actual) {
        return (actual.substring(actual.lastIndexOf("/") + 1)).replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
    }

    //Use regex to create default readable name from actual file name (for new entries)
    //ex: FC_OROKIN --> Orokin
    public String createSimpleFactionName(String actual) {
        String word = actual.substring(actual.lastIndexOf("_") + 1, actual.lastIndexOf("_") + 2);
        word += actual.substring(actual.lastIndexOf("_") + 2).toLowerCase();
        return word;
    }

    //Use regex to create default readable name from actual file name (for new entries)
    //ex: MT_ASSASSINATION --> Assassination
    public String createSimpleMissionTypeName(String actual) {
        String word = actual.substring(actual.lastIndexOf("_") + 1, actual.lastIndexOf("_") + 2);
        word += actual.substring(actual.lastIndexOf("_") + 2).toLowerCase();
        return word;
    }

    //Use regex to create default readable name from actual file name (for new entries)
    //In this case, will just use actual file name
    //ex: SolNode1 --> SolNode1
    public String createSimpleLocationName(String actual) {
        return actual;
    }

    //Turns all notifications for items off
    public void clearAllItemNotifications(){
        String query = "UPDATE " + ITEMS_TABLE + " SET " +  ITEMS_IS_ALARM_SET + " = 0 ";
        db.execSQL(query);
    }

    //Turns all notifications for items on
    public void activateAllItemNotifications(){
        String query = "UPDATE " + ITEMS_TABLE + " SET " +  ITEMS_IS_ALARM_SET + " = 1 ";
        db.execSQL(query);
    }

    //Checks if notification is set for specific item
    //Returns false if item not in database
    public boolean isAlarmSet(String itemName){
        String query = "select * from " + ITEMS_TABLE + " where "  + ITEMS_SIMPLE_COLUMN + " like '%" + itemName + "%' LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            if(cursor.getInt(cursor.getColumnIndex(ITEMS_IS_ALARM_SET)) != 0){
                cursor.close();
                return true;
            }else{
                cursor.close();
                return false;
            }
        } else {
            cursor.close();
            return false;
        }
    }

    //This class helps manage database, sets up database if not yet created
    private static class DataBaseHelper extends SQLiteOpenHelper {
        Context context;

        public DataBaseHelper(Context context) {
            super(context, DB_FILE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        //Creates database if not created and populates tables with default data
        @Override
        public void onCreate(SQLiteDatabase db) {
            String sqlTable = "CREATE TABLE IF NOT EXISTS " + FACTIONS_TABLE +" ( " +
                    ID_FOR_ALL_TABLES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FACTIONS_ACTUAL_COLUMN + " VARCHAR(15), " +
                    FACTIONS_SIMPLE_COLUMN + " VARCHAR(15) )";
            db.execSQL(sqlTable);

            sqlTable = "CREATE TABLE IF NOT EXISTS " + LOCATIONS_TABLE + " ( " +
                    ID_FOR_ALL_TABLES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LOCATIONS_ACTUAL_COLUMN + " VARCHAR(15), " +
                    LOCATIONS_SIMPLE_COLUMN + " VARCHAR2(15) )";
            db.execSQL(sqlTable);

            sqlTable = "CREATE TABLE IF NOT EXISTS " + MISSION_TYPES_TABLE + " ( " +
                    ID_FOR_ALL_TABLES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MISSION_TYPES_ACTUAL_COLUMN + " VARCHAR(15), " +
                    MISSION_TYPES_SIMPLE_COLUMN + " VARCHAR2(15) )";
            db.execSQL(sqlTable);

            sqlTable = "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE + " ( " +
                    ID_FOR_ALL_TABLES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ITEMS_ACTUAL_COLUMN + " VARCHAR(35), " +
                    ITEMS_SIMPLE_COLUMN + " VARCHAR2(45), " +
                    ITEMS_IS_ALARM_SET + " BOOLEAN NOT NULL )";
            db.execSQL(sqlTable);

            //populate default data
            try {
                populateDefaultFactions(db);
                populateDefaultItems(db);
                populateDefaultLocations(db);
                populateDefaultMissionTypes(db);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //Not used
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        //populate default entries for items table
        //notifications off by default
        //uses asset items.json
        private void populateDefaultItems(SQLiteDatabase db) throws IOException {
            JSONObject factionsJSON = getAssetJSON(context, "items.json");
            ContentValues values = new ContentValues();
            Iterator<String> iter = factionsJSON.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = "";
                try {
                    value = factionsJSON.get(key).toString();
                } catch (JSONException e) {
                    // Something went wrong!
                }
                values.put(ITEMS_ACTUAL_COLUMN, key);
                values.put(ITEMS_SIMPLE_COLUMN, value);
                values.put(ITEMS_IS_ALARM_SET, 0);
                db.insert(ITEMS_TABLE, null, values);
            }
        }

        //populate default entries for factionss table
        //uses asset factions.json
        private void populateDefaultFactions(SQLiteDatabase db) throws IOException {
            JSONObject factionsJSON = getAssetJSON(context, "factions.json");
            ContentValues values = new ContentValues();
            Iterator<String> iter = factionsJSON.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = "";
                try {
                    value = factionsJSON.get(key).toString();
                } catch (JSONException e) {
                    // Something went wrong!
                }
                values.put(FACTIONS_ACTUAL_COLUMN, key);
                values.put(FACTIONS_SIMPLE_COLUMN, value);
                db.insert(FACTIONS_TABLE, null, values);
            }

        }

        //populate default entries for locations table
        //uses asset nodeInfo.json
        private void populateDefaultLocations(SQLiteDatabase db) throws IOException {
            JSONObject factionsJSON = getAssetJSON(context, "nodeInfo.json");
            ContentValues values = new ContentValues();
            Iterator<String> iter = factionsJSON.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = "";
                try {
                    JSONObject planetInfo = factionsJSON.getJSONObject(key);
                    value = planetInfo.get("value").toString();
                } catch (JSONException e) {
                    // Something went wrong!
                }
                values.put(LOCATIONS_ACTUAL_COLUMN, key);
                values.put(LOCATIONS_SIMPLE_COLUMN, value);
                db.insert(LOCATIONS_TABLE, null, values);
            }
        }

        //populate default entries for mission types table
        //uses asset missions.json
        private void populateDefaultMissionTypes(SQLiteDatabase db) throws IOException {
            JSONObject factionsJSON = getAssetJSON(context, "missions.json");
            ContentValues values = new ContentValues();
            Iterator<String> iter = factionsJSON.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = "";
                try {
                    value = factionsJSON.get(key).toString();
                } catch (JSONException e) {
                    // Something went wrong!
                }
                values.put(MISSION_TYPES_ACTUAL_COLUMN, key);
                values.put(MISSION_TYPES_SIMPLE_COLUMN, value);
                db.insert(MISSION_TYPES_TABLE, null, values);
            }
        }

        //Loads json from assets
        private JSONObject getAssetJSON(Context context, String filename) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            JSONObject theJSON = null;
            StringBuilder sb = new StringBuilder();
            String mLine = reader.readLine();
            while (mLine != null) {
                sb.append(mLine);
                mLine = reader.readLine();
            }
            reader.close();
            try {
                theJSON = new JSONObject(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return theJSON;
        }

        //Deletes all data and reloads with default data
        public void resetDatabaseToDefault(SQLiteDatabase db) {
            db.execSQL("delete from " + FACTIONS_TABLE);
            db.execSQL("delete from " + LOCATIONS_TABLE);
            db.execSQL("delete from " + ITEMS_TABLE);
            db.execSQL("delete from " + MISSION_TYPES_TABLE);

            this.onCreate(db);
        }
    }
}
