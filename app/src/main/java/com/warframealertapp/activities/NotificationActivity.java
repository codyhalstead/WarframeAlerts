package com.warframealertapp.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.warframealertapp.adapters.NotificationCursorAdapter;
import com.warframealertapp.other_utilities.CustomFilterQueryProvider;
import com.warframealertapp.data_managers.DataParserAndManager;
import com.warframealertapp.data_managers.DatabaseHandler;
import com.warframealertapp.R;

import java.util.HashMap;

/**
 * Created by Cody on 11/9/2017.
 */

public class NotificationActivity extends AppCompatActivity {
    DatabaseHandler dh;
    DataParserAndManager dm;
    Cursor cursor;
    NotificationCursorAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentOrientation = getResources().getConfiguration().orientation;
        //lock orientation to current orientation to prevent accidental data refreshes
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        setContentView(R.layout.rename_list_view);
        this.dm = MainActivity.dataParserAndManager;
        this.dh = new DatabaseHandler(this);
        ListView LV = (ListView) findViewById(R.id.renameListView);
        dh.open();
        //get all items from database ordered alphabetically
        String query = "SELECT * FROM " + DatabaseHandler.ITEMS_TABLE + " ORDER BY " + DatabaseHandler.ITEMS_SIMPLE_COLUMN + " ASC";
        cursor = dh.getDataBase().rawQuery(query, null);
        this.adapter = new NotificationCursorAdapter(this, cursor, 0);
        //set search query provider for search option
        CustomFilterQueryProvider fqp = new CustomFilterQueryProvider(DatabaseHandler.ITEMS_TABLE, DatabaseHandler.ITEMS_SIMPLE_COLUMN, dh.getDataBase());
        adapter.setFilterQueryProvider(fqp);
        LV.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //set custom action bar layout
        inflater.inflate(R.menu.menu_rename_activity, menu);
        SearchManager SManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.app_bar_search);
        //set up search button on action bar
        final android.support.v7.widget.SearchView searchViewAction = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewAction.setSearchableInfo(SManager.getSearchableInfo(getComponentName()));
        searchViewAction.setIconifiedByDefault(true);
        //make search bar extend full length of action bar when used
        searchViewAction.setMaxWidth(Integer.MAX_VALUE);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the back button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //add text listener to search bar
        android.support.v7.widget.SearchView.OnQueryTextListener textChangeListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {

           //update results
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }

            //update results and clear focus
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                searchViewAction.clearFocus();
                return true;
            }
        };
        searchViewAction.setOnQueryTextListener(textChangeListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_search:
                return true;

            //set back arrow to onBackPressed function
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get changes made hashmap from adapter
    //set to footer button onClick
    public void saveChangesAndClose(View view) {
        HashMap<Integer, Boolean> changesMade = adapter.getChangesMade();
        for (int key : changesMade.keySet()) {
            //if alarm changed to off (false), set to 0 for database
            int isAlarmSet = 0;
            //if alarm changed to on (true), set to 1 for database
            if(changesMade.get(key)){
                isAlarmSet = 1;
            }
            //commit changes for item to database
            dh.editRowByID(DatabaseHandler.ITEMS_TABLE, DatabaseHandler.ITEMS_IS_ALARM_SET, key, isAlarmSet);
        }
        // close database and cursor, show toast confirming actions, set dataParserAndManager to refresh and finish activity
        dh.close();
        cursor.close();
        Toast.makeText(this, this.getString(R.string.changes_saved), Toast.LENGTH_LONG).show();
        dm.setDBToRefresh();
        finish();
    }

    //close database and cursor onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dh.close();
        cursor.close();
    }
}
