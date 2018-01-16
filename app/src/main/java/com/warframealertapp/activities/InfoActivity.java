package com.warframealertapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.warframealertapp.adapters.AlertArrayAdapter;
import com.warframealertapp.adapters.InvasionArrayAdapter;
import com.warframealertapp.adapters.MessageArrayAdapter;
import com.warframealertapp.data_nodes.missionNode;
import com.warframealertapp.data_managers.DataParserAndManager;
import com.warframealertapp.R;

import java.util.ArrayList;

/**
 * Created by Cody on 10/16/2017.
 */

public class InfoActivity extends AppCompatActivity {
    private String type;
    public DataParserAndManager dm;
    Handler handler;
    Runnable runnable;

    //Activity creates list view for alerts, invasions, or messages
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        setupActionBar();
        dm = MainActivity.dataParserAndManager;
        handler = new Handler();
        Bundle bundle = getIntent().getExtras();
        ListView LV = (ListView) findViewById(R.id.mainListView);
        //determines what type of list view to create (which adapter to use)
        type = bundle.getString("viewType");
        if (type != null) {
            switch (type) {
                case ("alert"):
                    //create alert adapter and adds to list
                    ArrayList<missionNode> alertsArray = dm.getAlertArray();
                    final AlertArrayAdapter alertAdapter = new AlertArrayAdapter(this, alertsArray);
                    LV.setAdapter(alertAdapter);
                    setActionBarTitle("Alerts");
                    //runnable refreshes adapter every second for timer countdown
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            alertAdapter.notifyDataSetChanged();
                            handler.postDelayed(this, 1000);
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                    break;
                case ("message"):
                    //create message adapter and adds to list
                    //does not refresh
                    ArrayList<missionNode> messagesArray = dm.getMessageArray();
                    MessageArrayAdapter messageAdapter = new MessageArrayAdapter(this, messagesArray);
                    LV.setAdapter(messageAdapter);
                    setActionBarTitle("Messages");
                    break;
                case ("invasion"):
                    //creates invasion adapter and adds to list
                    ArrayList<missionNode> invasionsArray = dm.getInvasionArray();
                    final InvasionArrayAdapter invasionAdapter = new InvasionArrayAdapter(this, invasionsArray);
                    LV.setAdapter(invasionAdapter);
                    setActionBarTitle("Invasions");
                    //refreshes every minute to update data, refresh rate not as important as alerts
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            invasionAdapter.notifyDataSetChanged();
                            handler.postDelayed(this, 60000);
                        }
                    };
                    handler.postDelayed(runnable, 60000);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //restarts refreshing runnable
        if (type.equals("alert")) {
            handler.postDelayed(runnable, 1000);
        }
        if (type.equals("invasion")) {
            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stops screen from refreshing when paused
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Set the back button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //create custom action bar with minimize button
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //sets back arrow function
            case android.R.id.home:
                onBackPressed();
                return true;

            //sets minimize button to minimize
            case R.id.action_collapse_app:
                moveTaskToBack(true);
                return true;

            //sets refresh button to refresh data
            case R.id.action_refresh_info:
                if(MainActivity.isNetworkConnected) {
                    MainActivity.dataRefresher.manualGetData();
                    Toast.makeText(this, this.getString(R.string.data_refreshed), Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(this, this.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //sets onBackPressed to finish
        finish();
    }

    public void setActionBarTitle(String title) {
        //allows ability to change action bar title for each type of adapter being used
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

}
